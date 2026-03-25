# 🤖 AI_FEATURE_USERS_SCHEMA

This document describes the **Users Schema** feature — the database entity and JPA configuration for managing user data in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Users Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `UserEntity` JPA class that represents a user in the RocketDelivery system. The entity must:
- Map correctly to the `users` table in MySQL
- Include all required user fields with appropriate data types
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- UserEntity class definition
- JPA @Entity and @Table annotations
- All required fields (id, email, firstName, lastName, phone, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Email, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- Getters, setters, and constructors (Lombok-generated)

### Out of Scope (Excluded)

- User repository or service logic
- CRUD operations
- Authentication or authorization
- Password encryption or hashing
- User relationships (will be handled in separate features)
- User business logic or validation methods beyond annotations

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all user fields with appropriate Java types (UUID id, String email, String firstName, etc.)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue with correct configurations
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Email, @Size, @Pattern, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor for automatic getters/setters/toString

---

## User Flow / Logic (High Level)

1. **Entity Definition:** UserEntity is defined with all necessary fields and annotations
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `users` table
3. **Data Validation:** When a user object is persisted, validations are triggered
4. **CRUD Operations:** Services use UserEntity to represent user data in memory and persist/retrieve from database

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/UserEntity.java`

---

## Data Used or Modified

### UserEntity Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| email | String | VARCHAR(255) | NOT NULL, UNIQUE, @Email |
| firstName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| lastName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| phoneNumber | String | VARCHAR(20) | NOT NULL, @Size(min=10, max=20) |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `email`: Not null, valid email format, must be unique in database
- `firstName`: Not null, 2-100 characters
- `lastName`: Not null, 2-100 characters
- `phoneNumber`: Not null, 10-20 characters

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., first_name)

---

## Acceptance Criteria

- [ ] UserEntity.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "users")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied with correct nullable, unique, length properties
- [ ] All fields have appropriate Jakarta validation annotations (@NotNull, @Email, @Size, etc.)
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `users` table with all columns correctly mapped

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Email Validation:** Use @Email annotation from jakarta.validation.constraints
- **Phone Number:** No strict validation beyond size; client can add more specific patterns if needed
- **Thread Safety:** LocalDateTime is immutable and thread-safe
- **No Relationships Yet:** Do not add @OneToMany or @ManyToOne annotations in this feature; user relationships will be handled separately
