package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UserEntity represents a user in the RocketDelivery system.
 * Maps to the 'users' table in the database with proper JPA annotations and validations.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Unique identifier for the user.
     * Auto-generated using MySQL AUTO_INCREMENT strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * User's email address.
     * Must be unique and valid email format.
     */
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * User's first name.
     * Must be between 2 and 100 characters.
     */
    @NotNull(message = "First name cannot be null")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * User's last name.
     * Must be between 2 and 100 characters.
     */
    @NotNull(message = "Last name cannot be null")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * User's phone number.
     * Must be between 10 and 20 characters.
     */
    @NotNull(message = "Phone number cannot be null")
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * Timestamp when the user record was created.
     * Automatically set by Hibernate on first persistence.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user record was last updated.
     * Automatically updated by Hibernate on modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
