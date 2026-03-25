# Back Office Backend Feature Specification

## Feature Overview

Implement the Spring Boot backend infrastructure and Thymeleaf-based web interface for the Back Office restaurant management system. Users will access a dashboard at `http://localhost:8080/backoffice/restaurants` to manage restaurants with full CRUD operations.

---

## Feature Goals

✅ Backend Infrastructure: RestaurantController, RestaurantService, RestaurantRepository
✅ Thymeleaf Web Interface: HTML templates for list, create, edit, delete operations
✅ Form Processing: Spring MVC form binding with validation
✅ Data Persistence: All operations persist to MySQL database
✅ Configuration & Security: application.properties setup, credential management, .gitignore

---

## Scope

### In Scope ✅

**Backend Components:**
- RestaurantController (@Controller) with Thymeleaf view returns
- RestaurantService with CRUD business logic
- RestaurantRepository (JPA interface)
- UserService and UserRepository (supporting infrastructure)
- AddressService and AddressRepository (supporting infrastructure)
- Exception handling and form validation
- Spring MVC form binding and model attributes

**Web Interface:**
- Restaurant list view (table display all restaurants)
- Create restaurant form (accessible from list)
- Edit restaurant form (pre-filled with existing data)
- Delete confirmation and processing
- Flash messages for success/error feedback
- Form validation error display

**Configuration & Security:**
- application.properties with MySQL connection details
- .gitignore to exclude application.properties from Git
- No hardcoded credentials in code
- CSRF protection on forms

### Out of Scope ❌

- User authentication/login
- Authorization/permissions
- Real-time updates or WebSockets
- Advanced filtering, sorting, pagination
- API documentation (Swagger)
- Customer order management
- Product/menu management
- Employee management
- Frontend frameworks (React, Vue, Angular)
- Mobile interface

---

## Endpoints & User Flows

### Thymeleaf Form-Based Endpoints

**List Restaurants**
- `GET /backoffice/restaurants`
- Returns: Restaurant list HTML template
- Displays: All restaurants in table format (name, email, phone, city, actions)

**Create Restaurant Form**
- `GET /backoffice/restaurants/new`
- Returns: Create form HTML template
- Fields: name, email, phoneNumber, street, city, postalCode, country, openingTime, closingTime, description

**Process Create**
- `POST /backoffice/restaurants`
- Accepts: Form data with restaurant details
- Validates: Server-side validation in service layer
- Result: Saves to database and redirects to list with success message

**Edit Restaurant Form**
- `GET /backoffice/restaurants/{id}/edit`
- Returns: Edit form HTML template with pre-filled data
- Fields: Same as create form

**Process Update**
- `POST /backoffice/restaurants/{id}`
- Accepts: Updated form data
- Validates: Server-side validation
- Result: Updates database and redirects to list with success message

**Delete Restaurant**
- `GET /backoffice/restaurants/{id}/delete` (confirmation)
- `POST /backoffice/restaurants/{id}/delete` (process)
- Result: Removes from database and redirects to list with success message

### Optional REST API Endpoints (Postman Testing)

- `GET /api/restaurants` — List all as JSON
- `GET /api/restaurants/{id}` — Single restaurant as JSON
- `POST /api/restaurants` — Create via JSON
- `PUT /api/restaurants/{id}` — Update via JSON
- `DELETE /api/restaurants/{id}` — Delete restaurant

---

## Form Validation

### Fields & Validation Rules

| Field | Type | Validation | Required |
|-------|------|-----------|----------|
| name | String | 3-255 chars, unique | Yes |
| email | String | Valid email format, unique | Yes |
| phoneNumber | String | 10-20 chars | Yes |
| street | String | 5-255 chars | Yes |
| city | String | 2-100 chars | Yes |
| postalCode | String | 3-20 chars | Yes |
| country | String | 2-100 chars | Yes |
| openingTime | LocalTime | HH:mm format (e.g., 09:00) | Yes |
| closingTime | LocalTime | HH:mm format (e.g., 22:00) | Yes |
| description | String | Max 1000 chars | No |

### Jakarta Bean Validation Annotations

