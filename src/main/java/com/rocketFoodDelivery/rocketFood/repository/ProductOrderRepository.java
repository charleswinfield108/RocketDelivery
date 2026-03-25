package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.ProductOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProductOrderRepository provides database access for ProductOrderEntity.
 *
 * Extends JpaRepository to inherit standard CRUD operations:
 * - save(ProductOrderEntity)
 * - findById(Long)
 * - delete(ProductOrderEntity)
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
public interface ProductOrderRepository extends JpaRepository<ProductOrderEntity, Long> {

    // ==================== Order Item Queries ====================

    /**
     * Find all line items for a specific order.
     * Ordered by creation date (oldest first).
     *
     * @param orderId the order's ID
     * @return List of all line items in this order
     */
    List<ProductOrderEntity> findByOrderId(Long orderId);

    /**
     * Find all line items for a specific order, ordered by creation date.
     *
     * @param orderId the order's ID
     * @return List of line items ordered by createdAt ascending
     */
    List<ProductOrderEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    /**
     * Count the number of distinct products in an order.
     * Equivalent to number of different line items in an order.
     *
     * @param orderId the order's ID
     * @return count of line items for this order
     */
    long countByOrderId(Long orderId);

    /**
     * Check if an order has any line items.
     *
     * @param orderId the order's ID
     * @return true if order has at least one line item, false otherwise
     */
    boolean existsByOrderId(Long orderId);

    // ==================== Product Queries ====================

    /**
     * Find all orders containing a specific product.
     * Useful for product-centric analytics.
     *
     * @param productId the product's ID
     * @return List of all line items referencing this product
     */
    List<ProductOrderEntity> findByProductId(Long productId);

    /**
     * Find orders where a product was ordered with quantity greater than threshold.
     * Useful for identifying best-selling quantities.
     *
     * @param productId the product's ID
     * @param quantity the quantity threshold
     * @return List of line items for this product with quantity > threshold
     */
    List<ProductOrderEntity> findByProductIdAndQuantityGreaterThan(Long productId, Integer quantity);

    /**
     * Find a specific line item by order and product.
     * Use for verifying if product is already in order (unique constraint check).
     *
     * @param orderId the order's ID
     * @param productId the product's ID
     * @return Optional containing the line item if found, empty otherwise
     */
    Optional<ProductOrderEntity> findByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * Check if a product is already in a specific order.
     * Used to prevent duplicate line items (unique constraint).
     *
     * @param orderId the order's ID
     * @param productId the product's ID
     * @return true if product is in order, false otherwise
     */
    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    // ==================== Subtotal & Pricing Queries ====================

    /**
     * Sum the subtotal of all line items for a specific order.
     * Useful for validating order total price.
     * Should equal Order.totalPrice (with possible rounding variations).
     *
     * @param orderId the order's ID
     * @return sum of all subtotals for this order, or 0 if no items
     */
    @Query("SELECT COALESCE(SUM(po.subtotal), 0) FROM ProductOrderEntity po WHERE po.order.id = :orderId")
    BigDecimal sumSubtotalByOrderId(@Param("orderId") Long orderId);

    /**
     * Get total quantity of items ordered in a specific order.
     * Different from countByOrderId (which counts distinct products).
     *
     * @param orderId the order's ID
     * @return sum of all quantities in order
     */
    @Query("SELECT COALESCE(SUM(po.quantity), 0) FROM ProductOrderEntity po WHERE po.order.id = :orderId")
    Integer sumQuantityByOrderId(@Param("orderId") Long orderId);

    /**
     * Get average unit price for a specific product across all orders.
     * Useful for price tracking and analytics.
     *
     * @param productId the product's ID
     * @return average unit price, or null if product never ordered
     */
    @Query("SELECT AVG(po.unitPrice) FROM ProductOrderEntity po WHERE po.product = :productId")
    BigDecimal getAverageUnitPriceForProduct(@Param("productId") Long productId);

    // ==================== Date Range Queries ====================

    /**
     * Find line items created within a date range.
     * Useful for time-based reporting.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of line items within date range
     */
    List<ProductOrderEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all orders (via line items) for a product within a date range.
     * Useful for product revenue reporting.
     *
     * Note: Returns line items, not orders; group results for unique orders.
     *
     * @param productId the product's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of line items for this product within date range
     */
    List<ProductOrderEntity> findByProductIdAndCreatedAtBetween(
        Long productId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Get total quantity of a product ordered within a date range.
     * Useful for sales metrics.
     *
     * @param productId the product's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total units sold of this product in date range
     */
    @Query("SELECT COALESCE(SUM(po.quantity), 0) FROM ProductOrderEntity po " +
           "WHERE po.product = :productId AND po.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalQuantityOrderedInRange(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get total revenue (subtotal) for a product within a date range.
     * Useful for revenue reporting.
     *
     * @param productId the product's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total subtotal revenue for this product in date range
     */
    @Query("SELECT COALESCE(SUM(po.subtotal), 0) FROM ProductOrderEntity po " +
           "WHERE po.product = :productId AND po.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueForProductInRange(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ==================== Special Notes Queries ====================

    /**
     * Find line items with special preparation notes.
     * Useful for KDS (Kitchen Display System) filtering.
     *
     * @return List of all line items with non-null specialNotes
     */
    @Query("SELECT po FROM ProductOrderEntity po WHERE po.specialNotes IS NOT NULL AND po.specialNotes != ''")
    List<ProductOrderEntity> findAllWithSpecialNotes();

    /**
     * Find line items for an order that have special notes.
     * Useful for restaurant staff to identify items needing special attention.
     *
     * @param orderId the order's ID
     * @return List of line items in order with special notes
     */
    @Query("SELECT po FROM ProductOrderEntity po WHERE po.order.id = :orderId " +
           "AND po.specialNotes IS NOT NULL AND po.specialNotes != ''")
    List<ProductOrderEntity> findSpecialNotesForOrder(@Param("orderId") Long orderId);

    // ==================== Deletion & Cleanup ====================

    /**
     * Delete a line item by ID and order ID.
     * Authorization check: verifies line item belongs to the specified order.
     *
     * @param id the line item ID
     * @param orderId the order's ID
     * @return number of records deleted (0 or 1)
     */
    long deleteByIdAndOrderId(Long id, Long orderId);

    /**
     * Delete a specific product from an order (remove line item).
     * Authorization check: verifies product is in the specified order.
     *
     * @param orderId the order's ID
     * @param productId the product's ID
     * @return number of records deleted (0 or 1)
     */
    long deleteByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * Delete all line items for a specific order.
     * Useful when order is cancelled or fully refunded.
     *
     * @param orderId the order's ID
     * @return number of records deleted
     */
    long deleteByOrderId(Long orderId);

    /**
     * Delete all line items for a specific product.
     * Useful when product is discontinued or removed from catalog.
     *
     * @param productId the product's ID
     * @return number of records deleted
     */
    long deleteByProductId(Long productId);

}
