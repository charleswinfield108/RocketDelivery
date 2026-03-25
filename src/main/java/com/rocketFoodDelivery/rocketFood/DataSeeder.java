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
        @SuppressWarnings({"null", "deprecation"})
        public void seedData() {

                // Clear existing data from the database
                customerRepository.deleteAll();
                addressRepository.deleteAll();
                userRepository.deleteAll();

                // Seed new data
                UserEntity user = new UserEntity();
                user.setEmail("john.doe@codeboxx.com");
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setPhoneNumber("1234567890");
                userRepository.save(user);

                AddressEntity address = new AddressEntity();
                address.setUser(user);
                address.setStreet("123 Coding Ave.");
                address.setCity("Paradise City");
                address.setState("CA");
                address.setZipCode("42221");
                address.setCountry("USA");
                address.setAddressType("HOME");
                address.setIsDefault(true);
                addressRepository.save(address);

                CustomerEntity customer = new CustomerEntity();
                customer.setUser(user);
                customer.setPhoneNumber("1234567890");
                customer.setLoyaltyPoints(0);
                customer.setIsActive(true);
                customerRepository.save(customer);
        }


}