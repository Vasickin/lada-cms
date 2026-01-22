package com.community.cms.domain.service.people;

import com.community.cms.domain.model.people.User;
import com.community.cms.domain.repository.people.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы с пользователями.
 * Содержит бизнес-логику приложения и служит прослойкой между контроллерами и репозиториями.
 *
 * <p>Основные responsibilities:
 * <ul>
 *   <li>Валидация бизнес-правил при создании и обновлении пользователей</li>
 *   <li>Шифрование паролей перед сохранением в базу данных</li>
 *   <li>Управление ролями и правами доступа пользователей</li>
 *   <li>Обработка транзакций при операциях с пользователями</li>
 *   <li>Обработка исключительных ситуаций</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see User
 * @see UserRepository
 * @see PasswordEncoder
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор с внедрением зависимостей UserRepository и PasswordEncoder.
     * Использует Spring DI для автоматического связывания.
     *
     * @param userRepository репозиторий для работы с данными пользователей
     * @param passwordEncoder кодировщик паролей для безопасного хранения
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Сохраняет нового пользователя или обновляет существующего.
     * Выполняет проверку уникальности username и email перед сохранением.
     * Автоматически шифрует пароль перед сохранением в базу данных.
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь с зашифрованным паролем
     * @throws IllegalArgumentException если username или email уже существуют
     */
    public User saveUser(User user) {
        // Проверка уникальности username для новых пользователей
        if (user.getId() == null && userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Пользователь с именем '" + user.getUsername() + "' уже существует");
        }

        // Проверка уникальности email для новых пользователей
        if (user.getId() == null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }

        // Шифрование пароля перед сохранением
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Находит пользователя по уникальному идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional содержащий пользователя если найден
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Находит пользователя по имени пользователя.
     * Используется для аутентификации в Spring Security.
     *
     * @param username имя пользователя для поиска
     * @return Optional содержащий пользователя если найден
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Находит пользователя по email адресу.
     *
     * @param email email адрес для поиска
     * @return Optional содержащий пользователя если найден
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Возвращает всех пользователей системы.
     * В основном используется в административной части.
     *
     * @return список всех пользователей
     */
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Возвращает всех активных пользователей.
     *
     * @return список активных пользователей
     */
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    /**
     * Удаляет пользователя по идентификатору.
     * Выполняет проверку существования пользователя перед удалением.
     *
     * @param id идентификатор пользователя для удаления
     * @throws IllegalArgumentException если пользователь не найден
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
    }

    /**
     * Активирует учетную запись пользователя.
     *
     * @param id идентификатор пользователя
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    public User enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Деактивирует учетную запись пользователя.
     *
     * @param id идентификатор пользователя
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    public User disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        user.setEnabled(false);
        return userRepository.save(user);
    }

    /**
     * Добавляет роль пользователю.
     *
     * @param id идентификатор пользователя
     * @param role роль для добавления
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    public User addRoleToUser(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        user.addRole(role);
        return userRepository.save(user);
    }

    /**
     * Удаляет роль у пользователя.
     *
     * @param id идентификатор пользователя
     * @param role роль для удаления
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    public User removeRoleFromUser(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        user.removeRole(role);
        return userRepository.save(user);
    }

    /**
     * Обновляет пароль пользователя.
     * Автоматически шифрует новый пароль перед сохранением.
     *
     * @param id идентификатор пользователя
     * @param newPassword новый пароль
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден или пароль пустой
     */
    public User updatePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Проверяет существование пользователя с указанным именем пользователя.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email для проверки
     * @return true если пользователь существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Находит пользователей по роли.
     *
     * @param role роль для поиска
     * @return список пользователей с указанной ролью
     */
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * Выполняет поиск пользователей по имени пользователя (без учета регистра).
     *
     * @param username фрагмент имени пользователя для поиска
     * @return список найденных пользователей
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }
}
