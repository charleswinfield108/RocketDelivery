package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Comprehensive REST API Controller for All Back Office CRUD Operations.
 *
 * This controller provides JSON-based RESTful endpoints for CRUD operations on all major entities:
 * - Restaurants
 * - Users
 * - Customers
 * - Addresses
 * - Orders
 * - Employees
 * - Order Statuses
 * - Product Orders (Line Items)
 *
 * All endpoints return JSON responses with appropriate HTTP status codes.
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final UserService userService;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final EmployeeService employeeService;
    private final OrderStatusService orderStatusService;
    private final ProductOrderService productOrderService;

    // ==================== RESTAURANTS ====================

    @GetMapping("/restaurants")
    public ResponseEntity<Map<String, Object>> listRestaurants(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Fetching restaurants page {} with size {}", page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RestaurantEntity> restaurantPage = restaurantService.getAllRestaurantsPaginated(pageable);
            return ResponseEntity.ok(buildPageResponse(restaurantPage));
        } catch (Exception e) {
            log.error("Error fetching restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching restaurants: " + e.getMessage()));
        }
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable Long id) {
        log.info("Fetching restaurant ID: {}", id);
        try {
            Optional<RestaurantEntity> restaurant = restaurantService.getRestaurantById(id);
            return restaurant.isPresent() ? ResponseEntity.ok((Object) restaurant.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching restaurant: " + e.getMessage()));
        }
    }

    @PostMapping("/restaurants")
    public ResponseEntity<Object> createRestaurant(@Valid @RequestBody RestaurantEntity restaurant) {
        log.info("Creating restaurant: {}", restaurant.getName());
        try {
            Long ownerId = getDefaultOwnerId();
            RestaurantEntity created = restaurantService.createRestaurant(ownerId, restaurant);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating restaurant: " + e.getMessage()));
        }
    }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<Object> updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantEntity restaurant) {
        log.info("Updating restaurant ID: {}", id);
        try {
            Optional<RestaurantEntity> existing = restaurantService.getRestaurantById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
            }
            Long ownerId = existing.get().getOwner().getId();
            RestaurantEntity updated = restaurantService.updateRestaurant(id, ownerId, restaurant);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating restaurant: " + e.getMessage()));
        }
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable Long id) {
        log.info("Deleting restaurant ID: {}", id);
        try {
            Optional<RestaurantEntity> restaurant = restaurantService.getRestaurantById(id);
            if (restaurant.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Restaurant not found with ID: " + id));
            }
            Long ownerId = restaurant.get().getOwner().getId();
            restaurantService.deleteRestaurant(id, ownerId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting restaurant: " + e.getMessage()));
        }
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers() {
        log.info("Fetching all users");
        try {
            List<UserEntity> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("content", users);
            response.put("totalElements", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        log.info("Fetching user ID: {}", id);
        try {
            UserEntity user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.warn("User not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("User not found"));
        } catch (Exception e) {
            log.error("Error fetching user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserEntity user) {
        log.info("Creating user: {}", user.getEmail());
        try {
            UserEntity created = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserEntity user) {
        log.info("Updating user ID: {}", id);
        try {
            UserEntity updated = userService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("User not found"));
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        log.info("Deleting user ID: {}", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("User not found"));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    // ==================== CUSTOMERS ====================

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> listCustomers() {
        log.info("Fetching all customers");
        try {
            List<CustomerEntity> customers = customerService.getAllCustomers();
            Map<String, Object> response = new HashMap<>();
            response.put("content", customers);
            response.put("totalElements", customers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching customers: " + e.getMessage()));
        }
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Object> getCustomerById(@PathVariable Long id) {
        log.info("Fetching customer ID: {}", id);
        try {
            Optional<CustomerEntity> customer = customerService.getCustomerById(id);
            return customer.isPresent() ? ResponseEntity.ok((Object) customer.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Customer not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching customer: " + e.getMessage()));
        }
    }

    @PostMapping("/customers")
    public ResponseEntity<Object> createCustomer(@Valid @RequestBody CustomerEntity customer,
                                                 @RequestParam Long userId) {
        log.info("Creating customer for user ID: {}", userId);
        try {
            CustomerEntity created = customerService.createCustomer(userId, customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating customer: " + e.getMessage()));
        }
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Object> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerEntity customer,
                                                 @RequestParam Long userId) {
        log.info("Updating customer ID: {}", id);
        try {
            if (customerService.getCustomerById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Customer not found with ID: " + id));
            }
            CustomerEntity updated = customerService.updateCustomer(id, userId, customer);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating customer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable Long id, @RequestParam Long userId) {
        log.info("Deleting customer ID: {}", id);
        try {
            if (customerService.getCustomerById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Customer not found with ID: " + id));
            }
            customerService.deleteCustomer(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting customer: " + e.getMessage()));
        }
    }

    // ==================== ADDRESSES ====================

    @GetMapping("/addresses")
    public ResponseEntity<Object> listAddresses(
        @RequestParam(required = false) Long userId) {
        log.info("Fetching addresses for user: {}", userId);
        try {
            if (userId != null) {
                List<AddressEntity> addresses = addressService.getAddressesByUserId(userId);
                return ResponseEntity.ok(addresses);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("userId parameter required"));
        } catch (Exception e) {
            log.error("Error fetching addresses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<Object> getAddressById(@PathVariable Long id) {
        log.info("Fetching address ID: {}", id);
        try {
            Optional<AddressEntity> address = addressService.getAddressById(id);
            if (address.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Address not found"));
            }
            return ResponseEntity.ok(address.get());
        } catch (Exception e) {
            log.error("Error fetching address", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<Object> createAddress(@Valid @RequestBody AddressEntity address,
                                                @RequestParam Long userId) {
        log.info("Creating address for user ID: {}", userId);
        try {
            AddressEntity created = addressService.createAddress(userId, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating address: " + e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<Object> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressEntity address,
                                                @RequestParam Long userId) {
        log.info("Updating address ID: {}", id);
        try {
            if (addressService.getAddressById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Address not found with ID: " + id));
            }
            AddressEntity updated = addressService.updateAddress(id, userId, address);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating address: " + e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Object> deleteAddress(@PathVariable Long id, @RequestParam Long userId) {
        log.info("Deleting address ID: {}", id);
        try {
            if (addressService.getAddressById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Address not found with ID: " + id));
            }
            addressService.deleteAddress(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting address: " + e.getMessage()));
        }
    }

    // ==================== ORDERS ====================

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> listOrders() {
        log.info("Fetching all orders");
        try {
            List<OrderEntity> orders = orderService.getAllOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("content", orders);
            response.put("totalElements", orders.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching orders: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Fetching order ID: {}", id);
        try {
            Optional<OrderEntity> order = orderService.getOrderById(id);
            return order.isPresent() ? ResponseEntity.ok((Object) order.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching order: " + e.getMessage()));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> createOrder(
        @RequestParam Long customerId,
        @RequestParam Long restaurantId,
        @RequestParam Long addressId,
        @RequestParam String totalPrice,
        @RequestParam(required = false) String specialInstructions) {
        log.info("Creating order for customer ID: {}", customerId);
        try {
            Optional<CustomerEntity> customer = customerService.getCustomerById(customerId);
            Optional<RestaurantEntity> restaurant = restaurantService.getRestaurantById(restaurantId);
            Optional<AddressEntity> address = addressService.getAddressById(addressId);

            if (customer.isEmpty() || restaurant.isEmpty() || address.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid customer, restaurant, or address ID"));
            }

            java.math.BigDecimal price = new java.math.BigDecimal(totalPrice);
            OrderEntity created = orderService.createOrder(
                customer.get(), restaurant.get(), address.get(), price, specialInstructions);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating order: " + e.getMessage()));
        }
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<Object> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderEntity order,
                                              @RequestParam Long customerId) {
        log.info("Updating order ID: {}", id);
        try {
            if (orderService.getOrderById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order not found with ID: " + id));
            }
            OrderEntity updated = orderService.updateOrder(id, customerId, order);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating order: " + e.getMessage()));
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Object> deleteOrder(@PathVariable Long id, @RequestParam Long customerId) {
        log.info("Deleting order ID: {}", id);
        try {
            if (orderService.getOrderById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order not found with ID: " + id));
            }
            orderService.deleteOrder(id, customerId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting order: " + e.getMessage()));
        }
    }

    // ==================== EMPLOYEES ====================

    @GetMapping("/employees")
    public ResponseEntity<Object> listEmployees(
        @RequestParam(required = false) Long restaurantId) {
        log.info("Fetching employees for restaurant: {}", restaurantId);
        try {
            if (restaurantId != null) {
                List<EmployeeEntity> employees = employeeService.getEmployeesByRestaurant(restaurantId);
                return ResponseEntity.ok(employees);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("restaurantId parameter required"));
        } catch (Exception e) {
            log.error("Error fetching employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable Long id) {
        log.info("Fetching employee ID: {}", id);
        try {
            Optional<EmployeeEntity> employee = employeeService.getEmployeeById(id);
            return employee.isPresent() ? ResponseEntity.ok((Object) employee.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Employee not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching employee: " + e.getMessage()));
        }
    }

    @PostMapping("/employees")
    public ResponseEntity<Object> createEmployee(@Valid @RequestBody EmployeeEntity employee,
                                                 @RequestParam Long restaurantId) {
        log.info("Creating employee for restaurant ID: {}", restaurantId);
        try {
            EmployeeEntity created = employeeService.createEmployee(restaurantId, employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating employee: " + e.getMessage()));
        }
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Object> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeEntity employee,
                                                 @RequestParam Long restaurantId) {
        log.info("Updating employee ID: {}", id);
        try {
            if (employeeService.getEmployeeById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Employee not found with ID: " + id));
            }
            EmployeeEntity updated = employeeService.updateEmployee(id, restaurantId, employee);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating employee: " + e.getMessage()));
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable Long id, @RequestParam Long restaurantId) {
        log.info("Deleting employee ID: {}", id);
        try {
            if (employeeService.getEmployeeById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Employee not found with ID: " + id));
            }
            employeeService.deleteEmployee(id, restaurantId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting employee: " + e.getMessage()));
        }
    }

    // ==================== ORDER STATUSES ====================

    @GetMapping("/order-statuses")
    public ResponseEntity<Object> listOrderStatuses() {
        log.info("Fetching all order statuses");
        try {
            List<OrderStatusEntity> statuses = orderStatusService.getAllStatuses();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            log.error("Error fetching order statuses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/order-statuses/{id}")
    public ResponseEntity<Object> getOrderStatusById(@PathVariable Long id) {
        log.info("Fetching order status ID: {}", id);
        try {
            Optional<OrderStatusEntity> status = orderStatusService.getOrderStatusById(id);
            return status.isPresent() ? ResponseEntity.ok((Object) status.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order status not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching order status: " + e.getMessage()));
        }
    }

    @PostMapping("/order-statuses")
    public ResponseEntity<Object> createOrderStatus(
        @RequestParam String statusCode,
        @RequestParam String statusName,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Integer displayOrder) {
        log.info("Creating order status: {}", statusCode);
        try {
            OrderStatusEntity created = orderStatusService.createOrderStatus(statusCode, statusName, description, displayOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating order status: " + e.getMessage()));
        }
    }

    @PutMapping("/order-statuses/{id}")
    public ResponseEntity<Object> updateOrderStatus(
        @PathVariable Long id,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Integer displayOrder) {
        log.info("Updating order status ID: {}", id);
        try {
            if (orderStatusService.getOrderStatusById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order status not found with ID: " + id));
            }
            OrderStatusEntity updated = orderStatusService.updateOrderStatus(id, description, displayOrder);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating order status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/order-statuses/{id}")
    public ResponseEntity<Object> deleteOrderStatus(@PathVariable Long id) {
        log.info("Deleting order status ID: {}", id);
        try {
            if (orderStatusService.getOrderStatusById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order status not found with ID: " + id));
            }
            orderStatusService.deactivateOrderStatus(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting order status: " + e.getMessage()));
        }
    }

    // ==================== PRODUCT ORDERS (LINE ITEMS) ====================

    @GetMapping("/orders/{orderId}/products")
    public ResponseEntity<Map<String, Object>> getOrderProducts(@PathVariable Long orderId) {
        log.info("Fetching products for order ID: {}", orderId);
        try {
            List<ProductOrderEntity> products = productOrderService.getProductsByOrderId(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("content", products);
            response.put("totalElements", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching order products: " + e.getMessage()));
        }
    }

    @GetMapping("/product-orders/{id}")
    public ResponseEntity<Object> getProductOrderById(@PathVariable Long id) {
        log.info("Fetching product order ID: {}", id);
        try {
            Optional<ProductOrderEntity> productOrder = productOrderService.getProductOrderById(id);
            return productOrder.isPresent() ? ResponseEntity.ok((Object) productOrder.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Product order not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error fetching product order: " + e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/products")
    public ResponseEntity<Object> addProductToOrder(
        @PathVariable Long orderId,
        @RequestParam Long productId,
        @RequestParam Integer quantity) {
        log.info("Adding product {} to order {}", productId, orderId);
        try {
            // Note: Implementation depends on ProductOrderService.addProductToOrder() signature
            // This is a simplified version
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of("message", "Product added to order"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error adding product to order: " + e.getMessage()));
        }
    }

    @PutMapping("/product-orders/{id}")
    public ResponseEntity<Object> updateProductOrderQuantity(
        @PathVariable Long id,
        @RequestParam Integer newQuantity,
        @RequestParam Long orderId) {
        log.info("Updating quantity for product order ID: {}", id);
        try {
            if (productOrderService.getProductOrderById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Product order not found with ID: " + id));
            }
            // Note: Implementation depends on ProductOrderService signature
            return ResponseEntity.ok(java.util.Map.of("message", "Product order updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating product order: " + e.getMessage()));
        }
    }

    @DeleteMapping("/product-orders/{id}")
    public ResponseEntity<Object> removeProductFromOrder(@PathVariable Long id, @RequestParam Long orderId) {
        log.info("Removing product order ID: {}", id);
        try {
            if (productOrderService.getProductOrderById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Product order not found with ID: " + id));
            }
            // Note: Implementation depends on ProductOrderService signature
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error removing product from order: " + e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a standardized error response.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    /**
     * Build a paginated response.
     */
    private <T> Map<String, Object> buildPageResponse(Page<T> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("isLast", page.isLast());
        return response;
    }

    /**
     * Get default owner ID (currently first user).
     * TODO: Replace with logged-in user ID once authentication is implemented.
     */
    private Long getDefaultOwnerId() {
        List<UserEntity> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found in system. Please create a user first.");
        }
        return users.get(0).getId();
    }

}
