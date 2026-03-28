# Three Challenging Concepts — Rocket Delivery Module 11

This document outlines three key concepts encountered during the Rocket Delivery project implementation. These concepts represent areas of significant learning and challenge during development.

---

## Concept 1: Testing (Unit & Integration Testing)

### Concept Name
**Unit Testing with Mocking and Assertions**

### Purpose in the Project
Testing validates that individual components (controllers, services, repositories) function correctly in isolation and when integrated. Tests protect against regressions when code changes and provide confidence in application behavior.

### Why It Was Challenging
- Understanding the difference between unit tests (isolated) and integration tests (with real DB)
- Learning Mockito annotations (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- Writing proper test data setup (`@BeforeEach`)
- Structuring assertions (`assertEquals`, `assertTrue`, `verify()`)
- Determining what to mock vs. what to leave real

### Usage Location
**File:** [src/test/java/com/rocketFoodDelivery/rocketFood/UserServiceTest.java](src/test/java/com/rocketFoodDelivery/rocketFood/UserServiceTest.java)

- **Line 21:** `@ExtendWith(MockitoExtension.class)` — enables Mockito for test class
- **Lines 23–26:** `@Mock` and `@InjectMocks` — declares mocked repository and service under test
- **Lines 28–40:** `@BeforeEach setUp()` — initializes test data before each test
- **Line 44:** `@Test void testCreateUser_Success()` — individual test method with assertions

---

## Concept 2: Controller vs. REST API Controller

### Concept Name
**Distinction Between MVC Controllers and RESTful API Controllers**

### Purpose in the Project
The project uses two controller types to serve different client needs:
- **MVC Controller** renders server-side HTML views (Thymeleaf) for the back-office UI
- **REST API Controller** provides JSON endpoints for programmatic access (Postman, frontend frameworks)

### Why It Was Challenging
- Understanding when to return HTML views vs. JSON responses
- Learning the difference between `@Controller` and `@RestController` annotations
- Mapping request routes correctly (`@GetMapping`, `@PostMapping`, `@RequestMapping`)
- Handling validation and error responses differently for views vs. APIs
- Coordinating the same business logic across two different response types

### Usage Location

**MVC Controller (HTML/Thymeleaf Views):**  
**File:** [src/main/java/com/rocketFoodDelivery/rocketFood/controller/RestaurantController.java](src/main/java/com/rocketFoodDelivery/rocketFood/controller/RestaurantController.java)

- **Line 40:** `@Controller` — marks class as MVC controller returning views
- **Line 41:** `@RequestMapping("/backoffice/restaurants")` — base route for back-office
- **Comments (lines 20–33):** List of endpoints showing GET for form display, POST for form submission, redirects after updates

**REST API Controller (JSON Responses):**  
**File:** [src/main/java/com/rocketFoodDelivery/rocketFood/controller/RestaurantRestController.java](src/main/java/com/rocketFoodDelivery/rocketFood/controller/RestaurantRestController.java)

- **Line 42:** `@RestController` — marks class as REST API controller returning JSON
- **Line 43:** `@RequestMapping("/api/v1")` — base route for REST API
- **Lines 11–12:** imports for `ResponseEntity` and `HttpStatus` (JSON-specific response handling)

---

## Concept 3: Implementing Relational Databases with JPA/Spring Data

### Concept Name
**Relational Database Access via Spring Data JPA and Repositories**

### Purpose in the Project
Spring Data JPA provides an abstraction layer over direct SQL, allowing the project to:
- Define database tables as Java entity classes
- Perform CRUD operations through repository interfaces
- Write custom queries using method naming conventions
- Reduce repetitive database boilerplate code

### Why It Was Challenging
- Understanding JPA annotations (`@Entity`, `@Id`, `@Table`, `@Column`, relationships)
- Learning how repository interface methods map to database queries automatically
- Handling optional results and null checks (`Optional<T>`, `.isPresent()`, `.orElse()`)
- Writing custom query methods following Spring Data naming conventions
- Debugging SQL generated from ORM mappings
- Configuring database connection in `application.properties`

### Usage Location

**Repository Interface:**  
**File:** [src/main/java/com/rocketFoodDelivery/rocketFood/repository/RestaurantRepository.java](src/main/java/com/rocketFoodDelivery/rocketFood/repository/RestaurantRepository.java)

- **Line 12:** `@Repository` — marks interface as Spring Data repository
- **Line 13:** `extends JpaRepository<RestaurantEntity, Long>` — provides built-in CRUD methods (save, findAll, findById, delete)
- **Lines 19–23:** `findByName(String name)` — custom method returning Optional, auto-parsed into SQL query
- **Lines 28–33:** `findActiveRestaurants(Boolean isActive)` — custom filtered query

**Configuration:**  
**File:** [src/main/resources/application.properties](src/main/resources/application.properties)

- Database URL, username, password, and JPA/Hibernate settings control connection and schema behavior

---

## Video Script Reference
A corresponding video script explaining all three concepts in detail is available in [concepts.video.md](docs/concepts.video.md).
