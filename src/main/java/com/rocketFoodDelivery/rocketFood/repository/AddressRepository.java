package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AddressRepository provides data access methods for Address entities.
 * Extends JpaRepository for CRUD operations and custom query methods.
 * 
 * Handles:
 * - Creating and retrieving user addresses
 * - Finding default addresses
 * - Address type filtering
 * - Address deletion with authorization checks
 */
@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    /**
     * Find all addresses for a specific user.
     * 
     * @param userId the user's ID
     * @return list of addresses owned by the user
     */
    List<AddressEntity> findByUserId(Long userId);

    /**
     * Find all addresses for a user, sorted by default flag first.
     * 
     * @param userId the user's ID
     * @return list of addresses ordered by isDefault (true first), then by creation date
     */
    List<AddressEntity> findByUserIdOrderByIsDefaultDescCreatedAtAsc(Long userId);

    /**
     * Find the default address for a specific user.
     * 
     * @param userId the user's ID
     * @return Optional containing the default address if set
     */
    Optional<AddressEntity> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Find addresses by user ID and address type.
     * Useful for filtering HOME, WORK, OTHER addresses.
     * 
     * @param userId the user's ID
     * @param addressType the type of address (HOME, WORK, OTHER, etc.)
     * @return list of addresses matching the type
     */
    List<AddressEntity> findByUserIdAndAddressType(Long userId, String addressType);

    /**
     * Check if a user has a default address set.
     * 
     * @param userId the user's ID
     * @return true if user has a default address, false otherwise
     */
    boolean existsByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Delete an address by user ID and address ID.
     * Used to ensure the address belongs to the user before deletion.
     * 
     * @param userId the user's ID
     * @param addressId the address's ID
     * @return number of records deleted
     */
    long deleteByUserIdAndId(Long userId, Long addressId);

    /**
     * Find an address by ID and user ID.
     * Ensures user authorization - the address must belong to the user.
     * 
     * @param addressId the address's ID
     * @param userId the user's ID
     * @return Optional containing the address if found and belongs to user
     */
    Optional<AddressEntity> findByIdAndUserId(Long addressId, Long userId);

    /**
     * Count total addresses for a specific user.
     * 
     * @param userId the user's ID
     * @return number of addresses owned by the user
     */
    long countByUserId(Long userId);

    /**
     * Check if a user has any addresses.
     * 
     * @param userId the user's ID
     * @return true if user has at least one address
     */
    boolean existsByUserId(Long userId);
}