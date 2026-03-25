package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductEntity represents a product/menu item in the RocketFoodDelivery system.
 *
 * Placeholder entity created to support ProductOrderEntity relationships.
 * Will be fully implemented in future features.
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_restaurant_id", columnList = "restaurant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    /**
     * Unique identifier for the product.
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the restaurant that offers this product.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @NotNull(message = "Restaurant cannot be null")
    private RestaurantEntity restaurant;

    /**
     * Product name.
     */
    @Column(name = "name", length = 255, nullable = false)
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    /**
     * Product description.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Current price of the product.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price cannot exceed 99999.99")
    private BigDecimal price;

    /**
     * Whether this product is currently available for ordering.
     */
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    /**
     * Timestamp when this product was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this product was last modified.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
