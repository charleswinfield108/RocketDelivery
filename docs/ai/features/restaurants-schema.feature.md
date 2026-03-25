# Feature: Restaurants Schema Implementation

## Overview
Implement the Restaurant entity and related infrastructure to support restaurant management in the RocketDelivery application. This feature includes the JPA entity, repository layer, service layer with business logic, REST controller endpoints, and comprehensive unit/integration tests.

## Status
🚀 **Ready to Implement** - Building on Users and Employees Schema patterns

## Technical Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.1.2
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven 3.11.0
- **Testing:** JUnit 5 + Mockito
- **Database:** MySQL 8.0

## Database Schema

### Restaurant Table
```sql
CREATE TABLE restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_name ON restaurants(name);
CREATE INDEX idx_is_active ON restaurants(is_active);
CREATE INDEX idx_owner_id ON restaurants(owner_id);
```

## Implementation Components

### 1. Restaurant Entity (RestaurantEntity.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/RestaurantEntity.java`

**Status:** ✅ ALREADY CREATED (from Employees Schema)

**Fields (13 total):**
- `id` (Long) - Primary key, auto-increment
- `name` (String, 255) - Restaurant name, unique, required
- `description` (String, TEXT) - Restaurant description, optional
- `street` (String, 255) - Street address, optional
- `city` (String, 100) - City name, optional
- `state` (String, 100) - State/Province, optional
- `zipCode` (String, 20) - Postal code, optional
- `country` (String, 100) - Country, optional
- `phoneNumber` (String, 20) - Contact phone, optional, validated
- `email` (String, 255) - Contact email, optional, validated
- `isActive` (Boolean) - Status flag, defaults to TRUE
- `createdAt` (LocalDateTime) - Creation timestamp, auto-generated
- `updatedAt` (LocalDateTime) - Update timestamp, auto-updated

**JPA Annotations:**
- `@Entity` & `@Table` with indexes (name, is_active)
- `@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@Column` with nullable, unique, length constraints
- `@CreationTimestamp` & `@UpdateTimestamp`
- Jakarta Validation: `@NotBlank`, `@Email`, `@Size`

**Helper Methods:**
- `getFullAddress()` - Concatenates address components
- `isActive()` - Boolean status check

### 2. Restaurant Repository (RestaurantRepository.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/RestaurantRepository.java`

**Status:** ✅ ALREADY CREATED (from Employees Schema)

**Query Methods (5 total):**
1. `findByName(String name)` - Find by unique name
2. `findByIsActiveOrderByNameAsc(Boolean isActive)` - List active/inactive restaurants sorted
3. `countByIsActive(Boolean isActive)` - Count by status
4. `existsByName(String name)` - Check name availability
5. Inherited CRUD: save, findById, delete, deleteAll, findAll, etc.

**Enhancements Needed:**
- Add `findByOwnerIdOrderByNameAsc(Long ownerId)` - List owner's restaurants
- Add `findByOwnerIdAndIsActive(Long ownerId, Boolean isActive)` - Filter by owner and status
- Add `findByIdAndOwnerId(Long restaurantId, Long ownerId)` - Authorization check
- Add `deleteByIdAndOwnerId(Long restaurantId, Long ownerId)` - Authorized deletion
- Add `existsByIdAndOwnerId(Long restaurantId, Long ownerId)` - Ownership verification

### 3. Restaurant Service (RestaurantService.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/RestaurantService.java`

**Status:** 🔲 Not Created - Needs Implementation

**Public Methods (15+ expected):**
1. `createRestaurant(Long ownerId, RestaurantEntity restaurant)` - Create with owner verification
2. `getRestaurantById(Long restaurantId)` - Get restaurant by ID
3. `getRestaurantByIdAndOwner(Long restaurantId, Long ownerId)` - Get with authorization
4. `getAllRestaurants()` - Get all restaurants (paginated)
5. `getActiveRestaurants()` - Get only active restaurants
6. `getRestaurantsByOwner(Long ownerId)` - List owner's restaurants
7. `getActiveRestaurantsByOwner(Long ownerId)` - List active restaurants for owner
8. `getRestaurantByName(String name)` - Find by unique name
9. `updateRestaurant(Long restaurantId, Long ownerId, RestaurantEntity updated)` - Update with auth
10. `deleteRestaurant(Long restaurantId, Long ownerId)` - Delete with authorization
11. `setRestaurantStatus(Long restaurantId, Long ownerId, Boolean isActive)` - Change status
12. `getRestaurantCount()` - Total restaurant count
13. `getActiveRestaurantCount()` - Count active restaurants
14. `hasRestaurants(Long ownerId)` - Check if owner has restaurants
15. `validateRestaurantData(RestaurantEntity restaurant)` [private] - Validation method

