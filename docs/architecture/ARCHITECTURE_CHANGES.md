# Architecture Changes - Schema v2.0 Alignment

**Version:** 2.0  
**Date:** March 27, 2026  
**Focus:** Entity relationships, Foreign Key design, Service layer updates

---

## Overview

This document describes the architectural changes made to align with the database schema specification while preserving business logic improvements.

---

## Entity Relationship Diagram - Updated

### Before (v1.0)
```
UserEntity
  ├─ 1:1 ─→ CustomerEntity
  ├─ 1:M ─→ RestaurantEntity (owner)
  └─ 1:M ─→ EmployeeEntity

RestaurantEntity
  ├─ M:1 ─→ UserEntity (owner_id) ← COLUMN NAME: owner_id
  ├─ 1:M ─→ ProductEntity
  ├─ 1:M ─→ EmployeeEntity
  └─ EMBEDDED: Address fields

EmployeeEntity
  ├─ M:1 ─→ UserEntity (MISSING) ✗
  ├─ M:1 ─→ RestaurantEntity (required)
  └─ EMBEDDED: Address fields

CustomerEntity
  ├─ 1:1 ─→ UserEntity
  ├─ 1:M ─→ OrderEntity
  ├─ M:1 ─→ RestaurantEntity (preferred)
  └─ EMBEDDED: Address (no FK)

OrderEntity
  ├─ M:1 ─→ CustomerEntity
  ├─ M:1 ─→ RestaurantEntity
  ├─ M:1 ─→ AddressEntity (delivery address)
  ├─ status: String (PENDING, CONFIRMED, etc)
  └─ EMBEDDED: Timestamps, totals

OrderStatusEntity
  └─ Reference/lookup table with statuses
    (Not referenced by OrderEntity as FK) ✗
```

### After (v2.0)
```
UserEntity
  ├─ 1:1 ←─ CustomerEntity
  ├─ 1:1 ←─ EmployeeEntity (NEW FK FROM EMPLOYEE)
  ├─ 1:M ←─ RestaurantEntity (owner)
  ├─ 1:M ←─ EmployeeEntity (REMOVED: was 1:M)
  └─ 1:M ←─ AddressEntity (user's addresses)

AddressEntity (ENHANCED ROLE)
  ├─ 1:1 ─→ UserEntity (optional, user's default address)
  ├─ M:1 ←─ EmployeeEntity (employee's address)
  ├─ 1:1 ←─ RestaurantEntity (UNIQUE: restaurant's primary address)
  ├─ M:1 ←─ CustomerEntity (customer's primary address)
  ├─ M:1 ←─ OrderEntity (delivery address)
  └─ Fields: street, city, state, zip_code, country, address_type, is_default

EmployeeEntity (RESTRUCTURED)
  ├─ 1:1 ─────→ UserEntity (NEW: Schema requirement)
  ├─ M:1 ─────→ AddressEntity (NEW: Schema requirement)
  ├─ M:1 ─────→ RestaurantEntity (NOW OPTIONAL: flexibility)
  └─ Additional fields: role, salary, hireDate, etc.

RestaurantEntity (ENHANCED)
  ├─ M:1 ─────→ UserEntity (RENAMED FK: owner_id → user_id)
  ├─ 1:1 ─────→ AddressEntity (NEW: UNIQUE address FK - Schema requirement)
  ├─ 1:M ─────→ ProductEntity
  ├─ 1:M ─────→ EmployeeEntity
  ├─ 1:M ─────→ OrderEntity
  ├─ priceRange: Integer (NEW: 1-3 scale - Schema requirement)
  └─ EMBEDDED address fields retained (denormalization for performance)

CustomerEntity (ENHANCED)
  ├─ 1:1 ─────→ UserEntity
  ├─ M:1 ─────→ AddressEntity (NEW: Schema requirement)
  ├─ 1:M ─────→ OrderEntity
  ├─ M:1 ─────→ RestaurantEntity (preferred, optional)
  └─ Additional: loyaltyPoints, isActive, lastOrderDate

OrderEntity (MAJOR CHANGE)
  ├─ M:1 ─────→ CustomerEntity
  ├─ M:1 ─────→ RestaurantEntity
  ├─ M:1 ─────→ AddressEntity (delivery address)
  ├─ M:1 ─────→ OrderStatusEntity (NEW FK: replaces status String - Schema requirement)
  ├─ restaurantRating: Integer (NEW: 1-5 nullable - Schema requirement)
  ├─ getStatus(): String (COMPATIBILITY: returns orderStatus.getStatusCode())
  ├─ setStatus(String): deprecated (COMPATIBILITY: maps String to OrderStatusEntity FK)
  └─ Additional: orderNumber, totalPrice, specialInstructions, etc.

OrderStatusEntity (ENHANCED)
  ├─ name: String (NEW: Required by schema - user-friendly name)
  ├─ statusCode: String (KEPT: Machine-readable code - PENDING, CONFIRMED, etc.)
  ├─ statusName: String (KEPT: Backward compatibility mirror of 'name')
  ├─ description: String (KEPT: Admin-visible description)
  ├─ displayOrder: Integer (KEPT: UI ordering 1-7)
  └─ isActive: Boolean (KEPT: Soft delete capability)

ProductEntity (FIELD RENAME)
  ├─ M:1 ─────→ RestaurantEntity
  ├─ cost: BigDecimal (RENAMED from price - Schema requirement)
  ├─ name, description: Strings
  ├─ isAvailable: Boolean
  └─ Additional: timestamps

ProductOrderEntity (NO CHANGE)
  ├─ 1:M (part of composite FK) ─→ OrderEntity
  ├─ M:1 ──────→ ProductEntity
  ├─ quantity: Integer
  ├─ unitPrice: BigDecimal
  ├─ subtotal: BigDecimal (computed)
  └─ UNIQUE(order_id, product_id)
```