```
name: @NotNull @Size(min=3, max=255) @Column(unique)
email: @NotNull @Email @Size(min=5, max=255) @Column(unique)
phoneNumber: @NotNull @Size(min=10, max=20)
street: @NotNull @Size(min=5, max=255)
city: @NotNull @Size(min=2, max=100)
postalCode: @NotNull @Size(min=3, max=20)
country: @NotNull @Size(min=2, max=100)
openingTime: @NotNull @Temporal(TemporalType.TIME)
closingTime: @NotNull @Temporal(TemporalType.TIME)
description: @Size(max=1000)
```

---

## Thymeleaf Templates

### Template Files Structure

```
src/main/resources/templates/
├── backoffice/
│   └── restaurants/
│       ├── index.html          (List all restaurants)
│       ├── create.html         (Create form)
│       ├── edit.html           (Edit form)
│       └── form.html           (Shared form fragment - optional)
└── error.html                   (Error page - optional)
```

### Template Features

**index.html (List View)**
- Display all restaurants in HTML table
- Columns: Name, Email, Phone, City, CreatedAt, UpdatedAt, Actions
- Action buttons: Edit, Delete, View Details
- Link to create new restaurant form
- Display flash messages (success/error notifications)

**create.html (Create Form)**
- Form for new restaurant creation
- All 9 fields with proper labels
- Submit button ("Create Restaurant")
- Validation error display below each field
- Cancel/Back link to list

**edit.html (Edit Form)**
- Form pre-filled with existing restaurant data
- All 9 fields editable
- Submit button ("Update Restaurant")
- Validation error display
- Cancel/Back link to list
- Delete button or link (optional)

**form.html (Shared Form Fragment - Optional)**
- Reusable form fields snippet
- Can be included in both create.html and edit.html
- Reduces template duplication

**error.html (Error Page - Optional)**
- Display error messages (404, 500, etc.)
- User-friendly error descriptions
- Link back to dashboard

---

## Technology Stack

### Dependencies

- Spring Boot 3.1.2
- Spring Data JPA
- Spring Web (MVC)
- Spring Boot Starter Thymeleaf
- Jakarta Servlet API
- Jakarta Validation API
- MySQL 8.0 Connector/J
- Lombok 1.18.30

### Annotations & Frameworks

- **@Controller** (not @RestController) for web controllers
- **@Service** for business logic
- **@Repository** for data access
- **Spring MVC** with model/view binding
- **Thymeleaf** for server-side HTML rendering
- **Jakarta Validation** (@NotNull, @Size, @Email, etc.)
- **Spring Security** for CSRF protection (implicit in Thymeleaf forms)

---

## Implementation Phases

### Phase 1: Configuration & Repository Setup
- Add Thymeleaf dependency to pom.xml
- Configure application.properties with MySQL connection
- Create application.properties.example template
- Add application.properties to .gitignore
- Implement RestaurantRepository, UserRepository, AddressRepository

### Phase 2: Service Layer
- Implement RestaurantService with CRUD methods
- Add validation logic to service layer
- Implement UserService (basic)
- Implement AddressService (basic)
- Add exception handling

### Phase 3: Controller Layer
- Create RestaurantController with @Controller annotation
- Implement all 6 endpoint methods (list, create form, create, edit form, update, delete)
- Add Model attributes for view rendering
- Add RedirectAttributes for flash messages
- Add BindingResult for form validation errors

### Phase 4: Thymeleaf Views
- Create templates directory structure
- Implement index.html (list) template
- Implement create.html template
- Implement edit.html template
- Add CSS styling (basic layout)
- Add form error display
- Add flash message display

### Phase 5: Testing & Validation
- Test application startup: `./mvnw spring-boot:run`
- Test list page at http://localhost:8080/backoffice/restaurants
- Test create form and submission
- Test edit form with pre-filled data
- Test delete confirmation and execution
- Verify data persistence in MySQL database
- Test form validation errors
- Verify no credentials in Git commits

---

## Acceptance Criteria

### Configuration ✓

- [ ] application.properties exists with database connection details
- [ ] application.properties added to .gitignore
- [ ] application.properties NOT committed to Git repository
- [ ] application.properties.example exists with placeholder values
- [ ] Spring Boot connects to MySQL on startup without errors

