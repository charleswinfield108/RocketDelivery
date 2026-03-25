package com.rocketFoodDelivery.rocketFood;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService class using Mockito.
 * Tests service logic with mocked repository.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");
    }

    @Test
    void testCreateUser_Success() {
        assertNotNull(testUser);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserEntity result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_InvalidData_NullEmail() {
        assertNotNull(testUser);
        testUser.setEmail(null);
        
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_InvalidData_NullFirstName() {
        assertNotNull(testUser);
        testUser.setFirstName(null);
        
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_InvalidData_FirstNameTooShort() {
        assertNotNull(testUser);
        testUser.setFirstName("A");
        
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_InvalidData_PhoneNumberTooShort() {
        assertNotNull(testUser);
        testUser.setPhoneNumber("123");
        
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserEntity result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testGetUserById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(null));
    }

    @Test
    void testGetUserByEmail_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.getUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserByEmail_NullEmail() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(null));
    }

    @Test
    void testCreateUser_NullUser() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    void testGetAllUsers() {
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setPhoneNumber("9876543210");

        when(userRepository.findAllByOrderByIdDesc()).thenReturn(Arrays.asList(user2, testUser));

        List<UserEntity> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        verify(userRepository, times(1)).findAllByOrderByIdDesc();
    }

    @Test
    void testGetAllUsers_Empty() {
        when(userRepository.findAllByOrderByIdDesc()).thenReturn(Arrays.asList());

        List<UserEntity> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateUser_Success() {
        assertNotNull(testUser);
        UserEntity updatedUser = new UserEntity();
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPhoneNumber("9876543210");
        assertNotNull(updatedUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserEntity result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        assertNotNull(testUser);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(999L, testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_NullId() {
        assertNotNull(testUser);
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null, testUser));
    }

    @Test
    void testUpdateUser_NullUser() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, null));
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteUser_NullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
    }

    @Test
    void testExistsByEmail_True() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
    }

    @Test
    void testExistsByEmail_False() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }

    @Test
    void testGetUserCount() {
        when(userRepository.count()).thenReturn(5L);

        long result = userService.getUserCount();

        assertEquals(5L, result);
        verify(userRepository, times(1)).count();
    }
}
