package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.CustomerEntity;
import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Customer business logic.
 * 
 * Handles:
 * - Customer profile creation and management
 * - Customer retrieval and filtering
 * - Customer updates with authorization checks
 * - Customer deletion with proper constraints
 * - Customer status management
 * - Loyalty points tracking and redemption
 * - Activity monitoring via last order date
 * - Authorization verification (user ownership)
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("CustomerRepository cannot be null");
        }
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new customer profile for a user.
     * 
     * Validates:
     * - User ID is not null
     * - Customer data is valid
     * - User doesn't already have a customer profile (one-to-one constraint)
     * 
     * @param userId the ID of the user
     * @param customer the customer data
     * @return the created customer entity
     * @throws IllegalArgumentException if validation fails or customer already exists
     */
    public CustomerEntity createCustomer(Long userId, CustomerEntity customer) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Check if customer profile already exists for this user
        if (customerRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Customer profile already exists for user: " + userId);
        }

        validateCustomerData(customer);

        // Set the user - assumes user exists and is validated by caller
        UserEntity user = new UserEntity();
        user.setId(userId);
        customer.setUser(user);

        // Set default values if not provided
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }
        if (customer.getLoyaltyPoints() == null) {
            customer.setLoyaltyPoints(0);
        }

        return customerRepository.save(customer);
    }

    /**
     * Retrieve a customer by ID.
     * 
     * @param customerId the ID of the customer
     * @return Optional containing the customer if found
     * @throws IllegalArgumentException if customer ID is null
     */
    public Optional<CustomerEntity> getCustomerById(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        return customerRepository.findById(customerId);
    }

    /**
     * Retrieve a customer for a specific user.
     * 
     * @param userId the ID of the user
     * @return Optional containing the customer if found
     * @throws IllegalArgumentException if user ID is null
     */
    public Optional<CustomerEntity> getCustomerByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return customerRepository.findByUserId(userId);
    }

    /**
     * Retrieve an active customer for a user.
     * 
     * @param userId the ID of the user
     * @return Optional containing the customer if found and active
     * @throws IllegalArgumentException if user ID is null
     */
    public Optional<CustomerEntity> getCustomerByUserIdAndActive(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return customerRepository.findByUserIdAndIsActive(userId, true);
    }

    /**
     * Retrieve a customer with authorization check.
     * Verifies that the customer belongs to the specified user.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @return the customer if found and authorized
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity getCustomerByIdAndUserId(Long customerId, Long userId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return customerRepository.findByIdAndUserId(customerId, userId)
            .orElseThrow(() -> new RuntimeException(
                "Customer not found or unauthorized: " + customerId + " for user: " + userId
            ));
    }

    /**
     * Retrieve all customers.
     * 
     * @return list of all customers
     */
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Retrieve all active customers.
     * 
     * @return list of active customers
     */
    public List<CustomerEntity> getActiveCustomers() {
        return customerRepository.findByIsActiveOrderByCreatedAtDesc(true);
    }

    /**
     * Retrieve all customers who prefer a specific restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return list of customers sorted by loyalty points
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public List<CustomerEntity> getCustomersByPreferredRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return customerRepository.findByPreferredRestaurantIdOrderByLoyaltyPointsDesc(restaurantId);
    }

    /**
     * Retrieve active customers who prefer a specific restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return list of active customers
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public List<CustomerEntity> getActiveCustomersByRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return customerRepository.findByPreferredRestaurantIdAndIsActiveOrderByLoyaltyPointsDesc(restaurantId, true);
    }

    /**
     * Retrieve customers with loyalty points above threshold (leaderboard).
     * 
     * @param minPoints the minimum points threshold
     * @return list of qualifying customers sorted by points
     * @throws IllegalArgumentException if min points is negative
     */
    public List<CustomerEntity> getLoyaltyLeaderboard(int minPoints) {
        if (minPoints < 0) {
            throw new IllegalArgumentException("Minimum points cannot be negative");
        }
        return customerRepository.findByLoyaltyPointsGreaterThanOrderByLoyaltyPointsDesc(minPoints);
    }

    /**
     * Retrieve customers with recent order activity.
     * 
     * @return list of customers with order history, recent first
     */
    public List<CustomerEntity> getActiveCustomersWithOrders() {
        return customerRepository.findByIsActiveAndLastOrderDateIsNotNullOrderByLastOrderDateDesc(true);
    }

    /**
     * Update a customer's profile.
     * Verifies that the customer belongs to the specified user before updating.
     * 
     * @param customerId the ID of the customer to update
     * @param userId the ID of the user
     * @param updatedCustomer the updated customer data
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity updateCustomer(Long customerId, Long userId, CustomerEntity updatedCustomer) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (updatedCustomer == null) {
            throw new IllegalArgumentException("Updated customer data cannot be null");
        }

        validateCustomerData(updatedCustomer);

        CustomerEntity existingCustomer = getCustomerByIdAndUserId(customerId, userId);

        // Update fields
        existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        existingCustomer.setPreferredRestaurant(updatedCustomer.getPreferredRestaurant());

        return customerRepository.save(existingCustomer);
    }

    /**
     * Delete a customer profile.
     * Verifies that the customer belongs to the specified user before deletion.
     * 
     * @param customerId the ID of the customer to delete
     * @param userId the ID of the user
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if customer not found or unauthorized
     */
    public void deleteCustomer(Long customerId, Long userId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Verify customer exists and belongs to user
        if (!customerRepository.existsByIdAndUserId(customerId, userId)) {
            throw new RuntimeException(
                "Customer not found or unauthorized: " + customerId + " for user: " + userId
            );
        }

        customerRepository.deleteByIdAndUserId(customerId, userId);
    }

    /**
     * Change a customer's active status.
     * Verifies authorization before updating status.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @param isActive the new active status
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity setCustomerStatus(Long customerId, Long userId, Boolean isActive) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("Active status cannot be null");
        }

        CustomerEntity customer = getCustomerByIdAndUserId(customerId, userId);
        customer.setIsActive(isActive);
        return customerRepository.save(customer);
    }

    /**
     * Set the customer's preferred restaurant.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @param restaurantId the ID of the preferred restaurant (can be null to clear)
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity setPreferredRestaurant(Long customerId, Long userId, Long restaurantId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        CustomerEntity customer = getCustomerByIdAndUserId(customerId, userId);

        if (restaurantId == null) {
            // Clear preferred restaurant
            customer.setPreferredRestaurant(null);
        } else {
            // Set preferred restaurant (assumes restaurant exists and is validated by caller)
            RestaurantEntity restaurant = new RestaurantEntity();
            restaurant.setId(restaurantId);
            customer.setPreferredRestaurant(restaurant);
        }

        return customerRepository.save(customer);
    }

    /**
     * Add loyalty points to a customer's account.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @param points the number of points to add
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are invalid or points negative
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity addLoyaltyPoints(Long customerId, Long userId, int points) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Cannot add negative points");
        }

        CustomerEntity customer = getCustomerByIdAndUserId(customerId, userId);
        customer.addLoyaltyPoints(points);
        return customerRepository.save(customer);
    }

    /**
     * Redeem loyalty points from a customer's account.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @param points the number of points to redeem
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are invalid or insufficient points
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity redeemLoyaltyPoints(Long customerId, Long userId, int points) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Cannot redeem negative points");
        }

        CustomerEntity customer = getCustomerByIdAndUserId(customerId, userId);
        customer.redeemLoyaltyPoints(points);
        return customerRepository.save(customer);
    }

    /**
     * Update the customer's last order date to now.
     * Called after customer successfully places an order.
     * 
     * @param customerId the ID of the customer
     * @param userId the ID of the user
     * @return the updated customer
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if customer not found or unauthorized
     */
    public CustomerEntity updateLastOrderDate(Long customerId, Long userId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        CustomerEntity customer = getCustomerByIdAndUserId(customerId, userId);
        customer.setLastOrderDate(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    /**
     * Get count of all active customers.
     * 
     * @return the number of active customers
     */
    public long getActiveCustomerCount() {
        return customerRepository.countByIsActive(true);
    }

    /**
     * Get count of customers for a restaurant preference.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of customers
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public long getCustomerCountByRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return customerRepository.countByPreferredRestaurantId(restaurantId);
    }

    /**
     * Check if a customer exists for a user.
     * 
     * @param userId the ID of the user
     * @return true if customer profile exists
     * @throws IllegalArgumentException if user ID is null
     */
    public boolean hasCustomerProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return customerRepository.existsByUserId(userId);
    }

    /**
     * Validate customer data.
     * Ensures all required fields are present and valid.
     * 
     * @param customer the customer to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCustomerData(CustomerEntity customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Validate optional phone number if provided
        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isBlank()) {
            if (customer.getPhoneNumber().length() < 10 || customer.getPhoneNumber().length() > 20) {
                throw new IllegalArgumentException("Phone number must be between 10 and 20 characters");
            }
        }

        // Validate loyalty points
        if (customer.getLoyaltyPoints() != null && customer.getLoyaltyPoints() < 0) {
            throw new IllegalArgumentException("Loyalty points cannot be negative");
        }

        if (customer.getLoyaltyPoints() != null && customer.getLoyaltyPoints() > 999999) {
            throw new IllegalArgumentException("Loyalty points exceed maximum allowed value");
        }
    }
}