### Repository Layer ✓

- [ ] RestaurantRepository extends JpaRepository<RestaurantEntity, Long>
- [ ] UserRepository extends JpaRepository<UserEntity, Long>
- [ ] AddressRepository extends JpaRepository<AddressEntity, Long>
- [ ] CRUD methods available (findAll, findById, save, delete)
- [ ] All repositories compile without errors

### Service Layer ✓

- [ ] RestaurantService exists with @Service annotation
- [ ] RestaurantService has: findAll(), findById(Long), save(RestaurantEntity), deleteById(Long)
- [ ] RestaurantService performs form validation before service operations
- [ ] RestaurantService handles validation exceptions gracefully
- [ ] UserService exists (basic implementation)
- [ ] AddressService exists (basic implementation)
- [ ] All services have comprehensive JavaDoc

### Controller Layer ✓

- [ ] RestaurantController exists with @Controller annotation
- [ ] Controller mapped to `/backoffice/restaurants` base path
- [ ] GET /backoffice/restaurants returns list view with all restaurants
- [ ] GET /backoffice/restaurants/new returns create form view
- [ ] POST /backoffice/restaurants processes form and redirects to list
- [ ] GET /backoffice/restaurants/{id}/edit returns edit form with pre-filled data
- [ ] POST /backoffice/restaurants/{id} updates restaurant and redirects
- [ ] GET/POST /backoffice/restaurants/{id}/delete deletes and redirects
- [ ] All endpoints return appropriate Thymeleaf template names

### Thymeleaf Templates ✓

- [ ] Restaurant list displays all restaurants in table format
- [ ] List shows: name, email, phone, city, createdAt, updatedAt, actions
- [ ] List page has "Create New Restaurant" button/link
- [ ] Create form displays all 9 form fields
- [ ] Create form has submit button
- [ ] Create form shows validation errors for invalid input
- [ ] Edit form pre-fills with existing restaurant data
- [ ] Edit form allows modification of all fields
- [ ] Edit form has submit button
- [ ] Delete action shows confirmation before removing
- [ ] Success message displays after create/update/delete
- [ ] Error messages display for validation failures
- [ ] All templates are valid HTML5

### CRUD Operations ✓

- [ ] Create: New restaurant saved to database and appears in list
- [ ] Read: All restaurants displayed on list page without pagination errors
- [ ] Read: Individual restaurant data accessible in edit form
- [ ] Update: Modifications saved to database and reflected in list
- [ ] Update: List refreshes without stale data
- [ ] Delete: Restaurant removed from database
- [ ] Delete: Deleted restaurant no longer appears in list
- [ ] No data loss or integrity violations

### Testing ✓

- [ ] Application starts: `./mvnw spring-boot:run` without errors
- [ ] List page loads: http://localhost:8080/backoffice/restaurants
- [ ] Create form loads and submits successfully
- [ ] New restaurant appears in database (verify via MySQL client)
- [ ] Edit form loads with pre-filled data
- [ ] Updated data appears in list and database
- [ ] Delete operation removes entry from database and list
- [ ] Validation errors display for invalid email/phone/name
- [ ] Flash messages display after successful operations
- [ ] No console errors or exceptions during operations
- [ ] CSRF protection works (form has CSRF token)

---

## Database Considerations

### Existing Entities

The feature reuses existing JPA entities:
- **RestaurantEntity** (already exists in src/main/java/models/)
- **UserEntity** (already exists)
- **AddressEntity** (already exists)

### Additional Fields Needed

Restaurant entity should support:
- `openingTime` (LocalTime) - restaurant opening time
- `closingTime` (LocalTime) - restaurant closing time

If these fields don't exist, add them to RestaurantEntity with @Temporal(TemporalType.TIME) annotations.

### Database Indexes

Ensure indexes exist for performance:
- PK on id
- Unique indexes on name, email
- Indexes on city, isActive for filtering

---

## Security Considerations

### CSRF Protection
- Spring Security automatically provides CSRF tokens in Thymeleaf forms
- Include `<input type="hidden" name="_csrf" th:value="${_csrf.token}"/>`
- Or rely on Thymeleaf's automatic CSRF token inclusion

