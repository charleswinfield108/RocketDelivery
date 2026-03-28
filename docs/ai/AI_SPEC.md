# 🤖 AI_SPEC — Project Specification (Main)

This document is the **main AI specification** for RocketDelivery. It explains the overall context, scope, structure, and rules of the project. AI tools rely on this file to understand what should be built and how the project is organized.

---

## 📢 Schema v2.0 Update (March 27, 2026)

**IMPORTANT:** This project has been upgraded to **Schema v2.0** with significant database changes:

### Core Relationship Changes:
- **OrderEntity.status:** Changed from String field to `orderStatus` foreign key referencing OrderStatusEntity
- **EmployeeEntity:** Added `user_id` (OneToOne FK) and `address_id` (ManyToOne FK) relationships
- **RestaurantEntity:** Added `address_id` (ManyToOne FK, unique) and `priceRange` field; renamed `owner_id` → `user_id`
- **CustomerEntity:** Added `address_id` (ManyToOne FK) requirement
- **ProductEntity:** Renamed `price` → `cost` field
- **OrderStatusEntity:** Now populated reference table with status values (PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)

### Database Integrity Enhancements:
- All relationships now properly use Foreign Key constraints
- Status values controlled through OrderStatusEntity lookup table
- Address normalization with FK relationships vs. denormalized fields

**All implementations below reflect Schema v2.0.** See [SCHEMA_COMPLIANCE.md](../SCHEMA_COMPLIANCE.md) for migration details and SQL scripts.

---

## Project Identity

- **Project Name:** Rocket Food Delivery
- **Short Description:** A food delivery service backend and Back Office application for managing restaurants, customers, addresses, and orders. Built with Spring Boot and MySQL.
- **Project Type:** Java Spring Boot Backend API + Web Back Office Application

---

## Goal and Scope

### Goal

Build a proof of concept (POC) Back Office application for Rocket Food Delivery to manage their business data. The application must support CRUD operations on key entities (Restaurants, Customers, Addresses, Orders) through a relational MySQL database and a web-based user interface.

### In Scope (Build Now)

- **Phase 1:** Database schema implementation based on the provided ERD
  - Restaurants table and entity
  - Customers table and entity
  - Addresses table and entity
  - Orders table and entity (with relationships)
  - Primary Keys and Foreign Keys enforced
  
- **Phase 2:** Back Office Back-end API
  - REST API endpoints for CRUD operations on Restaurants (GET, POST, PUT, DELETE)
  - Spring Boot controllers and services
  - Data validation
  
- **Phase 3:** Back Office Web Interface
  - Web page accessible at `localhost:xxxx/backoffice/restaurants`
  - List all restaurants from database
  - Create new restaurant
  - Update existing restaurant
  - Delete restaurant
  - Real-time list refresh after operations
  
- **Testing:**
  - CRUD operations tested with Postman
  - Database changes verified with DBeaver

### Out of Scope (Do NOT Build)

- User authentication or authorization
- Restaurant search/filtering/sorting UI
- Order management interface
- Customer management interface
- Payment processing
- Real-time notifications
- Mobile app
- Advanced analytics or reporting
- Frontend frameworks beyond basic HTML/CSS/JavaScript

---

## Users and Use Cases

- **Back Office Staff:** Can log in and manage restaurant data through the Back Office interface
  - View all restaurants
  - Add new restaurants
  - Update restaurant information
  - Remove restaurants
  
- **System Administrator:** Manages the overall database and confirms schema implementation
  - Connects to MySQL database with DBeaver
  - Visualizes ERD and verifies relationships
  - Confirms data persistence

---

## Feature Index (Links Only)

- Restaurant CRUD operations (`ai_feature_restaurant_crud.md` - if created)
- Database schema and relationships (`ai_feature_database_schema.md` - if created)

---

## Pages / Screens / Routes (Project Map)

### Web Pages

- `/backoffice/restaurants` — Back Office restaurant management page
  - Displays list of all restaurants
  - Contains form for creating new restaurants
  - Enables in-line editing and deletion

