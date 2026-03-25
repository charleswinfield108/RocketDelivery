package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.OrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderStatusRepository provides database access for OrderStatusEntity.
 *
 * Manages CRUD operations and custom queries for order status reference data.
 * Since OrderStatusEntity is a lookup/reference table, it is typically not deleted
 * but rather marked inactive via isActive flag for historical tracking.
 *
 * Standard pre-populated statuses:
 * - PENDING (1)
 * - CONFIRMED (2)
 * - PREPARING (3)
 * - READY (4)
 * - OUT_FOR_DELIVERY (5)
 * - DELIVERED (6)
 * - CANCELLED (7)
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatusEntity, Long> {

    // ==================== Basic Lookup Methods ====================

    /**
     * Find a status by its machine-readable status code.
     * Status codes are unique identifiers like "PENDING", "CONFIRMED", etc.
     *
     * @param statusCode the machine-readable status code (e.g., "PENDING")
     * @return Optional containing the OrderStatusEntity if found, empty otherwise
     */
    Optional<OrderStatusEntity> findByStatusCode(String statusCode);

    /**
     * Find a status by its user-friendly display name.
     * Status names are unique identifiers like "Pending", "Confirmed", etc.
     *
     * @param statusName the user-friendly status name (e.g., "Pending")
     * @return Optional containing the OrderStatusEntity if found, empty otherwise
     */
    Optional<OrderStatusEntity> findByStatusName(String statusName);

    /**
     * Find an active status by its code.
     * This is the preferred lookup method as it respects the soft-delete flag.
     *
     * @param statusCode the machine-readable status code
     * @return Optional containing the active OrderStatusEntity if found, empty otherwise
     */
    Optional<OrderStatusEntity> findByStatusCodeAndIsActive(String statusCode, Boolean isActive);

    /**
     * Find an active status by its name.
     * This is the preferred lookup method as it respects the soft-delete flag.
     *
     * @param statusName the user-friendly status name
     * @return Optional containing the active OrderStatusEntity if found, empty otherwise
     */
    Optional<OrderStatusEntity> findByStatusNameAndIsActive(String statusName, Boolean isActive);

    // ==================== List/Query Methods ====================

    /**
     * Get all active statuses ordered by display order.
     * Used to populate UI dropdowns and lists in the correct sequence.
     *
     * @return List of all active OrderStatusEntity instances sorted by displayOrder ascending
     */
    List<OrderStatusEntity> findByIsActiveOrderByDisplayOrder(Boolean isActive);

    /**
     * Get all active statuses (without specific ordering).
     *
     * @return List of all active OrderStatusEntity instances
     */
    List<OrderStatusEntity> findByIsActive(Boolean isActive);

    /**
     * Get all statuses (both active and inactive) for administrative purposes.
     * Sorted by display order for consistency.
     *
     * @return List of all OrderStatusEntity instances
     */
    @Query("SELECT s FROM OrderStatusEntity s ORDER BY s.displayOrder ASC")
    List<OrderStatusEntity> getAllStatusesOrderedByDisplay();

    // ==================== Terminal Status Methods ====================

    /**
     * Get all terminal statuses (DELIVERED and CANCELLED).
     * Terminal statuses are endpoint statuses with no further transitions.
     * Useful for filtering completed/closed orders.
     *
     * @return List of terminal OrderStatusEntity instances
     */
    @Query("SELECT s FROM OrderStatusEntity s WHERE s.statusCode IN ('DELIVERED', 'CANCELLED') AND s.isActive = true ORDER BY s.displayOrder ASC")
    List<OrderStatusEntity> getTerminalStatuses();

    /**
     * Check if a status code represents a terminal status.
     * Terminal statuses: DELIVERED and CANCELLED
     *
     * @param statusCode the status code to check
     * @return 1 if terminal, 0 otherwise
     */
    @Query("SELECT COUNT(s) FROM OrderStatusEntity s WHERE s.statusCode IN ('DELIVERED', 'CANCELLED') AND s.statusCode = :statusCode AND s.isActive = true")
    long isTerminalStatus(@Param("statusCode") String statusCode);

    // ==================== Transition Validation Methods ====================

    /**
     * Get all non-terminal statuses.
     * These are statuses that can transition to other statuses.
     * Statuses: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY
     *
     * @return List of non-terminal OrderStatusEntity instances
     */
    @Query("SELECT s FROM OrderStatusEntity s WHERE s.statusCode NOT IN ('DELIVERED', 'CANCELLED') AND s.isActive = true ORDER BY s.displayOrder ASC")
    List<OrderStatusEntity> getTransitionableStatuses();

    /**
     * Get valid next statuses that can follow the given current status code.
     * Validates the order workflow transitions.
     *
     * Valid transitions:
     * - PENDING → CONFIRMED, CANCELLED
     * - CONFIRMED → PREPARING, CANCELLED
     * - PREPARING → READY
     * - READY → OUT_FOR_DELIVERY
     * - OUT_FOR_DELIVERY → DELIVERED
     * - DELIVERED, CANCELLED → (no transitions)
     *
     * @param currentStatusCode the current status code
     * @return List of valid next statuses
     */
    @Query("""
        SELECT s FROM OrderStatusEntity s
        WHERE s.isActive = true AND (
            (:currentStatus = 'PENDING' AND s.statusCode IN ('CONFIRMED', 'CANCELLED')) OR
            (:currentStatus = 'CONFIRMED' AND s.statusCode IN ('PREPARING', 'CANCELLED')) OR
            (:currentStatus = 'PREPARING' AND s.statusCode = 'READY') OR
            (:currentStatus = 'READY' AND s.statusCode = 'OUT_FOR_DELIVERY') OR
            (:currentStatus = 'OUT_FOR_DELIVERY' AND s.statusCode = 'DELIVERED')
        )
        ORDER BY s.displayOrder ASC
    """)
    List<OrderStatusEntity> getValidNextStatuses(@Param("currentStatus") String currentStatusCode);

    /**
     * Check if a transition from currentStatus to nextStatus is valid.
     * Returns the count: 1 if valid, 0 if invalid.
     *
     * @param currentStatusCode the current status code
     * @param nextStatusCode the target status code
     * @return count (1 if valid transition, 0 otherwise)
     */
    @Query("""
        SELECT COUNT(s) FROM OrderStatusEntity s
        WHERE s.isActive = true AND (
            (:currentStatus = 'PENDING' AND :nextStatus IN ('CONFIRMED', 'CANCELLED')) OR
            (:currentStatus = 'CONFIRMED' AND :nextStatus IN ('PREPARING', 'CANCELLED')) OR
            (:currentStatus = 'PREPARING' AND :nextStatus = 'READY') OR
            (:currentStatus = 'READY' AND :nextStatus = 'OUT_FOR_DELIVERY') OR
            (:currentStatus = 'OUT_FOR_DELIVERY' AND :nextStatus = 'DELIVERED')
        )
    """)
    long isValidTransition(@Param("currentStatus") String currentStatusCode, @Param("nextStatus") String nextStatusCode);

    // ==================== Existence Check Methods ====================

    /**
     * Check if a status with the given code exists and is active.
     *
     * @param statusCode the status code to check
     * @return true if status exists and is active, false otherwise
     */
    boolean existsByStatusCodeAndIsActive(String statusCode, Boolean isActive);

    /**
     * Check if a status with the given name exists and is active.
     *
     * @param statusName the status name to check
     * @return true if status exists and is active, false otherwise
     */
    boolean existsByStatusNameAndIsActive(String statusName, Boolean isActive);

    /**
     * Check if a status code exists (regardless of active status).
     *
     * @param statusCode the status code to check
     * @return true if status exists, false otherwise
     */
    boolean existsByStatusCode(String statusCode);

    // ==================== Count Methods ====================

    /**
     * Get the count of all active statuses.
     *
     * @return number of active statuses
     */
    long countByIsActive(Boolean isActive);

    /**
     * Get count of terminal statuses.
     *
     * @return number of terminal statuses (DELIVERED and CANCELLED)
     */
    @Query("SELECT COUNT(s) FROM OrderStatusEntity s WHERE s.statusCode IN ('DELIVERED', 'CANCELLED') AND s.isActive = true")
    long countTerminalStatuses();

    // ==================== Display/UI Methods ====================

    /**
     * Get the display order of a specific status.
     * Used for UI rendering and sorting.
     *
     * @param statusCode the status code
     * @return the display order Integer, or null if not found
     */
    @Query("SELECT s.displayOrder FROM OrderStatusEntity s WHERE s.statusCode = :statusCode AND s.isActive = true")
    Integer getDisplayOrder(@Param("statusCode") String statusCode);

    /**
     * Find a status by display order.
     * Useful for cycling through status options or sequential display.
     *
     * @param displayOrder the display order
     * @return Optional containing the OrderStatusEntity if found
     */
    Optional<OrderStatusEntity> findByDisplayOrderAndIsActive(Integer displayOrder, Boolean isActive);

    /**
     * Find the next status by display order.
     * Used for advancing to the next sequential status.
     *
     * @param displayOrder the current display order
     * @return List of statuses with higher display order (should typically have 1 result)
     */
    @Query("SELECT s FROM OrderStatusEntity s WHERE s.displayOrder > :displayOrder AND s.isActive = true ORDER BY s.displayOrder ASC LIMIT 1")
    Optional<OrderStatusEntity> findNextByDisplayOrder(@Param("displayOrder") Integer displayOrder);

    // ==================== Status Factory Methods ====================

    /**
     * Get the PENDING status.
     *
     * @return Optional containing the PENDING status if active
     */
    default Optional<OrderStatusEntity> getPendingStatus() {
        return findByStatusCodeAndIsActive("PENDING", true);
    }

    /**
     * Get the CONFIRMED status.
     *
     * @return Optional containing the CONFIRMED status if active
     */
    default Optional<OrderStatusEntity> getConfirmedStatus() {
        return findByStatusCodeAndIsActive("CONFIRMED", true);
    }

    /**
     * Get the PREPARING status.
     *
     * @return Optional containing the PREPARING status if active
     */
    default Optional<OrderStatusEntity> getPreparingStatus() {
        return findByStatusCodeAndIsActive("PREPARING", true);
    }

    /**
     * Get the READY status.
     *
     * @return Optional containing the READY status if active
     */
    default Optional<OrderStatusEntity> getReadyStatus() {
        return findByStatusCodeAndIsActive("READY", true);
    }

    /**
     * Get the OUT_FOR_DELIVERY status.
     *
     * @return Optional containing the OUT_FOR_DELIVERY status if active
     */
    default Optional<OrderStatusEntity> getOutForDeliveryStatus() {
        return findByStatusCodeAndIsActive("OUT_FOR_DELIVERY", true);
    }

    /**
     * Get the DELIVERED status.
     *
     * @return Optional containing the DELIVERED status if active
     */
    default Optional<OrderStatusEntity> getDeliveredStatus() {
        return findByStatusCodeAndIsActive("DELIVERED", true);
    }

    /**
     * Get the CANCELLED status.
     *
     * @return Optional containing the CANCELLED status if active
     */
    default Optional<OrderStatusEntity> getCancelledStatus() {
        return findByStatusCodeAndIsActive("CANCELLED", true);
    }

}
