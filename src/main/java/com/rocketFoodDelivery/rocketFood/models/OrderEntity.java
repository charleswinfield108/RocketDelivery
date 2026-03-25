package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderEntity represents a customer order in the RocketDelivery system.
 *
 * An order links a customer to a restaurant, tracks the order through its lifecycle,
 * and maintains delivery information. Orders use:
 * - Many-to-One relationships to Customer, Restaurant, and Address entities
 * - Status tracking (PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED/CANCELLED)
 * - BigDecimal for precise monetary calculations
 * - Automatic timestamp management via Hibernate annotations
 *
 * Key features:
 * - Unique order number for customer reference
 * - Total price with @Positive validation
 * - Estimated and actual delivery times for tracking
 * - Special instructions for delivery customization
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_order_number", columnList = "order_number"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_address_id", columnList = "address_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_order_date", columnList = "order_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "restaurant", "deliveryAddress"})
public class OrderEntity {

    /**
     * Unique primary key for the order.
     * Auto-generated using MySQL AUTO_INCREMENT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable, unique order identifier (e.g., "ORD-20260325-001234").
     * Used for customer reference and support inquiries.
     */
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    @NotNull(message = "Order number cannot be null")
    @Size(min = 1, max = 50, message = "Order number must be between 1 and 50 characters")
    private String orderNumber;

    /**
     * The customer who placed this order.
     * Many-to-One relationship: multiple orders per customer.
     * Eager loading to avoid N+1 queries when accessing customer details.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer cannot be null")
    private CustomerEntity customer;

    /**
     * The restaurant fulfilling this order.
     * Many-to-One relationship: multiple orders per restaurant.
     * Eager loading to avoid N+1 queries when accessing restaurant details.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @NotNull(message = "Restaurant cannot be null")
    private RestaurantEntity restaurant;

    /**
     * The address where the order will be delivered.
     * Many-to-One relationship: multiple orders can use the same address.
     * Eager loading to avoid N+1 queries when accessing address details.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    @NotNull(message = "Delivery address cannot be null")
    private AddressEntity deliveryAddress;

    /**
     * Timestamp when the order was created (placed).
     * Auto-set to current timestamp via @CreationTimestamp.
     * Not updatable after creation.
     */
    @CreationTimestamp
    @Column(name = "order_date", nullable = false, updatable = false)
    @NotNull(message = "Order date cannot be null")
    private LocalDateTime orderDate;

    /**
     * Current status of the order in its lifecycle.
     * Valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
     * Default value "PENDING" is set in service layer during order creation.
     *
     * Status workflow:
     * - PENDING: Order created, awaiting confirmation
     * - CONFIRMED: Customer confirmed, restaurant notified
     * - PREPARING: Restaurant is preparing the order
     * - READY: Order ready for pickup/delivery
     * - OUT_FOR_DELIVERY: Driver is delivering (if applicable)
     * - DELIVERED: Order successfully delivered
     * - CANCELLED: Order was cancelled
     */
    @Column(nullable = false, length = 50)
    @NotNull(message = "Status cannot be null")
    @Size(min = 1, max = 50, message = "Status must be between 1 and 50 characters")
    private String status;

    /**
     * Total price of the order in the platform's currency.
     * Stored as DECIMAL(10,2) for precise monetary calculations.
     * Includes all items, taxes, and discounts.
     * Must be positive (> 0).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price cannot be null")
    @Positive(message = "Total price must be greater than 0")
    private BigDecimal totalPrice;

    /**
     * Estimated time when the order will be delivered.
     * Typically set when order is confirmed (30-60 minutes from order date).
     * Optional; initially null.
     */
    @Column(name = "estimated_delivery_time", nullable = true)
    private LocalDateTime estimatedDeliveryTime;

    /**
     * Actual time when the order was delivered.
     * Set when order status changes to DELIVERED.
     * Optional; remains null until delivery is completed.
     */
    @Column(name = "actual_delivery_time", nullable = true)
    private LocalDateTime actualDeliveryTime;

    /**
     * Special instructions from the customer for delivery or food preparation.
     * Examples: "No onions", "Extra sauce", "Leave at door", "Ring doorbell twice"
     * Maximum 500 characters.
     * Optional field.
     */
    @Column(name = "special_instructions", nullable = true, length = 500)
    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;

    /**
     * Timestamp when this order record was created in the database.
     * Auto-set via @CreationTimestamp.
     * Not updatable after creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull(message = "Created timestamp cannot be null")
    private LocalDateTime createdAt;

    /**
     * Timestamp when this order record was last updated.
     * Auto-updated via @UpdateTimestamp on any modification.
     * Initially null; set on first update.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    // ==================== Helper Methods ====================

    /**
     * Check if the order has been delivered.
     *
     * @return true if actualDeliveryTime is set (order delivered), false otherwise
     */
    public boolean isDelivered() {
        return actualDeliveryTime != null;
    }

    /**
     * Check if the order is in PENDING status.
     *
     * @return true if status equals "PENDING", false otherwise
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * Check if the order is in CANCELLED status.
     *
     * @return true if status equals "CANCELLED", false otherwise
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }

    /**
     * Check if the order can still be modified (not yet confirmed or in progress).
     * Orders can only be modified in PENDING status.
     *
     * @return true if status is PENDING, false otherwise
     */
    public boolean canBeModified() {
        return isPending();
    }

    /**
     * Check if the order is out for delivery or has been delivered.
     *
     * @return true if status is OUT_FOR_DELIVERY or DELIVERED, false otherwise
     */
    public boolean isInDelivery() {
        return "OUT_FOR_DELIVERY".equals(this.status) || "DELIVERED".equals(this.status);
    }

    /**
     * Set the estimated delivery time for this order.
     * Typically called when order is confirmed.
     *
     * @param estimatedTime the estimated delivery timestamp
     */
    public void setDeliveryEstimate(LocalDateTime estimatedTime) {
        this.estimatedDeliveryTime = estimatedTime;
    }

    /**
     * Mark the order as delivered by setting actualDeliveryTime to now.
     * Called when order status changes to DELIVERED.
     */
    public void completeDelivery() {
        this.actualDeliveryTime = LocalDateTime.now();
    }

    /**
     * Get the delivery time estimate in minutes from order placement.
     * Returns difference between estimatedDeliveryTime and orderDate.
     *
     * @return number of minutes until estimated delivery, or -1 if not set
     */
    public long getEstimatedDeliveryMinutes() {
        if (estimatedDeliveryTime == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(orderDate, estimatedDeliveryTime);
    }

    /**
     * Get the actual delivery time in minutes from order placement.
     * Returns difference between actualDeliveryTime and orderDate.
     * Only meaningful after order is delivered.
     *
     * @return number of minutes taken for delivery, or -1 if not delivered
     */
    public long getActualDeliveryMinutes() {
        if (actualDeliveryTime == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(orderDate, actualDeliveryTime);
    }
}
