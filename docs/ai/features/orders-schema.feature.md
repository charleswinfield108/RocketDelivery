# Orders Schema Feature

## Overview

Create the **OrderEntity** JPA class representing customer orders for the RocketDelivery delivery system. The entity establishes relationships with Customer, Restaurant, and Address entities, tracks order status throughout its lifecycle, and provides complete data validation.

## Database Schema

### Table: `orders`

```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    estimated_delivery_time TIMESTAMP NULL,
    actual_delivery_time TIMESTAMP NULL,
    special_instructions VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE RESTRICT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE RESTRICT,
    FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE RESTRICT,
    
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_restaurant_id (restaurant_id),
    INDEX idx_address_id (address_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date)
);
```

### Fields & Constraints

| Field | Type | Nullable | Default | Unique | Constraints |
|-------|------|----------|---------|--------|-------------|
| `id` | BIGINT | NO | AUTO_INCREMENT | YES | Primary Key |
| `orderNumber` | VARCHAR(50) | NO | — | YES | Human-readable identifier |
| `orderDate` | TIMESTAMP | NO | CURRENT_TIMESTAMP | — | @CreationTimestamp |
| `status` | VARCHAR(50) | NO | 'PENDING' | — | Enum: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED |
| `totalPrice` | DECIMAL(10,2) | NO | — | — | @Positive, BigDecimal for precision |
| `customer_id` | BIGINT | NO | — | — | Foreign Key → users.id, @ManyToOne |
| `restaurant_id` | BIGINT | NO | — | — | Foreign Key → restaurants.id, @ManyToOne |
| `address_id` | BIGINT | NO | — | — | Foreign Key → addresses.id, @ManyToOne |
| `estimatedDeliveryTime` | TIMESTAMP | YES | — | — | Set when order confirmed |
| `actualDeliveryTime` | TIMESTAMP | YES | — | — | Set when order delivered |
| `specialInstructions` | VARCHAR(500) | YES | — | — | Customer notes, @Size(max=500) |
| `createdAt` | TIMESTAMP | NO | CURRENT_TIMESTAMP | — | @CreationTimestamp |
| `updatedAt` | TIMESTAMP | YES | — | — | @UpdateTimestamp |

## Entity Implementation

### OrderEntity.java

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/OrderEntity.java`

**Class Structure:**
- `@Entity` — Map to database table
- `@Table(name = "orders")` — Specify table name
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` — Lombok boilerplate
- Manual null validation in service layer (no @NonNull annotations)

**Fields (13 total):**

1. `id` (Long)
   - `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - Primary key, auto-increment

2. `orderNumber` (String)
   - `@Column(unique = true, nullable = false, length = 50)`
   - `@NotNull`, `@Size(min=1, max=50)`
   - Unique human-readable identifier (e.g., "ORD-20260325-001234")

3. `customer` (CustomerEntity)
   - `@ManyToOne(fetch = FetchType.EAGER, optional = false)`
   - `@JoinColumn(name = "customer_id", nullable = false)`
   - `@NotNull`
   - Link to customer who placed order

4. `restaurant` (RestaurantEntity)
   - `@ManyToOne(fetch = FetchType.EAGER, optional = false)`
   - `@JoinColumn(name = "restaurant_id", nullable = false)`
   - `@NotNull`
   - Restaurant fulfilling order

5. `deliveryAddress` (AddressEntity)
   - `@ManyToOne(fetch = FetchType.EAGER, optional = false)`
   - `@JoinColumn(name = "address_id", nullable = false)`
   - `@NotNull`
   - Delivery destination address

6. `orderDate` (LocalDateTime)
   - `@CreationTimestamp`
   - `@Column(nullable = false, updatable = false)`
   - `@NotNull`
   - Auto-set to current timestamp on creation

7. `status` (String)
   - `@Column(nullable = false, length = 50)`
   - `@NotNull`, `@Size(min=1, max=50)`
   - Valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
   - Defaults to "PENDING" (set in service on creation)

8. `totalPrice` (BigDecimal)
   - `@Column(nullable = false, precision = 10, scale = 2)`
   - `@NotNull`, `@Positive`
   - Total order amount (sum of items + taxes, minus discounts)

9. `estimatedDeliveryTime` (LocalDateTime)
   - `@Column(nullable = true)`
   - Optional, set when order is confirmed (typically +30-60 minutes)

10. `actualDeliveryTime` (LocalDateTime)
    - `@Column(nullable = true)`
    - Optional, set when order is delivered

11. `specialInstructions` (String)
    - `@Column(nullable = true, length = 500)`
    - `@Size(max=500)`
    - Customer delivery notes (e.g., "No onions", "Leave at door")

12. `createdAt` (LocalDateTime)
    - `@CreationTimestamp`
    - `@Column(nullable = false, updatable = false)`
    - Auto-set on entity creation

13. `updatedAt` (LocalDateTime)
    - `@UpdateTimestamp`
    - `@Column(nullable = true)`
    - Auto-updated on any modification

**Helper Methods:**
- `isDelivered()` — Returns true if actualDeliveryTime is not null
- `isPending()` — Returns true if status equals "PENDING"
- `isCancelled()` — Returns true if status equals "CANCELLED"
- `setDeliveryTimes(LocalDateTime estimatedTime)` — Set estimated delivery time
- `completeDelivery()` — Set actualDeliveryTime to now

**Relationships:**
- **Many-to-One:** Customer (required) — many orders per customer
- **Many-to-One:** Restaurant (required) — many orders per restaurant
- **Many-to-One:** Address (required) — many orders use same address

**Validation:**
- `orderNumber`: Not null, unique, 1-50 characters
- `orderDate`: Not null, auto-set
- `customer`: Not null, must reference existing customer
- `restaurant`: Not null, must reference existing restaurant
- `deliveryAddress`: Not null, must reference existing address
- `status`: Not null, valid enum values
- `totalPrice`: Not null, must be > 0
- `specialInstructions`: Optional, max 500 characters

## OrderRepository

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/OrderRepository.java`

