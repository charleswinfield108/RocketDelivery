package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a restaurant in the RocketFoodDelivery system.
 * 
 * Stores restaurant information including basic details and operational status.
 */
@Entity
@Table(
    name = "restaurants",
    indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_is_active", columnList = "is_active")
    }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEntity {

    /**
     * Unique identifier for the restaurant.
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Restaurant name.
     * Required field, must be unique.
     */
    @Column(name = "name", length = 255, nullable = false, unique = true)
    @NotBlank(message = "Restaurant name cannot be blank")
    @Size(min = 2, max = 255, message = "Restaurant name must be between 2 and 255 characters")
    private String name;

    /**
     * Restaurant description.
     * Optional field for additional details.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Restaurant's street address.
     */
    @Column(name = "street", length = 255)
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;

    /**
     * Restaurant's city.
     */
    @Column(name = "city", length = 100)
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    /**
     * Restaurant's state/province.
     */
    @Column(name = "state", length = 100)
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    /**
     * Restaurant's postal/zip code.
     */
    @Column(name = "zip_code", length = 20)
    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zipCode;

    /**
     * Restaurant's country.
     */
    @Column(name = "country", length = 100)
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    /**
     * Restaurant's phone number.
     */
    @Column(name = "phone_number", length = 20)
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phoneNumber;

    /**
     * Restaurant's email address.
     */
    @Column(name = "email", length = 255)
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Whether the restaurant is currently active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Timestamp for when this restaurant was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp for when this restaurant was last modified.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Get the restaurant's full address.
     * 
     * @return formatted address string
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (street != null && !street.isBlank()) {
            address.append(street);
        }
        if (city != null && !city.isBlank()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (state != null && !state.isBlank()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        if (zipCode != null && !zipCode.isBlank()) {
            if (address.length() > 0) address.append(" ");
            address.append(zipCode);
        }
        return address.toString();
    }

    @Override
    public String toString() {
        return "RestaurantEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
