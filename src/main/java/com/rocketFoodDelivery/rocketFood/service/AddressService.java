package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.AddressEntity;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * AddressService provides business logic for managing user addresses.
 * Handles CRUD operations, validation, authorization, and address-related queries.
 * 
 * Key responsibilities:
 * - Create and validate new addresses
 * - Retrieve addresses with ownership verification
 * - Update addresses with authorization checks
 * - Delete addresses and manage default address logic
 * - Enforce constraints (e.g., only one default address per user)
 */
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new address for a user.
     * 
     * @param userId the ID of the user
     * @param address the address to create
     * @return the created address
     * @throws IllegalArgumentException if address data is invalid
     * @throws RuntimeException if user not found
     */
    public AddressEntity createAddress(Long userId, AddressEntity address) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        // Verify user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Validate address data
        validateAddressData(address);

        // Set the user
        address.setUser(user);

        // If this is set as default, unset other defaults for this user
        if (address.getIsDefault() != null && address.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(existingDefault -> {
                existingDefault.setIsDefault(false);
                addressRepository.save(existingDefault);
            });
        } else {
            address.setIsDefault(false);
        }

        return addressRepository.save(address);
    }

    /**
     * Retrieve all addresses for a user.
     * 
     * @param userId the ID of the user
     * @return list of addresses, sorted by default first
     * @throws IllegalArgumentException if userId is null
     * @throws RuntimeException if user not found
     */
    public List<AddressEntity> getAddressesByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtAsc(userId);
    }

    /**
     * Get the default address for a user.
     * 
     * @param userId the ID of the user
     * @return the default address
     * @throws IllegalArgumentException if userId is null
     * @throws RuntimeException if no default address found
     */
    public AddressEntity getDefaultAddress(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("No default address found for user ID: " + userId));
    }

    /**
     * Get a specific address by ID.
     * 
     * @param addressId the ID of the address
     * @return Optional containing the address if found
     * @throws IllegalArgumentException if addressId is null
     */
    public Optional<AddressEntity> getAddressById(Long addressId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }

        return addressRepository.findById(addressId);
    }

    /**
     * Get a specific address, verifying user ownership.
     * 
     * @param addressId the ID of the address
     * @param userId the ID of the user
     * @return the address if found and belongs to user
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if address not found or doesn't belong to user
     */
    public AddressEntity getAddressByIdAndUserId(Long addressId, Long userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));
    }

    /**
     * Update an existing address.
     * 
     * @param addressId the ID of the address to update
     * @param userId the ID of the address owner (for authorization)
     * @param updatedAddress the updated address data
     * @return the updated address
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if address not found or doesn't belong to user
     */
    public AddressEntity updateAddress(Long addressId, Long userId, AddressEntity updatedAddress) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (updatedAddress == null) {
            throw new IllegalArgumentException("Updated address cannot be null");
        }

        // Verify ownership and get existing address
        AddressEntity existingAddress = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));

        // Validate new address data
        validateAddressData(updatedAddress);

        // Update fields
        existingAddress.setStreet(updatedAddress.getStreet());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setState(updatedAddress.getState());
        existingAddress.setZipCode(updatedAddress.getZipCode());
        existingAddress.setCountry(updatedAddress.getCountry());
        existingAddress.setAddressType(updatedAddress.getAddressType());

        // Handle default flag changes
        if (updatedAddress.getIsDefault() != null && updatedAddress.getIsDefault() && !existingAddress.getIsDefault()) {
            // Setting this as default - unset others
            addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(currentDefault -> {
                currentDefault.setIsDefault(false);
                addressRepository.save(currentDefault);
            });
            existingAddress.setIsDefault(true);
        } else if ((updatedAddress.getIsDefault() == null || !updatedAddress.getIsDefault()) && existingAddress.getIsDefault()) {
            // Unsetting as default
            existingAddress.setIsDefault(false);
        }

        return addressRepository.save(existingAddress);
    }

    /**
     * Delete an address.
     * 
     * @param addressId the ID of the address to delete
     * @param userId the ID of the address owner (for authorization)
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if address not found or doesn't belong to user
     */
    public void deleteAddress(Long addressId, Long userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Verify ownership
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));

        // Note: Log warning if deleting default address
        if (address.getIsDefault()) {
            System.out.println("WARNING: Deleting default address for user " + userId);
        }

        addressRepository.delete(address);
    }

    /**
     * Set an address as the default address for a user.
     * 
     * @param addressId the ID of the address to set as default
     * @param userId the ID of the user
     * @return the updated address
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if address not found or doesn't belong to user
     */
    public AddressEntity setDefaultAddress(Long addressId, Long userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Get the address to set as default
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));

        // Unset current default if exists
        addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(currentDefault -> {
            currentDefault.setIsDefault(false);
            addressRepository.save(currentDefault);
        });

        // Set this as default
        address.setIsDefault(true);
        return addressRepository.save(address);
    }

    /**
     * Get addresses by type for a user.
     * 
     * @param userId the ID of the user
     * @param addressType the type of address (HOME, WORK, OTHER, etc.)
     * @return list of addresses matching the type
     * @throws IllegalArgumentException if parameters are null
     */
    public List<AddressEntity> getAddressesByType(Long userId, String addressType) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (addressType == null || addressType.isBlank()) {
            throw new IllegalArgumentException("Address type cannot be null or empty");
        }

        return addressRepository.findByUserIdAndAddressType(userId, addressType);
    }

    /**
     * Check if user has addresses.
     * 
     * @param userId the ID of the user
     * @return true if user has at least one address
     * @throws IllegalArgumentException if userId is null
     */
    public boolean hasAddresses(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return addressRepository.existsByUserId(userId);
    }

    /**
     * Get count of addresses for a user.
     * 
     * @param userId the ID of the user
     * @return number of addresses owned by user
     * @throws IllegalArgumentException if userId is null
     */
    public long getAddressCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return addressRepository.countByUserId(userId);
    }

    /**
     * Validate address data before persistence.
     * Checks required fields and business rules.
     * 
     * @param address the address to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAddressData(AddressEntity address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (address.getStreet() == null || address.getStreet().isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        if (address.getStreet().length() < 2 || address.getStreet().length() > 255) {
            throw new IllegalArgumentException("Street must be between 2 and 255 characters");
        }

        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (address.getCity().length() < 2 || address.getCity().length() > 100) {
            throw new IllegalArgumentException("City must be between 2 and 100 characters");
        }

        if (address.getState() == null || address.getState().isBlank()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }
        if (address.getState().length() < 2 || address.getState().length() > 100) {
            throw new IllegalArgumentException("State must be between 2 and 100 characters");
        }

        if (address.getZipCode() == null || address.getZipCode().isBlank()) {
            throw new IllegalArgumentException("ZIP code cannot be null or empty");
        }
        if (address.getZipCode().length() < 3 || address.getZipCode().length() > 20) {
            throw new IllegalArgumentException("ZIP code must be between 3 and 20 characters");
        }

        if (address.getCountry() == null || address.getCountry().isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
        if (address.getCountry().length() < 2 || address.getCountry().length() > 100) {
            throw new IllegalArgumentException("Country must be between 2 and 100 characters");
        }

        if (address.getAddressType() != null && address.getAddressType().length() > 50) {
            throw new IllegalArgumentException("Address type must not exceed 50 characters");
        }
    }
}