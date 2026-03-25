package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Restaurant Management.
 *
 * Provides JSON-based RESTful endpoints for CRUD operations on restaurants.
 * All endpoints return JSON responses with appropriate HTTP status codes.
 *
 * Available Endpoints:
 * - GET /api/restaurants — List all restaurants (paginated)
 * - GET /api/restaurants/{id} — Get a specific restaurant
 * - POST /api/restaurants — Create a new restaurant
 * - PUT /api/restaurants/{id} — Update a restaurant
 * - DELETE /api/restaurants/{id} — Delete a restaurant
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    // ==================== Read Endpoints ====================

    /**
     * Get paginated list of all restaurants.
     * GET /api/restaurants
     *
     * @param page page number (0-indexed, default 0)
     * @param size number of restaurants per page (default 10)
     * @return paginated list of restaurants with HTTP 200
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listRestaurants(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

        log.info("Fetching restaurants page {} with size {} via API", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RestaurantEntity> restaurantPage = restaurantService.getAllRestaurantsPaginated(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", restaurantPage.getContent());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalElements", restaurantPage.getTotalElements());
            response.put("totalPages", restaurantPage.getTotalPages());
            response.put("isLast", restaurantPage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching restaurants: " + e.getMessage()));
        }
    }

    /**
     * Get a specific restaurant by ID.
     * GET /api/restaurants/{id}
     *
     * @param id the restaurant ID
     * @return restaurant details with HTTP 200, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable Long id) {
        log.info("Fetching restaurant by ID: {} via API", id);

        try {
            Optional<RestaurantEntity> restaurantOpt = restaurantService.getRestaurantById(id);

            if (restaurantOpt.isEmpty()) {
                log.warn("Restaurant not found for ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
            }

            return ResponseEntity.ok(restaurantOpt.get());
        } catch (Exception e) {
            log.error("Error fetching restaurant ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching restaurant: " + e.getMessage()));
        }
    }

    // ==================== Create Endpoint ====================

    /**
     * Create a new restaurant.
     * POST /api/restaurants
     *
     * Request body example:
     * {
     *   "name": "Pizza Palace",
     *   "email": "info@pizzapalace.com",
     *   "phoneNumber": "555-1234",
     *   "street": "123 Main St",
     *   "city": "Springfield",
     *   "state": "IL",
     *   "zipCode": "62701",
     *   "country": "USA",
     *   "description": "Best pizza in town",
     *   "isActive": true
     * }
     *
     * @param restaurant the restaurant entity from request body
     * @return created restaurant with HTTP 201, or error with HTTP 400/500
     */
    @PostMapping
    public ResponseEntity<Object> createRestaurant(
        @Valid @RequestBody RestaurantEntity restaurant) {

        log.info("Creating new restaurant via API: {}", restaurant.getName());

        try {
            // TODO: In future, get owner ID from logged-in user
            // For now, use first user or throw error if no users
            Long ownerId = getDefaultOwnerId();

            RestaurantEntity createdRestaurant = restaurantService.createRestaurant(ownerId, restaurant);
            log.info("Restaurant created successfully via API: {} (ID: {})", 
                createdRestaurant.getName(), createdRestaurant.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdRestaurant);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating restaurant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating restaurant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating restaurant: " + e.getMessage()));
        }
    }

    // ==================== Update Endpoint ====================

    /**
     * Update an existing restaurant.
     * PUT /api/restaurants/{id}
     *
     * Request body example:
     * {
     *   "name": "Updated Pizza Palace",
     *   "email": "new_email@pizzapalace.com",
     *   "phoneNumber": "555-5678",
     *   "street": "456 Oak Ave",
     *   "city": "Springfield",
     *   "state": "IL",
     *   "zipCode": "62702",
     *   "country": "USA",
     *   "description": "Even better pizza now",
     *   "isActive": true
     * }
     *
     * @param id the restaurant ID
     * @param restaurant the updated restaurant data
     * @return updated restaurant with HTTP 200, or error with HTTP 400/404/500
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateRestaurant(
        @PathVariable Long id,
        @Valid @RequestBody RestaurantEntity restaurant) {

        log.info("Updating restaurant ID: {} via API", id);

        try {
            // Fetch existing restaurant to preserve owner during update
            Optional<RestaurantEntity> existingOpt = restaurantService.getRestaurantById(id);
            if (existingOpt.isEmpty()) {
                log.warn("Restaurant not found for update, ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
            }

            // TODO: In future, verify owner matches current logged-in user
            Long ownerId = existingOpt.get().getOwner().getId();

            RestaurantEntity updatedRestaurant = restaurantService.updateRestaurant(id, ownerId, restaurant);
            log.info("Restaurant updated successfully via API: {} (ID: {})", 
                updatedRestaurant.getName(), updatedRestaurant.getId());

            return ResponseEntity.ok(updatedRestaurant);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating restaurant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating restaurant ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating restaurant: " + e.getMessage()));
        }
    }

    // ==================== Delete Endpoint ====================

    /**
     * Delete a restaurant by ID.
     * DELETE /api/restaurants/{id}
     *
     * @param id the restaurant ID
     * @return HTTP 204 No Content on success, or error with HTTP 404/500
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable Long id) {
        log.info("Deleting restaurant ID: {} via API", id);

        try {
            Optional<RestaurantEntity> restaurantOpt = restaurantService.getRestaurantById(id);

            if (restaurantOpt.isEmpty()) {
                log.warn("Restaurant not found for deletion, ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
            }

            String restaurantName = restaurantOpt.get().getName();

            // TODO: In future, verify owner matches current logged-in user
            Long ownerId = restaurantOpt.get().getOwner().getId();

            restaurantService.deleteRestaurant(id, ownerId);
            log.info("Restaurant deleted successfully via API: {} (ID: {})", restaurantName, id);

            // Return 204 No Content
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting restaurant ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting restaurant: " + e.getMessage()));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get default owner ID for API operations.
     * TODO: Replace with logged-in user ID once authentication is implemented.
     *
     * @return the owner ID (currently defaults to first user)
     * @throws RuntimeException if no users exist
     */
    private Long getDefaultOwnerId() {
        if (userService.getAllUsers().isEmpty()) {
            throw new RuntimeException("No users found in system. Please create a user first.");
        }
        return userService.getAllUsers().get(0).getId();
    }

    /**
     * Create a standardized error response.
     *
     * @param message error message
     * @return error response map
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

}
