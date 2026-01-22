package com.community.cms.web.mvc.controller.admin.page;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.service.page.CustomPageService;
import com.community.cms.web.mvc.controller.admin.AdminController;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для обработки HTTP запросов связанных со страницами.
 * Обеспечивает взаимодействие между веб-интерфейсом и сервисным слоем.
 *
 * <p>Основные responsibilities:
 * <ul>
 *   <li>Обработка HTTP запросов (GET, POST, PUT, DELETE)</li>
 *   <li>Валидация входящих данных</li>
 *   <li>Подготовка данных для представления (Model)</li>
 *   <li>Обработка исключений и перенаправлений</li>
 * </ul>
 *
 * <p>Управляет кастомными страницами созданными через админку.
 * Основные страницы сайта (about, projects, gallery, patrons, contact)
 * управляются через AdminController.</p>
 *
 * @author Vasickin
 * @version 1.1
 * @since 2025
 * @see CustomPageService
 * @see CustomPage
 * @see AdminController
 */
@Controller
@RequestMapping("/pages")
public class AdminPageCustomController {

    private final CustomPageService pageService;

    /**
     * Конструктор с внедрением зависимости CustomPageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public AdminPageCustomController(CustomPageService pageService) {
        this.pageService = pageService;
    }

    /**
     * Отображает список всех кастомных страниц (для административной части).
     *
     * <p>Показывает только кастомные страницы (PageType.CUSTOM), созданные через админку.
     * Основные страницы сайта отображаются через отдельный интерфейс в AdminController.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона для отображения списка страниц ("pages/list")
     */
    @GetMapping
    public String listPages(Model model) {
        List<CustomPage> pages = pageService.findAllPages();
        model.addAttribute("pages", pages);
        return "pages/list";
    }

    /**
     * Отображает форму для создания новой кастомной страницы.
     *
     * <p>Предоставляет интерфейс для создания произвольных страниц с уникальными slug.
     * Для редактирования основных страниц сайта используется отдельный интерфейс.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания страницы ("pages/create")
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("page", new CustomPage());
        return "pages/create";
    }

    /**
     * Обрабатывает отправку формы создания новой кастомной страницы.
     * Выполняет валидацию данных и сохраняет страницу.
     *
     * <p>Проверяет уникальность slug и корректность введенных данных.
     * В случае ошибок возвращает пользователя к форме с сообщениями об ошибках.</p>
     *
     * @param page создаваемая страница
     * @param bindingResult результаты валидации
     * @param model модель для передачи данных в представление
     * @return перенаправление на список страниц или возврат к форме при ошибках
     */
    @PostMapping("/create")
    public String createPage(@Valid @ModelAttribute CustomPage page,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "pages/create";
        }

        try {
            pageService.savePage(page);
            return "redirect:/pages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/create";
        }
    }

    /**
     * Отображает форму редактирования существующей кастомной страницы.
     *
     * <p>Предоставляет доступ к редактированию заголовка, содержимого и мета-данных
     * кастомных страниц. Для основных страниц сайта используется отдельный интерфейс.</p>
     *
     * @param id идентификатор редактируемой страницы
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования ("pages/edit") или перенаправление при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<CustomPage> page = pageService.findPageById(id);
        if (page.isPresent()) {
            model.addAttribute("page", page.get());
            return "pages/edit";
        } else {
            return "redirect:/pages";
        }
    }

    /**
     * Обрабатывает отправку формы редактирования кастомной страницы.
     *
     * <p>Обновляет данные страницы в базе данных после прохождения валидации.
     * Сохраняет временные метки обновления для отслеживания изменений.</p>
     *
     * @param id идентификатор редактируемой страницы
     * @param page обновленные данные страницы
     * @param bindingResult результаты валидации
     * @param model модель для передачи данных в представление
     * @return перенаправление на список страниц или возврат к форме при ошибках
     */
    @PostMapping("/edit/{id}")
    public String updatePage(@PathVariable Long id,
                             @Valid @ModelAttribute CustomPage page,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "pages/edit";
        }

        try {
            page.setId(id); // Убеждаемся что ID сохраняется
            pageService.savePage(page);
            return "redirect:/pages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/edit";
        }
    }

    /**
     * Удаляет кастомную страницу по идентификатору.
     *
     * <p>Выполняет мягкое удаление страницы из системы. Операция необратима
     * и должна подтверждаться через интерфейс администратора.</p>
     *
     * @param id идентификатор удаляемой страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/delete/{id}")
    public String deletePage(@PathVariable Long id) {
        try {
            pageService.deletePage(id);
        } catch (IllegalArgumentException e) {
            // Логируем ошибку, но не прерываем выполнение для пользовательского опыта
            System.err.println("Ошибка при удалении страницы: " + e.getMessage());
        }
        return "redirect:/pages";
    }

    /**
     * Публикует кастомную страницу (устанавливает статус published = true).
     *
     * <p>Делает страницу доступной для публичного просмотра по её slug.
     * После публикации страница становится видна всем посетителям сайта.</p>
     *
     * @param id идентификатор страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/publish/{id}")
    public String publishPage(@PathVariable Long id) {
        try {
            pageService.publishPage(id);
        } catch (IllegalArgumentException e) {
            // Логируем ошибку для отладки
            System.err.println("Ошибка при публикации страницы: " + e.getMessage());
        }
        return "redirect:/pages";
    }

    /**
     * Снимает кастомную страницу с публикации (устанавливает статус published = false).
     *
     * <p>Скрывает страницу от публичного доступа, переводя её в статус черновика.
     * Страница остается в системе и может быть опубликована снова.</p>
     *
     * @param id идентификатор страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/unpublish/{id}")
    public String unpublishPage(@PathVariable Long id) {
        try {
            pageService.unpublishPage(id);
        } catch (IllegalArgumentException e) {
            // Логируем ошибку для отладки
            System.err.println("Ошибка при снятии с публикации: " + e.getMessage());
        }
        return "redirect:/pages";
    }
}