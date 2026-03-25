# Order Statuses Schema Feature

## Overview

Create the **OrderStatusEntity** JPA class representing the master list of order statuses in the RocketDelivery system. This entity acts as a reference/lookup table defining all possible order lifecycle states with descriptions, display ordering, and activation control.

## Database Schema

### Table: `order_statuses`

```sql
CREATE TABLE order_statuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status_code VARCHAR(50) NOT NULL UNIQUE,
    status_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_status_code (status_code),
    INDEX idx_is_active (is_active),
    INDEX idx_display_order (display_order)
);
```

### Pre-Populated Reference Data

| status_code | status_name | Description | display_order | is_active |
|------------|-------------|-------------|---------------|-----------|
| PENDING | Pending | Order received, awaiting confirmation | 1 | true |
| CONFIRMED | Confirmed | Order confirmed by restaurant | 2 | true |
| PREPARING | Preparing | Restaurant is preparing the order | 3 | true |
| READY | Ready | Order is ready for pickup/delivery | 4 | true |
| OUT_FOR_DELIVERY | Out for Delivery | Order is on its way to customer | 5 | true |
| DELIVERED | Delivered | Order has been successfully delivered | 6 | true |
| CANCELLED | Cancelled | Order was cancelled | 7 | true |

### Fields & Constraints

| Field | Type | Nullable | Default | Unique | Constraints |
|-------|------|----------|---------|--------|-------------|
| `id` | BIGINT | NO | AUTO_INCREMENT | YES | Primary Key |
| `statusCode` | VARCHAR(50) | NO | — | YES | Machine-readable code (e.g., "PENDING") |
| `statusName` | VARCHAR(100) | NO | — | YES | User-friendly display name (e.g., "Pending") |
| `description` | VARCHAR(500) | YES | — | — | Detailed explanation of status |
| `displayOrder` | INT | NO | 1 | — | Display sequence in UI (1-10) |
| `isActive` | BOOLEAN | NO | true | — | Enable/disable status (soft-delete) |
| `createdAt` | TIMESTAMP | NO | CURRENT_TIMESTAMP | — | Auto-set on creation |
| `updatedAt` | TIMESTAMP | YES | — | — | Auto-update on modification |

## Entity Implementation

### OrderStatusEntity.java

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/OrderStatusEntity.java`

**Class Structure:**
- `@Entity` — Map to database table
- `@Table(name = "order_statuses")` — Specify table name
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` — Lombok boilerplate
- Manual null validation in service layer (no @NonNull annotations)

**Fields (8 total):**

1. `id` (Long)
   - `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - Primary key, auto-increment
   - Immutable after creation

2. `statusCode` (String)
   - `@Column(unique = true, nullable = false, length = 50)`
   - `@NotNull`, `@Size(min=1, max=50)`
   - Machine-readable status identifier (e.g., "PENDING", "CONFIRMED")
   - Used internally by services for status checks
   - Immutable reference data

3. `statusName` (String)
   - `@Column(unique = true, nullable = false, length = 100)`
   - `@NotNull`, `@Size(min=1, max=100)`
   - User-friendly display name (e.g., "Pending", "Confirmed")
   - Shown to customers in order tracking
   - Immutable reference data

4. `description` (String)
   - `@Column(nullable = true, length = 500)`
   - `@Size(max=500)`
   - Detailed explanation of what status means
   - Optional field; helps staff understand status intent
   - Example: "Order received, awaiting confirmation"

5. `displayOrder` (Integer)
   - `@Column(nullable = false)`
   - `@NotNull`, `@Min(1)`, `@Max(10)`
   - Determines display sequence in UI dropdown/list
   - Lower numbers appear first
   - Used for sorted queries

6. `isActive` (Boolean)
   - `@Column(nullable = false)`
   - `@NotNull`
   - true if status is available, false if deprecated/disabled
   - Enables soft-delete without losing historical data
   - Defaults to true on creation

7. `createdAt` (LocalDateTime)
   - `@CreationTimestamp`
   - `@Column(nullable = false, updatable = false)`
   - Auto-set to current timestamp on creation
   - Not updatable

8. `updatedAt` (LocalDateTime)
   - `@UpdateTimestamp`
   - `@Column(nullable = true)`
   - Auto-updated on any modification
   - Initially null; set on first update

**Helper Methods:**
- `getPendingStatus()` [static] — Get PENDING status by code (factory)
- `getDeliveredStatus()` [static] — Get DELIVERED status by code (factory)
- `getByStatusCode(String)` [static] — Lookup status by code
- `isTerminalStatus()` — Returns true if DELIVERED or CANCELLED
- `canTransitionTo(OrderStatusEntity)` — Validate status transition rules
- `getDisplayText()` — Return formatted display name with description

**Relationships:**
- 1-to-Many: one OrderStatus can be referenced by many Orders (implicit, not mapped here)
- Read-only reference table: no modifications during runtime

**Validation:**
- `statusCode`: Not null, unique, 1-50 characters, uppercase (convention)
- `statusName`: Not null, unique, 1-100 characters
- `description`: Optional, max 500 characters
- `displayOrder`: Not null, 1-10 range
- `isActive`: Not null, defaults to true

## OrderStatusRepository

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/OrderStatusRepository.java`