### API Endpoints (REST)

**Restaurants:**
- `GET /api/restaurants` — Retrieve all restaurants
- `GET /api/restaurants/{id}` — Retrieve specific restaurant
- `POST /api/restaurants` — Create new restaurant
- `PUT /api/restaurants/{id}` — Update restaurant
- `DELETE /api/restaurants/{id}` — Delete restaurant

**Future Endpoints (Not Implemented Yet):**
- Customer management endpoints
- Address management endpoints
- Order management endpoints

---

## Data and Models (Simple)

### Database Entities

The system uses a relational MySQL database with the following main entities (based on provided ERD):

**Restaurant**
- ID (Primary Key)
- Name
- Address
- Phone number
- Email
- Opening/Closing times

**Customer**
- ID (Primary Key)
- First Name
- Last Name
- Email
- Phone number
- Has one or more Addresses

**Address**
- ID (Primary Key)
- Street
- City
- Postal Code
- Country
- Customer ID (Foreign Key) — Many addresses can belong to one customer

**Order**
- ID (Primary Key)
- Order number
- Date/Time
- Status
- Customer ID (Foreign Key)
- Restaurant ID (Foreign Key)
- Delivery Address ID (Foreign Key)

### Relationships

- **One-to-Many:** One Customer has many Addresses
- **One-to-Many:** One Customer has many Orders
- **One-to-Many:** One Restaurant has many Orders
- **Many-to-One:** Many Orders belong to one Customer, one Restaurant

---

## Tech Stack and Tools

### Backend

- **Java 17** — Programming language
- **Spring Boot 2.x / 3.x** — Web application framework
- **Spring Data JPA** — ORM for database operations
- **Spring MVC** — Model-View-Controller pattern implementation
- **Maven** — Build and dependency management tool

### Database

- **MySQL 8.x** — Relational SQL database
- **SQLException and JDBC** — Database connectivity

### Frontend (Back Office)

- **HTML5** — Page structure
- **CSS3** — Styling
- **JavaScript (Vanilla)** — Client-side logic and AJAX
- **Thymeleaf** — Server-side templating (if used with Spring Boot)

### Development & Testing Tools

- **DBeaver** — Database visualization and management
- **Postman** — API endpoint testing
- **Git** — Version control
- **Lombok** — Java boilerplate reduction (optional but recommended)

---

## Repository Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/rocketFoodDelivery/rocketFood/
│   │       ├── RocketFoodApplication.java (Main entry point)
│   │       ├── controller/ (Spring MVC Controllers)
│   │       │   ├── RestaurantController.java
│   │       │   └── (other controllers)
│   │       ├── service/ (Business logic)
│   │       │   ├── RestaurantService.java
│   │       │   ├── CustomerService.java
│   │       │   ├── AddressService.java
│   │       │   └── (other services)
│   │       ├── repository/ (JPA Repositories for DB access)
│   │       │   ├── RestaurantRepository.java
│   │       │   ├── CustomerRepository.java
│   │       │   ├── AddressRepository.java
│   │       │   └── (other repositories)
│   │       ├── models/ (Entity classes / POJO)
│   │       │   ├── Restaurant.java
│   │       │   ├── Customer.java
│   │       │   ├── Address.java
│   │       │   ├── UserEntity.java
│   │       │   └── (other entities)
│   │       └── DataSeeder.java (Database initialization)
│   └── resources/
│       ├── application.properties (Configuration)
│       └── templates/ (Thymeleaf HTML templates - if used)
├── test/
│   └── java/ (Unit tests)
├── pom.xml (Maven configuration, dependencies)
└── mvnw (Maven wrapper for builds)
```

---

## Rules for the AI

- **Follow the MVC pattern strictly:** All new code must be organized into models, services, controllers, and repositories
- **Use Spring Boot best practices:** Leverage Spring annotations (@RestController, @Service, @Repository, @Entity, etc.)
- **Enforce database relationships:** Use Primary Keys and Foreign Keys; implement JPA/Hibernate associations
- **CRUD-only at this stage:** Only Create, Read, Update, Delete operations. No advanced queries or complex business logic
- **Keep controllers slim:** Business logic belongs in services, not controllers
- **Use constructor or setter injection:** Prefer dependency injection over manual instantiation
- **Test endpoints with Postman:** Document all API endpoints and verify with POST/GET/PUT/DELETE requests
- **Do not modify the core framework setup:** Only add controllers, services, and repositories as needed
- **Keep code readable:** Use meaningful variable/method names and avoid over-engineering at this stage
- **Do not introduce authentication yet:** POC does not require login or security
- **Database changes must persist:** Verify all CRUD operations save data to the MySQL database

---

## How to Run / Test the Project

### Prerequisites

- Java 17 installed
- MySQL 8.x installed and running
- Maven installed (or use `./mvnw` wrapper)
- Postman installed (for API testing)
- DBeaver installed (for database visualization)

### Setup Steps

1. **Clone and navigate to project:**
   ```bash
   git clone <repository-url>
   cd RocketDelivery
   ```

2. **Configure database connection** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/rocketfood
   spring.datasource.username=root
   spring.datasource.password=<your-password>
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Install dependencies and build:**
   ```bash
   ./mvnw clean install
   ```

4. **Run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application:**
   - Back Office: `http://localhost:8080/backoffice/restaurants`
   - API base URL: `http://localhost:8080/api`

