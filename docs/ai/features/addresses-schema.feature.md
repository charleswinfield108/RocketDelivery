# Feature: Addresses Schema Implementation

## Overview
Implement the Address entity and related infrastructure to support user addresses in the RocketDelivery application. This feature includes the JPA entity, repository layer, service layer with business logic, REST controller endpoints, and comprehensive unit/integration tests.

## Status
🚀 **Ready to Implement** - Following Users Schema pattern

## Technical Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.1.2
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven 3.11.0
- **Testing:** JUnit 5 + Mockito
- **Database:** MySQL 8.0

## Database Schema

### Address Table
```sql
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    address_type VARCHAR(50),
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_default_address (user_id, is_default)
);

CREATE INDEX idx_user_id ON addresses(user_id);
```

## Implementation Components

### 1. Address Entity (AddressEntity.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/AddressEntity.java`

**Fields:**
- `id` (Long) - Primary key, auto-increment
- `userId` (Long) - Foreign key to users table
- `street` (String, 255) - Street address, required
- `city` (String, 100) - City name, required
- `state` (String, 100) - State/Province, required
- `zipCode` (String, 20) - Postal code, required
- `country` (String, 100) - Country name, required
- `addressType` (String, 50) - Type (HOME, WORK, OTHER), optional
- `isDefault` (Boolean) - Default address flag, defaults to false
- `createdAt` (LocalDateTime) - Creation timestamp, auto-set
- `updatedAt` (LocalDateTime) - Update timestamp

**Annotations:**
- `@Entity` - JPA entity
- `@Table(name = "addresses")` - Map to addresses table
- `@Data` - Lombok for getters/setters
- `@Getter` / `@Setter` - Explicit field annotations if needed
- `@CreationTimestamp` / `@UpdateTimestamp` - For temporal fields
- `@NotNull`, `@NotBlank`, `@Size` - Validation annotations

**Relationships:**
- `@ManyToOne` relationship to UserEntity (load user addresses)

### 2. AddressRepository (AddressRepository.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/AddressRepository.java`

**Methods:**
- `findByUserId(Long userId)` - Get all addresses for a user
- `findByUserIdAndIsDefaultTrue(Long userId)` - Get default address for user
- `findByUserIdAndAddressType(Long userId, String type)` - Get addresses by type
- `existsByUserIdAndIsDefaultTrue(Long userId)` - Check if user has default address
- `deleteByUserIdAndId(Long userId, Long addressId)` - Delete address by user and address id

**Extends:** `JpaRepository<AddressEntity, Long>`

### 3. AddressService (AddressService.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/AddressService.java`

**Methods:**
- `createAddress(Long userId, AddressEntity address)` - Create new address
  - Validate user exists
  - Validate address data
  - If isDefault=true, set other addresses to false for that user
  - Return created address

- `getAddressesByUserId(Long userId)` - Get all addresses for user
  - Validate user exists
  - Return addresses sorted by isDefault first

- `getDefaultAddress(Long userId)` - Get user's default address
  - Return default address or throw RuntimeException

- `updateAddress(Long addressId, AddressEntity updatedAddress)` - Update address
  - Check address exists
  - Check ownership (verify user_id match)
  - If isDefault changed to true, update other addresses
  - Return updated address

- `deleteAddress(Long addressId)` - Delete address
  - Check address exists
  - Check if default (warn in logs if deleting default)
  - Delete and return success

- `setDefaultAddress(Long userId, Long addressId)` - Set address as default
  - Verify user owns address
  - Update isDefault flags

- `validateAddressData(AddressEntity address)` - Validation logic
  - Check required fields not null/blank
  - Validate street (2-255 chars)
  - Validate city (2-100 chars)
  - Validate state (2-100 chars)
  - Validate zipCode (3-20 chars)
  - Validate country (2-100 chars)

**Annotations:**
- `@Service` - Spring service bean

**Exceptions:**
- `IllegalArgumentException` - Invalid address data
- `RuntimeException` - Address/user not found

### 4. AddressController (AddressController.java)
**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/controller/AddressController.java`

**Endpoints:**

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/{userId}/addresses` | Create new address |
| GET | `/api/users/{userId}/addresses` | Get all addresses for user |
| GET | `/api/users/{userId}/addresses/default` | Get default address |
| GET | `/api/addresses/{addressId}` | Get specific address |
| PUT | `/api/addresses/{addressId}` | Update address |
| DELETE | `/api/addresses/{addressId}` | Delete address |
| PATCH | `/api/addresses/{addressId}/set-default` | Set as default |