**Extends:** `JpaRepository<OrderEntity, Long>`

**Query Methods (15+ methods):**

1. `findByOrderNumber(String)` — Get order by human-readable number
2. `findByCustomerId(Long)` — Get all orders for customer
3. `findByCustomerIdOrderByOrderDateDesc(Long)` — Customer's orders (newest first)
4. `findByRestaurantId(Long)` — Get all orders for restaurant
5. `findByRestaurantIdAndStatusOrderByOrderDateDesc(Long, String)` — Filter by restaurant & status
6. `findByStatusOrderByOrderDateDesc(String)` — Get orders by status
7. `findByStatus(String)` — List orders with specific status
8. `findByCustomerIdAndStatus(Long, String)` — Customer's orders with status
9. `findByIdAndCustomerId(Long, Long)` — Authorization check (order belongs to customer)
10. `findByIdAndRestaurantId(Long, Long)` — Authorization check (order for restaurant)
11. `findByOrderDateBetween(LocalDateTime, LocalDateTime)` — Date range query
12. `findByRestaurantIdAndOrderDateBetween(Long, LocalDateTime, LocalDateTime)` — Restaurant orders in date range
13. `countByRestaurantIdAndStatus(Long, String)` — Count restaurant orders by status
14. `countByCustomerIdAndStatus(Long, String)` — Count customer orders by status
15. `deleteByIdAndCustomerId(Long, Long)` — Authorized deletion

**Verification Methods:**
- `existsByOrderNumber(String)` — Check if order number exists
- `existsByIdAndCustomerId(Long, Long)` — Verify order belongs to customer
- `existsByIdAndRestaurantId(Long, Long)` — Verify order for restaurant

