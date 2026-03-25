# Product Orders Schema Feature

## Overview

Create the **ProductOrderEntity** JPA class representing individual line items within a customer order. This entity acts as a join table between orders and products, tracking what items were ordered, quantities, unit prices, and subtotals for each order.

## Database Schema

### Table: `product_orders`

```sql
CREATE TABLE product_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    special_notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT uc_order_product UNIQUE (order_id, product_id)
);
```

### Fields & Constraints

| Field | Type | Nullable | Default | Unique | Constraints |
|-------|------|----------|---------|--------|-------------|
| `id` | BIGINT | NO | AUTO_INCREMENT | YES | Primary Key |
| `order_id` | BIGINT | NO | — | NO* | Foreign Key → orders.id, @ManyToOne |
| `product_id` | BIGINT | NO | — | NO* | Foreign Key → products.id, @ManyToOne |
| `quantity` | INT | NO | 1 | — | @Min(1), @Max(999) |
| `unit_price` | DECIMAL(10,2) | NO | — | — | @Positive, price at time of order |
| `subtotal` | DECIMAL(10,2) | NO | — | — | @Positive, quantity × unit_price |
| `special_notes` | VARCHAR(500) | YES | — | — | Item-specific notes (e.g., "extra spicy", "on the side") |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | — | @CreationTimestamp |
| `updated_at` | TIMESTAMP | YES | — | — | @UpdateTimestamp |

**Note:** Composite unique constraint (order_id, product_id) prevents duplicate line items in same order

## Entity Implementation

### ProductOrderEntity.java

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/models/ProductOrderEntity.java`

**Class Structure:**
- `@Entity` — Map to database table
- `@Table(name = "product_orders")` — Specify table name with unique constraint
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` — Lombok boilerplate
- Manual null validation in service layer (no @NonNull annotations)

**Fields (8 total):**

1. `id` (Long)
   - `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - Primary key, auto-increment

2. `order` (OrderEntity)
   - `@ManyToOne(fetch = FetchType.EAGER, optional = false)`
   - `@JoinColumn(name = "order_id", nullable = false)`
   - `@NotNull`
   - Link to parent order

3. `product` (ProductEntity)
   - `@ManyToOne(fetch = FetchType.EAGER, optional = false)`
   - `@JoinColumn(name = "product_id", nullable = false)`
   - `@NotNull`
   - Link to product (ProductEntity will be created in future feature)

4. `quantity` (Integer)
   - `@Column(nullable = false)`
   - `@NotNull`, `@Min(1)`, `@Max(999)`
   - Number of units ordered (cannot be zero)

5. `unitPrice` (BigDecimal)
   - `@Column(nullable = false, precision = 10, scale = 2)`
   - `@NotNull`, `@Positive`
   - Price per unit at time of order (snapshot, not product's current price)

6. `subtotal` (BigDecimal)
   - `@Column(nullable = false, precision = 10, scale = 2)`
   - `@NotNull`, `@Positive`
   - Calculated as quantity × unitPrice (should equal unitPrice * quantity)

7. `specialNotes` (String)
   - `@Column(nullable = true, length = 500)`
   - `@Size(max=500)`
   - Item-specific customization (e.g., "extra spicy", "gluten-free bread", "no onions")

8. `createdAt` (LocalDateTime)
   - `@CreationTimestamp`
   - `@Column(nullable = false, updatable = false)`
   - Auto-set on entity creation

9. `updatedAt` (LocalDateTime)
   - `@UpdateTimestamp`
   - `@Column(nullable = true)`
   - Auto-updated on modification

**Helper Methods:**
- `recalculateSubtotal()` — Recomputes subtotal as quantity × unitPrice
- `isValidLineItem()` — Validates quantity and prices are positive and consistent
- `getItemTotal()` — Returns subtotal (alias for clarity)
- `getItemDescription()` — Returns human-readable item description
- `updateQuantity(int newQuantity)` — Sets quantity and recalculates subtotal
- `updateUnitPrice(BigDecimal newPrice)` — Updates price and recalculates subtotal

**Relationships:**
- **Many-to-One:** Order (required) — multiple line items per order
- **Many-to-One:** Product (required) — multiple line items can reference same product

**Validation:**
- `order`: Not null, must reference existing order
- `product`: Not null, must reference existing product
- `quantity`: Not null, must be 1-999
- `unitPrice`: Not null, must be > 0
- `subtotal`: Not null, must be > 0 and equal to quantity × unitPrice
- `specialNotes`: Optional, max 500 characters

**Unique Constraint:**
- Combination of (order_id, product_id) must be unique (prevent duplicate line items)

## ProductOrderRepository

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/ProductOrderRepository.java`

