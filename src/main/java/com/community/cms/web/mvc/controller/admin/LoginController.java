package com.community.cms.web.mvc.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер для обработки страниц аутентификации и авторизации.
 *
 * <p>Управляет отображением формы входа в систему, обработкой параметров
 * аутентификации и отображением страниц связанных с безопасностью.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Отображение формы входа с обработкой ошибок</li>
 *   <li>Обработка успешного выхода из системы</li>
 *   <li>Отображение страницы отказа в доступе</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@Controller
public class LoginController {

    /**
     * Отображает страницу входа в систему с обработкой параметров ошибок и выхода.
     *
     * <p>Метод обрабатывает GET запросы к /login и отображает форму аутентификации.
     * Поддерживает отображение сообщений об ошибках и успешном выходе из системы.</p>
     *
     * @param error необязательный параметр, указывающий на неудачную попытку аутентификации
     * @param logout необязательный параметр, указывающий на успешный выход из системы
     * @param model модель Spring MVC для передачи данных в представление
     * @return имя шаблона страницы логина ("login")
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        //
        // ERROR HANDLING / ОБРАБОТКА ОШИБОК
        // Add error message to model if authentication failed /
        // Добавляем сообщение об ошибке в модель если аутентификация не удалась
        //
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }

        //
        // LOGOUT HANDLING / ОБРАБОТКА ВЫХОДА
        // Add success message to model after logout /
        // Добавляем сообщение об успехе в модель после выхода
        //
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }

    /**
     * Отображает страницу отказа в доступе.
     *
     * <p>Вызывается когда аутентифицированный пользователь пытается получить доступ
     * к ресурсу, для которого у него недостаточно прав.</p>
     *
     * @return имя шаблона страницы отказа в доступе ("error/access-denied")
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