### Credential Management
- **NEVER commit application.properties to Git**
- All developers must create local application.properties
- Use environment variables in production
- Provide application.properties.example as template

### Input Validation
- All form input validated server-side (don't trust client)
- Use Jakarta Bean Validation annotations
- Validate in service layer before database operations
- Display validation errors on form

### SQL Injection Prevention
- Use Spring Data JPA (parametrized queries)
- No string concatenation in SQL
- Parameter binding via method arguments

---

## Notes for Implementation

### Controller Design Patterns

- Use `@GetMapping` and `@PostMapping` for HTTP methods
- Return String (view name) or ModelAndView
- Use Model.addAttribute() to pass data to template
- Use RedirectAttributes for flash messages across redirects
- Implement POST-Redirect-GET pattern (no form resubmission issues)

### Service Layer Best Practices

- Keep business logic separate from controller
- Validate all input in service layer
- Use @Transactional for multi-step operations
- Handle exceptions with custom exceptions or @ExceptionHandler
- Return DTOs or entities (keep persistence layer clean)

### Thymeleaf Integration

- Use `th:field` for two-way form binding from model
- Use `th:errors` to display field-level validation errors
- Use `th:if` for conditional content (show/hide buttons, etc.)
- Use `th:each` for iterating over restaurant list
- Use `th:object` for form binding context
- Always include CSRF token in POST forms

### Form Binding Example

```html
<form th:action="@{/backoffice/restaurants}" th:object="${restaurant}" method="post">
    <input type="text" th:field="*{name}" placeholder="Restaurant name">
    <span th:errors="*{name}" class="error"></span>
    
    <input type="email" th:field="*{email}" placeholder="Email">
    <span th:errors="*{email}" class="error"></span>
    
    <button type="submit">Create</button>
</form>
```

### Error Handling

- Create GlobalExceptionHandler with @ControllerAdvice
- Handle validation errors gracefully (display on form)
- Create custom error.html template
- Log exceptions for debugging
- Return user-friendly error messages

### Testing Approach

- Manual browser testing (primary method for UI)
- Test form submission and validation
- Verify data persistence in MySQL
- Use browser developer tools for form inspection
- Optional: Postman for REST API testing (if built)
- Check console for JavaScript/Thymeleaf errors

---

## Deliverables

### Code Files to Create

1. **RestaurantController.java** — POST/GET handlers for CRUD
2. **RestaurantService.java** — Business logic with validation
3. **UserService.java** — Basic user management
4. **AddressService.java** — Basic address management
5. **RestaurantRepository.java** — JPA interface with custom queries
6. **UserRepository.java** — JPA interface
7. **AddressRepository.java** — JPA interface

### Template Files to Create

1. **templates/backoffice/restaurants/index.html** — Restaurant list
2. **templates/backoffice/restaurants/create.html** — Create form
3. **templates/backoffice/restaurants/edit.html** — Edit form
4. **templates/error.html** — Error page (optional)

### Configuration Files

1. **application.properties** — Database connection (in .gitignore)
2. **application.properties.example** — Template for developers
3. **pom.xml** — Add spring-boot-starter-thymeleaf dependency

### Documentation

1. **README.md section** — Setup instructions for developers
2. **Setup guide** — How to create local application.properties

---

## Success Metrics

✅ All 6 endpoint routes working
✅ Form submission saves to database
✅ Form validation errors display on page
✅ CRUD operations complete without errors
✅ Data persists across page refreshes
✅ Application compiles with zero warnings
✅ No credentials committed to Git
✅ Flash messages display for user feedback

---

## Timeline & Phases

**Estimated: 2-3 hours**

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| Config & Repos | 30 min | app props, repos, models |
| Services | 45 min | RestaurantService, UserService, validation |
| Controller | 45 min | RestaurantController with 6 endpoints |
| Templates | 60 min | All 4 HTML templates with styling |
| Testing | 30 min | Manual testing, database verification |

---

## References & Resources

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring MVC @Controller](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Thymeleaf Official](https://www.thymeleaf.org/)
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/3.0/)
- [Spring Security CSRF Protection](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-csrf)
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)

