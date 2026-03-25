# 🤖 AI_FEATURE_RESTAURANTS_SCHEMA

This document describes the **Restaurants Schema** feature — the database entity and JPA configuration for managing restaurants in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Restaurants Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `Restaurant` JPA class that represents a restaurant in the RocketDelivery system. The entity must:
- Map correctly to the `restaurants` table in MySQL
- Include all required restaurant fields with appropriate data types
- Enforce data validation at the entity level
- Support CRUD operations for the Back Office
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- Restaurant class definition
- JPA @Entity and @Table annotations
- All required fields (id, name, email, phoneNumber, street, city, postalCode, country, openingTime, closingTime, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Email, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- Getters, setters, and constructors (Lombok-generated)

### Out of Scope (Excluded)

- Employee or Order relationships (will be implemented separately)
- Restaurant repository or service logic
- CRUD operations implementation
- Restaurant business logic or validation methods beyond annotations
- Menu management or food item handling
- Ratings or reviews
- Operating hours with complex timezone handling
- Restaurant status or deactivation logic
- Image or logo storage

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all restaurant fields with appropriate Java types (UUID id, String name, String email, String phoneNumber, String street, String city, String postalCode, String country, LocalTime openingTime, LocalTime closingTime, etc.)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue with correct configurations
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Email, @Size, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor for automatic getters/setters/toString

---

## User Flow / Logic (High Level)

1. **Entity Definition:** Restaurant is defined with all necessary fields and annotations
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `restaurants` table
3. **Data Validation:** When a restaurant object is persisted, validations are triggered
4. **CRUD Operations:** Back Office controllers and services use Restaurant to manage restaurant data
5. **API Operations:** REST endpoints receive/return Restaurant data for create, read, update, delete operations

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/Restaurant.java`

### API Endpoints Using This Entity

- `GET /api/restaurants` — List all restaurants
- `GET /api/restaurants/{id}` — Get single restaurant
- `POST /api/restaurants` — Create new restaurant
- `PUT /api/restaurants/{id}` — Update restaurant
- `DELETE /api/restaurants/{id}` — Delete restaurant

### Frontend Pages Using This Entity

- `/backoffice/restaurants` — Back Office restaurant management page

---

## Data Used or Modified

### Restaurant Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| name | String | VARCHAR(255) | NOT NULL, UNIQUE, @Size(min=3, max=255) |
| email | String | VARCHAR(255) | NOT NULL, UNIQUE, @Email |
| phoneNumber | String | VARCHAR(20) | NOT NULL, @Size(min=10, max=20) |
| street | String | VARCHAR(255) | NOT NULL, @Size(min=5, max=255) |
| city | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| postalCode | String | VARCHAR(20) | NOT NULL, @Size(min=3, max=20) |
| country | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| openingTime | LocalTime | TIME | NOT NULL |
| closingTime | LocalTime | TIME | NOT NULL |
| description | String | TEXT / VARCHAR(1000) | Nullable, @Size(max=1000) |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `name`: Not null, 3-255 characters, must be unique
- `email`: Not null, valid email format, must be unique
- `phoneNumber`: Not null, 10-20 characters
- `street`: Not null, 5-255 characters
- `city`: Not null, 2-100 characters
- `postalCode`: Not null, 3-20 characters
- `country`: Not null, 2-100 characters
- `openingTime`: Not null, valid time format (HH:mm)
- `closingTime`: Not null, valid time format (HH:mm)
- `description`: Optional, max 1000 characters

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., phone_number, opening_time, postal_code)
- **Time Type:** Use LocalTime for opening/closing times (no date component needed)
- **Uniqueness:** Use unique=true in @Column for name and email to enforce at database level
- **Description:** Keep as optional field for flexibility

---

## Acceptance Criteria

- [ ] Restaurant.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "restaurants")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all fields with correct nullable, unique, length properties
- [ ] name and email fields have unique=true constraint in @Column
- [ ] @Email annotation applied to email field
- [ ] @Size annotations applied to appropriate string fields
- [ ] All required fields have @NotNull annotation
- [ ] LocalTime fields correctly configured for openingTime and closingTime
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `restaurants` table with all columns correctly mapped
- [ ] Unique constraints visible in DBeaver for name and email columns

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Time Type:** Use LocalTime (not String) for opening/closing times for type safety
- **Unique Constraint:** Database enforces uniqueness for name and email via @Column(unique=true)
- **Phone Format:** No strict pattern validation; keep flexible for different country formats
- **Email Uniqueness:** Email should be UNIQUE at database level; Spring validates with @Email
- **Description:** Optional field; allows business to add restaurant descriptions later
- **Do Not Add Relationships Yet:** Do not add @OneToMany annotations for Employees or Orders in this feature; handle separately
- **Postal Code:** Allow flexible format since different countries have different formats
- **JSON Serialization:** Timestamps and LocalTime fields will serialize correctly to JSON in REST endpoints

---

## POC-Specific Notes

This entity is central to the **Back Office POC** for restaurant CRUD operations. Key considerations:

- **Back Office Focus:** The main feature is creating a UI page at `/backoffice/restaurants` where staff can manage restaurants
- **API Testing:** All REST endpoints must work with Postman for testing
- **Data Persistence:** Changes must persist in MySQL database
- **List Refresh:** After any create/update/delete, the restaurant list must refresh without manual page reload
- **Validation Feedback:** Invalid data should provide clear error messages in the API response