### Testing

**Database visualization:**
1. Open DBeaver
2. Connect to your MySQL database
3. Navigate to the `rocketfood` database
4. View ER Diagram and verify table structure

**API Testing with Postman:**
- Import or manually create requests for each endpoint
- Test all CRUD operations: GET, POST, PUT, DELETE
- Verify response status codes and data

**Manual Testing:**
- Open Back Office page and test:
  - Create restaurant
  - View restaurant list
  - Update restaurant information
  - Delete restaurant
- Refresh the page and confirm data persists

---

## Definition of Done

Project is complete when **ALL** of the following are satisfied:

### Database

- [ ] MySQL database created and configured
- [ ] All ERD tables implemented (Restaurants, Customers, Addresses, Orders)
- [ ] Primary Keys defined on all tables
- [ ] Foreign Keys enforced to maintain relationships
- [ ] Spring Boot entities created and annotated correctly
- [ ] Database initializes without errors on `mvnw spring-boot:run`
- [ ] DBeaver successfully connects and displays ER diagram
- [ ] Sample data visible in DBeaver after application startup

### Backend API

- [ ] RestaurantController created with REST endpoints
- [ ] All 5 CRUD endpoints implemented (GET all, GET by ID, POST, PUT, DELETE)
- [ ] RestaurantService handles business logic
- [ ] RestaurantRepository extends JpaRepository
- [ ] API returns proper HTTP status codes (200, 201, 404, 500, etc.)
- [ ] All endpoints tested and working in Postman
- [ ] Database changes persist after API calls

### Front Office Web Interface

- [ ] Page created at `localhost:8080/backoffice/restaurants`
- [ ] Page displays list of all restaurants from database
- [ ] Form to create new restaurant (with submit button)
- [ ] Edit functionality for existing restaurants
- [ ] Delete button for each restaurant in list
- [ ] List refreshes after creating/updating/deleting a restaurant
- [ ] No manual page refresh required for changes
- [ ] Basic styling and layout applied (clean, readable UI)

### Testing

- [ ] All CRUD operations tested with Postman
- [ ] POST request creates restaurant and persists to DB
- [ ] GET request retrieves all restaurants
- [ ] PUT request updates restaurant successfully
- [ ] DELETE request removes restaurant from DB
- [ ] Application runs without errors: `./mvnw spring-boot:run`
- [ ] No console errors or warnings on startup

### Code Quality

- [ ] Code follows MVC pattern
- [ ] No hardcoded values (use configuration)
- [ ] Variable and method names are meaningful
- [ ] Comments explain non-obvious logic
- [ ] No dead code or unused imports
- [ ] Code is readable and maintainable
