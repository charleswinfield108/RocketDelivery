# 🤖 AI_FEATURE_CUSTOMERS_SCHEMA

This document describes the **Customers Schema** feature — the database entity and JPA configuration for managing customers in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Customers Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `Customer` JPA class that represents a customer in the RocketDelivery system. The entity must:
- Map correctly to the `customers` table in MySQL
- Include all required customer fields with appropriate data types
- Establish One-to-Many relationships with Address and Order entities
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- Customer class definition
- JPA @Entity and @Table annotations
- All required fields (id, firstName, lastName, email, phoneNumber, registrationDate, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, @OneToMany, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Email, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Relationship collections (addresses, orders) with proper annotations

### Out of Scope (Excluded)

- Address or Order repository/service logic
- CRUD operations implementation
- Customer authentication or password management
- Loyalty points or rewards system
- Customer preferences or dietary restrictions
- Payment method storage
- Customer deactivation or soft-delete logic
- Email verification or confirmation
- Relationship cascade delete logic (will be configured separately)

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all customer fields with appropriate Java types (UUID id, String firstName, String lastName, String email, String phoneNumber, LocalDateTime registrationDate, List<Address> addresses, List<Order> orders)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue with correct configurations
- **Relationships** — Configure @OneToMany relationships for addresses and orders
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Email, @Size, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor, @ToString for automatic generation

---

## User Flow / Logic (High Level)

1. **Entity Definition:** Customer is defined with all necessary fields and annotations, including one-to-many relationships
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `customers` table
3. **Relationships:** Customer is linked to multiple Address records and multiple Order records
4. **Data Validation:** When a customer object is persisted, validations are triggered
5. **CRUD Operations:** Services and controllers use Customer to manage customer data
6. **Data Retrieval:** Fetch customer with related addresses and orders as needed

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/Customer.java`
- **Related:** Address.java (one-to-many side), Order.java (one-to-many side)

### API Endpoints Using This Entity

- `GET /api/customers` — List all customers
- `GET /api/customers/{id}` — Get single customer with addresses and orders
- `POST /api/customers` — Create new customer
- `PUT /api/customers/{id}` — Update customer
- `DELETE /api/customers/{id}` — Delete customer

---

## Data Used or Modified

### Customer Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| firstName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| lastName | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| email | String | VARCHAR(255) | NOT NULL, UNIQUE, @Email |
| phoneNumber | String | VARCHAR(20) | NOT NULL, @Size(min=10, max=20) |
| registrationDate | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| addresses | List<Address> | N/A (Relationship) | @OneToMany(mappedBy="customer"), Optional |
| orders | List<Order> | N/A (Relationship) | @OneToMany(mappedBy="customer"), Optional |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `firstName`: Not null, 2-100 characters
- `lastName`: Not null, 2-100 characters
- `email`: Not null, valid email format, must be unique in database
- `phoneNumber`: Not null, 10-20 characters
- `registrationDate`: Not null, typically set to current timestamp on creation
- `addresses`: Optional, collection of Address objects (many-to-one from Address side)
- `orders`: Optional, collection of Order objects (many-to-one from Order side)

### Relationships

- **One-to-Many:** One Customer has many Addresses
  - Relationship field: `addresses`
  - Annotation: `@OneToMany(mappedBy="customer", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)`
  - Mapped by: Address.customer
  
- **One-to-Many:** One Customer has many Orders
  - Relationship field: `orders`
  - Annotation: `@OneToMany(mappedBy="customer", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)`
  - Mapped by: Order.customer

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., first_name, phone_number, registration_date)
- **Collections:** Use List<Address> and List<Order> with proper @OneToMany annotations
- **Cascade:** Use CascadeType.REMOVE for automatic cleanup of dependent records
- **Lazy Loading:** Use FetchType.LAZY for collections to improve performance
- **Lombok @ToString:** Be careful with bidirectional relationships; use @ToString(exclude={"addresses", "orders"}) or similar

---

## Acceptance Criteria

- [ ] Customer.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "customers")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all direct fields with correct nullable, unique, length properties
- [ ] email field has unique=true constraint in @Column
- [ ] @Email annotation applied to email field
- [ ] @Size annotations applied to firstName, lastName, phoneNumber
- [ ] All required fields have @NotNull annotation
- [ ] @OneToMany(mappedBy="customer") annotations applied to addresses and orders collections
- [ ] Collections use List<Address> and List<Order> types
- [ ] Cascade strategy configured (typically CascadeType.REMOVE)
- [ ] Lazy loading configured for collections (FetchType.LAZY)
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] @ToString properly handles bidirectional relationships (excludes or handles cycles)
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `customers` table with all columns correctly mapped
- [ ] One-to-many relationships visible in DBeaver ER diagram

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Collection Initialization:** Initialize collections as empty lists in entity: `private List<Address> addresses = new ArrayList<>();`
- **Bidirectional Relationships:** 
  - Address side has `@ManyToOne` with `@JoinColumn(name = "customer_id")`
  - Customer side has `@OneToMany(mappedBy = "customer")` 
  - Both sides must be kept in sync if modified programmatically
- **Lazy Loading:** Use FetchType.LAZY for collections to avoid N+1 query problems
- **Cascade Remove:** When customer is deleted, dependent addresses and orders are also deleted
- **Cascade Considerations:** May need CascadeType.ALL or selective cascade based on business rules
- **ToString and Equals:** Lombok's @Data generates these; be careful with circular dependencies in bidirectional relationships
- **Registration Date:** Set automatically on creation; represent as LocalDateTime or Instant
- **Email Uniqueness:** Enforce at database level with unique=true in @Column
- **Relationships Not Yet Implemented:** Order entity will be created separately; ensure consistency with this schema
- **Initialization:** Always initialize collections to prevent NullPointerException when adding elements

---

## POC-Specific Notes

While this is a foundational schema:

- **Back Office Priority:** Current POC focuses on Restaurants CRUD first
- **Customer Management:** Future phase; not yet implemented in Back Office UI
- **Order Processing:** Orders will be tied to customers; this schema supports that relationship
- **Address Management:** Customers must have at least one address to place orders (business rule to validate later)
- **Cascade Behavior:** Decide if deleting a customer should cascade delete their addresses and orders, or if orders should be preserved for audit
