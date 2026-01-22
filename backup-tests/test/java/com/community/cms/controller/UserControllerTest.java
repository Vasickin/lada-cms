package com.community.cms.controller;

import com.community.cms.model.User;
import com.community.cms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserAdminController class.
 * Тесты для класса UserAdminController.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private UserController userController;

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
        testUser.setRoles(new HashSet<>(Arrays.asList("USER", "EDITOR")));

        // УБИРАЕМ все лишние моки из setUp - они не используются в большинстве тестов
    }

    /**
     * Test listing all users.
     * Тест отображения списка всех пользователей.
     */
    @Test
    void listUsers_ShouldReturnUserList() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.findAllUsers()).thenReturn(users);

        // Act
        String viewName = userController.listUsers(model);

        // Assert
        assertEquals("admin/users/list", viewName);
        verify(userService, times(1)).findAllUsers();
        verify(model, times(1)).addAttribute("users", users);
    }

    /**
     * Test showing user creation form.
     * Тест отображения формы создания пользователя.
     */
    @Test
    void showCreateForm_ShouldReturnCreateForm() {
        // Act
        String viewName = userController.showCreateForm(model);

        // Assert
        assertEquals("admin/users/create", viewName);
        verify(model, times(1)).addAttribute(eq("user"), any(User.class));
        // Исправляем порядок ролей согласно реальному контроллеру
        verify(model, times(1)).addAttribute("availableRoles", Set.of("ADMIN", "EDITOR", "USER"));
    }

    /**
     * Test creating user with valid data.
     * Тест создания пользователя с валидными данными.
     */
    @Test
    void createUser_WithValidData_ShouldRedirectToList() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);

        // Act
        String viewName = userController.createUser(testUser, bindingResult, redirectAttributes, model);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).saveUser(testUser);
        // Исправляем сообщение - должно включать имя пользователя
        verify(redirectAttributes, times(1)).addFlashAttribute("success", "Пользователь testuser успешно создан");
    }

    /**
     * Test creating user with validation errors.
     * Тест создания пользователя с ошибками валидации.
     */
    @Test
    void createUser_WithValidationErrors_ShouldReturnForm() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = userController.createUser(testUser, bindingResult, redirectAttributes, model);

        // Assert
        assertEquals("admin/users/create", viewName);
        // Исправляем порядок ролей согласно реальному контроллеру
        verify(model, times(1)).addAttribute("availableRoles", Set.of("ADMIN", "EDITOR", "USER"));
        verify(userService, never()).saveUser(any());
    }

    /**
     * Test showing user edit form for existing user.
     * Тест отображения формы редактирования существующего пользователя.
     */
    @Test
    void showEditForm_WhenUserExists_ShouldReturnEditForm() {
        // Arrange
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        // Act
        String viewName = userController.showEditForm(1L, model);

        // Assert
        assertEquals("admin/users/edit", viewName);
        verify(userService, times(1)).findUserById(1L);
        verify(model, times(1)).addAttribute("user", testUser);
        // Исправляем порядок ролей согласно реальному контроллеру
        verify(model, times(1)).addAttribute("availableRoles", Set.of("ADMIN", "EDITOR", "USER"));
    }

    /**
     * Test showing user edit form for non-existing user.
     * Тест отображения формы редактирования несуществующего пользователя.
     */
    @Test
    void showEditForm_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.showEditForm(999L, model);
        });

        assertEquals("Пользователь не найден: 999", exception.getMessage());
        verify(userService, times(1)).findUserById(999L);
    }

    /**
     * Test updating user with valid data.
     * Тест обновления пользователя с валидными данными.
     */
    @Test
    void updateUser_WithValidData_ShouldRedirectToList() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "EDITOR"));
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.saveUser(any(User.class))).thenReturn(testUser);

        // Act
        String viewName = userController.updateUser(1L, testUser, roles, redirectAttributes, model);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).findUserById(1L);
        verify(userService, times(1)).saveUser(any(User.class));
        // Исправляем сообщение - должно включать имя пользователя
        verify(redirectAttributes, times(1)).addFlashAttribute("success", "Пользователь testuser успешно обновлен");
    }

    /**
     * Test updating non-existing user.
     * Тест обновления несуществующего пользователя.
     */
    @Test
    void updateUser_WhenUserNotExists_ShouldShowEditFormWithError() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        // Act
        String viewName = userController.updateUser(999L, testUser, roles, redirectAttributes, model);

        // Assert
        assertEquals("admin/users/edit", viewName);
        verify(userService, times(1)).findUserById(999L);
        verify(userService, never()).saveUser(any());
        // Исправляем сообщение об ошибке - должно включать ID
        verify(model, times(1)).addAttribute("error", "Пользователь не найден: 999");
        // Исправляем порядок ролей согласно реальному контроллеру
        verify(model, times(1)).addAttribute("availableRoles", Set.of("ADMIN", "EDITOR", "USER"));
    }

    /**
     * Test enabling user.
     * Тест активации пользователя.
     */
    @Test
    void enableUser_ShouldRedirectWithSuccess() {
        // Arrange
        when(userService.enableUser(1L)).thenReturn(testUser);

        // Act
        String viewName = userController.enableUser(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).enableUser(1L);
        verify(redirectAttributes, times(1)).addFlashAttribute("success", "Пользователь testuser активирован");
    }

    /**
     * Test enabling non-existing user.
     * Тест активации несуществующего пользователя.
     */
    @Test
    void enableUser_WhenUserNotExists_ShouldRedirectWithError() {
        // Arrange
        when(userService.enableUser(999L)).thenThrow(new IllegalArgumentException("Пользователь не найден"));

        // Act
        String viewName = userController.enableUser(999L, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).enableUser(999L);
        verify(redirectAttributes, times(1)).addFlashAttribute("error", "Пользователь не найден");
    }

    /**
     * Test adding role to user.
     * Тест добавления роли пользователю.
     */
    @Test
    void addRoleToUser_ShouldRedirectWithSuccess() {
        // Arrange
        when(userService.addRoleToUser(1L, "ADMIN")).thenReturn(testUser);

        // Act
        String viewName = userController.addRoleToUser(1L, "ADMIN", redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).addRoleToUser(1L, "ADMIN");
        verify(redirectAttributes, times(1)).addFlashAttribute("success", "Роль ADMIN добавлена пользователю testuser");
    }

    /**
     * Test removing role from user.
     * Тест удаления роли у пользователя.
     */
    @Test
    void removeRoleFromUser_ShouldRedirectWithSuccess() {
        // Arrange
        when(userService.removeRoleFromUser(1L, "EDITOR")).thenReturn(testUser);

        // Act
        String viewName = userController.removeRoleFromUser(1L, "EDITOR", redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(1)).removeRoleFromUser(1L, "EDITOR");
        verify(redirectAttributes, times(1)).addFlashAttribute("success", "Роль EDITOR удалена у пользователя testuser");
    }
}