package com.community.cms.web.mvc.validation;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller for handling HTTP errors and displaying user-friendly error pages.
 * Implements Spring Boot's ErrorController interface to override default white-label error pages.
 * Пользовательский контроллер для обработки HTTP ошибок и отображения пользовательских страниц ошибок.
 * Реализует интерфейс ErrorController Spring Boot для переопределения стандартных страниц ошибок.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2024
 * @see org.springframework.boot.web.servlet.error.ErrorController
 * @see HttpStatus
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles all error requests by analyzing the HTTP status code and routing to appropriate error pages.
     * Extracts error information from the request attributes and provides user-friendly error messages.
     * This method is automatically called by Spring Boot when an error occurs in the application.
     *
     * Обрабатывает все запросы ошибок путем анализа HTTP кода статуса и направления на соответствующие страницы ошибок.
     * Извлекает информацию об ошибке из атрибутов запроса и предоставляет пользовательские сообщения об ошибках.
     * Этот метод автоматически вызывается Spring Boot при возникновении ошибки в приложении.
     *
     * @param request the HTTP request object containing error attributes such as status code, error message,
     *                and exception information. The request attributes are populated by Spring Boot's
     *                error handling mechanism.
     *                HTTP объект запроса, содержащий атрибуты ошибки, такие как код статуса, сообщение об ошибке
     *                и информация об исключении. Атрибуты запроса заполняются механизмом обработки ошибок Spring Boot.
     * @param model the Spring UI model for passing data to the view. Used to provide error-specific
     *              information to the error page templates, such as the HTTP status code.
     *              Spring UI модель для передачи данных в представление. Используется для предоставления
     *              информации об ошибке шаблонам страниц ошибок, такой как HTTP код статуса.
     * @return the name of the error page template to render. Returns specific error templates for
     *         common HTTP status codes (404, 500, 403, 400) or a default error template for unhandled codes.
     *         имя шаблона страницы ошибки для отображения. Возвращает конкретные шаблоны ошибок для
     *         распространенных HTTP кодов статуса (404, 500, 403, 400) или шаблон ошибки по умолчанию для необработанных кодов.
     *
     * @see HttpServletRequest
     * @see RequestDispatcher#ERROR_STATUS_CODE
     * @see HttpStatus
     * @see Model
     *
     * @implNote This method maps to the "/error" path and is called by Spring Boot's default error handling.
     *           It examines the HTTP status code from the request attributes and routes to the appropriate
     *           custom error page. If no specific handler is found for the status code, it defaults to the 500 error page.
     *
     * @implNote Этот метод маппится на путь "/error" и вызывается стандартной обработкой ошибок Spring Boot.
     *           Он проверяет HTTP код статуса из атрибутов запроса и направляет на соответствующую
     *           пользовательскую страницу ошибки. Если для кода статуса не найден конкретный обработчик,
     *           используется страница ошибки 500 по умолчанию.
     *
     * @implSpec The method follows this decision logic:
     *           - 404 (NOT_FOUND) → "error/404"
     *           - 500 (INTERNAL_SERVER_ERROR) → "error/500"
     *           - 403 (FORBIDDEN) → "error/403"
     *           - 400 (BAD_REQUEST) → "error/400"
     *           - All other codes → "error/500" (default)
     *
     * @implSpec Метод следует следующей логике принятия решений:
     *           - 404 (NOT_FOUND) → "error/404"
     *           - 500 (INTERNAL_SERVER_ERROR) → "error/500"
     *           - 403 (FORBIDDEN) → "error/403"
     *           - 400 (BAD_REQUEST) → "error/400"
     *           - Все остальные коды → "error/500" (по умолчанию)
     *
     * @throws NumberFormatException if the status code attribute cannot be parsed to an integer.
     *         This should not occur in normal operation as Spring Boot provides valid status codes.
     *
     * @throws NumberFormatException если атрибут кода статуса не может быть преобразован в целое число.
     *         Это не должно происходить при нормальной работе, так как Spring Boot предоставляет valid коды статуса.
     *
     * @example
     * When a user accesses a non-existent page: "/non-existent-page"
     * → Spring Boot detects 404 error
     * → Calls this method with status code 404
     * → Returns "error/404" template
     * → User sees custom 404 page
     *
     * @example
     * Когда пользователь обращается к несуществующей странице: "/non-existent-page"
     * → Spring Boot обнаруживает ошибку 404
     * → Вызывает этот метод с кодом статуса 404
     * → Возвращает шаблон "error/404"
     * → Пользователь видит пользовательскую страницу 404
     *
     * @since 1.0
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get the error status code from request attributes /
        // Получаем код статуса ошибки из атрибутов запроса
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Add error code to model for displaying in the template /
            // Добавляем код ошибки в модель для отображения в шаблоне
            model.addAttribute("errorCode", statusCode);

            // Route to specific error pages based on status code /
            // Направляем на конкретные страницы ошибок на основе кода статуса
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return "error/400";
            }
        }

        // Default fallback for unhandled error codes /
        // Запасной вариант по умолчанию для необработанных кодов ошибок
        return "error/500";
    }

    /**
     * Returns the error path that this controller handles.
     * Required by the ErrorController interface implementation.
     * Возвращает путь ошибки, который обрабатывает этот контроллер.
     * Требуется для реализации интерфейса ErrorController.
     *
     * @return the error path string / строку пути ошибки
     * @see org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
     */
    public String getErrorPath() {
        return "/error";
    }
}
