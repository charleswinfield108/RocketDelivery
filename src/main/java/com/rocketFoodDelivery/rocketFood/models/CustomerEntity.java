package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a customer in the RocketFoodDelivery system.
 * 
 * Each customer is associated with exactly one user and has profile information
 * including contact details, loyalty points, and order preferences.
 * 
 * Schema: customers table with OneToOne user relationship and ManyToOne preferred restaurant
 */
@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_preferred_restaurant", columnList = "preferred_restaurant_id"),
        @Index(name = "idx_loyalty_points", columnList = "loyalty_points")
    }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {

    /**
     * Unique identifier for the customer.
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Reference to the user account for this customer.
     * OneToOne relationship - each customer belongs to exactly one user.
     * Cannot be null - every customer must have a user account.
     * Unique constraint ensures one customer per user.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_customer_user")
    )
    @NotNull(message = "User cannot be null")
    private UserEntity user;

    /**
     * Customer's phone number.
     * Optional contact information with length constraints.
     */
    @Column(name = "phone_number", length = 20)
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phoneNumber;

    /**
     * Customer's accumulated loyalty/reward points.
     * Non-negative integer that can be redeemed for discounts or rewards.
     * Defaults to 0 for new customers.
     */
    @Column(name = "loyalty_points", nullable = false)
    @Min(value = 0, message = "Loyalty points cannot be negative")
    @Max(value = 999999, message = "Loyalty points exceed maximum allowed")
    private Integer loyaltyPoints = 0;

    /**
     * Customer's active status.
     * Indicates whether the customer account is active and can place orders.
     * Defaults to TRUE for new customer accounts.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Reference to the customer's preferred restaurant.
     * ManyToOne relationship - multiple customers can prefer the same restaurant.
     * Optional - customer may not have a preferred restaurant.
     * Can be null if no preference is set.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
        name = "preferred_restaurant_id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_customer_preferred_restaurant")
    )
    private RestaurantEntity preferredRestaurant;

    /**
     * Timestamp of the customer's last order.
     * Used to track customer activity and engagement.
     * Optional - null if customer has never placed an order.
     */
    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    /**
     * Timestamp for when this customer record was created.
     * Automatically set by Hibernate using @CreationTimestamp.
     * Used for audit trail and history tracking.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp for when this customer record was last modified.
     * Automatically updated by Hibernate using @UpdateTimestamp.
     * Used for audit trail and optimistic locking.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Checks if the customer is eligible for loyalty point redemption.
     * Customer must be active and have sufficient points.
     * 
     * @return true if customer has loyalty points to redeem
     */
    public boolean isLoyaltyEligible() {
        return isActive() && loyaltyPoints != null && loyaltyPoints > 0;
    }

    /**
     * Adds loyalty points to the customer's account.
     * Points accumulate for rewards and discounts.
     * 
     * @param points the number of points to add
     * @throws IllegalArgumentException if points are negative
     */
    public void addLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot add negative points");
        }
        this.loyaltyPoints = (this.loyaltyPoints != null ? this.loyaltyPoints : 0) + points;
    }

    /**
     * Redeems loyalty points from the customer's account.
     * Points must be available and customer must be eligible.
     * 
     * @param points the number of points to redeem
     * @throws IllegalArgumentException if points exceed available balance
     */
    public void redeemLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot redeem negative points");
        }
        int currentPoints = this.loyaltyPoints != null ? this.loyaltyPoints : 0;
        if (points > currentPoints) {
            throw new IllegalArgumentException(
                "Insufficient loyalty points: have " + currentPoints + ", attempting to redeem " + points
            );
        }
        this.loyaltyPoints = currentPoints - points;
    }

    /**
     * Checks if the customer has a preferred restaurant set.
     * 
     * @return true if preferred restaurant is not null
     */
    public boolean hasPreferredRestaurant() {
        return preferredRestaurant != null;
    }

    /**
     * Checks if the customer is currently active.
     * 
     * @return true if isActive is TRUE
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    @Override
    public String toString() {
        return "CustomerEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                ", isActive=" + isActive +
                ", preferredRestaurantId=" + (preferredRestaurant != null ? preferredRestaurant.getId() : null) +
                ", lastOrderDate=" + lastOrderDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
