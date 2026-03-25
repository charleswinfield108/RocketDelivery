# 🤖 AI_FEATURE_BACK_OFFICE_BACKEND

This document describes the **Back Office Backend** feature — the Spring Boot backend, Thymeleaf controllers, services, and repositories that power the restaurant management Back Office interface for the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Back Office Backend
- **Related Area:** Fullstack (Backend Controllers, Services, Repositories + Frontend Thymeleaf Views)

---

## Feature Goal

Implement the backend infrastructure and Thymeleaf-based web interface for the Back Office restaurant management POC. Staff should be able to:
- Access a web page at `http://localhost:8080/backoffice/restaurants`
- View all restaurants in a list
- Create new restaurants via a form
- Update existing restaurants
- Delete restaurants
- Have all changes persist to MySQL database

---

## Feature Scope

### In Scope (Included)

**Backend Components:**
- RestaurantController with @Controller annotation (returns Thymeleaf views)
- RestaurantService (business logic layer)
- RestaurantRepository (JPA data access)
- UserService, UserRepository (for future authentication)
- AddressService, AddressRepository (supporting infrastructure)
- Exception handling and validation
- Form processing and CRUD operations
- Thymeleaf template integration

**Security & Configuration:**
- application.properties with database credentials
- .gitignore configuration to exclude sensitive files
- Environment-specific property management
- No hardcoded credentials

**Frontend (Thymeleaf Views):**
- Restaurant list view (index.html or restaurants.html)
- Create restaurant form
- Edit restaurant form
- Delete confirmation
- Success/error message display
- Basic HTML/CSS styling

**User Flow:**
- Staff opens Back Office page
- Sees list of all restaurants
- Can create, read, update, delete restaurants
- Changes persist to database
- List refreshes after operations

### Out of Scope (Excluded)

- User authentication or login
- Authorization/permissions
- Customer order management
- Product/menu management
- Employee management
- Real-time updates or WebSockets
- Advanced filtering, sorting, or pagination
- Bulk operations
- API documentation (Swagger/OpenAPI)
- Frontend frameworks (React, Vue, Angular)
- Mobile interface
- Internationalization

---

## Sub-Requirements (Feature Breakdown)

- **Configuration & Security** — application.properties setup, .gitignore configuration, no credentials in Git
- **Repository Layer** — RestaurantRepository, UserRepository, AddressRepository (JPA interfaces)
- **Service Layer** — RestaurantService, UserService, AddressService with CRUD business logic
- **Controller Layer** — RestaurantController with @Controller and Thymeleaf view methods
- **Thymeleaf Views** — HTML templates for list, create form, edit form, display pages
- **Form Handling** — Model binding, validation, error messages
- **Data Persistence** — CRUD operations save/retrieve from MySQL
- **Error Handling** — Exception handling, validation error messages
- **Testing** — Manual testing via browser and Postman

---

## User Flow / Logic (High Level)

1. **User Accesses Back Office:** Staff opens browser and navigates to `http://localhost:8080/backoffice/restaurants`
2. **Controller Routes Request:** RestaurantController intercepts GET request to `/backoffice/restaurants`
3. **Service Fetches Data:** RestaurantService queries database for all restaurants
4. **Views Rendered:** Thymeleaf renders HTML list view with all restaurants
5. **User Actions:**
   - **Create:** User fills form, submits → Controller processes POST → Service saves → Redirect to list
   - **Read:** List displays all restaurants with details
   - **Update:** User clicks edit, form pre-fills → User modifies → Submits → Service updates → Redirect to list
   - **Delete:** User clicks delete → Confirmation → Service deletes → Redirect to list
6. **Database Persistence:** All operations reflect immediately in database and on reload

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Endpoints (Thymeleaf Form-Based)

- `GET /backoffice/restaurants` — Display restaurant list (Thymeleaf view)
- `GET /backoffice/restaurants/new` — Display create restaurant form (Thymeleaf view)
- `POST /backoffice/restaurants` — Process restaurant creation form (redirect to list)
- `GET /backoffice/restaurants/{id}/edit` — Display edit restaurant form (Thymeleaf view)
- `POST /backoffice/restaurants/{id}` or `PUT /backoffice/restaurants/{id}` — Process restaurant update (redirect to list)
- `GET /backoffice/restaurants/{id}/delete` or `POST /backoffice/restaurants/{id}/delete` — Delete restaurant (redirect to list)

### Optional REST API Endpoints (for Postman testing)

- `GET /api/restaurants` — JSON list of all restaurants
- `GET /api/restaurants/{id}` — Get single restaurant as JSON
- `POST /api/restaurants` — Create restaurant (JSON request body)
- `PUT /api/restaurants/{id}` — Update restaurant (JSON request body)
- `DELETE /api/restaurants/{id}` — Delete restaurant

### Thymeleaf Views/Pages

- `templates/backoffice/restaurants/index.html` — List all restaurants
- `templates/backoffice/restaurants/create.html` — Create restaurant form
- `templates/backoffice/restaurants/edit.html` — Edit restaurant form
- `templates/backoffice/restaurants/form.html` — Shared form fragment (optional)
- `templates/error.html` — Error page (optional)

