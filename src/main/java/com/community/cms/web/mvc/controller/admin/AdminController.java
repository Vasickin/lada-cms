package com.community.cms.web.mvc.controller.admin;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.service.page.CustomPageService;
import com.community.cms.web.mvc.dto.PageStatistics;
import com.community.cms.domain.enums.PageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для административной панели управления.
 *
 * <p>Обеспечивает отображение дашборда и других административных страниц,
 * доступных только аутентифицированным пользователям с соответствующими правами.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Отображение главного дашборда с общей статистикой</li>
 *   <li>Предоставление обзора системы и быстрого доступа к функциям</li>
 *   <li>Отображение последней активности и состояния контента</li>
 *   <li>Управление основными страницами сайта</li>
 * </ul>
 *
 * @author Vasickin
 * @version 2.0
 * @since 2025
 * @see CustomPageService
 */
@Controller
public class AdminController {

    private final CustomPageService pageService;

    /**
     * Конструктор с внедрением зависимости CustomPageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public AdminController(CustomPageService pageService) {
        this.pageService = pageService;
    }

    /**
     * Отображает главную страницу административной панели (дашборд).
     *
     * <p>На дашборде отображается общая статистика по страницам,
     * последние созданные страницы и быстрые ссылки для управления контентом.</p>
     *
     * @param model модель для передачи данных в представление
     * @param authentication объект аутентификации Spring Security
     * @return имя шаблона дашборда ("admin/dashboard")
     */
    @GetMapping("/admin")
    public String dashboard(Model model, Authentication authentication) {
        // Используем эффективные методы сервиса
        PageStatistics statistics = pageService.getPageStatistics();
        List<CustomPage> recentPages = pageService.findRecentPages(5);

        // Получаем основные страницы сайта для быстрого доступа
        List<CustomPage> sitePages = pageService.findAllSitePages();

        // Передаем данные в модель
        model.addAttribute("totalPages", statistics.totalPages());
        model.addAttribute("publishedCount", statistics.publishedCount());
        model.addAttribute("draftCount", statistics.draftCount());
        model.addAttribute("recentPages", recentPages);
        model.addAttribute("sitePages", sitePages);

        // Добавляем информацию о текущем пользователе
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("currentUsername", username);

            // Получаем роли пользователя
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            model.addAttribute("currentUserRoles", roles);

            // Добавляем флаг аутентификации
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("currentUsername", "Гость");
            model.addAttribute("currentUserRoles", List.of("ROLE_ANONYMOUS"));
        }

        return "admin/dashboard";
    }

    /**
     * Отображает страницу управления основными страницами сайта.
     *
     * <p>Показывает список всех основных страниц сайта (О нас, Проекты, Галерея и т.д.)
     * с возможностью быстрого редактирования каждой страницы.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона списка основных страниц ("admin/site-pages/list")
     */
    @GetMapping("/admin/site-pages")
    public String manageSitePages(Model model) {
        List<CustomPage> sitePages = pageService.findAllSitePages();
        model.addAttribute("sitePages", sitePages);

        // Добавляем информацию о типах страниц для отображения
        model.addAttribute("pageTypes", PageType.values());

        return "admin/site-pages/list";
    }

    /**
     * Отображает форму редактирования основной страницы сайта.
     *
     * <p>Предоставляет интерфейс для редактирования контента, заголовка и SEO-параметров
     * основных страниц сайта. Автоматически определяет тип страницы по переданному параметру.</p>
     *
     * @param pageType тип страницы для редактирования
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования ("admin/site-pages/edit")
     */
    @GetMapping("/admin/site-pages/edit/{pageType}")
    public String editSitePage(@PathVariable String pageType, Model model) {
        try {
            PageType type = PageType.valueOf(pageType.toUpperCase());
            Optional<CustomPage> pageOpt = pageService.findPublishedPageByType(type);

            CustomPage page;
            if (pageOpt.isPresent()) {
                page = pageOpt.get();
            } else {
                // Создаем новую страницу с настройками по умолчанию
                page = new CustomPage();
                page.setPageType(type);
                page.setTitle(getDefaultTitleForPageType(type));
                page.setSlug(getDefaultSlugForPageType(type));
                page.setPublished(true);
            }

            model.addAttribute("page", page);
            model.addAttribute("pageType", type);
            model.addAttribute("pageTypeName", getPageTypeDisplayName(type));

            return "admin/site-pages/edit";

        } catch (IllegalArgumentException e) {
            // Если передан неизвестный тип страницы
            return "redirect:/admin/site-pages?error=invalid_page_type";
        }
    }

    /**
     * Обрабатывает сохранение изменений основной страницы сайта.
     *
     * @param pageType тип страницы
     * @param title заголовок страницы
     * @param content содержимое страницы
     * @param metaDescription SEO описание
     * @param model модель для передачи данных в представление
     * @return перенаправление на список основных страниц
     */
    @PostMapping("/admin/site-pages/update/{pageType}")
    public String updateSitePage(@PathVariable String pageType,
                                 @RequestParam String title,
                                 @RequestParam String content,
                                 @RequestParam(required = false) String metaDescription,
                                 Model model) {
        try {
            PageType type = PageType.valueOf(pageType.toUpperCase());

            // Находим существующую страницу или создаем новую
            Optional<CustomPage> existingPageOpt = pageService.findPublishedPageByType(type);
            CustomPage page;

            if (existingPageOpt.isPresent()) {
                page = existingPageOpt.get();
            } else {
                page = new CustomPage();
                page.setPageType(type);
                page.setSlug(getDefaultSlugForPageType(type));
                page.setPublished(true);
            }

            // Обновляем данные
            page.setTitle(title);
            page.setContent(content);
            page.setMetaDescription(metaDescription);

            pageService.savePage(page);

            return "redirect:/admin/site-pages?success=page_updated";

        } catch (IllegalArgumentException e) {
            return "redirect:/admin/site-pages?error=invalid_page_type";
        }
    }

    /**
     * Возвращает отображаемое имя для типа страницы.
     *
     * @param pageType тип страницы
     * @return читаемое имя для отображения в интерфейсе
     */
    private String getPageTypeDisplayName(PageType pageType) {
        switch (pageType) {
            case ABOUT:
                return "О нас";
            case PROJECTS:
                return "Наши проекты";
            case GALLERY:
                return "Галерея";
            case PATRONS:
                return "Меценатам";
            case CONTACT:
                return "Контакты";
            default:
                return "Страница сайта";
        }
    }

    /**
     * Возвращает заголовок по умолчанию для типа страницы.
     *
     * @param pageType тип страницы
     * @return заголовок по умолчанию
     */
    private String getDefaultTitleForPageType(PageType pageType) {
        switch (pageType) {
            case ABOUT:
                return "О нашей организации";
            case PROJECTS:
                return "Наши проекты";
            case GALLERY:
                return "Галерея мероприятий";
            case PATRONS:
                return "Информация для меценатов";
            case CONTACT:
                return "Контактная информация";
            default:
                return "Страница сайта";
        }
    }

    /**
     * Возвращает slug по умолчанию для типа страницы.
     *
     * @param pageType тип страницы
     * @return slug по умолчанию
     */
    private String getDefaultSlugForPageType(PageType pageType) {
        switch (pageType) {
            case ABOUT:
                return "about";
            case PROJECTS:
                return "projects";
            case GALLERY:
                return "gallery";
            case PATRONS:
                return "patrons";
            case CONTACT:
                return "contact";
            default:
                return "page";
        }
    }
}