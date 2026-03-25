package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Restaurant business logic.
 * 
 * Handles:
 * - Restaurant creation with owner verification
 * - Restaurant retrieval and filtering
 * - Restaurant updates with authorization checks
 * - Restaurant deletion with proper constraints
 * - Restaurant status management
 * - Authorization verification (owner verification)
 */
@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("RestaurantRepository cannot be null");
        }
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Create a new restaurant owned by a user.
     * 
     * Validates:
     * - Owner ID is not null
     * - Restaurant data is valid
     * - Restaurant name is not already in use
     * 
     * @param ownerId the ID of the restaurant owner
     * @param restaurant the restaurant data
     * @return the created restaurant entity
     * @throws IllegalArgumentException if validation fails
     */
    public RestaurantEntity createRestaurant(Long ownerId, RestaurantEntity restaurant) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }

        validateRestaurantData(restaurant);

        // Check if name is already in use
        if (restaurantRepository.existsByName(restaurant.getName())) {
            throw new IllegalArgumentException("Restaurant name is already in use: " + restaurant.getName());
        }

        // Set the owner - assumes owner exists and is validated by caller
        UserEntity owner = new UserEntity();
        owner.setId(ownerId);
        restaurant.setOwner(owner);

        // Set default status if not provided
        if (restaurant.getIsActive() == null) {
            restaurant.setIsActive(true);
        }

        return restaurantRepository.save(restaurant);
    }

    /**
     * Retrieve a restaurant by ID.
     * 
     * @param restaurantId the ID of the restaurant
     * @return Optional containing the restaurant if found
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public Optional<RestaurantEntity> getRestaurantById(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return restaurantRepository.findById(restaurantId);
    }

    /**
     * Retrieve a restaurant with authorization check.
     * Verifies that the restaurant belongs to the specified owner.
     * 
     * @param restaurantId the ID of the restaurant
     * @param ownerId the ID of the owner
     * @return the restaurant if found and authorized
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if restaurant not found or unauthorized
     */
    public RestaurantEntity getRestaurantByIdAndOwner(Long restaurantId, Long ownerId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

        return restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
            .orElseThrow(() -> new RuntimeException(
                "Restaurant not found or unauthorized: " + restaurantId + " for owner: " + ownerId
            ));
    }

    /**
     * Retrieve all restaurants.
     * 
     * @return list of all restaurants
     */
    public List<RestaurantEntity> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Retrieve all active restaurants.
     * 
     * @return list of active restaurants sorted by name
     */
    public List<RestaurantEntity> getActiveRestaurants() {
        return restaurantRepository.findByIsActiveOrderByNameAsc(true);
    }

    /**
     * Retrieve all restaurants owned by a user.
     * 
     * @param ownerId the ID of the owner
     * @return list of owner's restaurants sorted by name
     * @throws IllegalArgumentException if owner ID is null
     */
    public List<RestaurantEntity> getRestaurantsByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return restaurantRepository.findByOwnerIdOrderByNameAsc(ownerId);
    }

    /**
     * Retrieve active restaurants owned by a user.
     * 
     * @param ownerId the ID of the owner
     * @return list of owner's active restaurants sorted by name
     * @throws IllegalArgumentException if owner ID is null
     */
    public List<RestaurantEntity> getActiveRestaurantsByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return restaurantRepository.findByOwnerIdAndIsActiveOrderByNameAsc(ownerId, true);
    }

    /**
     * Find a restaurant by name.
     * 
     * @param name the restaurant name
     * @return Optional containing the restaurant if found
     * @throws IllegalArgumentException if name is null
     */
    public Optional<RestaurantEntity> getRestaurantByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or blank");
        }
        return restaurantRepository.findByName(name);
    }

    /**
     * Update a restaurant.
     * Verifies that the restaurant belongs to the specified owner before updating.
     * 
     * @param restaurantId the ID of the restaurant to update
     * @param ownerId the ID of the owner
     * @param updatedRestaurant the updated restaurant data
     * @return the updated restaurant
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if restaurant not found or unauthorized
     */
    public RestaurantEntity updateRestaurant(Long restaurantId, Long ownerId, RestaurantEntity updatedRestaurant) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (updatedRestaurant == null) {
            throw new IllegalArgumentException("Updated restaurant data cannot be null");
        }

        validateRestaurantData(updatedRestaurant);

        RestaurantEntity existingRestaurant = getRestaurantByIdAndOwner(restaurantId, ownerId);

        // Check if name is being changed to an already-used name
        if (!existingRestaurant.getName().equals(updatedRestaurant.getName()) &&
            restaurantRepository.existsByName(updatedRestaurant.getName())) {
            throw new IllegalArgumentException("Restaurant name is already in use: " + updatedRestaurant.getName());
        }

        // Update fields
        existingRestaurant.setName(updatedRestaurant.getName());
        existingRestaurant.setDescription(updatedRestaurant.getDescription());
        existingRestaurant.setStreet(updatedRestaurant.getStreet());
        existingRestaurant.setCity(updatedRestaurant.getCity());
        existingRestaurant.setState(updatedRestaurant.getState());
        existingRestaurant.setZipCode(updatedRestaurant.getZipCode());
        existingRestaurant.setCountry(updatedRestaurant.getCountry());
        existingRestaurant.setPhoneNumber(updatedRestaurant.getPhoneNumber());
        existingRestaurant.setEmail(updatedRestaurant.getEmail());

        return restaurantRepository.save(existingRestaurant);
    }

    /**
     * Delete a restaurant.
     * Verifies that the restaurant belongs to the specified owner before deletion.
     * 
     * @param restaurantId the ID of the restaurant to delete
     * @param ownerId the ID of the owner
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if restaurant not found or unauthorized
     */
    public void deleteRestaurant(Long restaurantId, Long ownerId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

        // Verify restaurant exists and belongs to owner
        if (!restaurantRepository.existsByIdAndOwnerId(restaurantId, ownerId)) {
            throw new RuntimeException(
                "Restaurant not found or unauthorized: " + restaurantId + " for owner: " + ownerId
            );
        }

        restaurantRepository.deleteByIdAndOwnerId(restaurantId, ownerId);
    }

    /**
     * Change a restaurant's active status.
     * Verifies authorization before updating status.
     * 
     * @param restaurantId the ID of the restaurant
     * @param ownerId the ID of the owner
     * @param isActive the new active status
     * @return the updated restaurant
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if restaurant not found or unauthorized
     */
    public RestaurantEntity setRestaurantStatus(Long restaurantId, Long ownerId, Boolean isActive) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("Active status cannot be null");
        }

        RestaurantEntity restaurant = getRestaurantByIdAndOwner(restaurantId, ownerId);
        restaurant.setIsActive(isActive);
        return restaurantRepository.save(restaurant);
    }

    /**
     * Get count of all restaurants.
     * 
     * @return the total number of restaurants
     */
    public long getRestaurantCount() {
        return restaurantRepository.count();
    }

    /**
     * Get count of active restaurants.
     * 
     * @return the number of active restaurants
     */
    public long getActiveRestaurantCount() {
        return restaurantRepository.countByIsActive(true);
    }

    /**
     * Get count of restaurants owned by a user.
     * 
     * @param ownerId the ID of the owner
     * @return the number of restaurants owned
     * @throws IllegalArgumentException if owner ID is null
     */
    public long getRestaurantCountByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return restaurantRepository.countByOwnerId(ownerId);
    }

    /**
     * Get count of active restaurants owned by a user.
     * 
     * @param ownerId the ID of the owner
     * @return the number of active restaurants owned
     * @throws IllegalArgumentException if owner ID is null
     */
    public long getActiveRestaurantCountByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return restaurantRepository.countByOwnerIdAndIsActive(ownerId, true);
    }

    /**
     * Check if an owner has any restaurants.
     * 
     * @param ownerId the ID of the owner
     * @return true if owner has at least one restaurant
     * @throws IllegalArgumentException if owner ID is null
     */
    public boolean hasRestaurants(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return restaurantRepository.existsByOwnerId(ownerId);
    }

    /**
     * Check if a restaurant name is available.
     * 
     * @param name the restaurant name to check
     * @return true if name is available (not in use)
     * @throws IllegalArgumentException if name is null
     */
    public boolean isNameAvailable(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or blank");
        }
        return !restaurantRepository.existsByName(name);
    }

    /**
     * Validate restaurant data.
     * Ensures all required fields are present and valid.
     * 
     * @param restaurant the restaurant to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRestaurantData(RestaurantEntity restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }

        if (restaurant.getName() == null || restaurant.getName().isBlank()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or blank");
        }

        // Validate name length
        if (restaurant.getName().length() < 2 || restaurant.getName().length() > 255) {
            throw new IllegalArgumentException("Restaurant name must be between 2 and 255 characters");
        }

        // Validate optional email if provided
        if (restaurant.getEmail() != null && !restaurant.getEmail().isBlank()) {
            if (!restaurant.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format: " + restaurant.getEmail());
            }
            if (restaurant.getEmail().length() > 255) {
                throw new IllegalArgumentException("Email cannot exceed 255 characters");
            }
        }

        // Validate optional phone number if provided
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isBlank()) {
            if (restaurant.getPhoneNumber().length() < 10 || restaurant.getPhoneNumber().length() > 20) {
                throw new IllegalArgumentException("Phone number must be between 10 and 20 characters");
            }
        }

        // Validate optional address fields length
        if (restaurant.getStreet() != null && restaurant.getStreet().length() > 255) {
            throw new IllegalArgumentException("Street address cannot exceed 255 characters");
        }

        if (restaurant.getCity() != null && restaurant.getCity().length() > 100) {
            throw new IllegalArgumentException("City cannot exceed 100 characters");
        }

        if (restaurant.getState() != null && restaurant.getState().length() > 100) {
            throw new IllegalArgumentException("State cannot exceed 100 characters");
        }

        if (restaurant.getZipCode() != null && restaurant.getZipCode().length() > 20) {
            throw new IllegalArgumentException("Zip code cannot exceed 20 characters");
        }

        if (restaurant.getCountry() != null && restaurant.getCountry().length() > 100) {
            throw new IllegalArgumentException("Country cannot exceed 100 characters");
        }
    }
}
