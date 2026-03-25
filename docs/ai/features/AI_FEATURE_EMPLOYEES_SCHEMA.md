# 🤖 AI_FEATURE_EMPLOYEES_SCHEMA

This document describes the **Employees Schema** feature — the database entity and JPA configuration for managing restaurant employees in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Employees Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `Employee` JPA class that represents a restaurant employee in the RocketDelivery system. The entity must:
- Map correctly to the `employees` table in MySQL
- Include all required employee fields with appropriate data types
- Establish a Many-to-One relationship with Restaurant entity
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- Employee class definition
- JPA @Entity and @Table annotations
- All required fields (id, firstName, lastName, email, phoneNumber, position, hireDate, restaurant, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Email, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Foreign Key relationship to Restaurant entity (@ManyToOne)

### Out of Scope (Excluded)

- Restaurant entity implementation (handled separately)
- Employee repository or service logic
- CRUD operations
- Employee business logic or validation methods beyond annotations
- Salary management or payroll features
- Shift scheduling or time tracking
- Employee roles or permissions (simple position field only)
- Employee deactivation or soft-delete logic

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all employee fields with appropriate Java types (UUID id, String firstName, String lastName, String email, String phoneNumber, String position, LocalDate hireDate, Restaurant restaurant)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn with correct configurations
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Email, @Size, @Pattern, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor for automatic getters/setters/toString
- **Restaurant Relationship** — Define Many-to-One relationship to Restaurant with proper annotations

---

## User Flow / Logic (High Level)

1. **Entity Definition:** Employee is defined with all necessary fields and annotations, including relationship to Restaurant
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `employees` table with foreign key to `restaurants` table
3. **Relationship Setup:** Each Employee is linked to exactly one Restaurant via the `restaurant_id` foreign key
4. **Data Validation:** When an employee object is persisted, validations are triggered
5. **CRUD Operations:** Services use Employee to represent employee data in memory and persist/retrieve from database

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/Employee.java`
- **Related:** Restaurant.java (one-to-many side of relationship)

---

## Data Used or Modified

### Employee Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| firstName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| lastName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| email | String | VARCHAR(255) | NOT NULL, UNIQUE, @Email |
| phoneNumber | String | VARCHAR(20) | NOT NULL, @Size(min=10, max=20) |
| position | String | VARCHAR(100) | NOT NULL, @Size(min=3, max=100) |
| hireDate | LocalDate | DATE | NOT NULL |
| restaurant | Restaurant | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="restaurant_id") |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `firstName`: Not null, 2-100 characters
- `lastName`: Not null, 2-100 characters
- `email`: Not null, valid email format, must be unique in database
- `phoneNumber`: Not null, 10-20 characters
- `position`: Not null, 3-100 characters (e.g., "Manager", "Chef", "Delivery Driver", "Host")
- `hireDate`: Not null, must be a valid date (typically in past)
- `restaurant`: Not null, must reference existing Restaurant

### Relationship

- **Many-to-One:** Many Employees belong to one Restaurant
- **Cascade Behavior:** When a Restaurant is deleted, decide cascade strategy (typically REMOVE to delete employees)

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., first_name, hire_date, restaurant_id)
- **Foreign Key:** Use @ManyToOne on restaurant field with @JoinColumn(name = "restaurant_id")
- **Cascade:** Determine cascade strategy (typically CascadeType.REMOVE for employee lifecycle tied to restaurant)
- **Date Type:** Use LocalDate for hire date (no time component needed)

---

## Acceptance Criteria

- [ ] Employee.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "employees")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all fields with correct nullable, unique, length properties
- [ ] All string fields have appropriate @Size or @Pattern annotations
- [ ] email field has @Email annotation
- [ ] @NotNull annotation applied to all required fields
- [ ] @ManyToOne annotation applied to restaurant field
- [ ] @JoinColumn(name = "restaurant_id") applied to restaurant field
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `employees` table with all columns correctly mapped
- [ ] Foreign key relationship visible in DBeaver ER diagram

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Restaurant Relationship:** Use @ManyToOne with @JoinColumn to establish foreign key relationship
- **Cascade Strategy:** For this feature, consider CascadeType.REMOVE so employees are deleted when restaurant is deleted
- **Lazy Loading:** Consider @ManyToOne(fetch = FetchType.LAZY) for performance
- **Position Field:** Keep as simple String for flexibility (no enum); business can add specific positions later
- **Hire Date:** Use LocalDate (not LocalDateTime) since specific time is not needed
- **Email Uniqueness:** Email should be UNIQUE at database level; Spring validates with @Email
- **Phone Number:** Allow flexible format; no strict pattern validation
- **Do Not Create Bidirectional Relationship Yet:** Do not add @OneToMany in Restaurant class in this feature
