# 🤖 AI_FEATURE_PRODUCT_ORDERS_SCHEMA

This document describes the **Product Orders Schema** feature — the database entity and JPA configuration for managing individual line items within a customer order in the RocketDelivery system.

---

## Feature Identity

- **Feature Name:** Product Orders Schema (Order Items/Line Items)
- **Related Area:** Backend (Database Layer)

---

## Feature Goal

Create a properly annotated `ProductOrder` JPA class that represents a single product item within an order in the RocketDelivery system. The entity must:
- Map correctly to the `product_orders` table in MySQL
- Track individual product quantities, pricing, and customizations per order
- Establish Many-to-One relationships with Order and Product entities
- Calculate line item totals
- Enforce data validation at the entity level
- Use Spring Boot best practices (Lombok, JPA annotations)

---

## Feature Scope

### In Scope (Included)

- ProductOrder class definition (also known as OrderItem or LineItem)
- JPA @Entity and @Table annotations
- All required fields (id, order, product, quantity, unitPrice, totalPrice, specialInstructions, notes, etc.)
- Proper Java data types and column definitions
- JPA annotations (@Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn, etc.)
- Jakarta Bean Validation annotations (@NotNull, @Positive, @Min, @Max, @Size, etc.)
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString, etc.)
- Getters, setters, and constructors (Lombok-generated)
- Foreign Key relationships to Order and Product entities

### Out of Scope (Excluded)

- Product repository or service logic
- CRUD operations implementation
- OrderItem aggregation or calculation logic (will be in service layer)
- Promotional pricing or discounts per item
- Variant or modifier selection (size, toppings, etc.)
- Ingredient substitutions or allergies tracking
- Item-level tax calculation
- Order item status tracking (separate from Order status)
- Item cancellation or returns
- Nutritional information

---

## Sub-Requirements (Feature Breakdown)

- **Setup & Imports** — Correct package structure, imports (javax.persistence.*, org.hibernate.validator.constraints, lombok.*)
- **Fields & Types** — Define all product order fields with appropriate Java types (UUID id, Order order, Product product, int quantity, BigDecimal unitPrice, BigDecimal totalPrice, String specialInstructions, String notes)
- **JPA Annotations** — Apply @Entity, @Table, @Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn with correct configurations
- **Relationships** — Configure @ManyToOne relationships for order and product
- **Validation Rules** — Apply Jakarta/Hibernate validators (@NotNull, @Positive, @Min, @Max, @Size, etc.)
- **Lombok Configuration** — Apply @Data, @NoArgsConstructor, @AllArgsConstructor, @ToString for automatic generation

---

## User Flow / Logic (High Level)

1. **Entity Definition:** ProductOrder (OrderItem) is defined with relationships to Order and Product
2. **Database Mapping:** When Spring Boot starts, Hibernate maps the entity to the `product_orders` table with foreign keys
3. **Order Creation:** When customer adds items to order, ProductOrder records are created for each product
4. **Line Item Details:** Each ProductOrder tracks:
   - What product (Coffee, Burger, etc.)
   - How many (quantity)
   - What price (captured at order time)
   - Any special requests (no onions, extra sauce, etc.)
5. **Order Total Calculation:** Service sums all ProductOrder totalPrice values to get Order total
6. **Data Retrieval:** Fetch order with all its product items and details

---

## Interfaces (Pages, Endpoints, Screens)

### Backend Files Involved

