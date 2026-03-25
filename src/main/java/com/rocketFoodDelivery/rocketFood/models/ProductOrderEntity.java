package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductOrderEntity represents a line item within a customer order.
 *
 * This entity acts as a join table between orders and products, tracking what items
 * were ordered, quantities, unit prices (snapshot at order time), and calculated subtotals.
 *
 * Key features:
 * - Many-to-One relationships to Order and Product entities
 * - Price snapshot: unitPrice stored for historical accuracy (not current product price)
 * - Subtotal calculation: quantity × unitPrice (verified on creation/update)
 * - Special notes: item-level customization (e.g., "extra spicy", "no onions")
 * - Unique constraint: prevent duplicate products in same order
 *
 * Example: An order contains 2 ProductOrder items:
 *   - ProductOrder 1: Pizza (qty=2, unitPrice=$15.99, subtotal=$31.98)
 *   - ProductOrder 2: Drink (qty=1, unitPrice=$2.49, subtotal=$2.49)
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Entity
@Table(
    name = "product_orders",
    indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_order_product", columnNames = {"order_id", "product_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "product"})
public class ProductOrderEntity {

    /**
     * Unique primary key for the product order line item.
     * Auto-generated using MySQL AUTO_INCREMENT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent order containing this line item.
     * Many-to-One relationship: multiple line items per order.
     * Eager loading to avoid N+1 queries when accessing order details.
     * Cascade delete: when order is deleted, all its line items are deleted.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order cannot be null")
    private OrderEntity order;

    /**
     * The product being ordered in this line item.
     * Many-to-One relationship: multiple line items can reference the same product.
     * Eager loading to avoid N+1 queries when accessing product details.
     * Do NOT cascade delete: products should persist even if removed from order (for history).
     *
     * Note: ProductEntity will be created in a future feature.
     * Currently referenced as generic entity; will be fully typed when ProductEntity exists.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product cannot be null")
    private Object product; // TODO: Change to ProductEntity when product-schema feature is complete

    /**
     * The number of units of this product ordered.
     * Must be between 1 and 999 (server-side business constraint).
     * Cannot be zero or negative.
     */
    @Column(nullable = false)
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    private Integer quantity;

    /**
     * The price per unit at the time the order was placed.
     * This is a snapshot of the product's price at that moment, not the current price.
     * Important for historical accuracy: if product price changes, existing orders preserve original price.
     * Stored as DECIMAL(10,2) for precise monetary calculations.
     * Must be positive (> 0).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price cannot be null")
    @Positive(message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    /**
     * The total price for this line item: quantity × unitPrice.
     * Automatically calculated and stored for query efficiency.
     * Should always equal: unitPrice.multiply(new BigDecimal(quantity))
     * Must be positive (> 0).
     *
     * This field is redundant mathematically but improves query performance
     * (e.g., summing order totals without multiplication in queries).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Subtotal cannot be null")
    @Positive(message = "Subtotal must be greater than 0")
    private BigDecimal subtotal;

    /**
     * Item-specific customization notes from the customer.
     * Examples: "extra spicy", "no onions", "gluten-free bread", "on the side", "light sauce"
     * Maximum 500 characters.
     * Optional field; can be null if no special preparation needed.
     */
    @Column(nullable = true, length = 500)
    @Size(max = 500, message = "Special notes must not exceed 500 characters")
    private String specialNotes;

    /**
     * Timestamp when this line item was added to the order.
     * Auto-set to current timestamp via @CreationTimestamp.
     * Not updatable after creation.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @NotNull(message = "Created timestamp cannot be null")
    private LocalDateTime createdAt;

    /**
     * Timestamp when this line item was last updated.
     * Auto-updated via @UpdateTimestamp on any modification (e.g., quantity change).
     * Initially null; set on first update.
     */
    @UpdateTimestamp
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // ==================== Helper Methods ====================

