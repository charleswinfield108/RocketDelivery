# Developer Guide - Schema-Aligned Codebase

**Version:** 2.0  
**Date:** March 27, 2026  
**Audience:** Developers working on RocketDelivery project

---

## Quick Start for Developers

### First Time Setup

```bash
# 1. Clone and navigate
git clone https://github.com/charleswinfield108/RocketDelivery.git
cd RocketDelivery

# 2. Copy configuration (add your DB credentials)
cp src/main/resources/application.properties.example src/main/resources/application.properties
nano src/main/resources/application.properties  # Edit with your credentials

# 3. Ensure MySQL is running
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rdelivery;"

# 4. Build and run
./mvnw clean install
./mvnw spring-boot:run

# 5. Verify it works
curl http://localhost:8080/api/restaurants
# Should return: {"content": [...], "totalElements": X, ...}
```

---

## Understanding the Schema v2.0 Changes

### What's Different from v1.0?

| Component | v1.0 | v2.0 | Impact |
|-----------|------|------|--------|
| **Order Status** | String ("PENDING") | FK to OrderStatusEntity | Must lookup status from DB |
| **Employee Entity** | No User link | 1:1 link to User | Employees linked to accounts |
| **Restaurant Address** | Embedded only | FK + Embedded | Denormalized for performance |
| **Order Ratings** | N/A | restaurantRating field | Can store customer ratings |
| **Schema Compliance** | Partial | ✅ Full | Matches specification |

### Before You Code

**📖 Required Reading:**
1. [SCHEMA_COMPLIANCE.md](SCHEMA_COMPLIANCE.md) (5 min read) - Understand what changed
2. [ARCHITECTURE_CHANGES.md](ARCHITECTURE_CHANGES.md) (10 min read) - Context for decisions

**✅ Nothing to install (no new dependencies added)**

---

## Working with Orders & Status

### The Old Way (Still Works - Don't Use for New Code)

```java
// ⚠️ DEPRECATED - Works but marked for removal
OrderEntity order = new OrderEntity();
order.setStatus("PENDING");  // Direct String assignment

if (order.getStatus().equals("PENDING")) {
    // Do something when pending
}
```

### The New Way (Use This)

```java
// ✅ CORRECT - Uses FK relationship
@Autowired
private OrderStatusRepository orderStatusRepository;

// In service layer:
OrderStatusEntity pendingStatus = orderStatusRepository
    .findByStatusCodeAndIsActive("PENDING", true)
    .orElseThrow(() -> new IllegalArgumentException("PENDING status not found"));

OrderEntity order = new OrderEntity();
order.setOrderStatus(pendingStatus);  // Set FK relationship

// Check status:
if (order.getOrderStatus().getStatusCode().equals("PENDING")) {
    // Do something when pending
}

// Or use compatibility method:
if ("PENDING".equals(order.getStatus())) {  // Still returns status code
    // Also works, but relies on deprecated getStatus()
}
```

### Transitioning Order Status

```java
// In OrderService.java

// ✅ NEW METHOD - Use this for status transitions
public OrderEntity setOrderStatusByCode(Long orderId, String statusCode) {
    OrderStatusEntity status = orderStatusRepository
        .findByStatusCodeAndIsActive(statusCode.trim(), true)
        .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + statusCode));
    
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));
    
    order.setOrderStatus(status);
    return orderRepository.save(order);
}

// Usage:
orderService.setOrderStatusByCode(orderId, "CONFIRMED");
orderService.setOrderStatusByCode(orderId, "OUT_FOR_DELIVERY");
orderService.setOrderStatusByCode(orderId, "DELIVERED");
```

---

## Working with Entities

### OrderStatusEntity (Reference Table)

```java
// OrderStatusEntity is a lookup table with 7 standard statuses:
// PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

// Get in service:
OrderStatusEntity status = orderStatusRepository
    .findByStatusCodeAndIsActive("CONFIRMED", true)
    .orElseThrow(() -> new RuntimeException("Status not found"));

// Access fields:
String code = status.getStatusCode();        // "CONFIRMED"
String name = status.getName();              // "Confirmed" (user-friendly)
String description = status.getDescription(); // "Restaurant confirmed the order"
Integer displayOrder = status.getDisplayOrder(); // 2 (for UI ordering)
```

### OrderEntity with FK

```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatusEntity orderStatus;  // NEW: FK relationship
    
    private String status;  // KEPT: For backward compatibility during migration
    
    // ✅ Get status code (new way)
    public String getStatusCode() {
        return orderStatus != null ? orderStatus.getStatusCode() : null;
    }
    
    // ⚠️ OLD WAY - Backward compatibility only
    public String getStatus() {
        return getStatusCode();  // Returns status code string
    }
    
    // ⚠️ DEPRECATED - Don't use in new code
    public void setStatus(String statusCode) {
        // Maps String to FK (relies on DB lookup in service)
        // This is why the service layer must handle it
    }
    
    // ✅ NEW WAY
    public void setOrderStatus(OrderStatusEntity orderStatus) {
        this.orderStatus = orderStatus;
    }
}
```

### EmployeeEntity with New FKs