---

## Key Architectural Changes

### 1. Foreign Key Strategy

#### Change: String status → OrderStatusEntity FK

**Before:**
```java
@Column(name = "status", nullable = false, length = 50)
String status;  // Values: "PENDING", "CONFIRMED", etc.
```

**After:**
```java
@ManyToOne(fetch = FetchType.EAGER, optional = false)
@JoinColumn(name = "order_status_id", nullable = false)
OrderStatusEntity orderStatus;  // Reference to reference table
```

**Why:**
- ✅ Referential integrity: Database enforces valid statuses
- ✅ Normalization: Single source of truth for status definitions
- ✅ Extensibility: Easy to add new fields to OrderStatus (description, display_order, etc.)
- ✅ Schema compliance: Matches required schema design

**Compatibility:**
- `getStatus()` returns `orderStatus.getStatusCode()` (String)
- Old code continues to work; marked for future deprecation

---

#### Change: EmployeeEntity now has user_id FK

**Before:**
```java
@ManyToOne
@JoinColumn(name = "restaurant_id")
RestaurantEntity restaurant;

// Missing link to User - EmployeeEntity was isolated from user context
```

**After:**
```java
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id", nullable = false, unique = true)
UserEntity user;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "address_id", nullable = false)
AddressEntity address;

@ManyToOne(fetch = FetchType.LAZY, optional = true)
RestaurantEntity restaurant;  // Now optional
```

**Why:**
- ✅ Every employee linked to user account (authentication, permissions)
- ✅ Employee has dedicated address FK (not embedded)
- ✅ Optional restaurant allows employees not assigned to specific restaurant
- ✅ Schema compliance: Matches reference design

---

#### Change: RestaurantEntity address becomes FK (not embedded)

**Before:**
```java
@Embedded
RestaurantAddressEmbeddable address;  // Embedded fields only

// Separate database fields:
@Column(name = "street")
String street;
@Column(name = "city")
String city;
```

**After:**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "address_id", nullable = false, unique = true)
AddressEntity address;  // FK to AddressEntity

// Embedded fields STILL PRESENT (denormalization for performance)
@Column(name = "street")
String street;
@Column(name = "city")
String city;
```

**Why:**
- ✅ Schema compliance: Required address FK
- ✅ Performance: Keep denormalized fields for query efficiency
- ✅ Flexibility: Can fetch related addresses directly
- ✅ Data integrity: Single address record per restaurant

---

### 2. Service Layer Updates

#### OrderService Changes

**Before:**
```java
public OrderEntity createOrder(OrderEntity order) {
    order.setStatus("PENDING");  // String assignment
    return orderRepository.save(order);
}

