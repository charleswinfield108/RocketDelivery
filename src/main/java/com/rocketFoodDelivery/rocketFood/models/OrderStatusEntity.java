package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * OrderStatusEntity represents a status type in the order lifecycle.
 *
 * This entity serves as a master reference/lookup table defining all possible
 * order statuses (PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)
 * with descriptions and display metadata.
 *
 * Key features:
 * - Reference/lookup table with pre-populated standard statuses
 * - Unique statusCode (machine-readable) and statusName (user-friendly)
 * - Display ordering for UI dropdown/list presentation
 * - Soft-delete capability via isActive flag
 * - Immutable reference data (typically not modified at runtime)
 *
 * Status Workflow:
 * PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED (terminal)
 *       ↓ (alternative)
 *    CANCELLED (terminal)
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Entity
@Table(
    name = "order_statuses",
    indexes = {
        @Index(name = "idx_status_code", columnList = "status_code"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_display_order", columnList = "display_order")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderStatusEntity {

    /**
     * Unique primary key for the order status.
     * Auto-generated using MySQL AUTO_INCREMENT.
     * Immutable after creation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Primary user-friendly name for the order status (schema requirement: name).
     * Shown to customers in order tracking and UI dropdowns.
     * Examples: "Pending", "Confirmed", "Preparing", "Ready", "Out for Delivery", "Delivered", "Cancelled"
     * Must be unique and descriptive.
     * Immutable reference data.
     */
    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotNull(message = "Status name cannot be null")
    @Size(min = 1, max = 100, message = "Status name must be between 1 and 100 characters")
    private String name;

    /**
     * Machine-readable status code identifier.
     * Used internally by services for status checks and validations.
     * Examples: "PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
     * Must be unique, uppercase with underscores (convention).
     * Immutable reference data.
     */
    @Column(name = "status_code", unique = true, nullable = false, length = 50)
    @NotNull(message = "Status code cannot be null")
    @Size(min = 1, max = 50, message = "Status code must be between 1 and 50 characters")
    private String statusCode;

    /**
     * User-friendly display name for the status (kept for backward compatibility).
     * Mirrors the primary 'name' field for seamless integration.
     * Shown to customers in order tracking and UI dropdowns.
     * Examples: "Pending", "Confirmed", "Preparing", "Ready", "Out for Delivery", "Delivered", "Cancelled"
     * Must be unique; allows customers to identify order state.
     * Note: 'name' field is the schema-required primary field.
     * Immutable reference data.
     */
    @Column(name = "status_name", unique = true, nullable = false, length = 100)
    @NotNull(message = "Status name cannot be null")
    @Size(min = 1, max = 100, message = "Status name must be between 1 and 100 characters")
    private String statusName;

    /**
     * Detailed description explaining the status.
     * Provides context for staff and can be displayed to users.
     * Examples:
     * - "Order received, awaiting confirmation"
     * - "Restaurant is preparing the order"
     * - "Order is ready for pickup/delivery"
     * Optional field; helps clarify status intent.
     */
    @Column(nullable = true, length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Display order for UI presentation.
     * Lower numbers appear first in dropdowns and lists.
     * Range: 1-10 (standard statuses use 1-7, leaving room for custom statuses)
     * Used for sorting in SELECT statements and UI rendering.
     * Examples: PENDING=1, CONFIRMED=2, PREPARING=3, READY=4, OUT_FOR_DELIVERY=5, DELIVERED=6, CANCELLED=7
     */
    @Column(name = "display_order", nullable = false)
    @NotNull(message = "Display order cannot be null")
    @Min(value = 1, message = "Display order must be at least 1")
    @Max(value = 10, message = "Display order cannot exceed 10")
    private Integer displayOrder;

    /**
     * Flag indicating whether this status is currently available/active.
     * true = status is available for use in order workflow
     * false = status is deprecated/disabled (soft-delete)
     * Enables backward compatibility without losing historical data.
     * Default: true
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active flag cannot be null")
    private Boolean isActive = true;

    /**
     * Timestamp when this status record was created.
     * Auto-set to current timestamp via @CreationTimestamp.
     * Not updatable after creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this status record was last updated.
     * Auto-updated via @UpdateTimestamp on any modification.
     * Initially null; set on first update.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    // ==================== Helper Methods ====================

    /**
     * Check if this is a terminal status (no further transitions possible).
     * Terminal statuses are DELIVERED and CANCELLED.
     *
     * @return true if status is DELIVERED or CANCELLED, false otherwise
     */
    public boolean isTerminalStatus() {
        return "DELIVERED".equals(statusCode) || "CANCELLED".equals(statusCode);
    }

    /**
     * Check if this is the PENDING status.
     *
     * @return true if statusCode equals "PENDING", false otherwise
     */
    public boolean isPending() {
        return "PENDING".equals(statusCode);
    }

    /**
     * Check if this is the CONFIRMED status.
     *
     * @return true if statusCode equals "CONFIRMED", false otherwise
     */
    public boolean isConfirmed() {
        return "CONFIRMED".equals(statusCode);
    }

    /**
     * Check if this is the PREPARING status.
     *
     * @return true if statusCode equals "PREPARING", false otherwise
     */
    public boolean isPreparing() {
        return "PREPARING".equals(statusCode);
    }

    /**
     * Check if this is the READY status.
     *
     * @return true if statusCode equals "READY", false otherwise
     */
    public boolean isReady() {
        return "READY".equals(statusCode);
    }

    /**
     * Check if this is the OUT_FOR_DELIVERY status.
     *
     * @return true if statusCode equals "OUT_FOR_DELIVERY", false otherwise
     */
    public boolean isOutForDelivery() {
        return "OUT_FOR_DELIVERY".equals(statusCode);
    }

    /**
     * Check if this is the DELIVERED status.
     *
     * @return true if statusCode equals "DELIVERED", false otherwise
     */
    public boolean isDelivered() {
        return "DELIVERED".equals(statusCode);
    }

    /**
     * Check if this is the CANCELLED status.
     *
     * @return true if statusCode equals "CANCELLED", false otherwise
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(statusCode);
    }

    /**
     * Get a formatted display string combining name and description.
     * Useful for UI rendering and logging.
     *
     * @return formatted string: "StatusName (description)" or just "StatusName" if no description
     */
    public String getDisplayText() {
        if (description == null || description.trim().isEmpty()) {
            return statusName;
        }
        return statusName + " (" + description + ")";
    }

    /**
     * Validate that this status can transition to another status.
     * Enforces the order workflow rules.
     *
     * Valid transitions:
     * - PENDING → CONFIRMED, CANCELLED
     * - CONFIRMED → PREPARING, CANCELLED
     * - PREPARING → READY
     * - READY → OUT_FOR_DELIVERY
     * - OUT_FOR_DELIVERY → DELIVERED
     * - DELIVERED → (terminal, no transitions)
     * - CANCELLED → (terminal, no transitions)
     *
     * @param nextStatus the target status to transition to
     * @return true if transition is valid, false otherwise
     * @throws IllegalArgumentException if either status is invalid
     */
    public boolean canTransitionTo(OrderStatusEntity nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Next status cannot be null");
        }

        // Terminal statuses cannot transition
        if (this.isTerminalStatus()) {
            return false;
        }

        // Check valid transitions
        return switch (this.statusCode) {
            case "PENDING" -> "CONFIRMED".equals(nextStatus.statusCode) || "CANCELLED".equals(nextStatus.statusCode);
            case "CONFIRMED" -> "PREPARING".equals(nextStatus.statusCode) || "CANCELLED".equals(nextStatus.statusCode);
            case "PREPARING" -> "READY".equals(nextStatus.statusCode);
            case "READY" -> "OUT_FOR_DELIVERY".equals(nextStatus.statusCode);
            case "OUT_FOR_DELIVERY" -> "DELIVERED".equals(nextStatus.statusCode);
            default -> false;
        };
    }

    /**
     * Get the next valid status in the standard workflow.
     * Returns the single valid next status if one exists, null if terminal state.
     *
     * Note: Some statuses have multiple valid next statuses (e.g., PENDING can go to CONFIRMED or CANCELLED).
     * This method returns only the "happy path" next status.
     *
     * @return the next status code in workflow, or null if terminal
     */
    public String getNextStatusInWorkflow() {
        return switch (this.statusCode) {
            case "PENDING" -> "CONFIRMED";
            case "CONFIRMED" -> "PREPARING";
            case "PREPARING" -> "READY";
            case "READY" -> "OUT_FOR_DELIVERY";
            case "OUT_FOR_DELIVERY" -> "DELIVERED";
            default -> null; // Terminal status
        };
    }

    /**
     * Get all valid next status codes for this status.
     * Includes all possible transitions (including CANCELLED options).
     *
     * @return array of valid next status codes
     */
    public String[] getAllValidNextStatuses() {
        return switch (this.statusCode) {
            case "PENDING" -> new String[]{"CONFIRMED", "CANCELLED"};
            case "CONFIRMED" -> new String[]{"PREPARING", "CANCELLED"};
            case "PREPARING" -> new String[]{"READY"};
            case "READY" -> new String[]{"OUT_FOR_DELIVERY"};
            case "OUT_FOR_DELIVERY" -> new String[]{"DELIVERED"};
            default -> new String[]{}; // Terminal status
        };
    }

}
