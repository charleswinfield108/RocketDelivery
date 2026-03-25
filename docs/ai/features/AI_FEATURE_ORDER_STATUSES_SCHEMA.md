# 🤖 AI_FEATURE_ORDER_STATUSES_SCHEMA

This document describes the **Order Statuses Schema** feature — the database entity and JPA configuration for managing the master list of order statuses in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Order Statuses Schema
- **Related Area:** Backend (Database Layer - Reference/Lookup Table)

---

## Feature Goal

Create a properly annotated `OrderStatus` JPA class that represents a status type that an order can have in the RocketDelivery system. The entity must:
- Map correctly to the `order_statuses` table in MySQL as a reference/lookup table
- Define all possible order states (PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)
- Include status descriptions and metadata
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)
- Support order status tracking and workflow transitions

---

## Feature Scope

### In Scope (Included)

- OrderStatus class definition
- JPA @Entity and @Table annotations
- All required fields (id, statusCode, statusName, description, displayOrder, isActive, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Pre-populated reference data in database

### Out of Scope (Excluded)

- Status workflow or state machine logic
- Status transition validation
- Order repository or service logic
- Status change history or audit logging
- Permissions or role-based status access
- API CRUD endpoints for status management
- Status UI management screens
- Status-specific actions or handlers
- Internationalization (multi-language status names)

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all status fields with appropriate Java types (UUID id, String statusCode, String statusName, String description, int displayOrder, boolean isActive)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue with correct configurations
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Size, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor for automatic generation
- **Reference Data** — Pre-populate with standard order statuses

---

## User Flow / Logic (High Level)

1. **Entity Definition:** OrderStatus is defined as a reference/lookup table with all possible order states
2. **Database Setup:** On application startup, Hibernate creates the `order_statuses` table
3. **Initial Data:** Reference data is seeded with standard statuses (pending setup via DataSeeder or Flyway)
4. **Order Status Assignment:** When orders are created, they reference an OrderStatus via foreign key
5. **Status Display:** UI fetches OrderStatus to display user-friendly status names and descriptions
6. **Status Workflow:** Services use OrderStatus to validate status transitions during order lifecycle

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/OrderStatus.java`
- **Related:** Order.java (references OrderStatus via foreign key)

### Optional API Endpoints

- `GET /api/order-statuses` — List all available order statuses (public reference data)

---

## Data Used or Modified

### OrderStatus Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| statusCode | String | VARCHAR(50) | NOT NULL, UNIQUE, Machine-readable code |
| statusName | String | VARCHAR(100) | NOT NULL, UNIQUE, User-friendly display name |
| description | String | VARCHAR(500) | Nullable, @Size(max=500), Detailed explanation |
| displayOrder | Integer | INT | NOT NULL, Order for UI sorting (1-10) |
| isActive | Boolean | TINYINT(1) | NOT NULL, Default=true, Enable/disable status |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Pre-Populated Reference Data

| statusCode | statusName | Description | displayOrder | isActive |
|------------|------------|-------------|--------------|----------|
| PENDING | Pending | Order received, awaiting confirmation | 1 | true |
| CONFIRMED | Confirmed | Order confirmed by restaurant | 2 | true |
| PREPARING | Preparing | Restaurant is preparing the order | 3 | true |
| READY | Ready | Order is ready for pickup/delivery | 4 | true |
| OUT_FOR_DELIVERY | Out for Delivery | Order is on its way to customer | 5 | true |
| DELIVERED | Delivered | Order has been successfully delivered | 6 | true |
| CANCELLED | Cancelled | Order was cancelled | 7 | true |

### Validations

- `statusCode`: Not null, unique, 1-50 characters, uppercase machine-readable format
- `statusName`: Not null, unique, 1-100 characters, human-readable display name
- `description`: Optional, max 500 characters, explains status to users
- `displayOrder`: Not null, integer 1-10, determines UI display sequence
- `isActive`: Not null, default true, allows soft-disable of statuses

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., status_code, status_name, display_order, is_active)
- **Unique Constraints:** Both statusCode and statusName should be unique to prevent duplicates
- **Display Order:** Allow sorting/ranking for UI presentation
- **Active Flag:** Enable soft-delete capability for backward compatibility
- **Reference Data:** Immutable after seeding; do not typically modify via application UI

---

## Acceptance Criteria

- [ ] OrderStatus.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "order_statuses")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all fields with correct nullable, unique, length properties
- [ ] statusCode and statusName have unique=true constraints in @Column
- [ ] @Size annotations applied to statusCode, statusName, description
- [ ] @NotNull annotation applied to all required fields (statusCode, statusName, displayOrder, isActive)
- [ ] displayOrder is Integer type for correct sorting
- [ ] isActive is Boolean type with default value (true)
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `order_statuses` table with all columns correctly mapped
- [ ] Reference data successfully seeded: 7 standard statuses (PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)
- [ ] Unique constraints prevent duplicate statusCode or statusName entries
- [ ] displayOrder correctly sorts statuses in query results

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **statusCode:** Machine-readable format (e.g., "OUT_FOR_DELIVERY", "PENDING") for use in code and APIs
- **statusName:** User-friendly display name (e.g., "Out for Delivery", "Pending") for UI
- **Reference Table:** This is a lookup/reference table, not transactional; typically static data
- **Pre-Population:** Use DataSeeder.java to seed initial statuses, or consider using Flyway/Liquibase migrations
- **Immutability:** Once seeded, these status types should rarely change; any new statuses are significant business logic additions
- **Active Flag:** Allows deprecating old statuses without breaking data integrity (foreign key references)
- **Display Order:** Enables UI to show statuses in a sensible order (workflow sequence)
- **No Foreign Keys in This Table:** OrderStatus is referenced BY Order, not the other way around
- **Status Values Are Business Logic:** These 7 statuses represent the business workflow; do not add or remove lightly
- **Future Enhancement:** Can add color codes, icons, or role-based visibility later
- **Query Performance:** Small reference table; no special indexing needed beyond primary key

---

## Reference Data SQL

For reference, here's the SQL to seed initial statuses:

```sql
INSERT INTO order_statuses (status_code, status_name, description, display_order, is_active, created_at, updated_at)
VALUES 
('PENDING', 'Pending', 'Order received, awaiting confirmation', 1, true, NOW(), NOW()),
('CONFIRMED', 'Confirmed', 'Order confirmed by restaurant', 2, true, NOW(), NOW()),
('PREPARING', 'Preparing', 'Restaurant is preparing the order', 3, true, NOW(), NOW()),
('READY', 'Ready', 'Order is ready for pickup/delivery', 4, true, NOW(), NOW()),
('OUT_FOR_DELIVERY', 'Out for Delivery', 'Order is on its way to customer', 5, true, NOW(), NOW()),
('DELIVERED', 'Delivered', 'Order has been successfully delivered', 6, true, NOW(), NOW()),
('CANCELLED', 'Cancelled', 'Order was cancelled', 7, true, NOW(), NOW());
```

---

## POC-Specific Notes

For the Rocket Food Delivery POC:

- **Order Status Management:** Not exposed in initial Back Office (Restaurants CRUD focus)
- **Status Display:** Will be used by future order management and customer-facing features
- **Reference Data:** These 7 statuses cover basic food delivery workflow (pending → delivered or cancelled)
- **Workflow:** Service layer will implement status transition logic based on these defined statuses
- **Future Expansion:** Additional statuses (e.g., RETURNED, REFUNDED) can be added later without code changes
- **Integration:** Order.status field will reference OrderStatus.id as foreign key
- **Scalability:** Approach allows easy multi-status workflows, batch operations, and status-specific actions in future phases
- **Data Quality:** Pre-populated reference data ensures consistency across the system; prevents typos or inconsistent status strings in Order.status field
