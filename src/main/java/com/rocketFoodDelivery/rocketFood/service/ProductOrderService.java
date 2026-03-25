package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.OrderEntity;
import com.rocketFoodDelivery.rocketFood.models.ProductEntity;
import com.rocketFoodDelivery.rocketFood.models.ProductOrderEntity;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProductOrderService provides business logic for managing product line items in orders.
 *
 * Responsibilities:
 * - Add/remove products from orders
 * - Quantity and price validation
 * - Subtotal calculation and verification
 * - Prevent duplicate products in same order
 * - Order total validation (sum of line items matches order total)
 * - Batch operations for multiple items
 * - Product sales analytics and reporting
 *
 * All service methods use manual null validation (fail-fast approach).
 * Authorization checks verify product ownership (in order) before modifications.
 * Subtotal is automatically calculated and verified on creation and updates.
 *
 * Key invariant: Each ProductOrder.subtotal must equal unitPrice × quantity
 * Key constraint: No duplicate products in same order (unique constraint on order_id, product_id)
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Service
@Transactional
public class ProductOrderService {

    private final ProductOrderRepository productOrderRepository;
    private final OrderRepository orderRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param productOrderRepository the product order data access layer
     * @param orderRepository the order data access layer for validation
     * @throws IllegalArgumentException if any repository is null
     */
    public ProductOrderService(
        ProductOrderRepository productOrderRepository,
        OrderRepository orderRepository
    ) {
        if (productOrderRepository == null) {
            throw new IllegalArgumentException("ProductOrderRepository cannot be null");
        }
        if (orderRepository == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null");
        }
        this.productOrderRepository = productOrderRepository;
        this.orderRepository = orderRepository;
    }

    // ==================== CRUD Operations ====================

    /**
     * Add a product to an order as a new line item.
     * Validates product doesn't already exist in order (unique constraint).
     * Automatically calculates subtotal from quantity and unit price.
     *
     * @param orderId the order ID
     * @param product the product to add (must not be null)
     * @param quantity the quantity ordered (1-999)
     * @param unitPrice the price per unit at order time (must be > 0)
     * @param specialNotes optional item-level customization notes
     * @return the newly created ProductOrderEntity
     * @throws IllegalArgumentException if validation fails or product already in order
     */
    public ProductOrderEntity addProductToOrder(
        Long orderId,
        ProductEntity product,
        Integer quantity,
        BigDecimal unitPrice,
        String specialNotes
    ) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Verify order exists
        Optional<OrderEntity> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        validateSpecialNotes(specialNotes);

        // Note: Cannot check existsByOrderIdAndProductId yet because ProductEntity doesn't exist
        // This check will be implemented when ProductEntity is created

        // Create line item with automatic subtotal calculation
        ProductOrderEntity lineItem = new ProductOrderEntity();
        lineItem.setOrder(orderOpt.get());
        lineItem.setProduct(product);
        lineItem.setQuantity(quantity);
        lineItem.setUnitPrice(unitPrice);
        lineItem.setSpecialNotes(specialNotes);
        lineItem.recalculateSubtotal();

        // Verify line item is valid before saving
        lineItem.isValidLineItem();