    /**
     * Recalculate the subtotal based on current quantity and unitPrice.
     * Formula: subtotal = quantity × unitPrice
     * Called after quantity or unitPrice changes to keep subtotal in sync.
     *
     * @throws IllegalArgumentException if calculation results in invalid value
     */
    public void recalculateSubtotal() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive to calculate subtotal");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive to calculate subtotal");
        }

        this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
    }

    /**
     * Validate that the line item data is consistent and logically correct.
     * Checks:
     * - quantity is 1-999
     * - unitPrice and subtotal are positive
     * - subtotal equals quantity × unitPrice (within rounding tolerance)
     *
     * @return true if all validations pass
     * @throws IllegalArgumentException if any validation fails
     */
    public boolean isValidLineItem() {
        if (quantity == null || quantity < 1 || quantity > 999) {
            throw new IllegalArgumentException("Quantity must be between 1 and 999");
        }

        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0");
        }

        if (subtotal == null || subtotal.signum() <= 0) {
            throw new IllegalArgumentException("Subtotal must be greater than 0");
        }

        // Verify subtotal calculation (allow small rounding errors)
        BigDecimal expectedSubtotal = unitPrice.multiply(new BigDecimal(quantity));
        if (subtotal.compareTo(expectedSubtotal) != 0) {
            throw new IllegalArgumentException(
                "Subtotal mismatch: expected " + expectedSubtotal + " but got " + subtotal
            );
        }

        return true;
    }

    /**
     * Get the item total (alias for subtotal for semantic clarity).
     *
     * @return the subtotal (quantity × unitPrice)
     */
    public BigDecimal getItemTotal() {
        return subtotal;
    }

    /**
     * Get a human-readable description of this line item.
     * Format: "{quantity} × Product (${unitPrice} each) = ${subtotal}"
     *
     * @return human-readable description
     */
    public String getItemDescription() {
        return String.format("%d × Product ($%.2f each) = $%.2f",
            quantity,
            unitPrice,
            subtotal
        );
    }

    /**
     * Update the quantity and automatically recalculate subtotal.
     * The new quantity is validated before being set.
     *
     * @param newQuantity the new quantity (must be 1-999)
     * @throws IllegalArgumentException if newQuantity is invalid
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1 || newQuantity > 999) {
            throw new IllegalArgumentException("Quantity must be between 1 and 999");
        }

        this.quantity = newQuantity;
        recalculateSubtotal();
    }

    /**
     * Update the unit price and automatically recalculate subtotal.
     * The new price is validated before being set.
     *
     * @param newUnitPrice the new unit price (must be > 0)
     * @throws IllegalArgumentException if newUnitPrice is invalid
     */
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0");
        }

        this.unitPrice = newUnitPrice;
        recalculateSubtotal();
    }

    /**
     * Check if the line item can be modified.
     * Generally, items can be modified while the parent order is in PENDING status.
     * Once order is confirmed or in preparation, items cannot be changed.
     *
     * @return true if order parent status allows modifications, false otherwise
     */
    public boolean canBeModified() {
        if (order == null) {
            return false;
        }
        return order.canBeModified();
    }

    /**
     * Get the total discount/savings for this line item.
     * If a discounted price was applied, returns the difference between
     * the standard unit price and the discounted unit price.
     *
     * Note: This is a placeholder for future discount feature.
     * Currently returns 0.
     *
     * @return discount amount for this line item
     */
    public BigDecimal getDiscountAmount() {
        // TODO: Implement when discount feature is added
        return BigDecimal.ZERO;
    }

    /**
     * Calculate and return the effective unit price after discounts.
     * Currently returns the unitPrice (no discounts implemented yet).
     *
     * @return the effective price per unit
     */
    public BigDecimal getEffectiveUnitPrice() {
        return unitPrice.subtract(getDiscountAmount());
    }

    /**
     * Check if this line item has special preparation instructions.
     *
     * @return true if specialNotes is not null and not empty
     */
    public boolean hasSpecialNotes() {
        return specialNotes != null && !specialNotes.trim().isEmpty();
    }

    /**
     * Get the number of minutes this line item has been in the order.
     * Useful for tracking preparation time.
     *
     * @return number of minutes since createdAt, or -1 if createdAt not set
     */
    public long getMinutesInOrder() {
        if (createdAt == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
    }
}