## OrderService

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/OrderService.java`

**Dependencies:**
- `OrderRepository` (required)
- Constructor injection with null validation

**Public Methods (18+ methods):**

### CRUD Operations
1. `createOrder(customerId, restaurantId, addressId, totalPrice, specialInstructions)` — Create new order
   - Generate unique orderNumber
   - Set status to "PENDING"
   - Auto-set orderDate
   - Validate customer, restaurant, address exist
   - Return created OrderEntity

2. `getOrderById(id)` — Get order by ID
   - Return Optional<OrderEntity>

3. `getOrderByNumber(orderNumber)` — Get order by order number
   - Return Optional<OrderEntity>

4. `getOrderByIdAndCustomerId(id, customerId)` — Get with authorization
   - Verify order belongs to customer
   - Throw if unauthorized

5. `getOrderByIdAndRestaurantId(id, restaurantId)` — Get with restaurant authorization
   - Verify order is for restaurant
   - Throw if unauthorized

6. `getAllOrders()` — List all orders
   - Return List<OrderEntity>

7. `updateOrder(id, customerId, updates)` — Update order with authorization
   - Only allow updates before delivery
   - Validate specialInstructions length
   - Return updated entity

8. `deleteOrder(id, customerId)` — Delete with authorization
   - Only allow for pending orders
   - Verify customer ownership
   - Throw if already confirmed/in-progress

### Status Management
9. `setOrderStatus(id, customerId, newStatus)` — Change order status with validation
   - Update status field
   - Validate status value
   - Log status change

10. `confirmOrder(id, customerId)` — Move to CONFIRMED
    - Validate status is PENDING
    - Set estimatedDeliveryTime (30-60 min from now)
    - Return updated entity

11. `startPreparation(id, restaurantId)` — Move to PREPARING
    - Validate restaurant ownership
    - Validate status is CONFIRMED

12. `markReady(id, restaurantId)` — Move to READY
    - Validate restaurant ownership
    - Validate status is PREPARING

13. `markOutForDelivery(id, restaurantId)` — Move to OUT_FOR_DELIVERY
    - Validate restaurant ownership
    - Validate status is READY

14. `completeDelivery(id)` — Move to DELIVERED
    - Set actualDeliveryTime to now
    - Return updated entity

15. `cancelOrder(id, customerId)` — Move to CANCELLED
    - Only allow if status is PENDING or CONFIRMED
    - Verify customer ownership

### Query & Filtering
16. `getCustomerOrders(customerId)` — Get all customer orders
    - Return List<OrderEntity>, ordered by date DESC

17. `getCustomerOrdersByStatus(customerId, status)` — Get customer orders with status
    - Return List<OrderEntity>

18. `getRestaurantOrders(restaurantId)` — Get all restaurant orders
    - Return List<OrderEntity>, ordered by date DESC

19. `getRestaurantOrdersByStatus(restaurantId, status)` — Get with status filter
    - Return List<OrderEntity>

20. `getOrdersByStatus(status)` — Get all orders with status
    - Return List<OrderEntity>, ordered by date DESC

### Analytics & Counts
21. `getRestaurantPendingOrderCount(restaurantId)` — Count pending
22. `getRestaurantOrderCountByStatus(restaurantId, status)` — Count by status
23. `getCustomerOrderCountByStatus(customerId, status)` — Count customer by status
24. `getOrdersInDateRange(startDate, endDate)` — Get orders between dates

### Utilities
25. `hasOrder(customerId, orderId)` — Check if customer has order
26. `isOrderDelivered(orderId)` — Check delivery status
27. `validateOrderData(order)` [private] — Comprehensive validation

**Validation & Authorization:**
- Manual null checks in every method
- `validateOrderData()` [private]:
  - specialInstructions: 0-500 characters if provided
  - totalPrice > 0
  - customer, restaurant, address: not null
  - status: valid enum value
- Authorization: orderId + customerId or restaurantId verification in all applicable methods
- Status transitions: Enforce valid workflow (PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED)

**Transactional Behavior:**
- Use `@Transactional` on methods modifying data
- Order updates should be atomic

## REST API Endpoints (Future Implementation)

- `GET /api/orders` — List all orders (admin only)
- `GET /api/orders/{id}` — Get order details
- `POST /api/orders` — Create new order
- `PUT /api/orders/{id}` — Update order
- `DELETE /api/orders/{id}` — Cancel order
- `GET /api/orders/{id}/status` — Get order status
- `PATCH /api/orders/{id}/status` — Update order status
- `GET /api/customers/{customerId}/orders` — Get customer's orders
- `GET /api/restaurants/{restaurantId}/orders` — Get restaurant's orders

## Testing Strategy

### Unit Tests (OrderEntityTest)
- Entity instantiation with valid/invalid data
- Getter/setter functionality
- Relationship handling
- Validation annotations trigger correctly

### Service Tests (OrderServiceTest)
- createOrder with valid/invalid inputs
- Authorization checks (customer/restaurant ownership)
- Status transitions and validation
- Query methods return correct results
- Edge cases (null checks, boundary values)

### Integration Tests
- End-to-end order creation → status transitions → delivery
- Database integrity (foreign keys enforced)
- Transaction rollback on validation failure

**Target:** 40+ tests (unit + integration)

## Acceptance Criteria

- ✅ OrderEntity.java exists with all 13 fields
- ✅ @Entity, @Table(name="orders") applied
- ✅ All relationships (@ManyToOne) correctly configured with EAGER fetch
- ✅ @JoinColumn annotations specify correct FK names
- ✅ @CreationTimestamp, @UpdateTimestamp annotations applied
- ✅ @Positive, @Size, @NotNull annotations on appropriate fields
- ✅ Orders table created in MySQL with all columns and indexes
- ✅ Foreign key constraints prevent deletion of referenced records
- ✅ OrderRepository with 15+ query methods
- ✅ OrderService with 25+ business logic methods
- ✅ Manual null validation in all service methods
- ✅ Authorization checks (customer/restaurant ownership)
- ✅ mvn clean compile runs without errors
- ✅ Spring Boot starts without Hibernate warnings
- ✅ DBeaver shows orders table with relationships to customers, restaurants, addresses

## Implementation Notes

1. **Order Number Generation:** Generate format like "ORD-20260325-001234" (date + counter)
2. **Eager Loading:** Use FetchType.EAGER for customer, restaurant, address to avoid N+1 queries
3. **BigDecimal:** Essential for monetary precision; never use Float/Double
4. **Status Enum:** Keep as String for flexibility; valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
5. **Foreign Keys:** Use ON DELETE RESTRICT (don't allow deletion of customer/restaurant while orders exist for audit)
6. **Timestamps:** orderDate set on creation, estimatedDeliveryTime on confirmation, actualDeliveryTime on delivery
7. **No Cascades:** Don't use cascade delete on orders (preserve for audit/history)
8. **Address Separation:** deliveryAddress references address table (not embedded in order)

## Future Considerations

- Order items/line items (separate feature with @OneToMany relationship)
- Order rating/review after delivery
- Payment information tracking
- Driver assignment and tracking
- Refund/cancellation workflow
- Order history archival

