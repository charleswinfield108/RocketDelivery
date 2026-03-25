package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AddressEntity represents a user address in the RocketDelivery system.
 * Each user can have multiple addresses with one marked as default.
 * 
 * Features:
 * - Complete address information (street, city, state, zip, country)
 * - Address type classification (HOME, WORK, OTHER)
 * - Default address flag for convenient retrieval
 * - Automatic timestamp tracking
 * - Cascade delete when associated user is deleted
 */
@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_default", columnList = "user_id, is_default")
})
@Data
@Getter
@Setter
public class AddressEntity {

    /**
     * Unique identifier for the address.
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user who owns this address.
     * Many addresses belong to one user.
     * Cascade delete ensures addresses are removed when user is deleted.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_user_id"))
    @NotNull(message = "User ID cannot be null")
    private UserEntity user;

    /**
     * Street address line.
     * Required field, must be 2-255 characters.
     */
    @Column(name = "street", nullable = false, length = 255)
    @NotBlank(message = "Street cannot be null or empty")
    @Size(min = 2, max = 255, message = "Street must be between 2 and 255 characters")
    private String street;

    /**
     * City name.
     * Required field, must be 2-100 characters.
     */
    @Column(name = "city", nullable = false, length = 100)
    @NotBlank(message = "City cannot be null or empty")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    /**
     * State or province name.
     * Required field, must be 2-100 characters.
     */
    @Column(name = "state", nullable = false, length = 100)
    @NotBlank(message = "State cannot be null or empty")
    @Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
    private String state;

    /**
     * Postal code or ZIP code.
     * Required field, must be 3-20 characters.
     */
    @Column(name = "zip_code", nullable = false, length = 20)
    @NotBlank(message = "ZIP code cannot be null or empty")
    @Size(min = 3, max = 20, message = "ZIP code must be between 3 and 20 characters")
    private String zipCode;

    /**
     * Country name.
     * Required field, must be 2-100 characters.
     */
    @Column(name = "country", nullable = false, length = 100)
    @NotBlank(message = "Country cannot be null or empty")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    /**
     * Address type classification.
     * Optional field: HOME, WORK, OTHER, etc.
     * Maximum 50 characters.
     */
    @Column(name = "address_type", length = 50)
    @Size(max = 50, message = "Address type must not exceed 50 characters")
    private String addressType;

    /**
     * Flag indicating if this is the user's default address.
     * Only one address per user should have this flag set to true.
     * Defaults to false for new addresses.
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * Timestamp when the address was created.
     * Automatically set by Hibernate on entity insertion.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the address was last updated.
     * Automatically updated by Hibernate on entity modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA.
     */
    public AddressEntity() {
    }

    /**
     * Constructor with required fields.
     * 
     * @param user the user who owns this address
     * @param street street address line
     * @param city city name
     * @param state state or province
     * @param zipCode postal code
     * @param country country name
     */
    public AddressEntity(UserEntity user, String street, String city, String state, String zipCode, String country) {
        this.user = user;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.isDefault = false;
    }

    /**
     * Constructor with all fields.
     * 
     * @param user the user who owns this address
     * @param street street address line
     * @param city city name
     * @param state state or province
     * @param zipCode postal code
     * @param country country name
     * @param addressType type of address
     * @param isDefault whether this is the default address
     */
    public AddressEntity(UserEntity user, String street, String city, String state, String zipCode, String country, String addressType, Boolean isDefault) {
        this.user = user;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.addressType = addressType;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    /**
     * Returns a user-friendly representation of the address.
     * 
     * @return formatted full address
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
    }

    @Override
    public String toString() {
        return "AddressEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", country='" + country + '\'' +
                ", addressType='" + addressType + '\'' +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
