package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.EmployeeEntity;
import com.rocketFoodDelivery.rocketFood.models.RestaurantEntity;
import com.rocketFoodDelivery.rocketFood.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 * 
 * Handles:
 * - Employee creation with validation
 * - Employee retrieval and filtering
 * - Employee updates with authorization checks
 * - Employee deletion with proper constraints
 * - Employee status management
 * - Authorization verification (restaurant ownership)
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        if (employeeRepository == null) {
            throw new IllegalArgumentException("EmployeeRepository cannot be null");
        }
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee for a restaurant.
     * 
     * Validates:
     * - Restaurant is not null
     * - Employee data is valid
     * - Email is not already in use
     * 
     * @param restaurantId the ID of the restaurant
     * @param employee the employee data
     * @return the created employee entity
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if restaurant not found
     */
    public EmployeeEntity createEmployee(Long restaurantId, EmployeeEntity employee) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        validateEmployeeData(employee);

        // Check if email is already in use
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + employee.getEmail());
        }

        // Set the restaurant - assumes restaurant exists and is validated by caller
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(restaurantId);
        employee.setRestaurant(restaurant);

        // Set default status if not provided
        if (employee.getEmploymentStatus() == null || employee.getEmploymentStatus().isBlank()) {
            employee.setEmploymentStatus("ACTIVE");
        }

        return employeeRepository.save(employee);
    }

    /**
     * Retrieve all employees for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return sorted list of employees
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public List<EmployeeEntity> getEmployeesByRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return employeeRepository.findByRestaurantIdOrderByLastNameAscFirstNameAsc(restaurantId);
    }

    /**
     * Retrieve active employees for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return list of active employees, sorted by name
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public List<EmployeeEntity> getActiveEmployeesByRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return employeeRepository.findByRestaurantIdAndEmploymentStatusOrderByLastNameAscFirstNameAsc(
            restaurantId,
            "ACTIVE"
        );
    }

    /**
     * Retrieve employees by role within a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @param role the role to filter by
     * @return list of employees with the specified role
     * @throws IllegalArgumentException if parameters are null
     */
    public List<EmployeeEntity> getEmployeesByRole(Long restaurantId, String role) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }
        return employeeRepository.findByRestaurantIdAndRole(restaurantId, role);
    }

    /**
     * Retrieve employees by employment status.
     * 
     * @param restaurantId the ID of the restaurant
     * @param employmentStatus the status to filter by
     * @return list of employees with the specified status
     * @throws IllegalArgumentException if parameters are null
     */
    public List<EmployeeEntity> getEmployeesByStatus(Long restaurantId, String employmentStatus) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (employmentStatus == null || employmentStatus.isBlank()) {
            throw new IllegalArgumentException("Employment status cannot be null or blank");
        }
        return employeeRepository.findByRestaurantIdAndEmploymentStatus(restaurantId, employmentStatus);
    }

    /**
     * Get a specific employee by ID.
     * 
     * @param employeeId the ID of the employee
     * @return Optional containing the employee if found
     * @throws IllegalArgumentException if employee ID is null
     */
    public Optional<EmployeeEntity> getEmployeeById(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        return employeeRepository.findById(employeeId);
    }

    /**
     * Get a specific employee with authorization check.
     * Verifies that the employee belongs to the specified restaurant.
     * 
     * @param employeeId the ID of the employee
     * @param restaurantId the ID of the restaurant
     * @return the employee if found and authorized
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if employee not found or unauthorized
     */
    public EmployeeEntity getEmployeeByIdAndRestaurant(Long employeeId, Long restaurantId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }

        return employeeRepository.findByIdAndRestaurantId(employeeId, restaurantId)
            .orElseThrow(() -> new RuntimeException(
                "Employee not found or unauthorized: " + employeeId + " for restaurant: " + restaurantId
            ));
    }

    /**
     * Find an employee by email.
     * 
     * @param email the employee's email address
     * @return Optional containing the employee if found
     * @throws IllegalArgumentException if email is null
     */
    public Optional<EmployeeEntity> getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        return employeeRepository.findByEmail(email);
    }

    /**
     * Update an employee's information.
     * Verifies that the employee belongs to the specified restaurant before updating.
     * 
     * @param employeeId the ID of the employee to update
     * @param restaurantId the ID of the restaurant
     * @param updatedEmployee the updated employee data
     * @return the updated employee
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if employee not found or unauthorized
     */
    public EmployeeEntity updateEmployee(Long employeeId, Long restaurantId, EmployeeEntity updatedEmployee) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (updatedEmployee == null) {
            throw new IllegalArgumentException("Updated employee data cannot be null");
        }

        validateEmployeeData(updatedEmployee);

        EmployeeEntity existingEmployee = getEmployeeByIdAndRestaurant(employeeId, restaurantId);

        // Check if email is being changed to an already-used email
        if (!existingEmployee.getEmail().equals(updatedEmployee.getEmail()) &&
            employeeRepository.existsByEmail(updatedEmployee.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + updatedEmployee.getEmail());
        }

        // Update fields
        existingEmployee.setFirstName(updatedEmployee.getFirstName());
        existingEmployee.setLastName(updatedEmployee.getLastName());
        existingEmployee.setEmail(updatedEmployee.getEmail());
        existingEmployee.setPhoneNumber(updatedEmployee.getPhoneNumber());
        existingEmployee.setRole(updatedEmployee.getRole());
        existingEmployee.setEmploymentStatus(updatedEmployee.getEmploymentStatus());
        existingEmployee.setHireDate(updatedEmployee.getHireDate());
        existingEmployee.setSalary(updatedEmployee.getSalary());
        existingEmployee.setStreet(updatedEmployee.getStreet());
        existingEmployee.setCity(updatedEmployee.getCity());
        existingEmployee.setState(updatedEmployee.getState());
        existingEmployee.setZipCode(updatedEmployee.getZipCode());

        return employeeRepository.save(existingEmployee);
    }

    /**
     * Delete an employee.
     * Verifies that the employee belongs to the specified restaurant before deletion.
     * 
     * @param employeeId the ID of the employee to delete
     * @param restaurantId the ID of the restaurant
     * @throws IllegalArgumentException if parameters are null
     * @throws RuntimeException if employee not found or unauthorized
     */
    public void deleteEmployee(Long employeeId, Long restaurantId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }

        // Verify employee exists and belongs to restaurant
        if (!employeeRepository.existsByIdAndRestaurantId(employeeId, restaurantId)) {
            throw new RuntimeException(
                "Employee not found or unauthorized: " + employeeId + " for restaurant: " + restaurantId
            );
        }

        employeeRepository.deleteByIdAndRestaurantId(employeeId, restaurantId);
    }

    /**
     * Change an employee's employment status.
     * Verifies authorization before updating status.
     * 
     * @param employeeId the ID of the employee
     * @param restaurantId the ID of the restaurant
     * @param newStatus the new employment status
     * @return the updated employee
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if employee not found or unauthorized
     */
    public EmployeeEntity setEmploymentStatus(Long employeeId, Long restaurantId, String newStatus) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Employment status cannot be null or blank");
        }

        EmployeeEntity employee = getEmployeeByIdAndRestaurant(employeeId, restaurantId);
        employee.setEmploymentStatus(newStatus);
        return employeeRepository.save(employee);
    }

    /**
     * Get count of employees for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of employees
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public long getEmployeeCount(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return employeeRepository.countByRestaurantId(restaurantId);
    }

    /**
     * Get count of active employees for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the number of active employees
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public long getActiveEmployeeCount(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return employeeRepository.countByRestaurantIdAndEmploymentStatus(restaurantId, "ACTIVE");
    }

    /**
     * Check if restaurant has any employees.
     * 
     * @param restaurantId the ID of the restaurant
     * @return true if restaurant has at least one employee
     * @throws IllegalArgumentException if restaurant ID is null
     */
    public boolean hasEmployees(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        return getEmployeeCount(restaurantId) > 0;
    }

    /**
     * Validate employee data.
     * Ensures all required fields are present and valid.
     * 
     * @param employee the employee to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEmployeeData(EmployeeEntity employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        if (employee.getFirstName() == null || employee.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }

        if (employee.getLastName() == null || employee.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }

        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        if (employee.getRole() == null || employee.getRole().isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }

        if (employee.getHireDate() == null) {
            throw new IllegalArgumentException("Hire date cannot be null");
        }

        // Validate lengths
        if (employee.getFirstName().length() < 2 || employee.getFirstName().length() > 100) {
            throw new IllegalArgumentException("First name must be between 2 and 100 characters");
        }

        if (employee.getLastName().length() < 2 || employee.getLastName().length() > 100) {
            throw new IllegalArgumentException("Last name must be between 2 and 100 characters");
        }

        if (employee.getEmail().length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }

        if (employee.getRole().length() < 3 || employee.getRole().length() > 50) {
            throw new IllegalArgumentException("Role must be between 3 and 50 characters");
        }

        if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isBlank()) {
            if (employee.getPhoneNumber().length() < 10 || employee.getPhoneNumber().length() > 20) {
                throw new IllegalArgumentException("Phone number must be between 10 and 20 characters");
            }
        }
    }
}
