package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for EmployeeEntity.
 * 
 * Provides data access methods for employee CRUD operations and custom queries
 * with authorization checks to ensure data integrity and security.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    /**
     * Find all employees by restaurant ID.
     * 
     * @param restaurantId the ID of the restaurant
     * @return list of employees working for the restaurant
     */
    List<EmployeeEntity> findByRestaurantId(Long restaurantId);

    /**
     * Find all employees by restaurant ID, ordered by last name then first name.
     * 
     * @param restaurantId the ID of the restaurant
     * @return sorted list of employees
     */
    List<EmployeeEntity> findByRestaurantIdOrderByLastNameAscFirstNameAsc(Long restaurantId);

    /**
     * Find all employees by restaurant ID and employment status.
     * Useful for filtering active vs inactive employees.
     * 
     * @param restaurantId the ID of the restaurant
     * @param employmentStatus the employment status to filter by
     * @return list of employees matching both criteria
     */
    List<EmployeeEntity> findByRestaurantIdAndEmploymentStatus(Long restaurantId, String employmentStatus);

    /**
     * Find all active employees for a specific restaurant.
     * Convenience method for filtering ACTIVE status only.
     * 
     * @param restaurantId the ID of the restaurant
     * @return list of active employees
     */
    List<EmployeeEntity> findByRestaurantIdAndEmploymentStatusOrderByLastNameAscFirstNameAsc(
        Long restaurantId,
        String employmentStatus
    );

    /**
     * Find employees by restaurant and role.
     * Allows filtering employees by their job position.
     * 
     * @param restaurantId the ID of the restaurant
     * @param role the role/position to filter by
     * @return list of employees with the specified role
     */
    List<EmployeeEntity> findByRestaurantIdAndRole(Long restaurantId, String role);

    /**
     * Find a specific employee by ID and restaurant ID.
     * Authorization check: verifies that the employee belongs to the restaurant.
     * 
     * @param employeeId the ID of the employee
     * @param restaurantId the ID of the restaurant
     * @return Optional containing the employee if found and authorized
     */
    Optional<EmployeeEntity> findByIdAndRestaurantId(Long employeeId, Long restaurantId);

    /**
     * Find an employee by email address.
     * Email is unique across the system.
     * 
     * @param email the employee's email
     * @return Optional containing the employee if found
     */
    Optional<EmployeeEntity> findByEmail(String email);

    /**
     * Find an employee by email and restaurant ID.
     * Additional check to ensure email belongs to the correct restaurant.
     * 
     * @param email the employee's email
     * @param restaurantId the ID of the restaurant
     * @return Optional containing the employee if found
     */
    Optional<EmployeeEntity> findByEmailAndRestaurantId(String email, Long restaurantId);

    /**
     * Delete an employee by ID and restaurant ID.
     * Authorization check: ensures the employee belongs to the restaurant before deletion.
     * 
     * @param employeeId the ID of the employee
     * @param restaurantId the ID of the restaurant
     * @return the number of employees deleted (0 or 1)
     */
    long deleteByIdAndRestaurantId(Long employeeId, Long restaurantId);

    /**
     * Count employees by restaurant ID.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of employees for the restaurant
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Count active employees by restaurant ID.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of active employees
     */
    long countByRestaurantIdAndEmploymentStatus(Long restaurantId, String employmentStatus);

    /**
     * Check if an employee exists for a restaurant by ID.
     * 
     * @param employeeId the ID of the employee
     * @param restaurantId the ID of the restaurant
     * @return true if employee exists and belongs to restaurant
     */
    boolean existsByIdAndRestaurantId(Long employeeId, Long restaurantId);

    /**
     * Check if an email is already taken.
     * 
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}
