package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.model.people.User;
import com.community.cms.domain.service.people.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Контроллер для управления пользователями в административной панели.
 *
 * <p>Предоставляет функциональность для создания, просмотра, редактирования
 * и удаления пользователей, а также управления их ролями и статусами.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Просмотр списка всех пользователей системы</li>
 *   <li>Создание новых пользователей с назначением ролей</li>
 *   <li>Редактирование существующих пользователей</li>
 *   <li>Управление активностью учетных записей</li>
 *   <li>Назначение и снятие ролей</li>
 * </ul>
 *
 * <p>Доступ к функциям управления пользователями ограничен ролью ADMIN.
 * Это обеспечивает безопасность системы и предотвращает несанкционированное
 * изменение прав доступа.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see UserService
 * @see User
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости UserService.
     *
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отображает список всех пользователей системы.
     *
     * <p>На странице отображается таблица с информацией о пользователях:
     * имя пользователя, email, роли, статус активности и дата создания.
     * Предоставляет возможности для управления каждым пользователем.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона списка пользователей ("admin/users/list")
     */
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();

        // Добавляем проверку для каждого пользователя
        Map<Long, Boolean> canDeleteMap = new HashMap<>();
        User currentUser = getCurrentUser();

        for (User user : users) {
            boolean canDelete = true;

            // Нельзя удалить самого себя
            if (user.getId().equals(currentUser.getId())) {
                canDelete = false;
            }
            // Нельзя удалить последнего администратора
            else if (user.hasRole("ADMIN")) {
                long activeAdminCount = userService.findUsersByRole("ADMIN").stream()
                        .filter(User::isEnabled)
                        .count();
                canDelete = activeAdminCount > 1;
            }

            canDeleteMap.put(user.getId(), canDelete);
        }

        model.addAttribute("users", users);
        model.addAttribute("canDeleteMap", canDeleteMap);
        return "admin/users/list";
    }

    /**
     * Отображает форму для создания нового пользователя.
     *
     * <p>Форма содержит поля для ввода основных данных пользователя:
     * имя пользователя, email и пароль. Также предоставляет выбор ролей
     * из предопределенного набора.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания пользователя ("admin/users/create")
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("availableRoles", getAvailableRoles());
        return "admin/users/create";
    }

    /**
     * Обрабатывает отправку формы создания пользователя.
     *
     * <p>Выполняет валидацию введенных данных и сохраняет нового пользователя
     * в базе данных. В случае успеха перенаправляет на список пользователей
     * с сообщением об успешном создании.</p>
     *
     * @param user создаваемый пользователь
     * @param bindingResult результаты валидации формы
     * @param redirectAttributes атрибуты для перенаправления
     * @param model модель для передачи данных в представление
     * @return перенаправление на список пользователей или возврат к форме при ошибках
     */
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/create";
        }

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " успешно создан");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/create";
        }
    }

    /**
     * Отображает форму для редактирования существующего пользователя.
     *
     * <p>Форма предзаполняется текущими данными пользователя и позволяет
     * изменять email, роли и статус активности. Изменение имени пользователя
     * не поддерживается для сохранения целостности данных.</p>
     *
     * @param id идентификатор редактируемого пользователя
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования пользователя ("admin/users/edit")
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        model.addAttribute("user", user);
        model.addAttribute("availableRoles", getAvailableRoles());
        return "admin/users/edit";
    }

    /**
     * Обрабатывает отправку формы редактирования пользователя.
     *
     * <p>Обновляет данные пользователя в базе данных. Поддерживает изменение
     * email, ролей и статуса активности. Пароль обновляется только если
     * указано новое значение.</p>
     *
     * @param id идентификатор редактируемого пользователя
     * @param user обновленные данные пользователя
     * @param roles выбранные роли пользователя
     * @param redirectAttributes атрибуты для перенаправления
     * @param model модель для передачи данных в представление
     * @return перенаправление на список пользователей или возврат к форме при ошибках
     */
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @RequestParam(value = "roles", required = false) Set<String> roles,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        try {
            // Находим существующего пользователя
            User existingUser = userService.findUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

            // Обновляем только разрешенные поля
            existingUser.setEmail(user.getEmail());
            existingUser.setEnabled(user.isEnabled());

            // Обновляем роли если переданы
            if (roles != null) {
                existingUser.setRoles(new HashSet<>(roles));
            } else {
                existingUser.setRoles(new HashSet<>());
            }

            // Обновляем пароль только если указан новый
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }

            userService.saveUser(existingUser);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + existingUser.getUsername() + " успешно обновлен");
            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/edit";
        }
    }

    /**
     * Активирует учетную запись пользователя.
     *
     * <p>Позволяет разблокировать ранее заблокированную учетную запись.
     * После активации пользователь сможет войти в систему.</p>
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.enableUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " активирован");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Деактивирует учетную запись пользователя.
     *
     * <p>Блокирует учетную запись пользователя. После деактивации
     * пользователь не сможет войти в систему до повторной активации.</p>
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User userToDisable = userService.findUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Проверяем можно ли заблокировать пользователя
            if (!canDisableUser(userToDisable)) {
                if (userToDisable.getId().equals(getCurrentUser().getId())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Нельзя заблокировать собственную учетную запись");
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "Нельзя заблокировать последнего администратора");
                }
                return "redirect:/admin/users";
            }

            User user = userService.disableUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " заблокирован");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Добавляет роль пользователю.
     *
     * <p>Назначает дополнительную роль существующему пользователю.
     * Расширяет права доступа пользователя в системе.</p>
     *
     * @param id идентификатор пользователя
     * @param role добавляемая роль
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/add-role/{id}")
    public String addRoleToUser(@PathVariable Long id,
                                @RequestParam String role,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.addRoleToUser(id, role);
            redirectAttributes.addFlashAttribute("success",
                    "Роль " + role + " добавлена пользователю " + user.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Удаляет роль у пользователя.
     *
     * <p>Снимает роль с пользователя. Уменьшает права доступа
     * пользователя в системе. Не позволяет удалить последнюю роль.</p>
     *
     * @param id идентификатор пользователя
     * @param role удаляемая роль
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/remove-role/{id}")
    public String removeRoleFromUser(@PathVariable Long id,
                                     @RequestParam String role,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.removeRoleFromUser(id, role);
            redirectAttributes.addFlashAttribute("success",
                    "Роль " + role + " удалена у пользователя " + user.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Текущий пользователь не найден"));
    }

    /**
     * Проверяет, можно ли заблокировать пользователя.
     * Запрещает блокировку самого себя и последнего администратора.
     *
     * @param user пользователь для проверки
     * @return true если блокировка разрешена, false если запрещена
     */
    private boolean canDisableUser(User user) {
        User currentUser = getCurrentUser();

        // Запрещаем блокировку самого себя
        if (user.getId().equals(currentUser.getId())) {
            return false;
        }

        // Запрещаем блокировку последнего активного администратора
        if (user.hasRole("ADMIN")) {
            long activeAdminCount = userService.findUsersByRole("ADMIN").stream()
                    .filter(User::isEnabled)
                    .count();
            return activeAdminCount > 1;
        }

        return true;
    }

    /**
     * Возвращает набор доступных ролей для назначения пользователям.
     *
     * <p>Определяет перечень ролей, которые могут быть назначены
     * пользователям через административный интерфейс.</p>
     *
     * @return множество доступных ролей
     */
    private Set<String> getAvailableRoles() {
        return Set.of("ADMIN", "EDITOR", "USER");
    }

    /**
     * Удаляет пользователя из системы.
     * Проверяет можно ли удалить пользователя (нельзя удалить самого себя или последнего администратора).
     *
     * @param id идентификатор пользователя для удаления
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User userToDelete = userService.findUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Проверяем можно ли удалить пользователя
            if (!canDeleteUser(userToDelete)) {
                if (userToDelete.getId().equals(getCurrentUser().getId())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Нельзя удалить собственную учетную запись");
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "Нельзя удалить последнего администратора");
                }
                return "redirect:/admin/users";
            }

            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + userToDelete.getUsername() + " успешно удален");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Проверяет, можно ли удалить пользователя.
     * Запрещает удаление самого себя и последнего администратора.
     *
     * @param user пользователь для проверки
     * @return true если удаление разрешено, false если запрещено
     */
    private boolean canDeleteUser(User user) {
        return canDisableUser(user); // Используем ту же логику что и для блокировки
    }
}