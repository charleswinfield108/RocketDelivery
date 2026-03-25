# 🤖 AI_FEATURE_ADDRESSES_SCHEMA

This document describes the **Addresses Schema** feature — the database entity and JPA configuration for managing addresses associated with customers in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Addresses Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `Address` JPA class that represents a customer address in the RocketDelivery system. The entity must:
- Map correctly to the `addresses` table in MySQL
- Include all required address fields with appropriate data types
- Establish a Many-to-One relationship with Customer entity
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- Address class definition
- JPA @Entity and @Table annotations
- All required fields (id, street, city, postalCode, country, customer, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Size, @Pattern, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Foreign Key relationship to Customer entity (@ManyToOne)

### Out of Scope (Excluded)

- Customer entity implementation (handled separately)
- Address repository or service logic
- CRUD operations
- Address business logic or validation methods beyond annotations
- Address deletion or soft-delete logic
- Geocoding or location validation

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all address fields with appropriate Java types (UUID id, String street, String city, String postalCode, String country, Customer customer)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn with correct configurations
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Size, @Pattern, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor for automatic getters/setters/toString
- **Customer Relationship** — Define Many-to-One relationship to Customer with proper annotations

---

## User Flow / Logic (High Level)

1. **Entity Definition:** Address is defined with all necessary fields and annotations, including relationship to Customer
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `addresses` table with foreign key to `customers` table
3. **Relationship Setup:** Each Address is linked to exactly one Customer via the `customer_id` foreign key
4. **Data Validation:** When an address object is persisted, validations are triggered
5. **CRUD Operations:** Services use Address to represent address data in memory and persist/retrieve from database

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/Address.java`
- **Related:** Customer.java (one-to-many side of relationship)

---

## Data Used or Modified

### Address Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| street | String | VARCHAR(255) | NOT NULL, @Size(min=5, max=255) |
| city | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| postalCode | String | VARCHAR(20) | NOT NULL, @Size(min=3, max=20) |
| country | String | VARCHAR(100) | NOT NULL, @Size(min=2, max=100) |
| customer | Customer | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="customer_id") |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `street`: Not null, 5-255 characters (minimum street address length)
- `city`: Not null, 2-100 characters
- `postalCode`: Not null, 3-20 characters (postal codes vary by country)
- `country`: Not null, 2-100 characters
- `customer`: Not null, must reference existing Customer

### Relationship

- **Many-to-One:** Many Addresses belong to one Customer
- **Cascade Behavior:** When a Customer is deleted, decide cascade strategy (REMOVE, DELETE_ORPHAN, or none)

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., postal_code, customer_id)
- **Foreign Key:** Use @ManyToOne on Customer field with @JoinColumn(name = "customer_id")
- **Cascade:** Determine cascade strategy (typically CascadeType.NONE for independent address lifecycle)

---

## Acceptance Criteria

- [ ] Address.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "addresses")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all fields with correct nullable, length properties
- [ ] All fields have appropriate Jakarta validation annotations (@NotNull, @Size, etc.)
- [ ] @ManyToOne annotation applied to customer field
- [ ] @JoinColumn(name = "customer_id") applied to customer field
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `addresses` table with all columns correctly mapped
- [ ] Foreign key relationship visible in DBeaver ER diagram

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Customer Relationship:** Use @ManyToOne with @JoinColumn to establish foreign key relationship
- **Cascade Strategy:** For this POC, use CascadeType.NONE (addresses are independent, deletion should be managed separately)
- **Lazy Loading:** Consider @ManyToOne(fetch = FetchType.LAZY) for performance if needed
- **String Fields:** No pattern validation for now; business can add regex patterns if needed
- **Postal Code:** Allow flexible format since different countries have different formats (no strict pattern)
- **Do Not Create Bidirectional Relationship Yet:** Do not add @OneToMany in Customer class in this feature
