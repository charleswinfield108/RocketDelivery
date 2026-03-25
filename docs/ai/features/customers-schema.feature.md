# Feature: Customers Schema Implementation

## Overview
Implement the Customer entity and related infrastructure to support customer management in the RocketDelivery application. This feature includes the JPA entity, repository layer, service layer with business logic, REST controller endpoints, and comprehensive unit/integration tests.

## Status
🚀 **Ready to Implement** - Building on Users, Addresses, Employees, and Restaurants Schema patterns

## Technical Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.1.2
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven 3.11.0
- **Testing:** JUnit 5 + Mockito
- **Database:** MySQL 8.0

## Database Schema

### Customer Table
```sql
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    phone_number VARCHAR(20),
    loyalty_points INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_restaurant_id BIGINT,
    last_order_date DATETIME,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (preferred_restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_customer (user_id)
);

CREATE INDEX idx_user_id ON customers(user_id);
CREATE INDEX idx_is_active ON customers(is_active);
CREATE INDEX idx_preferred_restaurant ON customers(preferred_restaurant_id);
```

## Implementation Components

### 1. Customer Entity (CustomerEntity.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/CustomerEntity.java`

**Status:** 🔲 Not Created - Needs Implementation

**Fields (10 total):**
- `id` (Long) - Primary key, auto-increment
- `user` (UserEntity) - OneToOne relationship to users table
- `phoneNumber` (String, 20) - Customer phone, optional, validated
- `loyaltyPoints` (Integer) - Reward points accumulator, defaults to 0
- `isActive` (Boolean) - Customer status, defaults to TRUE
- `preferredRestaurant` (RestaurantEntity) - ManyToOne optional reference
- `lastOrderDate` (LocalDateTime) - Track customer activity, optional
- `createdAt` (LocalDateTime) - Creation timestamp, auto-generated
- `updatedAt` (LocalDateTime) - Update timestamp, auto-updated

**JPA Annotations:**
- `@Entity` & `@Table` with indexes (user_id, is_active, preferred_restaurant_id)
- `@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@OneToOne(fetch = FetchType.LAZY, optional = false)` on user (unique relationship)
- `@ManyToOne(fetch = FetchType.LAZY, optional = true)` on preferredRestaurant
- `@Column` with nullable, unique, length constraints
- `@CreationTimestamp` & `@UpdateTimestamp`
- Jakarta Validation: `@NotNull`, `@Size`, `@Min`, `@Max`

**Helper Methods:**
- `isLoyaltyEligible()` - Check if customer can redeem points
- `addLoyaltyPoints(int points)` - Increment loyalty points
- `redeemLoyaltyPoints(int points)` - Decrement loyalty points
- `hasPreferredRestaurant()` - Check if preferred restaurant is set

### 2. Customer Repository (CustomerRepository.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/CustomerRepository.java`

**Status:** 🔲 Not Created - Needs Implementation

**Query Methods (12+ expected):**
1. `findByUserId(Long userId)` - Find customer by user ID
2. `findByUserIdAndIsActive(Long userId, Boolean isActive)` - Find active customer
3. `findByIsActiveOrderByCreatedAtDesc(Boolean isActive)` - List active customers
4. `findByPreferredRestaurantId(Long restaurantId)` - Find customers with preferred restaurant
5. `findByLoyaltyPointsGreaterThanOrderByLoyaltyPointsDesc()` - Loyalty leaderboard
6. `existsByUserId(Long userId)` - Check if customer exists for user
7. `countByIsActive(Boolean isActive)` - Count active customers
8. `countByPreferredRestaurantId(Long restaurantId)` - Count customers for restaurant
9. Inherited CRUD: save, findById, delete, deleteAll, findAll, etc.
10. `findByLastOrderDateIsNotNullOrderByLastOrderDateDesc()` - Active customers
11. `findByIdAndUserId(Long customerId, Long userId)` - Authorization check
12. `deleteByIdAndUserId(Long customerId, Long userId)` - Authorized deletion

### 3. Customer Service (CustomerService.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/CustomerService.java`

**Status:** 🔲 Not Created - Needs Implementation

**Public Methods (18+ expected):**
1. `createCustomer(Long userId, CustomerEntity customer)` - Create with user verification
2. `getCustomerById(Long customerId)` - Get customer by ID
3. `getCustomerByUserId(Long userId)` - Get customer for user
4. `getCustomerByUserIdAndActive(Long userId)` - Get active customer for user
5. `getAllCustomers()` - Get all customers
6. `getActiveCustomers()` - Get active only
7. `getCustomersByPreferredRestaurant(Long restaurantId)` - Filter by restaurant
8. `getLoyaltyLeaderboard(int limit)` - Top loyal customers
9. `getActiveCustomersByRestaurant(Long restaurantId)` - Active for restaurant
10. `updateCustomer(Long customerId, Long userId, CustomerEntity updated)` - Update with auth
11. `deleteCustomer(Long customerId, Long userId)` - Delete with authorization
12. `setCustomerStatus(Long customerId, Long userId, Boolean isActive)` - Change status
13. `setPreferredRestaurant(Long customerId, Long userId, Long restaurantId)` - Change preference
14. `addLoyaltyPoints(Long customerId, int points)` - Reward points
15. `redeemLoyaltyPoints(Long customerId, int points)` - Spend points
16. `updateLastOrderDate(Long customerId)` - Track activity
17. `getActiveCustomerCount()` - Total active customers
18. `validateCustomerData(CustomerEntity customer)` [private] - Validation method

