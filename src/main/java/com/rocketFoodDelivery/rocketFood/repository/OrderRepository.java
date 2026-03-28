package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderRepository provides database access for OrderEntity.
 *
 * Extends JpaRepository to inherit standard CRUD operations:
 * - save(OrderEntity)
 * - findById(Long)
 * - delete(OrderEntity)
 * - deleteAll()
 * - Plus Spring Data derived query methods below
 *
 * All query methods follow Spring Data naming conventions for automatic
 * implementation by the framework.
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // ==================== Order Number Queries ====================

    /**
     * Find order by its unique order number.
     *
     * @param orderNumber the human-readable order identifier (e.g., "ORD-20260325-001234")
     * @return Optional containing the order if found, empty otherwise
     */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    /**
     * Check if an order with the given order number exists.
     *
     * @param orderNumber the order number to check
     * @return true if order exists, false otherwise
     */
    boolean existsByOrderNumber(String orderNumber);

    // ==================== Customer Order Queries ====================

    /**
     * Find all orders for a specific customer.
     *
     * @param customerId the customer's ID
     * @return List of all orders placed by this customer
     */
    List<OrderEntity> findByCustomerId(Long customerId);

    /**
     * Find all orders for a customer, ordered by date (newest first).
     *
     * @param customerId the customer's ID
     * @return List of orders ordered by orderDate descending
     */
    List<OrderEntity> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    /**
     * Find all orders for a customer with a specific status code.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param customerId the customer's ID
     * @param statusCode the order status code (e.g., "PENDING", "DELIVERED")
     * @return List of matching orders
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.customer.id = :customerId AND o.orderStatus.statusCode = :statusCode")
    List<OrderEntity> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("statusCode") String statusCode);

    /**
     * Find order by ID and customer ID for authorization verification.
     * Ensures the order belongs to the specified customer.
     *
     * @param id the order ID
     * @param customerId the customer's ID
     * @return Optional containing the order if customer owns it, empty otherwise
     */
    Optional<OrderEntity> findByIdAndCustomerId(Long id, Long customerId);

    /**
     * Check if a customer owns a specific order.
     *
     * @param id the order ID
     * @param customerId the customer's ID
     * @return true if the order belongs to the customer, false otherwise
     */
    boolean existsByIdAndCustomerId(Long id, Long customerId);

    /**
     * Count orders for a customer with a specific status.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param customerId the customer's ID
     * @param statusCode the order status code
     * @return number of matching orders
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.customer.id = :customerId AND o.orderStatus.statusCode = :statusCode")
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("statusCode") String statusCode);

    // ==================== Restaurant Order Queries ====================

    /**
     * Find all orders for a specific restaurant.
     *
     * @param restaurantId the restaurant's ID
     * @return List of all orders for this restaurant
     */
    List<OrderEntity> findByRestaurantId(Long restaurantId);

    /**
     * Find all orders for a restaurant with a specific status.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param restaurantId the restaurant's ID
     * @param statusCode the order status code to filter by
     * @return List of matching orders ordered by orderDate descending
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.restaurant.id = :restaurantId AND o.orderStatus.statusCode = :statusCode ORDER BY o.orderDate DESC")
    List<OrderEntity> findByRestaurantIdAndStatusOrderByOrderDateDesc(@Param("restaurantId") Long restaurantId, @Param("statusCode") String statusCode);

    /**
     * Find all orders for a restaurant, ordered by date (newest first).
     *
     * @param restaurantId the restaurant's ID
     * @return List of orders ordered by orderDate descending
     */
    List<OrderEntity> findByRestaurantIdOrderByOrderDateDesc(Long restaurantId);

    /**
     * Find order by ID and restaurant ID for authorization verification.
     * Ensures the order is for the specified restaurant.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID
     * @return Optional containing the order if it's for this restaurant, empty otherwise
     */
    Optional<OrderEntity> findByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Check if an order belongs to a specific restaurant.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID
     * @return true if the order is for the restaurant, false otherwise
     */
    boolean existsByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Count orders for a restaurant with a specific status.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param restaurantId the restaurant's ID
     * @param statusCode the order status code
     * @return number of matching orders
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.restaurant.id = :restaurantId AND o.orderStatus.statusCode = :statusCode")
    long countByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("statusCode") String statusCode);

    // ==================== Status Queries ====================

    /**
     * Find all orders with a specific status.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     * Useful for monitoring orders in particular lifecycle stages.
     *
     * @param statusCode the order status code to search for (e.g., "PENDING", "DELIVERED")
     * @return List of all orders with this status, ordered by date descending
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus.statusCode = :statusCode ORDER BY o.orderDate DESC")
    List<OrderEntity> findByStatusOrderByOrderDateDesc(@Param("statusCode") String statusCode);

    /**
     * Find all orders with a specific status (unordered).
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param statusCode the order status code
     * @return List of all orders with this status
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus.statusCode = :statusCode")
    List<OrderEntity> findByStatus(@Param("statusCode") String statusCode);

    /**
     * Count the total number of orders with a specific status.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     *
     * @param statusCode the order status code
     * @return count of orders with this status
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.orderStatus.statusCode = :statusCode")
    long countByStatus(@Param("statusCode") String statusCode);

    // ==================== Date Range Queries ====================

    /**
     * Find all orders placed within a specific date/time range.
     * Useful for analytics and reporting.
     *
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return List of orders within the date range
     */
    List<OrderEntity> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all orders for a restaurant within a specific date range.
     * Useful for restaurant-specific reporting.
     *
     * @param restaurantId the restaurant's ID
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return List of matching orders ordered by date descending
     */
    List<OrderEntity> findByRestaurantIdAndOrderDateBetweenOrderByOrderDateDesc(
        Long restaurantId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find all orders for a customer within a specific date range.
     * Useful for customer order history filtering.
     *
     * @param customerId the customer's ID
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return List of matching orders ordered by date descending
     */
    List<OrderEntity> findByCustomerIdAndOrderDateBetweenOrderByOrderDateDesc(
        Long customerId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    // ==================== Delivery Queries ====================

    /**
     * Find all delivered orders for a restaurant within a date range.
     * Updated for Schema v2.0: filters by orderStatus.statusCode (FK relationship)
     * Useful for revenue reporting.
     *
     * @param restaurantId the restaurant's ID
     * @param statusCode the status code typically "DELIVERED"
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of delivered orders
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.restaurant.id = :restaurantId AND o.orderStatus.statusCode = :statusCode AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<OrderEntity> findByRestaurantIdAndStatusAndOrderDateBetweenOrderByOrderDateDesc(
        @Param("restaurantId") Long restaurantId,
        @Param("statusCode") String statusCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ==================== Authorization & Deletion ====================

    /**
     * Delete an order by ID and customer ID.
     * Only allows deletion if the customer owns the order (authorization).
     * Typically restricted to PENDING status in service layer.
     *
     * @param id the order ID
     * @param customerId the customer's ID
     * @return number of records deleted (0 or 1)
     */
    long deleteByIdAndCustomerId(Long id, Long customerId);

    /**
     * Delete an order by ID and restaurant ID.
     * Authorization check: order must be for the restaurant.
     *
     * @param id the order ID
     * @param restaurantId the restaurant's ID
     * @return number of records deleted (0 or 1)
     */
    long deleteByIdAndRestaurantId(Long id, Long restaurantId);

}