---

## Data Used or Modified

### Request/Response Data

**Restaurant Creation/Update Form Fields:**
- name (String, required, 3-255 chars)
- email (String, required, valid email format)
- phoneNumber (String, required, 10-20 chars)
- street (String, required, 5-255 chars)
- city (String, required, 2-100 chars)
- postalCode (String, required, 3-20 chars)
- country (String, required, 2-100 chars)
- openingTime (LocalTime, required, HH:mm format)
- closingTime (LocalTime, required, HH:mm format)
- description (String, optional, max 1000 chars)

**Display Data:**
- Restaurant list with all fields
- Timestamps (createdAt, updatedAt) shown in readable format
- Action buttons (Edit, Delete, View Details)

### Validation & Error Handling

- **Form Validation:** Spring @Valid + Jakarta Bean Validation annotations
- **Server-Side Validation:** Service layer checks business rules
- **Error Messages:** Displayed on page (name already exists, invalid email, etc.)
- **Database Constraints:** PRIMARY KEY, UNIQUE constraints enforced
- **Redirect on Success:** After CRUD operation, redirect to list

---

## Tech Constraints (Feature-Level)

### Backend Components

- Use **Spring MVC** @Controller (not @RestController)
- Return **Thymeleaf templates** (not JSON by default)
- Use **Spring Data JPA** for repositories
- Apply **Spring services** for business logic
- Use **Spring Validation** (@Valid, custom validators)
- Use **Spring MVC form binding** with Thymeleaf th:field
- No frontend build process (plain HTML/CSS/JavaScript)

### Configuration & Security

- **application.properties:** Must contain database credentials (username, password, URL)
- **application.properties:** Must be added to .gitignore (never commit credentials)
- **Environment:** Use environment variables or external config in production
- **No Hardcoding:** Avoid hardcoded database connections in code

### Thymeleaf Integration

- Use `spring-boot-starter-thymeleaf` dependency
- Use th: namespace for dynamic content
- Use th:field for form binding
- Use th:errors for validation error display
- Use th:if, th:each for conditional/loop rendering
- Use csrf tokens for form security

### Form Handling

- POST/PUT requests for modifications
- GET for retrieving views
- Consider POST-Redirect-GET pattern for CRUD operations
- Flash messages (Spring RedirectAttributes) for success/error feedback

---

## Acceptance Criteria

### Configuration & Security

- [ ] application.properties exists with database connection details
- [ ] application.properties added to .gitignore
- [ ] application.properties NOT committed to Git
- [ ] Spring Boot connects successfully to MySQL database on startup
- [ ] No compilation errors or security warnings

### Repository Layer

- [ ] RestaurantRepository extends JpaRepository<Restaurant, Long/UUID>
- [ ] UserRepository extends JpaRepository<UserEntity, Long/UUID>
- [ ] AddressRepository extends JpaRepository<Address, Long/UUID>
- [ ] All repositories compile without errors
- [ ] CRUD methods available (findAll, findById, save, delete)

### Service Layer

- [ ] RestaurantService exists with @Service annotation
- [ ] RestaurantService has methods: getAll(), findById(), save(), delete(), update()
- [ ] RestaurantService performs validation before save/update
- [ ] RestaurantService handles exceptions gracefully
- [ ] UserService exists (basic implementation)
- [ ] AddressService exists (basic implementation)
- [ ] All services compile without errors

### Controller Layer

- [ ] RestaurantController exists with @Controller annotation
- [ ] RestaurantController mapped to `/backoffice/restaurants`
- [ ] GET /backoffice/restaurants returns list view
- [ ] GET /backoffice/restaurants/new returns create form view
- [ ] POST /backoffice/restaurants processes form (saves data, redirects)
- [ ] GET /backoffice/restaurants/{id}/edit returns edit form with pre-filled data
- [ ] POST/PUT /backoffice/restaurants/{id} updates restaurant (redirects)
- [ ] GET/POST /backoffice/restaurants/{id}/delete deletes restaurant (redirects)
- [ ] All endpoints return appropriate Thymeleaf views

### Thymeleaf Views

- [ ] Restaurant list template displays all restaurants in table format
- [ ] List shows: name, email, phone, city, actions (edit/delete)
- [ ] Create form accessible from list page (link or button)
- [ ] Create form displays all restaurant fields
- [ ] Create form has submit button
- [ ] Create form shows validation errors if any
- [ ] Edit form pre-fills existing restaurant data
- [ ] Edit form allows modification of all fields
- [ ] Edit form has submit button
- [ ] Delete has confirmation (button or modal)
- [ ] Success message displayed after create/update/delete
- [ ] Error messages displayed if validation fails
- [ ] Page refreshes list after operations

### CRUD Operations