public OrderEntity confirmOrder(Long id) {
    OrderEntity order = orderRepository.findById(id)...;
    order.setStatus("CONFIRMED");  // Manual string change
    return orderRepository.save(order);
}
```

**After:**
```java
@Autowired
private OrderStatusRepository orderStatusRepository;

public OrderEntity createOrder(OrderEntity order) {
    OrderStatusEntity pendingStatus = orderStatusRepository
        .findByStatusCodeAndIsActive("PENDING", true)
        .orElseThrow(() -> new IllegalArgumentException("PENDING status not found"));
    order.setOrderStatus(pendingStatus);  // FK assignment
    return orderRepository.save(order);
}

public OrderEntity confirmOrder(Long id) {
    OrderEntity order = orderRepository.findById(id)...;
    return setOrderStatusByCode(id, "CONFIRMED");
}

// New method for status transitions
public OrderEntity setOrderStatusByCode(Long id, String statusCode) {
    OrderStatusEntity status = orderStatusRepository
        .findByStatusCodeAndIsActive(statusCode.trim(), true)
        .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + statusCode));
    return setOrderStatus(id, status);
}
```

**Benefits:**
- ✅ Database constraints ensure valid statuses
- ✅ Service validates statuses at lookup time
- ✅ Easier to debug (reference to actual OrderStatusEntity)
- ✅ Can access status metadata (description, display_order, etc.)

---

#### OrderStatusService Changes

**Before:**
```java
public OrderStatusEntity createOrderStatus(String statusCode, String statusName) {
    OrderStatusEntity status = new OrderStatusEntity();
    status.setStatusCode(statusCode);
    status.setStatusName(statusName);
    // Missing 'name' field
    return orderStatusRepository.save(status);
}
```

**After:**
```java
public OrderStatusEntity createOrderStatus(String statusCode, String name) {
    OrderStatusEntity status = new OrderStatusEntity();
    status.setStatusCode(statusCode);
    status.setName(name);  // NEW: Required by schema
    status.setStatusName(name);  // KEPT: Backward compatibility
    // ... set other fields (description, displayOrder, etc.)
    return orderStatusRepository.save(status);
}
```

---

### 3. Denormalization Strategy

The project uses **intentional denormalization** while maintaining FK relationships:

```java
// RestaurantEntity example
@ManyToOne
@JoinColumn(name = "address_id")
AddressEntity address;  // FK relationship