**Features:**
- Manual null validation (no @NonNull annotations)
- Authorization checks using userId verification
- Fail-fast exceptions (IllegalArgumentException, RuntimeException)
- Loyalty points tracking and management
- User-customer relationship enforcement (one-to-one)
- Activity tracking via lastOrderDate

### 4. REST Controller (CustomerController.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/controller/CustomerController.java`

**Status:** 🔲 Not Created - Needs Implementation

**REST Endpoints (9+ expected):**
- `POST /api/customers` - Create new customer
- `GET /api/customers` - List all customers
- `GET /api/customers/{id}` - Get specific customer
- `GET /api/users/{userId}/customer` - Get customer for user
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer
- `PATCH /api/customers/{id}/status` - Change customer status
- `PATCH /api/customers/{id}/preferred-restaurant` - Set preferred restaurant
- `PATCH /api/customers/{id}/loyalty-points` - Add/redeem loyalty points

**Request/Response DTOs:**
- `CustomerDTO` - API transfer object
- `CustomerCreateRequest` - Create request model
- `CustomerUpdateRequest` - Update request model
- `LoyaltyPointsRequest` - Loyalty operation request

## Testing & Validation

### Unit Tests
- **CustomerEntityTest.java** - Entity validation and field tests (Target: 15+ tests)
  - Tests for entity creation, field validation, getters/setters
  - Phone format validation
  - Loyalty points constraints
  - Timestamp auto-population
  - toString() and equals() implementations
  - Helper method tests (isLoyaltyEligible, addLoyaltyPoints, etc.)

- **CustomerServiceTest.java** - Business logic and authorization tests (Target: 25+ tests)
  - CRUD operation tests (success and failure scenarios)
  - Authorization checks (user verification)
  - Data validation tests
  - Null parameter handling
  - Loyalty points operations
  - Status management tests
  - Preferred restaurant updates
  - Activity tracking tests

### Integration Tests
- API endpoint tests with Spring Boot Test
- Database persistence verification
- Transaction management validation
- Loyalty points accumulation flow

### Test Coverage Target
- **Unit Tests:** 40+
- **Integration Tests:** 5+
- **Total Coverage:** 45+ tests
- **Target Pass Rate:** 100%

## Key Validations

### Field Validations
- `phoneNumber`: Optional, 10-20 characters if provided
- `loyaltyPoints`: Integer >= 0, defaults to 0
- `isActive`: Required Boolean, defaults to TRUE
- `lastOrderDate`: Optional, must be past or present

### Business Rules
- One customer per user (one-to-one relationship)
- Loyalty points cannot go negative
- Preferred restaurant must exist and be active
- Only active customers can place orders
- Loyalty points tracked for rewards
- Activity tracked via last order date
- Cascading delete when user is deleted

## Implementation Roadmap

### Phase 1: Entity & Repository
- [ ] Create CustomerEntity with OneToOne user relationship
- [ ] Create CustomerRepository with 12+ query methods
- [ ] Include authorization and activity tracking

### Phase 2: Service Implementation
- [ ] Create CustomerService with 18+ methods
- [ ] Implement authorization checks
- [ ] Add comprehensive validation
- [ ] Handle null cases with fail-fast approach
- [ ] Implement loyalty points logic

### Phase 3: REST & Testing
- [ ] Create CustomerController with 9+ endpoints
- [ ] Implement CustomerEntityTest (15+ tests)
- [ ] Implement CustomerServiceTest (25+ tests)
- [ ] Verify all tests pass (45+ total)

### Phase 4: Integration & Polish
- [ ] Verify all tests pass (100% pass rate)
- [ ] Verify zero compilation warnings
- [ ] Commit and merge with dev
- [ ] Update documentation

## Acceptance Criteria (Pattern-based)
- [ ] CustomerEntity with user one-to-one relationship
- [ ] CustomerRepository with authorization query methods
- [ ] CustomerService with business logic and null validation
- [ ] REST endpoints for CRUD and loyalty operations
- [ ] 45+ comprehensive unit+integration tests
- [ ] Zero compilation warnings
- [ ] All tests passing (100% pass rate)
- [ ] Feature merged with dev branch
- [ ] Code follows established patterns (Users/Restaurants/Employees)

## Notes
- Following established patterns from previous schema implementations
- User-customer one-to-one relationship (each user has one customer profile)
- Manual null validation (no @NonNull annotations)
- Comprehensive field validation
- Timestamp tracking for audit trail and activity monitoring
- Loyalty points as integer accumulator for rewards
- Support for preferred restaurant preference (can be null if no preference)

---

**Created:** 2026-03-25
**Status:** Ready for implementation
**Previous Features Merged:** Users ✅, Addresses ✅, Employees ✅, Restaurants ✅
**Next Step:** Implement CustomerEntity and CustomerRepository
