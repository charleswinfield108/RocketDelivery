package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for RestaurantEntity.
 * 
 * Provides data access methods for restaurant CRUD operations and custom queries.
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
     * Find all active restaurants.
     * 
     * @return list of active restaurants
     */
    List<RestaurantEntity> findByIsActiveOrderByNameAsc(Boolean isActive);

    /**
     * Count active restaurants.
     * 
     * @return the number of active restaurants
     */
    long countByIsActive(Boolean isActive);

    /**
     * Check if a restaurant exists by name.
     * 
     * @param name the restaurant name
     * @return true if restaurant exists
     */
    boolean existsByName(String name);
}