**Extends:** `JpaRepository<OrderStatusEntity, Long>`

**Query Methods (15+ methods):**

1. `findByStatusCode(String)` — Find by machine-readable code
2. `findByStatusName(String)` — Find by display name
3. `findByStatusCodeAndIsActive(String, Boolean)` — Get active status by code
4. `findByIdAndStatusCode(Long, String)` — Authorization/verification
5. `findByIsActiveOrderByDisplayOrder(Boolean)` — List active statuses in UI order
6. `findAllByIsActiveOrderByDisplayOrder(Boolean)` — Get all active statuses
7. `findAllOrderByDisplayOrder()` — All statuses in display order
8. `countByIsActive(Boolean)` — Count active/inactive statuses
9. `existsByStatusCode(String)` — Check status exists by code
10. `existsByStatusCodeAndIsActive(String, Boolean)` — Check active status exists
11. `existsByStatusName(String)` — Check status name exists
12. `getAllActiveStatuses()` — Get all enabled statuses (with @Query)
13. `getStatusByCode(String)` — Typed query for status lookup
14. `getTerminalStatuses()` — Get DELIVERED and CANCELLED statuses
15. `getTransitionableStatuses(String)` — Get valid next statuses (with @Query)

**Verification Methods:**
- `existsByStatusCode(String)` — Check if status code exists (prevent duplicates)
- `existsByStatusName(String)` — Check if status name exists

