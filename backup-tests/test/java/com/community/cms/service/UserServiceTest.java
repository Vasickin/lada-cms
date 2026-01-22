package com.community.cms.service;

import com.community.cms.model.User;
import com.community.cms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService class.
 * Тесты для класса UserService.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    /**
     * Set up test data before each test.
     * Подготовка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setEnabled(true);
    }

    /**
     * Test finding user by existing username.
     * Тест поиска пользователя по существующему имени.
     */
    @Test
    void findUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * Test finding user by non-existing username.
     * Тест поиска пользователя по несуществующему имени.
     */
    @Test
    void findUserByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    /**
     * Test checking if user exists by username.
     * Тест проверки существования пользователя по имени.
     */
    @Test
    void userExistsByUsername_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.userExistsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    /**
     * Test saving user with password encoding.
     * Тест сохранения пользователя с кодированием пароля.
     */
    @Test
    void saveUser_ShouldEncodePasswordAndSave() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.saveUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Test finding user by existing ID.
     * Тест поиска пользователя по существующему ID.
     */
    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * Test finding all users.
     * Тест поиска всех пользователей.
     */
    @Test
    void findAllUsers_ShouldReturnUserList() {
        // Act
        userService.findAllUsers();

        // Assert
        verify(userRepository, times(1)).findAll();
    }
}