- **File:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/ProductOrder.java`
- **Related:** Order.java, Product.java

### API Endpoints Using This Entity

- `POST /api/orders/{orderId}/items` — Add product to order
- `GET /api/orders/{orderId}/items` — Get all items in order
- `PUT /api/orders/{orderId}/items/{itemId}` — Update item quantity or details
- `DELETE /api/orders/{orderId}/items/{itemId}` — Remove item from order
- `GET /api/orders/{orderId}/items/{itemId}` — Get specific order item

---

## Data Used or Modified

### ProductOrder Fields

| Field | Type | Column Type | Constraints |
|-------|------|-------------|-------------|
| id | UUID or Long | BIGINT / VARCHAR | Primary Key, Auto-generated |
| order | Order | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="order_id") |
| product | Product | BIGINT (FK) | NOT NULL, @ManyToOne, @JoinColumn(name="product_id") |
| quantity | Integer | INT | NOT NULL, @Positive, @Min(1), @Max(999) |
| unitPrice | BigDecimal | DECIMAL(10,2) | NOT NULL, @Positive, Price at time of order |
| totalPrice | BigDecimal | DECIMAL(10,2) | NOT NULL, @Positive, unitPrice × quantity |
| specialInstructions | String | VARCHAR(500) | Nullable, @Size(max=500) |
| notes | String | VARCHAR(500) | Nullable, @Size(max=500), Internal notes |
| createdAt | LocalDateTime | TIMESTAMP | NOT NULL, Auto-set |
| updatedAt | LocalDateTime | TIMESTAMP | Nullable, Auto-update |

### Validations

- `order`: Not null, must reference existing Order
- `product`: Not null, must reference existing Product
- `quantity`: Not null, must be positive (>= 1), max 999 per item
- `unitPrice`: Not null, must be positive, captures price at order time (not dynamic product price)
- `totalPrice`: Not null, must equal unitPrice × quantity, calculated on creation
- `specialInstructions`: Optional, max 500 characters (e.g., "No onions", "Extra sauce", "Spicy")
- `notes`: Optional, max 500 characters for internal use (e.g., "Marked as allergic item")

### Relationships

- **Many-to-One:** Many ProductOrders belong to one Order
  - Annotation: `@ManyToOne(fetch=FetchType.EAGER)`
  - Join Column: `order_id`
  
- **Many-to-One:** Many ProductOrders reference one Product
  - Annotation: `@ManyToOne(fetch=FetchType.EAGER)`
  - Join Column: `product_id`

---

## Tech Constraints (Feature-Level)

- Use **JPA/Hibernate** for ORM (no SQL queries)
- Use **Lombok** for reducing boilerplate (@Data, @NoArgsConstructor, @AllArgsConstructor, @ToString)
- Use **Jakarta Bean Validation** (javax.validation or jakarta.validation) for annotations
- Use **Spring Boot** conventions and best practices
- **ID Generation:** Use @GeneratedValue(strategy = GenerationType.AUTO) or IDENTITY
- **Timestamps:** Use @CreationTimestamp and @UpdateTimestamp (from Hibernate validator) if available, or manually set
- **Column Naming:** Follow snake_case convention in database (e.g., order_id, product_id, unit_price, total_price, special_instructions)
- **BigDecimal:** Use for monetary values to avoid floating-point precision issues
- **Foreign Keys:** Use @ManyToOne with @JoinColumn for both relationships
- **Eager Loading:** Use FetchType.EAGER for relationships to avoid N+1 query issues
- **Price Capture:** unitPrice is stored at order time (snapshot), not dynamically linked to current product price
- **Total Calculation:** totalPrice should be calculated and stored; can be verified in service layer

---

## Acceptance Criteria

- [ ] ProductOrder.java file exists in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
- [ ] Alternatively named as OrderItem.java or similar if preferred
- [ ] Class is annotated with @Entity and @Table(name = "product_orders")
- [ ] All required fields are present with correct data types
- [ ] @Id and @GeneratedValue annotations applied to id field
- [ ] @Column annotations applied to quantity, unitPrice, totalPrice with correct nullable and numeric constraints
- [ ] @ManyToOne annotations applied to order and product
- [ ] @JoinColumn annotations correctly specify foreign key names (order_id, product_id)
- [ ] @Positive annotation applied to quantity, unitPrice, totalPrice
- [ ] @Min(1) and @Max(999) constraints applied to quantity
- [ ] @Size annotations applied to specialInstructions and notes (max 500)
- [ ] @NotNull annotation applied to all required fields (order, product, quantity, unitPrice, totalPrice)
- [ ] Fetch strategy set to EAGER for many-to-one relationships
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor) are applied
- [ ] @ToString properly handles relationships
- [ ] Entity compiles without errors
- [ ] When Spring Boot starts, no Hibernate errors occur
- [ ] Entity can be instantiated and tested programmatically
- [ ] Validation annotations trigger correctly when invalid data is assigned
- [ ] DBeaver shows the `product_orders` table with all columns correctly mapped
- [ ] Foreign key relationships visible in DBeaver ER diagram connecting to orders and products tables

---

## Notes for the AI

- **Lombok:** Generates getters, setters, equals(), hashCode(), toString() automatically
- **Timestamps:** Consider using `@CreationTimestamp` and `@UpdateTimestamp` from org.hibernate.annotations for automatic date handling
- **ID Strategy:** If using MySQL AUTO_INCREMENT, use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Price Capture:** Important: unitPrice represents the price AT ORDER TIME. This is a snapshot, not a reference to dynamic product price, so that order history shows what customer paid
- **Total Price Calculation:** totalPrice = unitPrice × quantity. This can be done in constructor, @PrePersist, or service layer
- **Quantity Constraints:** @Positive ensures >= 1, @Max(999) prevents unrealistic quantities
- **Special Instructions:** Customer-facing notes (no onions, extra sauce, not too spicy, etc.)
- **Notes Field:** Internal field for staff (e.g., "Customer is allergic to peanuts", "Prepare separately")
- **Relationships — Eager Loading:** ProductOrders always need order and product info, so use FetchType.EAGER to avoid N+1 issues
- **BigDecimal:** Prevents floating-point precision errors in price calculations
- **Cascade Behavior:** Do NOT cascade delete; line items should be preserved in order history even if product is deleted
- **Do Not Create Bidirectional Collections Yet:** Do not add @OneToMany in Order or Product classes in this feature; handle separately if needed
- **Relationship Consistency:** Ensure productOrder is linked to order and product consistently
- **Order Item vs ProductOrder Name:** Common names are OrderItem, LineItem, or ProductOrder; pick one consistently across the codebase
- **Future Variants:** If you later need product variants (size, toppings), consider a separate ProductVariant table with foreign key from ProductOrder

---

## POC-Specific Notes

While this is a foundational schema for complete order fulfillment:

- **Back Office Priority:** Current POC focuses on Restaurants CRUD first; OrderItems are foundational but not yet exposed in Back Office UI
- **Order Items Display:** Will need UI list showing all items in an order (future phase)
- **Price History:** The captured unitPrice is crucial for accurate order history and customer invoicing
- **Calculation Logic:** Service layer will sum ProductOrder.totalPrice values to calculate Order.totalPrice
- **Cart vs Order:** ProductOrder represents items in a confirmed order; may differ from shopping cart logic
- **Product Availability:** When creating ProductOrder, service should verify product is still available at restaurant
- **Pricing Rules:** Discounts, promotions, or item-level taxes would be added later; current design is simple per-item pricing
- **Composition:** Product entity (menu items) must exist before ProductOrder can reference it