- [ ] Create: New restaurant saved to database and appears in list
- [ ] Read: All restaurants displayed on list page
- [ ] Read: Individual restaurant details accessible
- [ ] Update: Restaurant modification saved to database
- [ ] Update: List refreshes after update
- [ ] Delete: Restaurant removed from database
- [ ] Delete: List refreshes after delete
- [ ] No stale data in list after operations

### Testing

- [ ] Application starts without errors: `./mvnw spring-boot:run`
- [ ] Access page in browser: http://localhost:8080/backoffice/restaurants
- [ ] List page loads successfully
- [ ] Create form displays and submits successfully
- [ ] New restaurant appears in database (verified in DBeaver)
- [ ] New restaurant appears in list (without manual refresh)
- [ ] Edit form displays and submits successfully
- [ ] Updated data reflects in database
- [ ] Updated data reflects in list
- [ ] Delete operation completes successfully
- [ ] Deleted restaurant removed from database
- [ ] Deleted restaurant removed from list
- [ ] Validation errors displayed for invalid input
- [ ] No console errors or exceptions during operations

---

## Notes for the AI

### Controller Design

- Use `@GetMapping` and `@PostMapping` for clarity
- Return ModelAndView or String (view name) with Model
- Add Model.addAttribute() for template data
- Use RedirectAttributes for flash messages

### Service Layer

- Keep business logic separate from controller
- Perform all validation in service
- Use @Transactional for multi-step operations
- Handle exceptions (try-catch or @ExceptionHandler)

### Thymeleaf Integration

- Use `th:field` for two-way form binding
- Use `th:errors` to display field-level errors
- Use `th:if` for conditional rendering
- Use `th:each` for list iteration
- Add `csrf:token` for form security

### Form Binding

- Use @ModelAttribute on controller method parameter
- Use @Valid on form object parameter
- Use BindingResult to access validation errors
- Use spring:bind or th:errors for error display

### Error Handling

- Custom GlobalExceptionHandler with @ControllerAdvice
- Custom error page (error.html in templates)
- Log exceptions for debugging
- Return user-friendly error messages

### Git & Security

- **NEVER commit application.properties with credentials**
- Use application.properties.example as template
- Provide setup instructions (copy example, fill in credentials)
- Consider using environment variables for production

### Testing Approach

- Manual browser testing for UI flow
- Form submission and validation
- Database verification in DBeaver after each operation
- Postman for API endpoint testing (if REST API developed)
- Check browser console for JavaScript errors

---

## Implementation Checklist

### Phase 1: Setup & Configuration

- [ ] Add spring-boot-starter-thymeleaf dependency to pom.xml
- [ ] Create application.properties with database config
- [ ] Add application.properties to .gitignore
- [ ] Create application.properties.example template

### Phase 2: Repository & Service Layer

- [ ] Implement RestaurantRepository
- [ ] Implement RestaurantService with CRUD methods
- [ ] Implement UserService (basic)
- [ ] Implement AddressService (basic)
- [ ] Add validation to service layer

### Phase 3: Controller Layer

- [ ] Implement RestaurantController with @Controller
- [ ] Add GET /backoffice/restaurants (list)
- [ ] Add GET /backoffice/restaurants/new (create form)
- [ ] Add POST /backoffice/restaurants (process create)
- [ ] Add GET /backoffice/restaurants/{id}/edit (edit form)
- [ ] Add POST /backoffice/restaurants/{id} (process update)
- [ ] Add POST /backoffice/restaurants/{id}/delete (process delete)

### Phase 4: Thymeleaf Views

- [ ] Create templates/backoffice/restaurants/index.html (list)
- [ ] Create templates/backoffice/restaurants/create.html (create form)
- [ ] Create templates/backoffice/restaurants/edit.html (edit form)
- [ ] Add CSS styling (basic layout)
- [ ] Add success/error message displays
- [ ] Add form validation error displays

### Phase 5: Testing & Deployment

- [ ] Test all CRUD operations via browser
- [ ] Verify data persists in database
- [ ] Test form validation
- [ ] Test error handling
- [ ] Verify no credentials in Git

---

## POC-Specific Notes

**Thymeleaf vs REST:**
- This feature uses Thymeleaf templates (server-side rendering) not REST API
- Traditional Spring MVC with form submission, not JSON APIs
- Simpler for POC; easier for beginners to understand
- Alternative: Can implement REST API endpoints separately for testing with Postman

**Database Credentials:**
- application.properties must NEVER be committed to Git
- staff must manually create local application.properties with their credentials
- Use application.properties.example as configuration template

**Simple Form Processing:**
- Use traditional form submission (POST) not AJAX
- Redirect-on-success pattern for simple user experience
- No real-time validation; validate on server-side on form submit
- Flash messages for feedback

**Back Office Scope:**
- Current POC focuses on Restaurants only
- Future: Add Customers, Orders, Employees management
- Each entity will follow similar controller-service-repository pattern
- Reusable template patterns for new entities

**No Heavy Frontend:**
- Keep CSS minimal; focus on functionality
- No JavaScript framework (React, Vue)
- Plain HTML with Thymeleaf for dynamic content
- Bootstrap or Tailwind optional for basic styling