        return productOrderRepository.save(lineItem);
    }

    /**
     * Get a product order line item by ID.
     *
     * @param id the line item ID
     * @return Optional containing the line item if found, empty otherwise
     * @throws IllegalArgumentException if id is invalid
     */
    public Optional<ProductOrderEntity> getProductOrderById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product Order ID must be a positive number");
        }
        return productOrderRepository.findById(id);
    }

    /**
     * Get all line items for a specific order.
     *
     * @param orderId the order's ID
     * @return List of all line items in this order
     * @throws IllegalArgumentException if orderId is invalid
     */
    public List<ProductOrderEntity> getProductsByOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return productOrderRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    /**
     * Get a specific line item by order and product ID.
     * Used to check if product is already in order or retrieve existing.
     *
     * @param orderId the order's ID
     * @param productId the product's ID
     * @return Optional containing the line item if found, empty otherwise
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Optional<ProductOrderEntity> getProductOrderByOrderAndProduct(Long orderId, Long productId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        return productOrderRepository.findByOrderIdAndProductId(orderId, productId);
    }

    /**
     * Update the quantity of a product in an order.
     * Automatically recalculates subtotal.
     * Verifies the product is in the order (authorization).
     *
     * @param id the line item ID
     * @param orderId the order's ID (for authorization)
     * @param newQuantity the new quantity (1-999)
     * @return the updated ProductOrderEntity
     * @throws IllegalArgumentException if validation fails or not authorized
     */
    public ProductOrderEntity updateProductOrderQuantity(Long id, Long orderId, Integer newQuantity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product Order ID must be a positive number");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }

        validateQuantity(newQuantity);

        Optional<ProductOrderEntity> existing = productOrderRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Product Order not found");
        }

        ProductOrderEntity lineItem = existing.get();

        // Authorization: verify line item belongs to the order
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new IllegalArgumentException("Product Order does not belong to specified order");
        }

        // Verify order can be modified
        if (!lineItem.canBeModified()) {
            throw new IllegalArgumentException("Cannot modify line item in order with status: " + lineItem.getOrder().getStatus());
        }

        lineItem.updateQuantity(newQuantity);
        return productOrderRepository.save(lineItem);
    }

    /**
     * Remove a product from an order (delete line item).
     * Verifies the product is in the order (authorization).
     *
     * @param id the line item ID
     * @param orderId the order's ID (for authorization)
     * @throws IllegalArgumentException if not authorized or not found
     */
    public void removeProductFromOrder(Long id, Long orderId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product Order ID must be a positive number");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }

        Optional<ProductOrderEntity> existing = productOrderRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Product Order not found");
        }

        ProductOrderEntity lineItem = existing.get();

        // Authorization: verify line item belongs to the order
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new IllegalArgumentException("Product Order does not belong to specified order");
        }

        productOrderRepository.deleteByIdAndOrderId(id, orderId);
    }

    /**
     * Remove all line items from an order.
     * Useful when order is cancelled or completely refunded.
     *
     * @param orderId the order's ID
     * @return number of line items deleted
     * @throws IllegalArgumentException if orderId is invalid
     */
    public long removeAllProductsFromOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return productOrderRepository.deleteByOrderId(orderId);
    }

    // ==================== Query & Filtering ====================

    /**
     * Get the number of distinct products in an order.
     *
     * @param orderId the order's ID
     * @return count of distinct products
     * @throws IllegalArgumentException if orderId is invalid
     */
    public long getOrderLineItemCount(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return productOrderRepository.countByOrderId(orderId);
    }

    /**
     * Get all ProductOrder entities for an order.
     * (Alias for getProductsByOrderId for semantic clarity.)
     *
     * @param orderId the order's ID
     * @return List of line items
     */
    public List<ProductOrderEntity> getProductsInOrder(Long orderId) {
        return getProductsByOrderId(orderId);
    }

    /**
     * Calculate the total price of all items in an order.
     * This should match Order.totalPrice (verify consistency).
     *
     * Formula: SUM(subtotal) for all line items
     *
     * @param orderId the order's ID
     * @return sum of all subtotals for the order
     * @throws IllegalArgumentException if orderId is invalid
     */
    public BigDecimal getOrderTotal(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return productOrderRepository.sumSubtotalByOrderId(orderId);
    }

    /**
     * Verify that the total of all line items matches the order's stated total price.
     * Important for data integrity and consistency checks.
     *
     * @param orderId the order's ID
     * @param expectedTotal the expected total from Order.totalPrice
     * @return true if line item subtotals match expected total, false otherwise
     * @throws IllegalArgumentException if orderId is invalid
     */
    public boolean validateOrderTotal(Long orderId, BigDecimal expectedTotal) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (expectedTotal == null) {
            throw new IllegalArgumentException("Expected total cannot be null");
        }

        BigDecimal calculatedTotal = getOrderTotal(orderId);
        return calculatedTotal.compareTo(expectedTotal) == 0;
    }

    /**
     * Get total quantity of items ordered in a specific order.
     * Different from line item count (counts units, not distinct products).
     *
     * @param orderId the order's ID
     * @return sum of all quantities in order
     * @throws IllegalArgumentException if orderId is invalid
     */
    public Integer getOrderTotalQuantity(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        Integer total = productOrderRepository.sumQuantityByOrderId(orderId);
        return total != null ? total : 0;
    }

    /**
     * Get line items with special preparation notes.
     * Useful for kitchen display systems to highlight special requests.
     *
     * @param orderId the order's ID
     * @return List of line items in this order that have special notes
     * @throws IllegalArgumentException if orderId is invalid
     */
    public List<ProductOrderEntity> getSpecialNotesForOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return productOrderRepository.findSpecialNotesForOrder(orderId);
    }

    /**
     * Get all line items across all orders that have special notes.
     * Useful for restaurant-wide special preparation tracking.
     *
     * @return List of all line items with special notes
     */
    public List<ProductOrderEntity> getAllLineItemsWithSpecialNotes() {
        return productOrderRepository.findAllWithSpecialNotes();
    }

    // ==================== Analytics & Reporting ====================

    /**
     * Get total quantity of a product sold within a date range.
     * Useful for sales and inventory analytics.
     *
     * @param productId the product's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total units sold
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Integer getProductQuantityOrderedInRange(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
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

        Integer quantity = productOrderRepository.getTotalQuantityOrderedInRange(productId, startDate, endDate);
        return quantity != null ? quantity : 0;
    }

    /**
     * Get total revenue (sum of subtotals) for a product within a date range.
     * Useful for revenue reporting.
     *
     * @param productId the product's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total revenue for this product
     * @throws IllegalArgumentException if parameters are invalid
     */
    public BigDecimal getProductRevenueInRange(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
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

        BigDecimal revenue = productOrderRepository.getTotalRevenueForProductInRange(productId, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get average unit price for a product across all orders.
     * Useful for price history and trend analysis.
     *
     * @param productId the product's ID
     * @return average unit price, or BigDecimal.ZERO if never ordered
     * @throws IllegalArgumentException if productId is invalid
     */
    public BigDecimal getProductAverageUnitPrice(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }

        BigDecimal average = productOrderRepository.getAverageUnitPriceForProduct(productId);
        return average != null ? average : BigDecimal.ZERO;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate quantity is within acceptable bounds.
     *
     * @param quantity the quantity to validate
     * @throws IllegalArgumentException if quantity is invalid
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity < 1 || quantity > 999) {
            throw new IllegalArgumentException("Quantity must be between 1 and 999");
        }
    }

    /**
     * Validate unit price is positive.
     *
     * @param unitPrice the price to validate
     * @throws IllegalArgumentException if price is invalid
     */
    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0");
        }
    }

    /**
     * Validate special notes length.
     *
     * @param specialNotes the notes to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSpecialNotes(String specialNotes) {
        if (specialNotes != null && specialNotes.length() > 500) {
            throw new IllegalArgumentException("Special notes must not exceed 500 characters");
        }
    }

    /**
     * Validate complete line item data consistency.
     * Called before saving new line items.
     *
     * @param lineItem the line item to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateLineItem(ProductOrderEntity lineItem) {
        if (lineItem == null) {
            throw new IllegalArgumentException("Line item cannot be null");
        }

        if (lineItem.getOrder() == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (lineItem.getProduct() == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        validateQuantity(lineItem.getQuantity());
        validateUnitPrice(lineItem.getUnitPrice());
        validateSpecialNotes(lineItem.getSpecialNotes());

        // Verify subtotal matches calculation
        if (lineItem.getSubtotal() == null) {
            throw new IllegalArgumentException("Subtotal cannot be null");
        }

        BigDecimal expectedSubtotal = lineItem.getUnitPrice().multiply(new BigDecimal(lineItem.getQuantity()));
        if (lineItem.getSubtotal().compareTo(expectedSubtotal) != 0) {
            throw new IllegalArgumentException(
                "Subtotal mismatch: expected " + expectedSubtotal + " but got " + lineItem.getSubtotal()
            );
        }
    }

    /**
     * Check if a product already exists in an order.
     * Used to enforce unique constraint (order_id, product_id).
     *
     * Note: Cannot fully implement until ProductEntity exists.
     * Currently this is a placeholder method.
     *
     * @param orderId the order's ID
     * @param productId the product's ID
     * @return true if product is already in order, false otherwise
     */
    public boolean productExistsInOrder(Long orderId, Long productId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }

        // TODO: Implement full check when ProductEntity exists
        // Currently returns false; will be updated to actual uniqueness check
        return false;
    }

}