```java
@Entity
@Table(name = "employees")
public class EmployeeEntity {
    
    // NEW: Required OneToOne to User
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    
    // NEW: Required ManyToOne to Address
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;
    
    // CHANGED: Now optional (was required)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "restaurant_id")
    private RestaurantEntity restaurant;
    
    // ... other fields (role, salary, etc.) ...
}

// Creating an employee (service layer):
public EmployeeEntity createEmployee(UserEntity user, AddressEntity address, 
                                     RestaurantEntity restaurant) {
    EmployeeEntity employee = new EmployeeEntity();
    employee.setUser(user);              // NEW: Required
    employee.setAddress(address);        // NEW: Required
    employee.setRestaurant(restaurant);  // Optional
    // ... set other fields ...
    return employeeRepository.save(employee);
}

// Querying employees:
List<EmployeeEntity> employees = employeeRepository.findByUserId(userId);
```

### RestaurantEntity with Address FK

```java
@Entity
@Table(name = "restaurants")
public class RestaurantEntity {
    
    // RENAMED: owner_id → user_id (schema compliance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;  // Previously called "owner"
    
    // NEW: Unique FK to Address
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false, unique = true)
    private AddressEntity address;
    
    // NEW: Price range 1-3
    @Column(name = "price_range", nullable = false)
    @Min(1) @Max(3)
    private Integer priceRange = 1;
    
    // KEPT: Denormalized address fields (for query performance)
    // These are synchronized with address FK in service layer
    @Column(name = "street")
    private String street;
    @Column(name = "city")
    private String city;
    // ... other denormalized fields ...
}

// Creating a restaurant (service layer):
public RestaurantEntity createRestaurant(RestaurantDTO dto) {
    UserEntity owner = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    AddressEntity address = addressRepository.findById(dto.getAddressId())
        .orElseThrow(() -> new RuntimeException("Address not found"));
    
    RestaurantEntity restaurant = new RestaurantEntity();
    restaurant.setUser(owner);  // RENAMED from setOwner()
    restaurant.setAddress(address);  // NEW: Set FK
    
    // IMPORTANT: Sync denormalized fields with FK address
    restaurant.setStreet(address.getStreet());
    restaurant.setCity(address.getCity());
    restaurant.setState(address.getState());
    restaurant.setZipCode(address.getZipCode());
    restaurant.setCountry(address.getCountry());
    
    restaurant.setPriceRange(dto.getPriceRange() != null ? dto.getPriceRange() : 1);
    
    return restaurantRepository.save(restaurant);
}
```

---

## Testing Your Changes

### Unit Test Example

```java
@SpringBootTest
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderStatusRepository orderStatusRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void createOrderSetsCorrectStatus() {
        // Arrange
        OrderStatusEntity pendingStatus = orderStatusRepository
            .findByStatusCodeAndIsActive("PENDING", true)
            .orElseThrow();
        
        CustomerEntity customer = // ... create test customer
        RestaurantEntity restaurant = // ... create test restaurant
        
        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        // DON'T set status directly - let service do it
        
        // Act
        orderService.createOrder(order);  // Service sets orderStatus FK
        
        // Assert
        OrderEntity saved = orderRepository.findById(order.getId()).orElseThrow();
        assertNotNull(saved.getOrderStatus());
        assertEquals("PENDING", saved.getOrderStatus().getStatusCode());
        assertEquals("PENDING", saved.getStatus());  // Backward compat
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@Transactional
class OrderStatusTransitionTest {
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void confirmOrderChangesStatus() {
        // Arrange
        OrderEntity order = createTestOrder("PENDING");
        
        // Act
        orderService.confirmOrder(order.getId());
        
        // Assert
        OrderEntity updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals("CONFIRMED", updated.getStatus());
        assertEquals("CONFIRMED", updated.getOrderStatus().getStatusCode());
    }
}
```

---

## Database Queries

### Check Status References

```sql
-- See all available order statuses
SELECT id, status_code, name, description, display_order 
FROM order_statuses 
WHERE is_active = true 
ORDER BY display_order;

-- Find orders with specific status
SELECT o.id, o.order_number, os.status_code, os.name
FROM orders o
JOIN order_statuses os ON o.order_status_id = os.id
WHERE os.status_code = 'PENDING';

-- Check for unmapped statuses (during migration)
SELECT COUNT(*) as unmapped_orders 
FROM orders 
WHERE order_status_id IS NULL;
```

### Check FK Relationships

```sql
-- Verify employees have user_id and address_id
SELECT e.id, e.user_id, e.address_id, u.email, a.street
FROM employees e
LEFT JOIN users u ON e.user_id = u.id
LEFT JOIN addresses a ON e.address_id = a.id
WHERE e.user_id IS NULL OR e.address_id IS NULL;

-- Verify restaurants have address_id
SELECT r.id, r.name, r.address_id, a.street
FROM restaurants r
LEFT JOIN addresses a ON r.address_id = a.id
WHERE r.address_id IS NULL;
```

---

## Common Patterns

### Fetching Order with Status Details

