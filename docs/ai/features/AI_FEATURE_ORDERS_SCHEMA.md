# 🤖 AI_FEATURE_ORDERS_SCHEMA

This document describes the **Orders Schema** feature — the database entity and JPA configuration for managing customer orders in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Orders Schema
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `Order` JPA class that represents a customer order in the RocketDelivery system. The entity must:
- Map correctly to the `orders` table in MySQL
- Include all required order fields with appropriate data types
- Establish Many-to-One relationships with Customer, Restaurant, and Address entities
- Track order status and delivery information
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- Order class definition
- JPA @Entity and @Table annotations
- All required fields (id, orderNumber, orderDate, status, totalPrice, customer, restaurant, deliveryAddress, estimatedDeliveryTime, actualDeliveryTime, specialInstructions, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Size, @Positive, @Min, @Max, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Foreign Key relationships to Customer, Restaurant, and Address entities

### Out of Scope (Excluded)

- Order items/products line items (will be separate feature)
- Order repository or service logic
- CRUD operations implementation
- Order status workflow or state machine logic
- Payment processing or payment tracking
- Order cancellation or refund logic
- Order tracking or real-time updates
- Driver assignment
- Delivery notes or feedback
- Order history or archival
- Ratings or reviews on orders

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all order fields with appropriate Java types (UUID id, String orderNumber, LocalDateTime orderDate, String status, BigDecimal totalPrice, Customer customer, Restaurant restaurant, Address deliveryAddress, etc.)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn with correct configurations
- **Relationships** — Configure @ManyToOne relationships for customer, restaurant, and delivery address
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Size, @Positive, @Min, @Max, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor, @ToString for automatic generation

---

## User Flow / Logic (High Level)

1. **Entity Definition:** Order is defined with all necessary fields and annotations, including many-to-one relationships
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `orders` table with foreign keys
3. **Order Creation:** Customer creates an order, linked to a restaurant and delivery address
4. **Status Tracking:** Order status changes through lifecycle (pending → confirmed → preparing → ready → out for delivery → delivered)
5. **Data Validation:** When an order object is persisted, validations are triggered
6. **CRUD Operations:** Services and controllers use Order to manage order data
7. **Data Retrieval:** Fetch order with related customer, restaurant, and address details

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/Order.java`
- **Related:** Customer.java, Restaurant.java, Address.java

### API Endpoints Using This Entity

- `GET /api/orders` — List all orders (future feature)
- `GET /api/orders/{id}` — Get single order details
- `POST /api/orders` — Create new order
- `PUT /api/orders/{id}` — Update order (status, estimated delivery, etc.)
- `GET /api/customers/{customerId}/orders` — Get customer's order history

---

## Data Used or Modified

### Order Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| orderNumber | String | VARCHAR(50) | NOT NULL, UNIQUE, Reference for display |
| orderDate | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| status | String | VARCHAR(50) | NOT NULL, Default="PENDING", @Size(min=1, max=50) |
| totalPrice | BigDecimal | DECIMAL(10,2) | NOT NULL, @Positive |
| customer | Customer | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="customer_id") |
| restaurant | Restaurant | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="restaurant_id") |
| deliveryAddress | Address | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="address_id") |
| estimatedDeliveryTime | LocalDateTime | TIMESTAMP | Nullable |
| actualDeliveryTime | LocalDateTime | TIMESTAMP | Nullable |
| specialInstructions | String | VARCHAR(500) | Nullable, @Size(max=500) |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `orderNumber`: Not null, unique, human-readable order identifier
- `orderDate`: Not null, set to current timestamp on creation
- `status`: Not null, valid values: "PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
- `totalPrice`: Not null, must be positive (> 0)
- `customer`: Not null, must reference existing Customer
- `restaurant`: Not null, must reference existing Restaurant
- `deliveryAddress`: Not null, must reference existing Address
- `estimatedDeliveryTime`: Optional, typically set when order is confirmed
- `actualDeliveryTime`: Optional, set when order is delivered
- `specialInstructions`: Optional, max 500 characters (e.g., "No onions", "Extra sauce")

### Relationships

- **Many-to-One:** Many Orders belong to one Customer
  - Annotation: `@ManyToOne(fetch=FetchType.EAGER)`
  - Join Column: `customer_id`
  
- **Many-to-One:** Many Orders are placed at one Restaurant
  - Annotation: `@ManyToOne(fetch=FetchType.EAGER)`
  - Join Column: `restaurant_id`
  
- **Many-to-One:** Many Orders use one Address for delivery
  - Annotation: `@ManyToOne(fetch=FetchType.EAGER)`
  - Join Column: `address_id`

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., order_number, order_date, total_price, customer_id, restaurant_id, address_id, estimated_delivery_time, actual_delivery_time, special_instructions)
- **BigDecimal:** Use for monetary values to avoid floating-point precision issues
- **Status:** Use String enum pattern (not Java enum) for flexibility; can evolve to @Enumerated(EnumType.STRING) later
- **Foreign Keys:** Use @ManyToOne with @JoinColumn for all three relationships
- **Eager Loading:** Use FetchType.EAGER for essential relationships (customer, restaurant, address)
- **Order Number:** Generate unique, human-readable format (e.g., "ORD-20240325-001234")

---

## Acceptance Criteria

- [ ] Order.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Class is annotated with @Entity and @Table(name = "orders")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to all direct fields with correct nullable, unique, length properties
- [ ] orderNumber field has unique=true constraint in @Column
- [ ] @ManyToOne annotations applied to customer, restaurant, deliveryAddress
- [ ] @JoinColumn annotations correctly specify foreign key names (customer_id, restaurant_id, address_id)
- [ ] @Positive annotation applied to totalPrice
- [ ] @Size annotations applied to status, specialInstructions
- [ ] @NotNull annotation applied to all required fields (id, orderNumber, orderDate, status, totalPrice, customer, restaurant, deliveryAddress)
- [ ] Fetch strategy set to EAGER for many-to-one relationships
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] @ToString properly handles relationships
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `orders` table with all columns correctly mapped
- [ ] Foreign key relationships visible in DBeaver ER diagram connecting to customers, restaurants, addresses tables

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Order Number Generation:** Implement logic in service layer to auto-generate unique order numbers; store as String for flexibility
- **BigDecimal for Money:** Prevents floating-point precision errors; use @Positive for validation
- **Status String:** Keep as String for now; can transition to Java enum later if needed. Valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
- **Relationships — Eager Loading:** Orders typically need customer and restaurant details, so use FetchType.EAGER to avoid N+1 query issues
- **Relationship Consistency:** Keep order linked to customer, restaurant, and address consistently; do not allow orphaned orders
- **Timestamps and Delivery:** 
  - `orderDate`: Set automatically on creation
  - `estimatedDeliveryTime`: Set when order is confirmed (e.g., +30 minutes)
  - `actualDeliveryTime`: Set when order is delivered
- **Special Instructions:** Allow customers to add notes (no onions, extra sauce, leave at door, etc.)
- **Do Not Create Bidirectional Collections Yet:** Do not add @OneToMany in Customer, Restaurant, or Address classes in this feature; handle separately if needed
- **Cascade Behavior:** Do NOT cascade delete; orders should be preserved even if customer/restaurant is deleted for audit purposes
- **Address vs Delivery Address:** The deliveryAddress is typically a copy or reference to a Customer's address; do not store embedded address in order
- **Future Considerations:** Order items/line items will be separate feature; Order will have @OneToMany(mappedBy="order") relationship later

---

## POC-Specific Notes

While this is a foundational schema for complete food delivery workflow:

- **Back Office Priority:** Current POC focuses on Restaurants CRUD first; Orders are foundational for future phases
- **Order Management UI:** Not yet implemented in Back Office; will be a future feature
- **Order Status Workflow:** Business logic for status transitions will be implemented separately in services
- **Delivery Tracking:** Real-time tracking and driver assignment are out of scope for this POC
- **Order Items:** Line items (what products customer ordered) are a separate feature; Order entity supports this future relationship
- **Complex Validations:** Status-based validations (e.g., can't move from DELIVERED back to PENDING) will be in service layer
- **Payment Integration:** Not included in this schema; payment info will be handled separately if needed