**Extends:** `JpaRepository<ProductOrderEntity, Long>`

**Query Methods (15+ methods):**

1. `findByOrderId(Long)` — Get all line items for order
2. `findByOrderIdOrderByCreatedAtAsc(Long)` — Line items in order of addition
3. `findByProductId(Long)` — Get all line items referencing product
4. `findByOrderIdAndProductId(Long, Long)` — Find specific line item (authorization)
5. `findByProductIdAndQuantityGreaterThan(Long, int)` — Products ordered frequently
6. `getAllProductsInOrder(Long)` — List of product IDs for order
7. `countByOrderId(Long)` — Number of distinct products in order
8. `sumSubtotalByOrderId(Long)` — Total price of order (sum subtotal)
9. `findByProductIdAndOrderDateBetween(Long, LocalDateTime, LocalDateTime)` — Product sales in date range
10. `deleteByOrderIdAndProductId(Long, Long)` — Remove line item with authorization
11. `deleteByOrderId(Long)` — Remove all line items for order (order deletion)
12. `existsByOrderIdAndProductId(Long, Long)` — Check if product already in order

**Verification Methods:**
- `existsByOrderId(Long)` — Check if order has line items
- `existsByOrderIdAndProductId(Long, Long)` — Verify line item membership

## ProductOrderService

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/ProductOrderService.java`

**Dependencies:**
- `ProductOrderRepository` (required)
- `OrderRepository` (required for order validation)
- Constructor injection with null validation

**Public Methods (20+ methods):**

### CRUD Operations
1. `addProductToOrder(orderId, productEntity, quantity, unitPrice, specialNotes)` — Add line item
   - Validate order exists
   - Validate product exists
   - Check for duplicate (product not already in order)
   - Calculate subtotal
   - Return created ProductOrderEntity

2. `getProductOrderById(id)` — Get line item by ID
   - Return Optional<ProductOrderEntity>

3. `getProductsByOrderId(orderId)` — Get all line items for order
   - Return List<ProductOrderEntity>

4. `getProductOrderByOrderAndProduct(orderId, productId)` — Get specific line item
   - Return Optional<ProductOrderEntity>

5. `updateProductOrderQuantity(id, orderId, newQuantity)` — Update quantity
   - Verify authorization (product in order)
   - Recalculate subtotal
   - Return updated entity

6. `removeProductFromOrder(id, orderId)` — Remove line item
   - Verify authorization
   - Delete and return success

7. `removeAllProductsFromOrder(orderId)` — Clear order items
   - Delete all line items for order
   - Useful when order is cancelled

### Batch Operations
8. `addMultipleProducts(orderId, productList)` — Add multiple items at once
   - productList contains ProductOrderEntity objects with product, quantity, unitPrice, etc.
   - Validate all before adding
   - Return list of created line items

9. `updateProductsInOrder(orderId, updates)` — Update multiple quantities
   - Validate all before updating
   - Return updated list

### Query & Filtering
10. `getOrderLineItemCount(orderId)` — Count distinct products in order
11. `getProductsInOrder(orderId)` — Get product entities for order
12. `getOrderTotal(orderId)` — Sum of all subtotals
    - Calculate total price of order (sum subtotal column)
    - Useful for validation against Order.totalPrice

### Analytics & Reporting
13. `getMostOrderedProducts(int limit)` — Top products by order count
14. `getProductSalesInDateRange(productId, startDate, endDate)` — Product revenue
15. `getProductQuantityOrderedInRange(productId, startDate, endDate)` — Units sold

### Validation & Authorization
16. `validateLineItem(orderId, productOrderEntity)` [private] — Complete validation
17. `validateQuantity(quantity)` [private] — Quantity bounds (1-999)
18. `validatePrices(unitPrice, subtotal, quantity)` [private] — Price consistency
19. `verifyProductInOrder(orderId, productId)` [private] — Authorization

**Validation & Authorization:**
- Manual null checks in every method
- `validateLineItem()` [private]:
  - order exists
  - product exists
  - quantity: 1-999
  - unitPrice > 0
  - subtotal > 0 and equals quantity × unitPrice
  - product not already in order (unique constraint)
- Authorization: verify product belongs to order before modification
- Subtotal calculation: automatic on creation and updates

## REST API Endpoints (Future Implementation)

- `GET /api/orders/{orderId}/items` — Get order line items
- `POST /api/orders/{orderId}/items` — Add product to order
- `PUT /api/orders/{orderId}/items/{itemId}` — Update line item quantity
- `DELETE /api/orders/{orderId}/items/{itemId}` — Remove product from order
- `GET /api/orders/{orderId}/total` — Get order total price
- `GET /api/products/{productId}/sales` — Product sales data

## Testing Strategy

### Unit Tests (ProductOrderEntityTest)
- Entity instantiation with valid/invalid data
- Getter/setter functionality
- Subtotal calculation accuracy
- Validation annotations trigger correctly

### Service Tests (ProductOrderServiceTest)
- addProductToOrder with valid/invalid inputs
- Duplicate product prevention
- Quantity and price validation
- Subtotal calculation
- Order total accuracy
- Batch operations

### Integration Tests
- End-to-end order creation with multiple products
- Stock/inventory deduction (if implemented)
- Discount/coupon application (if implemented)
- Database integrity (foreign keys, unique constraints)

**Target:** 35+ tests (unit + integration)

## Acceptance Criteria

- ✅ ProductOrderEntity.java exists with all 8 fields
- ✅ @Entity, @Table(name="product_orders") with unique constraint applied
- ✅ All relationships (@ManyToOne) correctly configured with EAGER fetch
- ✅ @JoinColumn annotations specify correct FK names (order_id, product_id)
- ✅ @CreationTimestamp, @UpdateTimestamp annotations applied
- ✅ @Positive, @Size, @NotNull, @Min, @Max annotations on appropriate fields
- ✅ Unique constraint (order_id, product_id) enforced at database level
- ✅ ProductOrderRepository with 15+ query methods
- ✅ ProductOrderService with 20+ business logic methods
- ✅ Manual null validation in all service methods
- ✅ Authorization checks (product in order verification)
- ✅ Subtotal calculation (quantity × unitPrice) working correctly
- ✅ mvn clean compile runs without errors
- ✅ Spring Boot starts without Hibernate warnings
- ✅ DBeaver shows product_orders table with correct relationships
- ✅ Unique constraint prevents duplicate line items in same order

## Implementation Notes

1. **Line Item vs Order:** ProductOrder is a join table; it links Order to Product with additional context
2. **Price Snapshot:** unitPrice stores the price at time of order, not product's current price (important for historical accuracy)
3. **Subtotal Calculation:** Always equals quantity × unitPrice; verify consistency on updates
4. **Unique Constraint:** (order_id, product_id) prevents adding same product twice to order
5. **Cascade Delete:** Use ON DELETE CASCADE for order (deleting order removes all line items)
6. **ProductEntity Future:** ProductEntity is not yet created; will be implemented in subsequent feature
7. **Eager Loading:** Use FetchType.EAGER for order and product to avoid N+1 queries
8. **Stock/Inventory:** Deduction logic belongs in OrderService, not ProductOrderService
9. **Order Total Validation:** Verify Order.totalPrice equals sum of ProductOrder.subtotal
10. **Special Notes:** Item-level customization (e.g., extra sauce, no onions, special preparation)

## Future Considerations

- Product inventory/stock management
- Price history tracking (why unitPrice is stored)
- Discount/coupon application per item
- Item refund/replacement logic
- Product ratings after delivery (link to this line item)
- Allergen/dietary restriction tracking
- Bundle/combo handling

## Dependencies

- **OrderEntity:** Must exist (foreign key reference)
- **ProductEntity:** Will be created in next feature (product-schema)
- **Database:** MySQL 8.0+ with InnoDB (supports foreign keys and constraints)
- **Spring Data JPA:** For automatic repository implementation
- **Hibernate:** For entity mapping and validation

