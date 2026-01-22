package com.community.cms.repository;

import com.community.cms.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User createTestUser(String username, String email, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$encodedPasswordHashForTesting");
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private User createTestUserWithRoles(String username, String email, boolean enabled, String... roles) {
        User user = createTestUser(username, email, enabled);
        for (String role : roles) {
            user.addRole(role);
        }
        return user;
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    void findByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WhenUserNotExists_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenUserNotExists_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByEnabledTrue_ShouldReturnOnlyEnabledUsers() {
        // Given
        User enabledUser1 = createTestUser("user1", "user1@example.com", true);
        User enabledUser2 = createTestUser("user2", "user2@example.com", true);
        User disabledUser = createTestUser("user3", "user3@example.com", false);

        entityManager.persist(enabledUser1);
        entityManager.persist(enabledUser2);
        entityManager.persist(disabledUser);
        entityManager.flush();

        // When
        List<User> enabledUsers = userRepository.findByEnabledTrue();

        // Then
        assertThat(enabledUsers).hasSize(2);
        assertThat(enabledUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
        assertThat(enabledUsers)
                .allMatch(User::isEnabled);
    }

    @Test
    void findByEnabledFalse_ShouldReturnOnlyDisabledUsers() {
        // Given
        User enabledUser = createTestUser("user1", "user1@example.com", true);
        User disabledUser1 = createTestUser("user2", "user2@example.com", false);
        User disabledUser2 = createTestUser("user3", "user3@example.com", false);

        entityManager.persist(enabledUser);
        entityManager.persist(disabledUser1);
        entityManager.persist(disabledUser2);
        entityManager.flush();

        // When
        List<User> disabledUsers = userRepository.findByEnabledFalse();

        // Then
        assertThat(disabledUsers).hasSize(2);
        assertThat(disabledUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("user2", "user3");
        assertThat(disabledUsers)
                .noneMatch(User::isEnabled);
    }

    @Test
    void findByRole_ShouldReturnUsersWithRole() {
        // Given
        User adminUser = createTestUserWithRoles("admin", "admin@example.com", true, "ROLE_ADMIN", "ROLE_USER");
        User editorUser = createTestUserWithRoles("editor", "editor@example.com", true, "ROLE_EDITOR", "ROLE_USER");
        User regularUser = createTestUserWithRoles("user", "user@example.com", true, "ROLE_USER");

        entityManager.persist(adminUser);
        entityManager.persist(editorUser);
        entityManager.persist(regularUser);
        entityManager.flush();

        // When
        List<User> adminUsers = userRepository.findByRole("ROLE_ADMIN");
        List<User> editorUsers = userRepository.findByRole("ROLE_EDITOR");
        List<User> userRoleUsers = userRepository.findByRole("ROLE_USER");

        // Then
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getUsername()).isEqualTo("admin");

        assertThat(editorUsers).hasSize(1);
        assertThat(editorUsers.get(0).getUsername()).isEqualTo("editor");

        assertThat(userRoleUsers).hasSize(3); // Все пользователи имеют ROLE_USER
    }

    @Test
    void findByUsernameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        // Given
        User johnDoe = createTestUser("johndoe", "john@example.com", true);
        User johnSmith = createTestUser("johnsmith", "john.smith@example.com", true);
        User janeDoe = createTestUser("janedoe", "jane@example.com", true);

        entityManager.persist(johnDoe);
        entityManager.persist(johnSmith);
        entityManager.persist(janeDoe);
        entityManager.flush();

        // When
        List<User> johnUsers = userRepository.findByUsernameContainingIgnoreCase("john");
        List<User> doeUsers = userRepository.findByUsernameContainingIgnoreCase("doe");
        List<User> jUsers = userRepository.findByUsernameContainingIgnoreCase("j");

        // Then
        assertThat(johnUsers).hasSize(2);
        assertThat(johnUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("johndoe", "johnsmith");

        assertThat(doeUsers).hasSize(2);
        assertThat(doeUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("johndoe", "janedoe");

        assertThat(jUsers).hasSize(3); // Все пользователи начинаются с 'j'
    }

    @Test
    void findByEmailContainingIgnoreCase_ShouldReturnMatchingUsers() {
        // Given
        User user1 = createTestUser("user1", "test.user@example.com", true);
        User user2 = createTestUser("user2", "test.admin@example.com", true);
        User user3 = createTestUser("user3", "other@domain.com", true);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        // When
        List<User> testUsers = userRepository.findByEmailContainingIgnoreCase("test");
        List<User> exampleUsers = userRepository.findByEmailContainingIgnoreCase("example");
        List<User> adminUsers = userRepository.findByEmailContainingIgnoreCase("admin");

        // Then
        assertThat(testUsers).hasSize(2);
        assertThat(testUsers)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("test.user@example.com", "test.admin@example.com");

        assertThat(exampleUsers).hasSize(2);
        assertThat(exampleUsers)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("test.user@example.com", "test.admin@example.com");

        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getEmail()).isEqualTo("test.admin@example.com");
    }

    @Test
    void findByUsernameContainingIgnoreCase_WhenNoMatches_ShouldReturnEmptyList() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        List<User> result = userRepository.findByUsernameContainingIgnoreCase("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailContainingIgnoreCase_WhenNoMatches_ShouldReturnEmptyList() {
        // Given
        User user = createTestUser("testuser", "test@example.com", true);
        entityManager.persistAndFlush(user);

        // When
        List<User> result = userRepository.findByEmailContainingIgnoreCase("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByRole_WhenNoUsersWithRole_ShouldReturnEmptyList() {
        // Given
        User user = createTestUserWithRoles("user", "user@example.com", true, "ROLE_USER");
        entityManager.persistAndFlush(user);

        // When
        List<User> result = userRepository.findByRole("ROLE_ADMIN");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEnabledTrue_WhenNoEnabledUsers_ShouldReturnEmptyList() {
        // Given
        User disabledUser1 = createTestUser("user1", "user1@example.com", false);
        User disabledUser2 = createTestUser("user2", "user2@example.com", false);

        entityManager.persist(disabledUser1);
        entityManager.persist(disabledUser2);
        entityManager.flush();

        // When
        List<User> result = userRepository.findByEnabledTrue();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEnabledFalse_WhenNoDisabledUsers_ShouldReturnEmptyList() {
        // Given
        User enabledUser1 = createTestUser("user1", "user1@example.com", true);
        User enabledUser2 = createTestUser("user2", "user2@example.com", true);

        entityManager.persist(enabledUser1);
        entityManager.persist(enabledUser2);
        entityManager.flush();

        // When
        List<User> result = userRepository.findByEnabledFalse();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void saveUser_ShouldPersistCorrectly() {
        // Given
        User newUser = createTestUser("newuser", "new@example.com", true);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void deleteUser_ShouldRemoveFromDatabase() {
        // Given
        User user = createTestUser("todelete", "delete@example.com", true);
        entityManager.persistAndFlush(user);

        Long userId = user.getId();

        // When
        userRepository.delete(user);
        entityManager.flush();

        // Then
        User deletedUser = entityManager.find(User.class, userId);
        assertThat(deletedUser).isNull();
    }
}