**Response Format:**
```json
{
  "id": 1,
  "userId": 1,
  "street": "123 Main St",
  "city": "San Francisco",
  "state": "CA",
  "zipCode": "94105",
  "country": "USA",
  "addressType": "HOME",
  "isDefault": true,
  "createdAt": "2026-03-25T12:00:00",
  "updatedAt": null
}
```

### 5. Tests

#### AddressEntityTest.java (15+ tests)
**Location:** `src/test/java/com/rocketFoodDelivery/rocketFood/AddressEntityTest.java`

**Test Cases:**
- Entity creation with valid data
- Entity creation with invalid data
- Validation annotations enforcement
- Timestamp auto-set on creation
- Field getter/setter functionality
- Equals/HashCode (if implemented)
- toString() representation

#### AddressServiceTest.java (20+ tests)
**Location:** `src/test/java/com/rocketFoodDelivery/rocketFood/AddressServiceTest.java`

**Test Cases:**
- `createAddress_Success` - Create address with valid data
- `createAddress_InvalidData` - Validation failures
- `createAddress_SetDefaultAddress` - Default address logic
- `createAddress_UserNotFound` - User validation
- `getAddressesByUserId_Success` - Retrieve user addresses
- `getAddressesByUserId_Empty` - No addresses for user
- `getDefaultAddress_Found` - Get default address
- `getDefaultAddress_NotFound` - No default set
- `updateAddress_Success` - Update address fields
- `updateAddress_NotFound` - Address doesn't exist
- `updateAddress_UnauthorizedUser` - User doesn't own address
- `deleteAddress_Success` - Delete address
- `deleteAddress_NotFound` - Delete non-existent address
- `setDefaultAddress_Success` - Change default address
- `setDefaultAddress_UnauthorizedUser` - User doesn't own address
- Validation tests for all fields

## Implementation Steps

### Phase 1: Entity & Repository
1. Create AddressEntity.java with all fields and validations
2. Create AddressRepository.java with required methods
3. Add @Table DDL comments for migration tracking
4. Verify Hibernate creates table on startup

### Phase 2: Service Layer
1. Create AddressService.java with business logic
2. Implement null checks and validation (manual, no @NonNull)
3. Add exception handling
4. Write service unit tests (AddressServiceTest.java)
5. Verify all 20+ tests pass

### Phase 3: REST Controller
1. Create AddressController.java with all endpoints
2. Add request/response validation
3. Add error handling with appropriate HTTP status codes
4. Test endpoints manually with curl/Postman

### Phase 4: Integration & Polish
1. Add database migration if using Flyway
2. Run full test suite (maven test)
3. Verify no warnings or errors
4. Commit and push feature branch
5. Create pull request to merge with dev

## Testing Strategy

**Unit Tests:**
- Service layer: Mock repository, test business logic
- Entity: Test validation annotations, getters/setters
- Use Mockito for repository mocks
- JUnit 5 assertions

**Integration Tests:**
- Test with real database (H2 for tests)
- Verify foreign key constraints
- Test cascade deletes
- Verify unique constraints

**Coverage Target:** 80%+ for service and entity classes

## Acceptance Criteria

- ✅ AddressEntity implemented with all fields and validations
- ✅ AddressRepository with required query methods
- ✅ AddressService with complete business logic
- ✅ 25+ unit tests, all passing
- ✅ Zero compilation warnings
- ✅ Code follows Users Schema patterns
- ✅ REST endpoints functional and documented
- ✅ Database schema matches specification
- ✅ Foreign key relationships enforced
- ✅ Manual null validation throughout (no @NonNull)

## Dependencies
- Feature: Users Schema (COMPLETED) - Users must exist before creating addresses
- Database: MySQL connectivity verified

## Time Estimate
- **Entity & Repository:** 20 minutes
- **Service Logic:** 40 minutes
- **Tests:** 45 minutes
- **Controller:** 30 minutes
- **Integration & Cleanup:** 25 minutes
- **Total:** ~2.5 hours

## Rollback Plan
- Delete AddressEntity, AddressRepository, AddressService files
- Delete AddressServiceTest.java, AddressEntityTest.java
- Drop addresses table from database
- Revert feature branch to previous commit

## Notes
- Follow the exact pattern established in Users Schema feature
- Use manual null checks instead of @NonNull annotations
- Ensure all exceptions throw on invalid state (fail-fast)
- Update DataSeeder.java to seed sample addresses
- Document REST API endpoints in README

## Related Issues
- #1 Users Schema ✅ COMPLETED
- #2 Addresses Schema (this)
- #3 Restaurants Schema
- #4 Orders Schema