```java
// ❌ WRONG - N+1 query problem with lazy loading
List<OrderEntity> orders = orderRepository.findAll();
for (OrderEntity order : orders) {
    String status = order.getOrderStatus().getStatusCode();  // Each iteration loads status
}

// ✅ CORRECT - Join fetch
List<OrderEntity> orders = orderRepository.findAllWithStatus();  // Uses JOIN FETCH

// In repository:
@Query("SELECT DISTINCT o FROM OrderEntity o JOIN FETCH o.orderStatus")
List<OrderEntity> findAllWithStatus();
```

### Creating Related Data

```java
// Service method showing proper data creation order
public OrderEntity placeOrder(Long customerId, Long restaurantId, OrderCreateDTO dto) {
    // 1. Load required entities
    CustomerEntity customer = customerRepository.findById(customerId).orElseThrow();
    RestaurantEntity restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
    AddressEntity deliveryAddress = addressRepository.findById(dto.getDeliveryAddressId()).orElseThrow();
    
    // 2. Get initial status from reference table
    OrderStatusEntity pendingStatus = orderStatusRepository
        .findByStatusCodeAndIsActive("PENDING", true)
        .orElseThrow(() -> new RuntimeException("PENDING status not initialized"));
    
    // 3. Create order with all required FKs
    OrderEntity order = new OrderEntity();
    order.setCustomer(customer);
    order.setRestaurant(restaurant);
    order.setDeliveryAddress(deliveryAddress);
    order.setOrderStatus(pendingStatus);  // FK, not String
    order.setOrderNumber(generateOrderNumber());
    order.setTotalPrice(dto.getTotalPrice());
    
    // 4. Save order
    OrderEntity saved = orderRepository.save(order);
    
    // 5. Add products and calculate totals
    for (OrderItemDTO item : dto.getItems()) {
        ProductEntity product = productRepository.findById(item.getProductId()).orElseThrow();
        ProductOrderEntity productOrder = new ProductOrderEntity();
        productOrder.setOrder(saved);
        productOrder.setProduct(product);
        productOrder.setQuantity(item.getQuantity());
        productOrder.setUnitPrice(product.getCost());  // RENAMED: was getPrice()
        productOrderRepository.save(productOrder);
    }
    
    return saved;
}
```

---

## Migration from Old Code

### If You Have Old Code Using String Status

```java
// OLD CODE ⚠️
if (order.getStatus().equals("PENDING")) {
    order.setStatus("CONFIRMED");
}

// NEW CODE ✅
if ("PENDING".equals(order.getStatus())) {  // Still works via compat method
    orderService.setOrderStatusByCode(order.getId(), "CONFIRMED");
}

// OR BETTER ✅
if ("PENDING".equals(order.getOrderStatus().getStatusCode())) {
    order.setOrderStatus(
        orderStatusRepository.findByStatusCodeAndIsActive("CONFIRMED", true)
            .orElseThrow()
    );
    orderRepository.save(order);
}
```

---

## Debugging Tips

### Order Status Issues

```java
// Check if status FK is loaded
OrderEntity order = orderRepository.findById(orderId).orElseThrow();
if (order.getOrderStatus() == null) {
    System.out.println("ERROR: orderStatus FK is null!");
    // Hibernate.initialize(order.getOrderStatus());  // Force lazy load if needed
}

// Check status code value
System.out.println("Status code: " + order.getOrderStatus().getStatusCode());
System.out.println("Status name: " + order.getOrderStatus().getName());

// Verify string compatibility method
System.out.println("Via getStatus(): " + order.getStatus());  // Should match statusCode
```

### Database

```sql
-- Check if order has order_status_id set
SELECT id, order_number, order_status_id, status
FROM orders 
WHERE id = ?;

-- Check if order_status_id references valid record
SELECT o.id, os.id, os.status_code
FROM orders o
JOIN order_statuses os ON o.order_status_id = os.id
WHERE o.id = ?;
```

---

## Key Takeaways

1. **Use OrderStatusRepository for statuses** - Don't hardcode status strings
2. **Set FK relationships in service layer** - Controllers just pass data
3. **Synchronize denormalized fields** - If you set restaurant.address, also update street/city
4. **Test status transitions** - Verify ordering and constraints
5. **Check the docs** - SCHEMA_COMPLIANCE.md and ARCHITECTURE_CHANGES.md explain everything
6. **Backward compatibility works** - Old code doesn't break, but mark it deprecated

---

## Resources

- **Entity Source:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- **Service Source:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/`
- **Schema Spec:** `docs/support_materials_11v2/db_schema_11.txt`
- **Migration Guide:** [MIGRATION.md](MIGRATION.md)
- **Architecture:** [ARCHITECTURE_CHANGES.md](ARCHITECTURE_CHANGES.md)
- **Compliance Report:** [SCHEMA_COMPLIANCE.md](SCHEMA_COMPLIANCE.md)

---

## Questions?

1. Check [SCHEMA_COMPLIANCE.md](SCHEMA_COMPLIANCE.md) for what changed
2. Check [ARCHITECTURE_CHANGES.md](ARCHITECTURE_CHANGES.md) for why it changed
3. Check entity source code for exact implementation
4. See service layer for patterns and examples
5. Run tests to understand expected behavior

