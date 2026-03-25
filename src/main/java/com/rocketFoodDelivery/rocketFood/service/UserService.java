package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * UserService provides business logic for managing users.
 * Handles CRUD operations, validation, and user-related queries.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user.
     * @param user the UserEntity to create
     * @return the created UserEntity with generated ID
     * @throws IllegalArgumentException if user is null or invalid
     */
    public UserEntity createUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        validateUserData(user);
        return userRepository.save(user);
    }

    /**
     * Retrieve a user by ID.
     * @param id the user ID
     * @return the user if found
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if user not found
     */
    public UserEntity getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Retrieve a user by email.
     * @param email the user's email
     * @return Optional containing the user if found
     * @throws IllegalArgumentException if email is null
     */
    public Optional<UserEntity> getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieve all users sorted by ID in descending order.
     * @return list of all users
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    /**
     * Update an existing user.
     * @param id the user ID to update
     * @param updatedUser the updated user data
     * @return the updated user
     * @throws IllegalArgumentException if id or updatedUser is null
     * @throws RuntimeException if user not found
     */
    public UserEntity updateUser(Long id, UserEntity updatedUser) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (updatedUser == null) {
            throw new IllegalArgumentException("Updated user cannot be null");
        }
        
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        validateUserData(updatedUser);
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        return userRepository.save(existingUser);
    }

    /**
     * Delete a user by ID.
     * @param id the user ID to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if user not found
     */
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Validate user data before persistence.
     * Checks required fields and business rules.
     * @param user the UserEntity to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserData(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (user.getFirstName().length() < 2 || user.getFirstName().length() > 100) {
            throw new IllegalArgumentException("First name must be between 2 and 100 characters");
        }
        if (user.getLastName().length() < 2 || user.getLastName().length() > 100) {
            throw new IllegalArgumentException("Last name must be between 2 and 100 characters");
        }
        if (user.getPhoneNumber().length() < 10 || user.getPhoneNumber().length() > 20) {
            throw new IllegalArgumentException("Phone number must be between 10 and 20 characters");
        }
    }

    /**
     * Check if a user exists by email.
     * @param email the email to check
     * @return true if user exists with this email
     * @throws IllegalArgumentException if email is null
     */
    public boolean existsByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Get total count of users.
     * @return the number of users in the database
     */
    public long getUserCount() {
        return userRepository.count();
    }
}
