package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.OrderStatusEntity;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * OrderStatusService provides business logic for order status management.
 *
 * Handles:
 * - CRUD operations on OrderStatusEntity
 * - Status lookup and factory methods
 * - Status transition validation
 * - Reference data initialization
 * - Display/UI helper methods
 *
 * Note: OrderStatusEntity is a reference/lookup table with pre-populated standard statuses.
 * Typical operations are reads and status transitions, not creation of new statuses.
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    // ==================== CRUD Operations ====================

    /**
     * Get an order status by its ID.
     *
     * @param id the status ID
     * @return Optional containing the OrderStatusEntity if found
     * @throws IllegalArgumentException if id is null
     */
    public Optional<OrderStatusEntity> getOrderStatusById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Status ID cannot be null");
        }
        return orderStatusRepository.findById(id);
    }

    /**
     * Get an active order status by its machine-readable code.
     * Preferred lookup method as it respects the soft-delete flag.
     *
     * @param statusCode the status code (e.g., "PENDING")
     * @return Optional containing the active OrderStatusEntity if found
     * @throws IllegalArgumentException if statusCode is null or empty
     */
    public Optional<OrderStatusEntity> getStatusByCode(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Status code cannot be null or empty");
        }
        return orderStatusRepository.findByStatusCodeAndIsActive(statusCode.trim(), true);
    }

    /**
     * Get an active order status by its user-friendly name.
     * Preferred lookup method as it respects the soft-delete flag.
     *
     * @param statusName the status name (e.g., "Pending")
     * @return Optional containing the active OrderStatusEntity if found
     * @throws IllegalArgumentException if statusName is null or empty
     */
    public Optional<OrderStatusEntity> getStatusByName(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }
        return orderStatusRepository.findByStatusNameAndIsActive(statusName.trim(), true);
    }

    /**
     * Create a new order status.
     * Note: Typically only used during initialization; standard statuses should be pre-populated.
     *
     * @param statusCode the machine-readable status code (must be unique)
     * @param statusName the user-friendly status name (must be unique)
     * @param description optional description of the status
     * @param displayOrder the display order (1-10)
     * @return the newly created OrderStatusEntity
     * @throws IllegalArgumentException if required fields are invalid or already exist
     */
    public OrderStatusEntity createOrderStatus(String statusCode, String statusName, String description, Integer displayOrder) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Status code cannot be null or empty");
        }
        if (statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }
        if (displayOrder == null || displayOrder < 1 || displayOrder > 10) {
            throw new IllegalArgumentException("Display order must be between 1 and 10");
        }

        statusCode = statusCode.trim();
        statusName = statusName.trim();

        // Check if status code already exists
        if (orderStatusRepository.existsByStatusCode(statusCode)) {
            throw new IllegalArgumentException("Status code '" + statusCode + "' already exists");
        }

        // Check if status name already exists
        if (orderStatusRepository.findByStatusName(statusName).isPresent()) {
            throw new IllegalArgumentException("Status name '" + statusName + "' already exists");
        }

        OrderStatusEntity status = new OrderStatusEntity();
        status.setStatusCode(statusCode);
        status.setStatusName(statusName);
        status.setDescription(description != null ? description.trim() : null);
        status.setDisplayOrder(displayOrder);
        status.setIsActive(true);

        OrderStatusEntity savedStatus = orderStatusRepository.save(status);
        log.info("Created new order status: {} ({})", statusCode, statusName);
        return savedStatus;
    }

    /**
     * Update an existing order status.
     * Typically used to update description or display order, not code/name (which are immutable).
     *
     * @param id the status ID
     * @param description new description (optional)
     * @param displayOrder new display order (optional)
     * @return the updated OrderStatusEntity
     * @throws IllegalArgumentException if status not found or invalid parameters
     */
    public OrderStatusEntity updateOrderStatus(Long id, String description, Integer displayOrder) {
        if (id == null) {
            throw new IllegalArgumentException("Status ID cannot be null");
        }

        OrderStatusEntity status = orderStatusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Status not found with ID: " + id));

        if (description != null) {
            status.setDescription(description.trim());
        }
        if (displayOrder != null) {
            if (displayOrder < 1 || displayOrder > 10) {
                throw new IllegalArgumentException("Display order must be between 1 and 10");
            }
            status.setDisplayOrder(displayOrder);
        }

        OrderStatusEntity updatedStatus = orderStatusRepository.save(status);
        log.info("Updated order status: {}", status.getStatusCode());
        return updatedStatus;
    }

    /**
     * Soft-delete an order status by marking it inactive.
     * Preserves historical data while removing from active workflow.
     *
     * @param id the status ID
     * @throws IllegalArgumentException if status not found
     */
    @Transactional
    public void deactivateOrderStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Status ID cannot be null");
        }

        OrderStatusEntity status = orderStatusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Status not found with ID: " + id));

        // Prevent deactivating terminal statuses (DELIVERED, CANCELLED)
        if (status.isTerminalStatus()) {
            throw new IllegalArgumentException("Cannot deactivate terminal status: " + status.getStatusCode());
        }

        status.setIsActive(false);
        orderStatusRepository.save(status);
        log.info("Deactivated order status: {}", status.getStatusCode());
    }

    /**
     * Reactivate a previously deactivated status.
     *
     * @param id the status ID
     * @throws IllegalArgumentException if status not found
     */
    public void reactivateOrderStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Status ID cannot be null");
        }

        OrderStatusEntity status = orderStatusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Status not found with ID: " + id));

        status.setIsActive(true);
        orderStatusRepository.save(status);
        log.info("Reactivated order status: {}", status.getStatusCode());
    }

    // ==================== List/Query Operations ====================

    /**
     * Get all active statuses ordered by display order.
     * Suitable for UI dropdowns and status lists.
     *
     * @return List of active statuses sorted by displayOrder
     */
    public List<OrderStatusEntity> getAllActiveStatuses() {
        return orderStatusRepository.findByIsActiveOrderByDisplayOrder(true);
    }

    /**
     * Get all statuses (active and inactive) for administrative purposes.
     *
     * @return List of all statuses sorted by displayOrder
     */
    public List<OrderStatusEntity> getAllStatuses() {
        return orderStatusRepository.getAllStatusesOrderedByDisplay();
    }

    /**
     * Get count of all active statuses.
     *
     * @return count of active statuses
     */
    public long getActiveStatusCount() {
        return orderStatusRepository.countByIsActive(true);
    }

    // ==================== Terminal Status Methods ====================

    /**
     * Check if a status code represents a terminal status.
     * Terminal statuses: DELIVERED and CANCELLED
     * Orders in terminal status cannot transition further.
     *
     * @param statusCode the status code to check
     * @return true if status is terminal, false otherwise
     */
    public boolean isTerminalStatus(String statusCode) {
        if (statusCode == null || statusCode.isEmpty()) {
            return false;
        }
        return orderStatusRepository.isTerminalStatus(statusCode) > 0;
    }

    /**
     * Check if a specific OrderStatusEntity is terminal.
     *
     * @param status the OrderStatusEntity to check
     * @return true if status is terminal, false otherwise
     * @throws IllegalArgumentException if status is null
     */
    public boolean isTerminalStatus(OrderStatusEntity status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return status.isTerminalStatus();
    }

    /**
     * Get all terminal statuses.
     * Terminal statuses: DELIVERED and CANCELLED
     *
     * @return List of terminal statuses
     */
    public List<OrderStatusEntity> getTerminalStatuses() {
        return orderStatusRepository.getTerminalStatuses();
    }

    /**
     * Get all non-terminal (transitionable) statuses.
     * These can transition to other statuses.
     *
     * @return List of non-terminal statuses
     */
    public List<OrderStatusEntity> getTransitionableStatuses() {
        return orderStatusRepository.getTransitionableStatuses();
    }

    // ==================== Status Transition Methods ====================

    /**
     * Check if a transition from one status to another is valid.
     * Enforces the order workflow rules.
     *
     * Valid transitions:
     * - PENDING → CONFIRMED, CANCELLED
     * - CONFIRMED → PREPARING, CANCELLED
     * - PREPARING → READY
     * - READY → OUT_FOR_DELIVERY
     * - OUT_FOR_DELIVERY → DELIVERED
     * - DELIVERED, CANCELLED → (terminal, no transitions)
     *
     * @param currentStatusCode the current status code
     * @param nextStatusCode the target status code
     * @return true if transition is valid, false otherwise
     * @throws IllegalArgumentException if either status code is null/empty
     */
    public boolean canTransitionStatus(String currentStatusCode, String nextStatusCode) {
        if (currentStatusCode == null || currentStatusCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Current status code cannot be null or empty");
        }
        if (nextStatusCode == null || nextStatusCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Next status code cannot be null or empty");
        }

        return orderStatusRepository.isValidTransition(currentStatusCode.trim(), nextStatusCode.trim()) > 0;
    }

    /**
     * Check if a transition from one OrderStatusEntity to another is valid.
     *
     * @param currentStatus the current OrderStatusEntity
     * @param nextStatus the target OrderStatusEntity
     * @return true if transition is valid, false otherwise
     * @throws IllegalArgumentException if either status is null
     */
    public boolean canTransitionStatus(OrderStatusEntity currentStatus, OrderStatusEntity nextStatus) {
        if (currentStatus == null) {
            throw new IllegalArgumentException("Current status cannot be null");
        }
        if (nextStatus == null) {
            throw new IllegalArgumentException("Next status cannot be null");
        }

        return currentStatus.canTransitionTo(nextStatus);
    }

    /**
     * Get all valid next statuses for a given current status.
     * Includes all possible transitions (e.g., PENDING can go to CONFIRMED or CANCELLED).
     *
     * @param currentStatusCode the current status code
     * @return List of valid next statuses
     * @throws IllegalArgumentException if currentStatusCode is null/empty
     */
    public List<OrderStatusEntity> getValidNextStatuses(String currentStatusCode) {
        if (currentStatusCode == null || currentStatusCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Current status code cannot be null or empty");
        }

        return orderStatusRepository.getValidNextStatuses(currentStatusCode.trim());
    }

    /**
     * Get all valid next statuses for a given OrderStatusEntity.
     *
     * @param currentStatus the current OrderStatusEntity
     * @return List of valid next statuses
     * @throws IllegalArgumentException if currentStatus is null
     */
    public List<OrderStatusEntity> getValidNextStatuses(OrderStatusEntity currentStatus) {
        if (currentStatus == null) {
            throw new IllegalArgumentException("Current status cannot be null");
        }

        return getValidNextStatuses(currentStatus.getStatusCode());
    }

    // ==================== Factory Methods ====================

    /**
     * Get the PENDING status.
     * Starting status for new orders.
     *
     * @return Optional containing the PENDING status if active
     */
    public Optional<OrderStatusEntity> getPendingStatus() {
        return orderStatusRepository.getPendingStatus();
    }

    /**
     * Get the CONFIRMED status.
     * After customer and restaurant confirm the order.
     *
     * @return Optional containing the CONFIRMED status if active
     */
    public Optional<OrderStatusEntity> getConfirmedStatus() {
        return orderStatusRepository.getConfirmedStatus();
    }

    /**
     * Get the PREPARING status.
     * Restaurant is preparing the order.
     *
     * @return Optional containing the PREPARING status if active
     */
    public Optional<OrderStatusEntity> getPreparingStatus() {
        return orderStatusRepository.getPreparingStatus();
    }

    /**
     * Get the READY status.
     * Order is ready for pickup/handoff to delivery.
     *
     * @return Optional containing the READY status if active
     */
    public Optional<OrderStatusEntity> getReadyStatus() {
        return orderStatusRepository.getReadyStatus();
    }

    /**
     * Get the OUT_FOR_DELIVERY status.
     * Delivery driver has picked up and is en route.
     *
     * @return Optional containing the OUT_FOR_DELIVERY status if active
     */
    public Optional<OrderStatusEntity> getOutForDeliveryStatus() {
        return orderStatusRepository.getOutForDeliveryStatus();
    }

    /**
     * Get the DELIVERED status.
     * Order successfully delivered to customer.
     * Terminal status: no further transitions.
     *
     * @return Optional containing the DELIVERED status if active
     */
    public Optional<OrderStatusEntity> getDeliveredStatus() {
        return orderStatusRepository.getDeliveredStatus();
    }

    /**
     * Get the CANCELLED status.
     * Order was cancelled by customer or restaurant.
     * Terminal status: no further transitions.
     *
     * @return Optional containing the CANCELLED status if active
     */
    public Optional<OrderStatusEntity> getCancelledStatus() {
        return orderStatusRepository.getCancelledStatus();
    }

    // ==================== Reference Data Initialization ====================

    /**
     * Initialize reference data with the 7 standard order statuses.
     * Should be called during application startup or data seeding.
     * Idempotent: safe to call multiple times (checks for existing statuses).
     *
     * Pre-populated statuses:
     * 1. PENDING - Order received, awaiting confirmation
     * 2. CONFIRMED - Restaurant confirmed the order
     * 3. PREPARING - Restaurant is preparing the order
     * 4. READY - Order is ready for delivery/pickup
     * 5. OUT_FOR_DELIVERY - Driver has picked up, en route
     * 6. DELIVERED - Successfully delivered to customer
     * 7. CANCELLED - Order was cancelled
     *
     * @return true if initialization completed (new statuses created), false if already initialized
     */
    @Transactional
    public boolean initializeReferenceData() {
        long existingCount = orderStatusRepository.countByIsActive(true);

        // If standard statuses already exist, initialization is complete
        if (existingCount >= 7) {
            log.debug("Order statuses already initialized; skipping reference data setup");
            return false;
        }

        log.info("Initializing order status reference data...");

        // Create standard statuses if they don't exist
        if (!orderStatusRepository.existsByStatusCode("PENDING")) {
            createOrderStatus("PENDING", "Pending", "Order received, awaiting confirmation", 1);
        }

        if (!orderStatusRepository.existsByStatusCode("CONFIRMED")) {
            createOrderStatus("CONFIRMED", "Confirmed", "Restaurant confirmed the order", 2);
        }

        if (!orderStatusRepository.existsByStatusCode("PREPARING")) {
            createOrderStatus("PREPARING", "Preparing", "Restaurant is preparing the order", 3);
        }

        if (!orderStatusRepository.existsByStatusCode("READY")) {
            createOrderStatus("READY", "Ready", "Order is ready for delivery or pickup", 4);
        }

        if (!orderStatusRepository.existsByStatusCode("OUT_FOR_DELIVERY")) {
            createOrderStatus("OUT_FOR_DELIVERY", "Out for Delivery", "Driver has picked up and is en route", 5);
        }

        if (!orderStatusRepository.existsByStatusCode("DELIVERED")) {
            createOrderStatus("DELIVERED", "Delivered", "Order successfully delivered to customer", 6);
        }

        if (!orderStatusRepository.existsByStatusCode("CANCELLED")) {
            createOrderStatus("CANCELLED", "Cancelled", "Order was cancelled by customer or restaurant", 7);
        }

        log.info("Order status reference data initialization complete");
        return true;
    }

    // ==================== Display/UI Helper Methods ====================

    /**
     * Get the display order of a specific status.
     * Used for sorting statuses in UI.
     *
     * @param statusCode the status code
     * @return the display order, or null if status not found
     */
    public Integer getDisplayOrder(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            return null;
        }
        return orderStatusRepository.getDisplayOrder(statusCode.trim());
    }

    /**
     * Get the next status by display order (for sequential advancement).
     *
     * @param currentStatusCode the current status code
     * @return Optional containing the next status in display order
     */
    public Optional<OrderStatusEntity> getNextStatusByDisplayOrder(String currentStatusCode) {
        if (currentStatusCode == null || currentStatusCode.trim().isEmpty()) {
            return Optional.empty();
        }

        Integer currentOrder = getDisplayOrder(currentStatusCode);
        if (currentOrder == null) {
            return Optional.empty();
        }

        return orderStatusRepository.findNextByDisplayOrder(currentOrder);
    }

    /**
     * Check if a status exists and is active.
     *
     * @param statusCode the status code
     * @return true if status exists and is active, false otherwise
     */
    public boolean statusExists(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            return false;
        }
        return orderStatusRepository.existsByStatusCodeAndIsActive(statusCode.trim(), true);
    }

}
