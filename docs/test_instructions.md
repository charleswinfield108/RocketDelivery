# Test Instructions

Quick reference for running tests in the RocketDelivery project.

## Prerequisites

- Java 17+ installed
- Maven 3.8+ installed
- MySQL 8.0+ running with `rocketfood` database
- `src/main/resources/application.properties` configured with database credentials

## Run All Tests

```bash
mvn test
```

## Run Specific Test Class

```bash
mvn test -Dtest=UserServiceTest
```

## Run Specific Test Method

```bash
mvn test -Dtest=UserServiceTest#testCreateUser
```

## Run Integration Tests

```bash
mvn verify
```

## Run Integration Test Class

```bash
mvn test -Dtest=UserRepositoryTest
```

## Run Tests with Coverage Report

```bash
mvn test jacoco:report
```

View report at: `target/site/jacoco/index.html`

## Clean and Compile Before Testing

```bash
mvn clean compile
```

## Current Test Files

Located in `src/test/java/com/rocketFoodDelivery/rocketFood/`:

- `RocketFoodApplicationTests.java` - Application context tests
- `UserEntityTest.java` - Unit tests for User entity
- `UserServiceTest.java` - Unit tests for User service

## Schema v2.0 Testing Considerations

### New FK Relationships to Test

With schema v2.0 alignment, update test fixtures to include:

**OrderEntity Tests:**
- Order must have `orderStatus` FK reference (not String `status`)
- Mock `OrderStatusRepository` injection in tests
- Test backward compatibility: `order.getStatus()` returns status code string
- Example fixture:
  ```java
  OrderStatusEntity pendingStatus = new OrderStatusEntity();
  pendingStatus.setStatusCode("PENDING");
  pendingStatus.setName("Pending");
  order.setOrderStatus(pendingStatus);
  ```

**EmployeeEntity Tests:**
- Employees now require `user_id` (OneToOne FK to UserEntity)
- Employees now require `address_id` (ManyToOne FK to AddressEntity)
- Restaurant assignment is now optional
- Update fixtures to provide UserEntity and AddressEntity

**RestaurantEntity Tests:**
- Restaurants now require `address_id` FK to AddressEntity (unique constraint)
- Add `priceRange` field (1-3 integer)
- FK column renamed from `owner_id` to `user_id`
- Update fixtures with address and priceRange data

**CustomerEntity Tests:**
- Customers now require `address_id` FK to AddressEntity
- Update fixtures to provide address reference

### Testing Backward Compatibility

Test that deprecated methods still work:
```java
@Test
void orderStatusBackwardCompatibility() {
    // Old code still works via compatibility methods
    order.setStatus("PENDING");  // Deprecated but functional
    assertEquals("PENDING", order.getStatus());
    
    // New code uses FK
    order.setOrderStatus(orderStatusEntity);
    assertEquals("PENDING", order.getOrderStatus().getStatusCode());
}
```

### Testing Service Layer Changes

**OrderService Tests:**
- Verify `OrderStatusRepository` is injected
- Test `setOrderStatusByCode(Long id, String code)` method
- Mock orderStatusRepository responses for status lookups
- Example:
  ```java
  @Mock
  private OrderStatusRepository orderStatusRepository;
  
  @Test
  void setOrderStatusByCode() {
      OrderStatusEntity confirmed = new OrderStatusEntity();
      confirmed.setStatusCode("CONFIRMED");
      when(orderStatusRepository.findByStatusCodeAndIsActive("CONFIRMED", true))
          .thenReturn(Optional.of(confirmed));
      
      orderService.setOrderStatusByCode(orderId, "CONFIRMED");
      // Verify order was updated with new status FK
  }
  ```

## Test Output

- Test results appear in terminal output
- Failed tests show assertion errors and stack traces
- Coverage reports are generated in `target/site/jacoco/`
- Deprecation warnings may appear for backward compatibility methods (expected)

## Troubleshooting

- **Database connection error**: Verify MySQL is running and credentials in `application.properties` are correct
- **Tests fail but compile succeeds**: Check database exists and tables are created with v2.0 schema
- **Port already in use**: Change server port in `application.properties` with `server.port=8081`
- **FK constraint violations in tests**: Ensure test fixtures create required FK entities (UserEntity, AddressEntity, OrderStatusEntity) before referencing them
- **OrderStatusRepository injection errors**: Verify `OrderStatusRepository` is available and initialized in test context
- **Deprecation warnings**: These are expected for backward compatibility methods; they can be suppressed with `@SuppressWarnings("deprecation")`
