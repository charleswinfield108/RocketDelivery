package com.rocketFoodDelivery.rocketFood;

import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.repository.*;
import com.rocketFoodDelivery.rocketFood.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("manual-seeding")
public class DataSeeder {

        private final UserRepository userRepository;
        private final CustomerRepository customerRepository;
        private final AddressRepository addressRepository;
        private final RestaurantRepository restaurantRepository;
        private final OrderRepository orderRepository;
        private final OrderStatusService orderStatusService;

        @Autowired
        public DataSeeder(UserRepository userRepository,
                        CustomerRepository customerRepository,
                        AddressRepository addressRepository,
                        RestaurantRepository restaurantRepository,
                        OrderRepository orderRepository,
                        OrderStatusService orderStatusService) {
                this.userRepository = userRepository;
                this.customerRepository = customerRepository;
                this.addressRepository = addressRepository;
                this.restaurantRepository = restaurantRepository;
                this.orderRepository = orderRepository;
                this.orderStatusService = orderStatusService;
        }

        @PostConstruct
        @SuppressWarnings("null")
        public void seedData() {

                // Initialize order statuses (reference data)
                orderStatusService.initializeReferenceData();

                // Clear existing data from the database (in reverse dependency order)
                orderRepository.deleteAll();
                restaurantRepository.deleteAll();
                customerRepository.deleteAll();
                addressRepository.deleteAll();
                userRepository.deleteAll();

                // Seed Users
                UserEntity customerUser = new UserEntity();
                customerUser.setEmail("john.doe@codeboxx.com");
                customerUser.setFirstName("John");
                customerUser.setLastName("Doe");
                customerUser.setPhoneNumber("1234567890");
                userRepository.save(customerUser);

                UserEntity restaurantOwner = new UserEntity();
                restaurantOwner.setEmail("maria.garcia@rocketfood.com");
                restaurantOwner.setFirstName("Maria");
                restaurantOwner.setLastName("Garcia");
                restaurantOwner.setPhoneNumber("9876543210");
                userRepository.save(restaurantOwner);

                // Seed Addresses
                AddressEntity customerAddress = new AddressEntity();
                customerAddress.setUser(customerUser);
                customerAddress.setStreet("123 Coding Ave.");
                customerAddress.setCity("Paradise City");
                customerAddress.setState("CA");
                customerAddress.setZipCode("42221");
                customerAddress.setCountry("USA");
                customerAddress.setAddressType("HOME");
                customerAddress.setIsDefault(true);
                addressRepository.save(customerAddress);

                // Seed Customers
                CustomerEntity customer = new CustomerEntity();
                customer.setUser(customerUser);
                customer.setPhoneNumber("1234567890");
                customer.setLoyaltyPoints(0);
                customer.setIsActive(true);
                customerRepository.save(customer);

                // Seed Restaurants
                RestaurantEntity restaurant = new RestaurantEntity();
                restaurant.setName("Paradise Pizza");
                restaurant.setOwner(restaurantOwner);
                restaurant.setPhoneNumber("5551234567");
                restaurant.setEmail("contact@paradisepizza.com");
                restaurant.setIsActive(true);
                restaurantRepository.save(restaurant);

                // Seed Orders
                OrderEntity order1 = new OrderEntity();
                order1.setOrderNumber("ORD-20260326-001");
                order1.setCustomer(customer);
                order1.setRestaurant(restaurant);
                order1.setDeliveryAddress(customerAddress);
                order1.setTotalPrice(new BigDecimal("25.99"));
                order1.setStatus("PENDING");
                order1.setSpecialInstructions("No extra cheese on the pizza");
                orderRepository.save(order1);

                OrderEntity order2 = new OrderEntity();
                order2.setOrderNumber("ORD-20260326-002");
                order2.setCustomer(customer);
                order2.setRestaurant(restaurant);
                order2.setDeliveryAddress(customerAddress);
                order2.setTotalPrice(new BigDecimal("18.50"));
                order2.setStatus("CONFIRMED");
                order2.setSpecialInstructions("Extra sauce please");
                orderRepository.save(order2);
        }


}