**Features:**
- Manual null validation (no @NonNull annotations)
- Authorization checks using ownerId verification
- Fail-fast exceptions (IllegalArgumentException, RuntimeException)
- Comprehensive data validation
- Email and phone number format validation

### 4. REST Controller (RestaurantController.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/controller/RestaurantController.java`

**Status:** 🔲 Not Created - Needs Implementation

**REST Endpoints (7 expected):**
- `POST /api/restaurants` - Create new restaurant
- `GET /api/restaurants` - List all restaurants (paginated)
- `GET /api/restaurants/{id}` - Get specific restaurant
- `GET /api/restaurants/owner/{ownerId}` - List owner's restaurants
- `PUT /api/restaurants/{id}` - Update restaurant
- `DELETE /api/restaurants/{id}` - Delete restaurant
- `PATCH /api/restaurants/{id}/status` - Change restaurant status

**Request/Response DTOs:**
- `RestaurantDTO` - API transfer object
- `RestaurantCreateRequest` - Create request model
- `RestaurantUpdateRequest` - Update request model

## Testing & Validation

### Unit Tests
- **RestaurantEntityTest.java** - Entity validation and field tests (Target: 15+ tests)
  - Tests for entity creation, field validation, getters/setters
  - Email and phone format validation
  - Timestamp auto-population
  - toString() and equals() implementations

- **RestaurantServiceTest.java** - Business logic and authorization tests (Target: 20+ tests)
  - CRUD operation tests (success and failure scenarios)
  - Authorization checks (owner verification)
  - Data validation tests
  - Null parameter handling
  - Email uniqueness checks
  - Status management tests

### Integration Tests
- API endpoint tests with Spring Boot Test
- Database persistence verification
- Transaction management validation

### Test Coverage Target
- **Unit Tests:** 35+
- **Integration Tests:** 5+
- **Total Coverage:** 40+ tests
- **Target Pass Rate:** 100%

## Key Validations

### Field Validations
- `name`: Required, 2-255 characters, unique
- `email`: Optional, must be valid email format if provided
- `phoneNumber`: Optional, 10-20 characters if provided
- `street`: Optional, max 255 characters
- `city`: Optional, max 100 characters
- `state`: Optional, max 100 characters
- `zipCode`: Optional, max 20 characters
- `country`: Optional, max 100 characters

### Business Rules
- Restaurant name must be unique across system
- Only restaurant owner can modify/delete restaurant
- Status changes require authorization
- Active status affects employee queries
- Cascading deletes when restaurant is deleted

## Implementation Roadmap

### Phase 1: Repository Enhancements ✅
- ✅ RestaurantEntity already created
- [ ] Add owner_id foreign key to RestaurantEntity
- [ ] Update RestaurantRepository with 5 owner-based query methods

### Phase 2: Service Implementation
- [ ] Create RestaurantService with 15+ methods
- [ ] Implement authorization checks
- [ ] Add comprehensive validation
- [ ] Handle null cases with fail-fast approach

### Phase 3: REST & Testing
- [ ] Create RestaurantController with 7 endpoints
- [ ] Implement RestaurantEntityTest (15+ tests)
- [ ] Implement RestaurantServiceTest (20+ tests)
- [ ] Verify all tests pass (40+ total)

### Phase 4: Integration & Polish
- [ ] Verify all tests pass (100% pass rate)
- [ ] Verify zero compilation warnings
- [ ] Commit and merge with dev
- [ ] Update documentation

## Acceptance Criteria (Pattern-based)
- [ ] RestaurantEntity has owner relationship and all fields
- [ ] RestaurantRepository has authorization query methods
- [ ] RestaurantService with business logic and null validation
- [ ] REST endpoints for CRUD operations
- [ ] 40+ comprehensive unit+integration tests
- [ ] Zero compilation warnings
- [ ] All tests passing (100% pass rate)
- [ ] Feature merged with dev branch
- [ ] Code follows established patterns (Users/Addresses/Employees)

## Notes
- Following established patterns from Users, Addresses, and Employees Schema
- Authority checks in service layer (owner verification)
- Manual null validation (no @NonNull annotations)
- Comprehensive field validation
- Timestamp tracking for audit trail
- RestaurantEntity partially created, needs owner_id enhancement

---

**Created:** 2026-03-25
**Status:** Ready for implementation
**Previous Features Merged:** Users Schema ✅, Addresses Schema ✅, Employees Schema ✅
**Next Step:** Implement RestaurantService and RestaurantController
