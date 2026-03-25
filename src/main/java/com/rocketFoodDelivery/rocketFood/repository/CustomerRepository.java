package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for CustomerEntity.
 * 
 * Provides data access methods for customer CRUD operations and custom queries
 * with authorization checks to ensure data integrity and security.
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    /**
     * Find a customer by user ID.
     * Each user has at most one customer profile.
     * 
     * @param userId the ID of the user
     * @return Optional containing the customer if found
     */
    Optional<CustomerEntity> findByUserId(Long userId);

    /**
     * Find an active customer by user ID.
     * 
     * @param userId the ID of the user
     * @param isActive the active status
     * @return Optional containing the customer if found and active
     */
    Optional<CustomerEntity> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * Find customer by ID and user ID for authorization.
     * Verification that the customer belongs to the user.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @return Optional containing the customer if found and authorized
     */
    Optional<CustomerEntity> findByIdAndUserId(Long customerId, Long userId);

    /**
     * Find all active customers, ordered by creation date descending.
     * 
     * @param isActive the active status
     * @return list of active customers
     */
    List<CustomerEntity> findByIsActiveOrderByCreatedAtDesc(Boolean isActive);

    /**
     * Find all customers by preferred restaurant, sorted by loyalty points descending.
     * 
     * @param restaurantId the ID of the preferred restaurant
     * @return list of customers who prefer this restaurant
     */
    List<CustomerEntity> findByPreferredRestaurantIdOrderByLoyaltyPointsDesc(Long restaurantId);

    /**
     * Find active customers by preferred restaurant.
     * 
     * @param restaurantId the ID of the preferred restaurant
     * @param isActive the active status
     * @return list of active customers for restaurant
     */
    List<CustomerEntity> findByPreferredRestaurantIdAndIsActiveOrderByLoyaltyPointsDesc(Long restaurantId, Boolean isActive);

    /**
     * Find customers with loyalty points greater than threshold, sorted by points descending.
     * Useful for loyalty leaderboard and tier system.
     * 
     * @param minPoints the minimum loyalty points threshold
     * @return list of customers ordered by loyalty points
     */
    List<CustomerEntity> findByLoyaltyPointsGreaterThanOrderByLoyaltyPointsDesc(Integer minPoints);

    /**
     * Find customers with last order activity, sorted by date descending.
     * Identifies recently active customers.
     * 
     * @return list of customers who have placed orders
     */
    List<CustomerEntity> findByLastOrderDateIsNotNullOrderByLastOrderDateDesc();

    /**
     * Find active customers with last order activity.
     * 
     * @return list of active customers with order history
     */
    List<CustomerEntity> findByIsActiveAndLastOrderDateIsNotNullOrderByLastOrderDateDesc(Boolean isActive);

    /**
     * Delete a customer by ID and user ID.
     * Authorization check: ensures the customer belongs to the user.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @return the number of customers deleted (0 or 1)
     */
    long deleteByIdAndUserId(Long customerId, Long userId);

    /**
     * Count all active customers.
     * 
     * @param isActive the active status
     * @return the number of active customers
     */
    long countByIsActive(Boolean isActive);

    /**
     * Count customers who prefer a specific restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of customers
     */
    long countByPreferredRestaurantId(Long restaurantId);

    /**
     * Check if a customer exists for a user.
     * 
     * @param userId the ID of the user
     * @return true if customer profile exists
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if a customer exists by ID and user ID.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @return true if customer exists and belongs to user
     */
    boolean existsByIdAndUserId(Long customerId, Long userId);
}
