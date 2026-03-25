package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for RestaurantEntity.
 * 
 * Provides data access methods for restaurant CRUD operations and custom queries
 * with authorization checks to ensure data integrity and security.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long> {

    /**
     * Find a restaurant by name.
     * 
     * @param name the restaurant name
     * @return Optional containing the restaurant if found
     */
    Optional<RestaurantEntity> findByName(String name);

    /**
     * Find all active restaurants, sorted by name.
     * 
     * @param isActive the active status
     * @return list of restaurants ordered by name
     */
    List<RestaurantEntity> findByIsActiveOrderByNameAsc(Boolean isActive);

    /**
     * Find all restaurants by owner, sorted by name.
     * 
     * @param ownerId the ID of the owner
     * @return list of owner's restaurants sorted by name
     */
    List<RestaurantEntity> findByOwnerIdOrderByNameAsc(Long ownerId);

    /**
     * Find active restaurants by owner, sorted by name.
     * 
     * @param ownerId the ID of the owner
     * @param isActive the active status
     * @return list of owner's active restaurants
     */
    List<RestaurantEntity> findByOwnerIdAndIsActiveOrderByNameAsc(Long ownerId, Boolean isActive);

    /**
     * Find a restaurant by ID and owner ID for authorization.
     * Verification that user owns the restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @param ownerId the ID of the owner
     * @return Optional containing the restaurant if found and authorized
     */
    Optional<RestaurantEntity> findByIdAndOwnerId(Long restaurantId, Long ownerId);

    /**
     * Delete a restaurant by ID and owner ID.
     * Authorization check: ensures the restaurant belongs to the owner.
     * 
     * @param restaurantId the ID of the restaurant
     * @param ownerId the ID of the owner
     * @return the number of restaurants deleted (0 or 1)
     */
    long deleteByIdAndOwnerId(Long restaurantId, Long ownerId);

    /**
     * Count restaurants by owner.
     * 
     * @param ownerId the ID of the owner
     * @return the number of restaurants owned
     */
    long countByOwnerId(Long ownerId);

    /**
     * Count active restaurants by owner.
     * 
     * @param ownerId the ID of the owner
     * @param isActive the active status
     * @return the number of active restaurants owned
     */
    long countByOwnerIdAndIsActive(Long ownerId, Boolean isActive);

    /**
     * Count restaurants by active status.
     * 
     * @param isActive the active status
     * @return the number of active/inactive restaurants
     */
    long countByIsActive(Boolean isActive);

    /**
     * Check if a restaurant exists by name.
     * 
     * @param name the restaurant name
     * @return true if restaurant exists
     */
    boolean existsByName(String name);

    /**
     * Check if owner has a restaurant by ID.
     * 
     * @param restaurantId the ID of the restaurant
     * @param ownerId the ID of the owner
     * @return true if restaurant exists and belongs to owner
     */
    boolean existsByIdAndOwnerId(Long restaurantId, Long ownerId);

    /**
     * Check if an owner has any restaurants.
     * 
     * @param ownerId the ID of the owner
     * @return true if owner has at least one restaurant
     */
    boolean existsByOwnerId(Long ownerId);
}

