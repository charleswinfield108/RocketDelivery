package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an employee in the RocketFoodDelivery system.
 * 
 * Employees are associated with restaurants and have specific roles,
 * employment status, and contact information.
 * 
 * Schema: employees table with indexes on restaurant_id and status
 */
@Entity
@Table(
    name = "employees",
    indexes = {
        @Index(name = "idx_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_employment_status", columnList = "employment_status"),
        @Index(name = "idx_restaurant_status", columnList = "restaurant_id, employment_status")
    }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {

    /**
     * Unique identifier for the employee.
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Reference to the restaurant this employee works for.
     * ManyToOne relationship - multiple employees per restaurant.
     * Cannot be null - every employee must belong to a restaurant.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "restaurant_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_employee_restaurant")
    )
    @NotNull(message = "Restaurant cannot be null")
    private RestaurantEntity restaurant;

    /**
     * Employee's first name.
     * Required field with length constraints.
     */
    @Column(name = "first_name", length = 100, nullable = false)
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    /**
     * Employee's last name.
     * Required field with length constraints.
     */
    @Column(name = "last_name", length = 100, nullable = false)
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    /**
     * Employee's email address.
     * Must be unique across all employees and follow email format.
     */
    @Column(name = "email", length = 255, nullable = false, unique = true)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    /**
     * Employee's phone number.
     * Optional contact information with length validation.
     */
    @Column(name = "phone_number", length = 20)
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phoneNumber;

    /**
     * Employee's job role/position.
     * Examples: MANAGER, CHEF, DELIVERY_DRIVER, CASHIER, etc.
     * Required field that classifies the employee's responsibilities.
     */
    @Column(name = "role", length = 50, nullable = false)
    @NotBlank(message = "Role cannot be blank")
    @Size(min = 3, max = 50, message = "Role must be between 3 and 50 characters")
    private String role;

    /**
     * Employment status of the employee.
     * Tracks current employment state: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
     * Defaults to ACTIVE for new employees.
     */
    @Column(name = "employment_status", length = 50, nullable = false)
    @NotBlank(message = "Employment status cannot be blank")
    @Size(min = 3, max = 50, message = "Employment status must be between 3 and 50 characters")
    private String employmentStatus = "ACTIVE";

    /**
     * Date the employee was hired.
     * Used for employment history and tenure tracking.
     * Cannot be in the future.
     */
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date cannot be null")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    /**
     * Employee's salary/compensation.
     * Optional field stored with precision for financial accuracy.
     * Must be a positive value if provided.
     */
    @Column(name = "salary", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    @DecimalMax(value = "999999.99", message = "Salary exceeds maximum allowed")
    private BigDecimal salary;

    /**
     * Employee's street address.
     * Optional field for storing employee's residential address.
     */
    @Column(name = "street", length = 255)
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;

    /**
     * Employee's city.
     * Optional field, paired with street if address is provided.
     */
    @Column(name = "city", length = 100)
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    /**
     * Employee's state/province.
     * Optional field, paired with city if address is provided.
     */
    @Column(name = "state", length = 100)
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    /**
     * Employee's postal/zip code.
     * Optional field for complete address information.
     */
    @Column(name = "zip_code", length = 20)
    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zipCode;

    /**
     * Timestamp for when this employee record was created.
     * Automatically set by Hibernate using @CreationTimestamp.
     * Used for audit trail and history tracking.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp for when this employee record was last modified.
     * Automatically updated by Hibernate using @UpdateTimestamp.
     * Used for audit trail and optimistic locking.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Returns the employee's full name by combining first and last names.
     * 
     * @return formatted full name string
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the employee's full address by combining street, city, state, and zip code.
     * Handles null values gracefully.
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

    /**
     * Checks if the employee is currently active.
     * 
     * @return true if employment status is ACTIVE
     */
    public boolean isActive() {
        return "ACTIVE".equals(employmentStatus);
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "id=" + id +
                ", restaurantId=" + (restaurant != null ? restaurant.getId() : null) +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", hireDate=" + hireDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
