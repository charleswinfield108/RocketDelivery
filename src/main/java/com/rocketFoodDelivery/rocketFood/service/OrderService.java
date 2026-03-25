package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.AddressEntity;
import com.rocketFoodDelivery.rocketFood.models.CustomerEntity;
import com.rocketFoodDelivery.rocketFood.models.OrderEntity;
import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * OrderService provides business logic for managing orders.
 *
 * Responsibilities:
 * - Order creation with validation and order number generation
 * - Order lifecycle management (status transitions)
 * - Customer and restaurant authorization checks
 * - Order queries and filtering
 * - Delivery tracking
 * - Comprehensive data validation
 *
 * All service methods use manual null validation (fail-fast approach).
 * Authorization checks verify ownership (customer/restaurant) before operations.
 * Status transitions enforce valid workflow (PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED/CANCELLED).
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Constructor with dependency injection.
     * @param orderRepository the order data access layer
     * @throws IllegalArgumentException if orderRepository is null
     */
    public OrderService(OrderRepository orderRepository) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null");
        }
        this.orderRepository = orderRepository;
    }

    // ==================== CRUD Operations ====================

    /**
     * Create a new order.
     * Generates a unique order number, sets status to PENDING, auto-sets orderDate.
     * Validates that customer, restaurant, and address exist and are linked properly.
     *
     * @param customer the customer placing the order
     * @param restaurant the restaurant fulfilling the order
     * @param deliveryAddress the delivery address
     * @param totalPrice the total order price (must be positive)
     * @param specialInstructions optional delivery notes
     * @return the newly created OrderEntity
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    public OrderEntity createOrder(
        CustomerEntity customer,
        RestaurantEntity restaurant,
        AddressEntity deliveryAddress,
        BigDecimal totalPrice,
        String specialInstructions
    ) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }

        validateOrderData(totalPrice, specialInstructions);

        OrderEntity order = new OrderEntity();
        order.setOrderNumber(generateUniqueOrderNumber());
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(deliveryAddress);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        order.setSpecialInstructions(specialInstructions);

        return orderRepository.save(order);
    }

    /**
     * Get an order by ID.
     *
     * @param id the order ID
     * @return Optional containing the order if found, empty otherwise
     * @throws IllegalArgumentException if id is null or invalid
     */
    public Optional<OrderEntity> getOrderById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return orderRepository.findById(id);
    }

    /**
     * Get an order by its unique order number.
     *
     * @param orderNumber the human-readable order identifier
     * @return Optional containing the order if found, empty otherwise
     * @throws IllegalArgumentException if orderNumber is null or empty
     */
    public Optional<OrderEntity> getOrderByNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * Get an order by ID with customer authorization.
     * Verifies the order belongs to the specified customer.
     *
     * @param id the order ID
     * @param customerId the customer's ID for authorization
     * @return Optional containing the order if found and customer owns it, empty otherwise
     * @throws IllegalArgumentException if id or customerId is invalid
     */
    public Optional<OrderEntity> getOrderByIdAndCustomerId(Long id, Long customerId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        return orderRepository.findByIdAndCustomerId(id, customerId);
    }

    /**
     * Get an order by ID with restaurant authorization.
     * Verifies the order is for the specified restaurant.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID for authorization
     * @return Optional containing the order if found and belongs to restaurant, empty otherwise
     * @throws IllegalArgumentException if id or restaurantId is invalid
     */
    public Optional<OrderEntity> getOrderByIdAndRestaurantId(Long id, Long restaurantId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        return orderRepository.findByIdAndRestaurantId(id, restaurantId);
    }

    /**
     * Get all orders (admin only, use with caution in large systems).
     *
     * @return List of all orders in the system
     */
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Update an order with customer authorization.
     * Only allows updates to specific fields; status changes require dedicated methods.
     * Blocks updates once order is confirmed (status != PENDING).
     *
     * @param id the order ID
     * @param customerId the customer's ID for authorization
     * @param updates the updated order data
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or order cannot be modified
     */
    public OrderEntity updateOrder(Long id, Long customerId, OrderEntity updates) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        if (updates == null) {
            throw new IllegalArgumentException("Order updates cannot be null");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndCustomerId(id, customerId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not owned by customer");
        }

        OrderEntity order = existing.get();

        if (!order.canBeModified()) {
            throw new IllegalArgumentException("Cannot modify order in status: " + order.getStatus());
        }

        // Only allow updating specialInstructions and totalPrice before confirmation
        if (updates.getSpecialInstructions() != null) {
            validateSpecialInstructions(updates.getSpecialInstructions());
            order.setSpecialInstructions(updates.getSpecialInstructions());
        }

        if (updates.getTotalPrice() != null) {
            validateTotalPrice(updates.getTotalPrice());
            order.setTotalPrice(updates.getTotalPrice());
        }

        return orderRepository.save(order);
    }

    /**
     * Delete an order with customer authorization.
     * Only allows deletion of PENDING orders (not yet confirmed).
     *
     * @param id the order ID
     * @param customerId the customer's ID for authorization
     * @throws IllegalArgumentException if not authorized or order cannot be deleted
     */
    public void deleteOrder(Long id, Long customerId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndCustomerId(id, customerId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not owned by customer");
        }

        OrderEntity order = existing.get();
        if (!order.isPending()) {
            throw new IllegalArgumentException("Can only delete PENDING orders");
        }

        orderRepository.deleteByIdAndCustomerId(id, customerId);
    }

    // ==================== Status Management ====================

    /**
     * Set order status with basic validation.
     * Status must be one of: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
     *
     * @param id the order ID
     * @param newStatus the new status value
     * @throws IllegalArgumentException if status is invalid
     */
    public OrderEntity setOrderStatus(Long id, String newStatus) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(newStatus);

        Optional<OrderEntity> existing = orderRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        OrderEntity order = existing.get();
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Confirm a pending order (move to CONFIRMED status).
     * Sets estimatedDeliveryTime to 45 minutes from now.
     *
     * @param id the order ID
     * @param customerId the customer's ID for authorization
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or not in PENDING status
     */
    public OrderEntity confirmOrder(Long id, Long customerId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndCustomerId(id, customerId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not owned by customer");
        }

        OrderEntity order = existing.get();
        if (!order.isPending()) {
            throw new IllegalArgumentException("Only PENDING orders can be confirmed. Current status: " + order.getStatus());
        }

        order.setStatus("CONFIRMED");
        order.setDeliveryEstimate(LocalDateTime.now().plusMinutes(45));
        return orderRepository.save(order);
    }

    /**
     * Start preparation of a confirmed order (move to PREPARING status).
     * Restaurant-only operation.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID for authorization
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or not in CONFIRMED status
     */
    public OrderEntity startPreparation(Long id, Long restaurantId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndRestaurantId(id, restaurantId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not for this restaurant");
        }

        OrderEntity order = existing.get();
        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only CONFIRMED orders can move to PREPARING. Current status: " + order.getStatus());
        }

        order.setStatus("PREPARING");
        return orderRepository.save(order);
    }

    /**
     * Mark order as ready for pickup/delivery (move to READY status).
     * Restaurant-only operation.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID for authorization
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or not in PREPARING status
     */
    public OrderEntity markReady(Long id, Long restaurantId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndRestaurantId(id, restaurantId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not for this restaurant");
        }

        OrderEntity order = existing.get();
        if (!"PREPARING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only PREPARING orders can move to READY. Current status: " + order.getStatus());
        }

        order.setStatus("READY");
        return orderRepository.save(order);
    }

    /**
     * Mark order as out for delivery (move to OUT_FOR_DELIVERY status).
     * Restaurant-only operation (for delivery orders).
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID for authorization
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or not in READY status
     */
    public OrderEntity markOutForDelivery(Long id, Long restaurantId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndRestaurantId(id, restaurantId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not for this restaurant");
        }

        OrderEntity order = existing.get();
        if (!"READY".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only READY orders can move to OUT_FOR_DELIVERY. Current status: " + order.getStatus());
        }

        order.setStatus("OUT_FOR_DELIVERY");
        return orderRepository.save(order);
    }

    /**
     * Complete delivery (move to DELIVERED status).
     * Sets actualDeliveryTime to now.
     *
     * @param id the order ID
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if order not found or not out for delivery
     */
    public OrderEntity completeDelivery(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        OrderEntity order = existing.get();
        if (!"OUT_FOR_DELIVERY".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only OUT_FOR_DELIVERY orders can be completed. Current status: " + order.getStatus());
        }

        order.setStatus("DELIVERED");
        order.completeDelivery();
        return orderRepository.save(order);
    }

    /**
     * Cancel an order (move to CANCELLED status).
     * Only allows canceling PENDING or CONFIRMED orders.
     *
     * @param id the order ID
     * @param customerId the customer's ID for authorization
     * @return the updated OrderEntity
     * @throws IllegalArgumentException if not authorized or order cannot be cancelled
     */
    public OrderEntity cancelOrder(Long id, Long customerId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }

        Optional<OrderEntity> existing = orderRepository.findByIdAndCustomerId(id, customerId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Order not found or not owned by customer");
        }

        OrderEntity order = existing.get();
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    // ==================== Query & Filtering ====================

    /**
     * Get all orders for a specific customer.
     *
     * @param customerId the customer's ID
     * @return List of all orders for this customer, ordered by date descending
     * @throws IllegalArgumentException if customerId is invalid
     */
    public List<OrderEntity> getCustomerOrders(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    /**
     * Get customer orders filtered by status.
     *
     * @param customerId the customer's ID
     * @param status the order status to filter by
     * @return List of matching orders
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<OrderEntity> getCustomerOrdersByStatus(Long customerId, String status) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }

    /**
     * Get all orders for a specific restaurant.
     *
     * @param restaurantId the restaurant's ID
     * @return List of all orders for this restaurant, ordered by date descending
     * @throws IllegalArgumentException if restaurantId is invalid
     */
    public List<OrderEntity> getRestaurantOrders(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        return orderRepository.findByRestaurantIdOrderByOrderDateDesc(restaurantId);
    }

    /**
     * Get restaurant orders filtered by status.
     *
     * @param restaurantId the restaurant's ID
     * @param status the order status to filter by
     * @return List of matching orders, ordered by date descending
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<OrderEntity> getRestaurantOrdersByStatus(Long restaurantId, String status) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.findByRestaurantIdAndStatusOrderByOrderDateDesc(restaurantId, status);
    }

    /**
     * Get all orders with a specific status (system-wide).
     *
     * @param status the order status
     * @return List of all orders with this status, ordered by date descending
     * @throws IllegalArgumentException if status is invalid
     */
    public List<OrderEntity> getOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    /**
     * Get orders within a date range (for reporting).
     *
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return List of orders within the range
     * @throws IllegalArgumentException if dates are invalid
     */
    public List<OrderEntity> getOrdersInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    /**
     * Get restaurant orders within a date range (for revenue reporting).
     *
     * @param restaurantId the restaurant's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of matching orders
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<OrderEntity> getRestaurantOrdersInDateRange(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return orderRepository.findByRestaurantIdAndOrderDateBetweenOrderByOrderDateDesc(restaurantId, startDate, endDate);
    }

    // ==================== Analytics & Counts ====================

    /**
     * Get count of PENDING orders for a restaurant.
     *
     * @param restaurantId the restaurant's ID
     * @return number of pending orders
     * @throws IllegalArgumentException if restaurantId is invalid
     */
    public long getRestaurantPendingOrderCount(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        return orderRepository.countByRestaurantIdAndStatus(restaurantId, "PENDING");
    }

    /**
     * Get count of orders for a restaurant with a specific status.
     *
     * @param restaurantId the restaurant's ID
     * @param status the order status
     * @return count of matching orders
     * @throws IllegalArgumentException if parameters are invalid
     */
    public long getRestaurantOrderCountByStatus(Long restaurantId, String status) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be a positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.countByRestaurantIdAndStatus(restaurantId, status);
    }

    /**
     * Get count of orders for a customer with a specific status.
     *
     * @param customerId the customer's ID
     * @param status the order status
     * @return count of matching orders
     * @throws IllegalArgumentException if parameters are invalid
     */
    public long getCustomerOrderCountByStatus(Long customerId, String status) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.countByCustomerIdAndStatus(customerId, status);
    }

    /**
     * Get count of all system orders with a specific status.
     *
     * @param status the order status
     * @return count of orders with this status
     * @throws IllegalArgumentException if status is invalid
     */
    public long getOrderCountByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return orderRepository.countByStatus(status);
    }

    // ==================== Utility Methods ====================

    /**
     * Check if a customer has a specific order.
     *
     * @param customerId the customer's ID
     * @param orderId the order's ID
     * @return true if the customer owns the order, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean hasOrder(Long customerId, Long orderId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return orderRepository.existsByIdAndCustomerId(orderId, customerId);
    }

    /**
     * Check if an order has been delivered.
     *
     * @param orderId the order's ID
     * @return true if order status is DELIVERED, false otherwise
     * @throws IllegalArgumentException if orderId is invalid
     */
    public boolean isOrderDelivered(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        return order.isPresent() && order.get().isDelivered();
    }

    /**
     * Check if an order number exists.
     *
     * @param orderNumber the order number to check
     * @return true if order exists, false otherwise
     * @throws IllegalArgumentException if orderNumber is null or empty
     */
    public boolean hasOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }
        return orderRepository.existsByOrderNumber(orderNumber);
    }

    // ==================== Validation Methods ====================

    /**
     * Validate order data (price, special instructions).
     * Called during order creation.
     *
     * @param totalPrice the total price to validate
     * @param specialInstructions optional delivery notes
     * @throws IllegalArgumentException if validation fails
     */
    private void validateOrderData(BigDecimal totalPrice, String specialInstructions) {
        validateTotalPrice(totalPrice);
        validateSpecialInstructions(specialInstructions);
    }

    /**
     * Validate total price.
     *
     * @param totalPrice the price to validate
     * @throws IllegalArgumentException if price is invalid
     */
    private void validateTotalPrice(BigDecimal totalPrice) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("Total price cannot be null");
        }
        if (totalPrice.signum() <= 0) {
            throw new IllegalArgumentException("Total price must be greater than 0");
        }
    }

    /**
     * Validate special instructions.
     *
     * @param specialInstructions the instructions to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSpecialInstructions(String specialInstructions) {
        if (specialInstructions != null && specialInstructions.length() > 500) {
            throw new IllegalArgumentException("Special instructions must not exceed 500 characters");
        }
    }

    /**
     * Validate order status.
     * Valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
     *
     * @param status the status to validate
     * @throws IllegalArgumentException if status is invalid
     */
    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String trimmed = status.trim().toUpperCase();
        if (!("PENDING".equals(trimmed) || "CONFIRMED".equals(trimmed) ||
            "PREPARING".equals(trimmed) || "READY".equals(trimmed) ||
            "OUT_FOR_DELIVERY".equals(trimmed) || "DELIVERED".equals(trimmed) ||
            "CANCELLED".equals(trimmed))) {
            throw new IllegalArgumentException(
                "Invalid status: " + status + ". Valid values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED"
            );
        }
    }

    /**
     * Generate a unique order number.
     * Format: "ORD-YYYYMMDD-" + random 6-digit suffix
     * Example: "ORD-20260325-A1B2C3"
     *
     * @return a unique order number
     */
    private String generateUniqueOrderNumber() {
        String timestamp = LocalDateTime.now().toString().replace("-", "").replace(":", "").substring(0, 8);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "ORD-" + timestamp + "-" + randomSuffix;

        // Verify uniqueness (retry if collision, though unlikely)
        int retries = 0;
        while (orderRepository.existsByOrderNumber(orderNumber) && retries < 5) {
            randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            orderNumber = "ORD-" + timestamp + "-" + randomSuffix;
            retries++;
        }

        if (retries >= 5) {
            throw new RuntimeException("Failed to generate unique order number after multiple attempts");
        }

        return orderNumber;
    }

}