## OrderStatusService

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/OrderStatusService.java`

**Dependencies:**
- `OrderStatusRepository` (required)
- Constructor injection with null validation

**Public Methods (20+ methods):**

### CRUD Operations
1. `createOrderStatus(statusCode, statusName, description, displayOrder)` — Create status
   - Validate code/name uniqueness
   - Defaults isActive to true
   - Not typically called at runtime (reference data)

2. `getOrderStatusById(id)` — Get by ID
   - Return Optional<OrderStatusEntity>

3. `getOrderStatusByCode(code)` — Get by statusCode
   - Return Optional<OrderStatusEntity>
   - Most common lookup method

4. `getOrderStatusByName(name)` — Get by statusName
   - Return Optional<OrderStatusEntity>

5. `updateOrderStatus(id, updates)` — Update status details
   - Only allow updates to description and displayOrder
   - Block updates to statusCode and statusName (immutable)

6. `deleteOrderStatus(id)` — Soft-delete by setting isActive=false
   - Do NOT hard-delete (preserve history)

### Query & Reference Data
7. `getAllOrderStatuses()` — Get all statuses (active + inactive)
8. `getActiveOrderStatuses()` — Get only active statuses
9. `getActiveOrderStatusesSorted()` — Get active statuses in UI display order
10. `getOrderStatusesByActive(Boolean)` — Filter by active flag
11. `isStatusActive(String statusCode)` — Check if status is enabled

### Status Management
12. `getPendingStatus()` — Get PENDING status (factory method)
13. `getConfirmedStatus()` — Get CONFIRMED status
14. `getPreparingStatus()` — Get PREPARING status
15. `getReadyStatus()` — Get READY status
16. `getOutForDeliveryStatus()` — Get OUT_FOR_DELIVERY status
17. `getDeliveredStatus()` — Get DELIVERED status
18. `getCancelledStatus()` — Get CANCELLED status

### Workflow Validation
19. `isTerminalStatus(statusCode)` — Check if status is DELIVERED or CANCELLED
20. `canTransitionStatus(currentCode, newCode)` — Validate transition rules
    - PENDING → CONFIRMED, CANCELLED
    - CONFIRMED → PREPARING, CANCELLED
    - PREPARING → READY
    - READY → OUT_FOR_DELIVERY
    - OUT_FOR_DELIVERY → DELIVERED
    - DELIVERED → terminal (no transitions)
    - CANCELLED → terminal (no transitions)

21. `getValidNextStatuses(currentCode)` — Get allowed next statuses
22. `validateStatusTransition(currentStatus, newStatus)` [private] — Verification

### Utilities
23. `hasOrderStatus(code)` — Check existence
24. `getStatusDisplayText(statusCode)` — Get formatted display string
25. `initializeReferenceData()` — Seed all standard statuses

**Validation & Authorization:**
- Manual null checks in every method
- `validateOrderStatusData()` [private]:
  - statusCode: 1-50 characters, uppercase convention
  - statusName: 1-100 characters, unique
  - description: max 500 characters if provided
  - displayOrder: 1-10 range
- Status transition validation: enforce workflow rules
- Immutability enforcement: block changes to statusCode/statusName

## REST API Endpoints (Future Implementation)

- `GET /api/order-statuses` — List all active statuses (public reference data)
- `GET /api/order-statuses/{id}` — Get status by ID
- `GET /api/order-statuses/code/{code}` — Get by statusCode
- `GET /api/order-statuses/active` — Get only active statuses
- `POST /api/order-statuses` — Create status (admin only)
- `PUT /api/order-statuses/{id}` — Update status (admin only)
- `DELETE /api/order-statuses/{id}` — Deactivate status (soft-delete)

## Testing Strategy

### Unit Tests (OrderStatusEntityTest)
- Entity instantiation with valid/invalid data
- Getter/setter functionality
- Terminal status detection
- Validation annotations trigger correctly

### Service Tests (OrderStatusServiceTest)
- Factory methods (getPendingStatus, getDeliveredStatus, etc.)
- Status transition validation (workflow rules)
- Query methods return correct results
- Reference data initialization
- Uniqueness enforcement (code/name)

### Integration Tests
- Reference data seeding on startup
- Status transitions in order workflow
- UI displays statuses in correct order
- Status availability (active flag respected)

**Target:** 30+ tests (unit + integration)

## Acceptance Criteria

- ✅ OrderStatusEntity.java exists with all 8 fields
- ✅ @Entity, @Table(name="order_statuses") applied
- ✅ Unique constraints on statusCode and statusName
- ✅ @CreationTimestamp, @UpdateTimestamp annotations applied
- ✅ @NotNull, @Size, @Min, @Max annotations on appropriate fields
- ✅ order_statuses table created in MySQL with all columns and indexes
- ✅ OrderStatusRepository with 15+ query methods
- ✅ OrderStatusService with 25+ business logic methods
- ✅ Factory methods for all standard statuses (PENDING, CONFIRMED, etc.)
- ✅ Status transition validation (workflow rules enforced)
- ✅ Manual null validation in all service methods
- ✅ Reference data seeded (7 standard statuses)
- ✅ mvn clean compile runs without errors
- ✅ Spring Boot starts without Hibernate warnings
- ✅ DBeaver shows order_statuses table with all columns

## Implementation Notes

1. **Reference/Lookup Table:** OrderStatus is immutable reference data; changes are rare
2. **7 Standard Statuses:** Must seed PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
3. **Display Order:** 1-7 for standard statuses; allows future additions not affecting ordering
4. **Soft Delete:** Use isActive flag, never hard-delete (preserve order history)
5. **Status Codes:** Follow UPPERCASE_WITH_UNDERSCORES convention (e.g., OUT_FOR_DELIVERY)
6. **Immutability:** statusCode and statusName should not change once seeded
7. **Workflow Rules:** Order status transitions follow strict paths (PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED)
8. **Terminal States:** DELIVERED and CANCELLED are final states (no further transitions)
9. **UI Ordering:** Use display_order for dropdown/list presentation to customers
10. **Performance:** Cache reference data in application for fast status lookups

## Status Transition Diagram

```
PENDING
  ├─→ CONFIRMED
  │    └─→ PREPARING
  │         └─→ READY
  │             └─→ OUT_FOR_DELIVERY
  │                 └─→ DELIVERED (terminal)
  └─→ CANCELLED (terminal)
```

## Future Considerations

- Status-specific icons or colors for UI
- Multi-language descriptions (internationalization)
- Status webhooks or notifications
- Custom status additions for specific restaurants
- Status change history/audit trail
- Role-based visibility of statuses
- Conditional status transitions based on business rules

## Dependencies

- **Database:** MySQL 8.0+ with InnoDB
- **Spring Data JPA:** For automatic repository implementation
- **Hibernate:** For entity mapping

