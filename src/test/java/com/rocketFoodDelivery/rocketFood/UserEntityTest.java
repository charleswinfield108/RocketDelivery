package com.rocketFoodDelivery.rocketFood;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserEntity class.
 * Tests entity creation, field validation, and JPA annotations.
 */
@SpringBootTest
class UserEntityTest {

    @Autowired
    private Validator validator;

    private UserEntity validUser;

    @BeforeEach
    void setUp() {
        validUser = new UserEntity();
        validUser.setEmail("test.user@example.com");
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setPhoneNumber("1234567890");
    }

    @Test
    void testUserEntityCreation() {
        assertNotNull(validUser);
        assertEquals("test.user@example.com", validUser.getEmail());
        assertEquals("John", validUser.getFirstName());
        assertEquals("Doe", validUser.getLastName());
        assertEquals("1234567890", validUser.getPhoneNumber());
    }

    @Test
    void testUserEntityWithAllArgsConstructor() {
        UserEntity user = new UserEntity(1L, "user@example.com", "Jane", "Smith", "9876543210", 
                                          LocalDateTime.now(), LocalDateTime.now());
        assertEquals(1L, user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("Jane", user.getFirstName());
    }

    @Test
    void testEmailValidation_InvalidFormat() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testEmailValidation_Null() {
        validUser.setEmail(null);
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testFirstNameValidation_TooShort() {
        validUser.setFirstName("A");
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void testFirstNameValidation_TooLong() {
        validUser.setFirstName("A".repeat(101));
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void testFirstNameValidation_Null() {
        validUser.setFirstName(null);
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLastNameValidation_TooShort() {
        validUser.setLastName("A");
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLastNameValidation_Null() {
        validUser.setLastName(null);
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPhoneNumberValidation_TooShort() {
        validUser.setPhoneNumber("123");
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @Test
    void testPhoneNumberValidation_TooLong() {
        validUser.setPhoneNumber("1".repeat(21));
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPhoneNumberValidation_Null() {
        validUser.setPhoneNumber(null);
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidUserEntity() {
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Valid user should have no constraint violations");
    }

    @Test
    void testUserEntityEquality() {
        UserEntity user1 = new UserEntity(1L, "test@example.com", "John", "Doe", "1234567890", null, null);
        UserEntity user2 = new UserEntity(1L, "test@example.com", "John", "Doe", "1234567890", null, null);
        assertEquals(user1, user2);
    }

    @Test
    void testUserEntityToString() {
        UserEntity user = new UserEntity(1L, "test@example.com", "John", "Doe", "1234567890", null, null);
        String toStringResult = user.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("email"));
    }
}
