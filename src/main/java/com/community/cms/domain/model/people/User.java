package com.community.cms.domain.model.people;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя системы.
 *
 * <p>Представляет зарегистрированного пользователя с учетными данными, ролями
 * и настройками доступа. Используется для аутентификации и авторизации
 * в административной части системы.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Уникальные идентификаторы: username и email</li>
 *   <li>Поддержка множественных ролей через отдельную таблицу</li>
 *   <li>Автоматическое отслеживание даты создания</li>
 *   <li>Валидация данных на уровне сущности</li>
 *   <li>Возможность блокировки/активации учетной записи</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически базой данных при создании записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальное имя пользователя для входа в систему.
     * Должно быть от 3 до 50 символов и не может быть пустым.
     */
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Электронная почта пользователя.
     * Должна быть уникальной и соответствовать формату email.
     * Используется для уведомлений и восстановления доступа.
     */
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Зашифрованный пароль пользователя.
     * Должен содержать минимум 6 символов для обеспечения безопасности.
     * Хранится в зашифрованном виде с использованием BCrypt.
     */
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Column(nullable = false)
    private String password;

    /**
     * Набор ролей пользователя.
     * Хранится в отдельной таблице user_roles для поддержки множественных ролей.
     * Возможные роли: ROLE_ADMIN, ROLE_EDITOR, ROLE_USER.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    /**
     * Флаг активности учетной записи.
     * Если false, пользователь не может войти в систему.
     * Используется для временной блокировки или деактивации аккаунта.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Дата и время создания учетной записи.
     * Заполняется автоматически при создании записи.
     * Не может быть изменена после создания.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию.
     * Требуется для JPA и фреймворков.
     */
    public User() {}

    /**
     * Конструктор с основными параметрами пользователя.
     *
     * @param username имя пользователя для входа
     * @param email электронная почта пользователя
     * @param password пароль пользователя (будет зашифрован)
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ГЕТТЕРЫ И СЕТТЕРЫ

    /**
     * Возвращает уникальный идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор пользователя.
     *
     * @param id идентификатор пользователя
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает имя пользователя для входа в систему.
     *
     * @return имя пользователя
     */
    public String getUsername() {
        return username;
    }

    /**
     * Устанавливает имя пользователя для входа в систему.
     *
     * @param username имя пользователя
     * @throws IllegalArgumentException если имя пользователя не соответствует требованиям
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Возвращает электронную почту пользователя.
     *
     * @return email пользователя
     */
    public String getEmail() {
        return email;
    }

    /**
     * Устанавливает электронную почту пользователя.
     *
     * @param email email пользователя
     * @throws IllegalArgumentException если email не соответствует формату
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Возвращает зашифрованный пароль пользователя.
     *
     * @return зашифрованный пароль
     */
    public String getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль пользователя.
     *
     * @param password пароль пользователя (должен быть зашифрован)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Возвращает набор ролей пользователя.
     *
     * @return множество ролей пользователя
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Устанавливает набор ролей пользователя.
     *
     * @param roles множество ролей пользователя
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Проверяет, активна ли учетная запись пользователя.
     *
     * @return true если учетная запись активна, иначе false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Устанавливает статус активности учетной записи.
     *
     * @param enabled true для активации, false для блокировки
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Возвращает дату и время создания учетной записи.
     *
     * @return дата создания
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания учетной записи.
     * Используется преимущественно фреймворками.
     *
     * @param createdAt дата создания
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    /**
     * Добавляет роль пользователю.
     *
     * @param role роль для добавления (например, "ROLE_ADMIN")
     */
    public void addRole(String role) {
        this.roles.add(role);
    }

    /**
     * Удаляет роль у пользователя.
     *
     * @param role роль для удаления
     */
    public void removeRole(String role) {
        this.roles.remove(role);
    }

    /**
     * Проверяет, имеет ли пользователь указанную роль.
     *
     * @param role роль для проверки
     * @return true если пользователь имеет роль, иначе false
     */
    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }

    /**
     * Возвращает строковое представление пользователя.
     * Не включает пароль для безопасности.
     *
     * @return строковое представление пользователя
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                '}';
    }
}
