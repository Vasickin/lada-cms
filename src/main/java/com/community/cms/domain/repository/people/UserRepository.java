package com.community.cms.domain.repository.people;

import com.community.cms.domain.model.people.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User в базе данных.
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов.
 *
 * <p>Наследует {@link JpaRepository} который предоставляет стандартные методы:
 * <ul>
 *   <li>{@code save()} - сохранение сущности</li>
 *   <li>{@code findById()} - поиск по идентификатору</li>
 *   <li>{@code findAll()} - получение всех записей</li>
 *   <li>{@code delete()} - удаление сущности</li>
 * </ul>
 *
 * <p>Дополнительные методы репозитория:
 * <ul>
 *   <li>Поиск пользователя по имени пользователя</li>
 *   <li>Поиск пользователя по email</li>
 *   <li>Проверка существования пользователя</li>
 *   <li>Поиск пользователей по роли</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see User
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по уникальному имени пользователя.
     * Используется для аутентификации при входе в систему.
     *
     * @param username имя пользователя для поиска
     * @return Optional содержащий пользователя если найден, иначе empty
     * @throws org.springframework.dao.DataAccessException при ошибках доступа к данным
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по уникальному email адресу.
     * Используется для проверки уникальности email при регистрации.
     *
     * @param email email адрес для поиска
     * @return Optional содержащий пользователя если найден, иначе empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным именем пользователя.
     * Используется для валидации при создании новых пользователей.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь с таким именем существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным email адресом.
     * Используется для валидации при создании новых пользователей.
     *
     * @param email email адрес для проверки
     * @return true если пользователь с таким email существует, иначе false
     */
    boolean existsByEmail(String email);

    /**
     * Находит всех пользователей с указанной ролью.
     * Используется для управления доступом и фильтрации пользователей.
     *
     * @param role роль для поиска (например, "ROLE_ADMIN")
     * @return список пользователей с указанной ролью
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") String role);

    /**
     * Находит всех активных пользователей (с enabled = true).
     * Используется для отображения списка доступных пользователей.
     *
     * @return список активных пользователей
     */
    List<User> findByEnabledTrue();

    /**
     * Находит всех неактивных пользователей (с enabled = false).
     * Используется для управления заблокированными учетными записями.
     *
     * @return список неактивных пользователей
     */
    List<User> findByEnabledFalse();

    /**
     * Находит пользователей по части имени пользователя (без учета регистра).
     * Используется для поиска пользователей в административной панели.
     *
     * @param username фрагмент имени пользователя для поиска
     * @return список найденных пользователей
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Находит пользователей по части email адреса (без учета регистра).
     * Используется для поиска пользователей в административной панели.
     *
     * @param email фрагмент email адреса для поиска
     * @return список найденных пользователей
     */
    List<User> findByEmailContainingIgnoreCase(String email);
}
