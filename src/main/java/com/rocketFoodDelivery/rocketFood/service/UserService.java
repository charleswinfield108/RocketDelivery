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
     */
    public UserEntity createUser(UserEntity user) {
        validateUserData(user);
        return userRepository.save(user);
    }

    /**
     * Retrieve a user by ID.
     * @param id the user ID
     * @return Optional containing the user if found
     */
    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieve a user by email.
     * @param email the user's email
     * @return Optional containing the user if found
     */
    public Optional<UserEntity> getUserByEmail(String email) {
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
     * @return Optional containing the updated user if found
     */
    public Optional<UserEntity> updateUser(Long id, UserEntity updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            validateUserData(updatedUser);
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            return userRepository.save(existingUser);
        });
    }

    /**
     * Delete a user by ID.
     * @param id the user ID to delete
     * @return true if user was deleted, false if not found
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
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
     */
    public boolean existsByEmail(String email) {
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
