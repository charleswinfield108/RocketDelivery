package com.rocketFoodDelivery.rocketFood;

import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("manual-seeding")
public class DataSeeder {

        private final UserRepository userRepository;
        private final CustomerRepository customerRepository;
        private final AddressRepository addressRepository;

        @Autowired
        public DataSeeder(UserRepository userRepository,
                        CustomerRepository customerRepository,
                        AddressRepository addressRepository) {
                this.userRepository = userRepository;
                this.customerRepository = customerRepository;
                this.addressRepository = addressRepository;
        }

        @PostConstruct
        @SuppressWarnings("null")
        public void seedData() {

                // Clear existing data from the database
                customerRepository.deleteAll();
                userRepository.deleteAll();
                addressRepository.deleteAll();

                // Seed new data
                UserEntity user = UserEntity.builder()
                                .name("John Doe")
                                .email("john.doe@codeboxx.com")
                                .password("password123")
                                .build();
                userRepository.save(user);

                Address address = Address.builder()
                                .street_address("123 Coding Ave.")
                                .city("Paradise City")
                                .postal_code("422211")
                                .build();
                addressRepository.save(address);

                Customer customer = Customer.builder()
                                .userEntity(user)
                                .address(address)
                                .email("john.eater@rocketfooddelivery.com")
                                .phone("1 23 4224424")
                                .active(true)
                                .build();
                customerRepository.save(customer);
        }


}