// But ALSO keep embedded fields for denormalization:
@Column(name = "street")
String street;
@Column(name = "city")
String city;
@Column(name = "state")
String state;
@Column(name = "zip_code")
String zip_code;
@Column(name = "country")
String country;
```

**Why this hybrid approach:**

| Aspect | Benefit |
|--------|---------|
| **FK relationship** | Ensures data integrity, supports schema compliance |
| **Denormalized fields** | Avoids N+1 queries, faster searches by address fields |
| **Synchronization** | Must keep in sync (service layer responsibility) |

**Service responsibility:**
```java
public void createRestaurant(RestaurantEntity restaurant, AddressEntity address) {
    restaurant.setAddress(address);  // Set FK
    
    // Also sync denormalized fields
    restaurant.setStreet(address.getStreet());
    restaurant.setCity(address.getCity());
    // ... etc
    
    restaurantRepository.save(restaurant);
}
```

---

### 4. Fetch Strategy Decisions

#### LAZY Loading (Default)
Used for most relationships to avoid N+1 query problems:

```java
@ManyToOne(fetch = FetchType.LAZY)  // Don't load restaurant unless accessed
@JoinColumn(name = "restaurant_id")
RestaurantEntity restaurant;
```

**When:** Most entity relationships (owner, employee→restaurant, etc.)

#### EAGER Loading (Selected)
Used for frequently accessed relationships that must exist:

```java
@ManyToOne(fetch = FetchType.EAGER)  // Always load order status
@JoinColumn(name = "order_status_id", nullable = false)
OrderStatusEntity orderStatus;
```

**When:** Order status (always needed to display orders), never null relationships

---

## Data Migration Impact

### Records Affected

| Entity | Change | Data Action Required |
|--------|--------|----------------------|
| **Orders** | status String → orderStatus FK | Migrate status codes to order_status_id FKs |
| **Restaurants** | Add address_id FK | Assign/create address for each restaurant |
| **Employees** | Add user_id + address_id FKs | Link to users and assign addresses |
| **Customers** | Add address_id FK | Assign/create address for each customer |
| **OrderStatuses** | Add name field | Populate from existing statusName |
| **Products** | Rename price → cost | No data change, just column rename |

---

## Testing Considerations

### Unit Tests to Update

1. **OrderService tests**: Mock OrderStatusRepository instead of String status
2. **EmployeeService tests**: Provide user_id and address_id in fixtures
3. **RestaurantService tests**: Include address_id relationships
4. **Compatibility tests**: Verify getStatus() returns correct string for backward compatibility

### Integration Tests

```java
@Test
void orderCreationUsesOrderStatusFK() {
    // Arrange
    OrderStatusEntity pending = orderStatusRepository.findByStatusCode("PENDING");
    
    // Act
    OrderEntity order = orderService.createOrder(customer, restaurant);
    
    // Assert
    assertNotNull(order.getOrderStatus());
    assertEquals("PENDING", order.getStatus());  // compatibility method
    assertEquals(pending.getId(), order.getOrderStatus().getId());
}
```

### Database Tests

- Verify FK constraints prevent invalid status assignments
- Verify unique constraint on restaurant.address_id
- Verify check constraint on price_range (1-3)

---

## Performance Implications

### Positive Impact

| Change | Performance Benefit |
|--------|---------------------|
| **OrderStatus FK** | Single query for status info instead of string comparison |
| **Address FK** | Can lazy-load detailed address without denormalized fields clogging cache |
| **Indexes on FKs** | Faster lookups by user_id, address_id, order_status_id |

### Potential Concerns

| Area | Mitigation |
|------|-----------|
| **Extra joins** | EAGER fetch on frequently accessed relationships (orderStatus) |
| **Lazy loading** | Use join fetch in queries that need relationships |
| **Denormalization sync** | Carefully manage in service layer; consider events for cross-entity updates |

---

## Backward Compatibility

### What Still Works

- ✅ `order.getStatus()` returns status code string
- ✅ Old database queries using `status` column still work (column preserved)
- ✅ All existing API endpoints return same response format via compatibility methods
- ✅ Legacy code using `setStatus(String)` still functions (marked deprecated)

### What's Deprecated

- ⚠️ `order.setStatus(String)` — Use `setOrderStatusByCode(Long, String)` instead
- ⚠️ Direct String comparisons on order status — Reference orderStatus.getStatusCode() or use helper methods

### Migration Path

```timeline
Phase 1 (Current):
  - New code uses FK relationships
  - Old code works via compatibility layer
  - Flags set to deprecated

Phase 2 (Future):
  - Remove deprecated methods
  - Update all code to use FK relationships
  - Option: Remove status String column from Orders table
```

---

## Summary of Improvements

| Aspect | Before | After | Benefit |
|--------|--------|-------|---------|
| **Schema Compliance** | Partial | ✅ Full | Matches specification |
| **Data Integrity** | String status | FK constraint | Valid states enforced |
| **Employee Isolation** | No user link | 1:1 to User | Better auth/permissions |
| **Address Management** | Denormalized | FK + Denormalized | Best of both |
| **Backward Compat** | N/A | ✅ Full | Smooth migration |
| **Query Performance** | String search | Index on FK | Faster lookups |

---

## Files Modified

See [SCHEMA_COMPLIANCE.md](SCHEMA_COMPLIANCE.md) for complete file list with line numbers.

---

## References

- Entity definitions: `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- Service implementations: `src/main/java/com/rocketFoodDelivery/rocketFood/service/`
- Schema specification: `docs/support_materials_11v2/db_schema_11.txt`
- Database diagram: `docs/support_materials_11v2/db_schema_11.jpg`
