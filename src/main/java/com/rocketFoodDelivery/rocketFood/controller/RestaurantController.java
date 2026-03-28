package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.models.AddressEntity;
import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.service.AddressService;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * Back Office Controller for Restaurant Management.
 *
 * Provides Thymeleaf-based web interface for CRUD operations on restaurants.
 * All endpoints return HTML views (not JSON).
 *
 * Available Endpoints:
 * - GET /backoffice/restaurants — List all restaurants
 * - GET /backoffice/restaurants/new — Display create form
 * - POST /backoffice/restaurants — Process create submission
 * - GET /backoffice/restaurants/{id}/edit — Display edit form
 * - POST /backoffice/restaurants/{id} — Process update submission
 * - GET /backoffice/restaurants/{id}/delete — Delete confirmation & processing
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Controller
@RequestMapping("/backoffice/restaurants")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;
    private final AddressService addressService;

    // ==================== List Endpoint ====================

    /**
     * Display paginated list of all restaurants.
     * GET /backoffice/restaurants
     *
     * @param page page number (0-indexed, default 0)
     * @param size number of restaurants per page (default 10)
     * @param model Spring MVC model for view rendering
     * @return template name: backoffice/restaurants/index
     */
    @GetMapping
    public String listRestaurants(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        Model model) {
        log.info("Fetching restaurants page {} with size {} for back office list view", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<RestaurantEntity> restaurantPage = restaurantService.getAllRestaurantsPaginated(pageable);
        
        model.addAttribute("restaurants", restaurantPage.getContent());
        model.addAttribute("restaurantPage", restaurantPage);
        model.addAttribute("restaurantCount", restaurantPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", restaurantPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "backoffice/restaurants/index";
    }

    // ==================== Create Endpoints ====================

    /**
     * Display create restaurant form.
     * GET /backoffice/restaurants/new
     *
     * @param model Spring MVC model for view rendering
     * @return template name: backoffice/restaurants/create
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.info("Displaying create restaurant form");

        if (!model.containsAttribute("restaurant")) {
            model.addAttribute("restaurant", new RestaurantEntity());
        }

        // Get list of available users (for owner selection)
        // TODO: In future, this could be scoped to current logged-in user
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "backoffice/restaurants/create";
    }

    /**
     * Process restaurant creation form submission.
     * POST /backoffice/restaurants
     *
     * @param restaurant the restaurant entity from form binding
     * @param ownerId the owner ID from the form selection
     * @param redirectAttributes for flash messages
     * @return redirect to list on success, or back to form on error
     */
    @PostMapping
    public String createRestaurant(
        @ModelAttribute("restaurant") RestaurantEntity restaurant,
        @RequestParam(required = false) Long ownerId,
        RedirectAttributes redirectAttributes) {

        try {
            // Validate required fields
            if (restaurant.getName() == null || restaurant.getName().isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Restaurant name is required");
                return "redirect:/backoffice/restaurants/new";
            }
            if (restaurant.getEmail() == null || restaurant.getEmail().isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Restaurant email is required");
                return "redirect:/backoffice/restaurants/new";
            }
            if (restaurant.getPhoneNumber() == null || restaurant.getPhoneNumber().isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Restaurant phone number is required");
                return "redirect:/backoffice/restaurants/new";
            }
            if (restaurant.getPriceRange() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Price range is required");
                return "redirect:/backoffice/restaurants/new";
            }

            // Use the selected owner ID, or default to first user if not provided
            Long effectiveOwnerId = ownerId;
            if (effectiveOwnerId == null) {
                effectiveOwnerId = getDefaultOwnerId();
            }

            // Step 1: Create an address from the restaurant's address fields
            // The restaurant form captures street, city, state, zipCode, country
            AddressEntity newAddress = new AddressEntity();
            newAddress.setStreet(restaurant.getStreet());
            newAddress.setCity(restaurant.getCity());
            newAddress.setState(restaurant.getState());
            newAddress.setZipCode(restaurant.getZipCode());
            newAddress.setCountry(restaurant.getCountry());
            newAddress.setIsDefault(false);

            // Create the address for the owner
            AddressEntity createdAddress = addressService.createAddress(effectiveOwnerId, newAddress);
            log.info("Address created for restaurant: ID {}", createdAddress.getId());

            // Step 2: Link the created address to the restaurant
            restaurant.setAddress(createdAddress);

            // Step 3: Create the restaurant
            RestaurantEntity createdRestaurant = restaurantService.createRestaurant(effectiveOwnerId, restaurant);
            log.info("Restaurant created successfully: {} (ID: {})", createdRestaurant.getName(), createdRestaurant.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                "Restaurant '" + createdRestaurant.getName() + "' created successfully!");

            return "redirect:/backoffice/restaurants";
        } catch (Exception e) {
            log.error("Error creating restaurant", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error creating restaurant: " + e.getMessage());

            return "redirect:/backoffice/restaurants/new";
        }
    }

    // ==================== Edit Endpoints ====================

    /**
     * Display edit restaurant form with pre-filled data.
     * GET /backoffice/restaurants/{id}/edit
     *
     * @param id the restaurant ID
     * @param model Spring MVC model for view rendering
     * @return template name: backoffice/restaurants/edit
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
        @PathVariable Long id,
        Model model) {

        log.info("Displaying edit form for restaurant ID: {}", id);

        Optional<RestaurantEntity> restaurantOpt = restaurantService.getRestaurantById(id);

        if (restaurantOpt.isEmpty()) {
            log.warn("Restaurant not found for ID: {}", id);
            return "redirect:/backoffice/restaurants";
        }

        model.addAttribute("restaurant", restaurantOpt.get());

        // Get list of available users for owner reassignment
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "backoffice/restaurants/edit";
    }

    /**
     * Process restaurant update form submission.
     * POST /backoffice/restaurants/{id}
     *
     * @param id the restaurant ID
     * @param restaurant the updated restaurant entity from form binding
     * @param bindingResult validation result from @Valid
     * @param redirectAttributes for flash messages
     * @return redirect to list on success, or back to form on error
     */
    @PostMapping("/{id}")
    public String updateRestaurant(
        @PathVariable Long id,
        @Valid @ModelAttribute("restaurant") RestaurantEntity restaurant,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Restaurant update form has validation errors for ID: {}", id);
            return "redirect:/backoffice/restaurants/" + id + "/edit";
        }

        try {
            // Fetch existing restaurant to preserve owner during update
            Optional<RestaurantEntity> existingOpt = restaurantService.getRestaurantById(id);
            if (existingOpt.isEmpty()) {
                log.warn("Restaurant not found for update, ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Restaurant not found");
                return "redirect:/backoffice/restaurants";
            }

            // TODO: In future, verify owner matches current logged-in user
            Long ownerId = existingOpt.get().getOwner().getId();

            RestaurantEntity updatedRestaurant = restaurantService.updateRestaurant(id, ownerId, restaurant);
            log.info("Restaurant updated successfully: {} (ID: {})", updatedRestaurant.getName(), updatedRestaurant.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                "Restaurant '" + updatedRestaurant.getName() + "' updated successfully!");

            return "redirect:/backoffice/restaurants";
        } catch (Exception e) {
            log.error("Error updating restaurant ID: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error updating restaurant: " + e.getMessage());

            return "redirect:/backoffice/restaurants/" + id + "/edit";
        }
    }

    // ==================== Delete Endpoint ====================

    /**
     * Delete a restaurant (POST for safety).
     * POST /backoffice/restaurants/{id}/delete
     *
     * @param id the restaurant ID
     * @param redirectAttributes for flash messages
     * @return redirect to list after deletion
     */
    @PostMapping("/{id}/delete")
    public String deleteRestaurant(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

        log.info("Attempting to delete restaurant ID: {}", id);

        try {
            Optional<RestaurantEntity> restaurantOpt = restaurantService.getRestaurantById(id);

            if (restaurantOpt.isEmpty()) {
                log.warn("Restaurant not found for deletion, ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Restaurant not found");
                return "redirect:/backoffice/restaurants";
            }

            String restaurantName = restaurantOpt.get().getName();

            // TODO: In future, verify owner matches current logged-in user
            Long ownerId = restaurantOpt.get().getOwner().getId();

            restaurantService.deleteRestaurant(id, ownerId);
            log.info("Restaurant deleted successfully: {} (ID: {})", restaurantName, id);

            redirectAttributes.addFlashAttribute("successMessage",
                "Restaurant '" + restaurantName + "' deleted successfully!");

            return "redirect:/backoffice/restaurants";
        } catch (Exception e) {
            log.error("Error deleting restaurant ID: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error deleting restaurant: " + e.getMessage());

            return "redirect:/backoffice/restaurants";
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get default owner ID for back office operations.
     * TODO: Replace with logged-in user ID once authentication is implemented.
     *
     * @return the owner ID (currently defaults to first user)
     * @throws RuntimeException if no users exist
     */
    private Long getDefaultOwnerId() {
        List<UserEntity> users = userService.getAllUsers();

        if (users.isEmpty()) {
            throw new RuntimeException("No users found in system. Please create a user first.");
        }

        return users.get(0).getId();
    }

}
