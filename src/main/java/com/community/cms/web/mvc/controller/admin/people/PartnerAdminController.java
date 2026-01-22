package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.enums.PartnerType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.PartnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер административной панели для управления партнерами.
 *
 * <p>Предоставляет интерфейс для создания, редактирования, удаления
 * и управления партнерами организации "ЛАДА". Интегрирован с системой
 * проектов для назначения партнеров на проекты.</p>
 *
 * @author Community CMS
 * @version 2.0
 * @since 2025
 * @see Partner
 * @see PartnerService
 */
@Controller
@RequestMapping("/admin/partners")
public class PartnerAdminController {

    private final PartnerService partnerService;
    private final ProjectService projectService;

    // Константы для сортировки по умолчанию
    private static final String DEFAULT_SORT_FIELD = "sortOrder";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param partnerService сервис для работы с партнерами
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public PartnerAdminController(PartnerService partnerService,
                                  ProjectService projectService) {
        this.partnerService = partnerService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ПАРТНЕРОВ С ПАГИНАЦИЕЙ И ФИЛЬТРАМИ ==================

    /**
     * Отображает список всех партнеров с поддержкой пагинации и фильтрации.
     *
     * @param search поисковый запрос (опционально)
     * @param type тип партнера (опционально)
     * @param status статус активности (ACTIVE/INACTIVE) (опционально)
     * @param hasLogo наличие логотипа (true/false) (опционально)
     * @param page номер страницы (начинается с 0)
     * @param size количество элементов на странице
     * @param model модель для передачи данных в представление
     * @return шаблон списка партнеров
     */
    @GetMapping
    public String listPartners(@RequestParam(required = false) String search,
                               @RequestParam(required = false) PartnerType type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String hasLogo,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model) {

        // Создаем Pageable с сортировкой по умолчанию
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_FIELD));

        Page<Partner> partnersPage;

        // ПРИМЕНЯЕМ ФИЛЬТРЫ В ЗАВИСИМОСТИ ОТ ПАРАМЕТРОВ
        if (search != null && !search.trim().isEmpty()) {
            // Если есть поисковый запрос
            partnersPage = searchPartnersWithPagination(search.trim(), pageable);
        } else if (type != null || (status != null && !status.isEmpty()) || (hasLogo != null && !hasLogo.isEmpty())) {
            // Если есть ДРУГИЕ фильтры (тип, статус, логотип)
            partnersPage = filterPartners(type, status, hasLogo, pageable);
        } else {
            // ИНАЧЕ: НЕТ ФИЛЬТРОВ - показываем ВСЕХ партнеров (и активных, и неактивных)
            partnersPage = partnerService.findAll(pageable); // ← ИЗМЕНИЛОСЬ!
        }

        // Добавляем данные в модель
        model.addAttribute("partnersPage", partnersPage);
        model.addAttribute("title", "Управление партнерами");
        model.addAttribute("partnerTypes", PartnerType.values());

        // Добавляем параметры фильтров для сохранения состояния
        model.addAttribute("selectedSearch", search);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedHasLogo", hasLogo);

        return "admin/partners/list";
    }

    /**
     * Поиск партнеров с пагинацией.
     *
     * @param search поисковый запрос
     * @param pageable параметры пагинации
     * @return страница с результатами поиска
     */
    private Page<Partner> searchPartnersWithPagination(String search, Pageable pageable) {
        return partnerService.searchByNameOrDescriptionWithPagination(search, pageable);
    }

    /**
     * Фильтрация партнеров с пагинацией.
     *
     * @param type тип партнера
     * @param status статус активности
     * @param hasLogo наличие логотипа
     * @param pageable параметры пагинации
     * @return страница с отфильтрованными партнерами
     */
    private Page<Partner> filterPartners(PartnerType type, String status, String hasLogo, Pageable pageable) {

        // 1. Начинаем со ВСЕХ партнеров (по умолчанию активных)
        List<Partner> filteredList;

        if (status != null && "INACTIVE".equalsIgnoreCase(status)) {
            // Если точно хотим неактивных - начинаем с неактивных
            filteredList = new ArrayList<>(partnerService.findAllInactive());
        } else {
            // Во всех остальных случаях (включая null, пустую строку, "ACTIVE") - начинаем с активных
            filteredList = new ArrayList<>(partnerService.findAllActive());
        }

        // 2. Фильтруем по статусу (если указан)
        if (status != null && !status.isEmpty()) {
            boolean isActive = "ACTIVE".equalsIgnoreCase(status);
            filteredList = filteredList.stream()
                    .filter(p -> p.isActive() == isActive)
                    .collect(Collectors.toList());
        }

        // 3. Фильтруем по типу (если указан)
        if (type != null) {
            filteredList = filteredList.stream()
                    .filter(p -> type.equals(p.getType()))
                    .collect(Collectors.toList());
        }

        // 4. Фильтруем по логотипу (если указан) ← ИСПРАВЛЕНО!
        if (hasLogo != null && !hasLogo.isEmpty()) {
            if ("true".equalsIgnoreCase(hasLogo)) {
                // С логотипом
                filteredList = filteredList.stream()
                        .filter(Partner::hasLogo)
                        .collect(Collectors.toList());
            } else if ("false".equalsIgnoreCase(hasLogo)) {
                // Без логотипа
                filteredList = filteredList.stream()
                        .filter(p -> !p.hasLogo())
                        .collect(Collectors.toList());
            }
        }

        // 5. Применяем пагинацию
        if (filteredList.isEmpty()) {
            return Page.empty(pageable);
        }

        return createPageFromList(filteredList, pageable);
    }

    /**
     * Создает Page из List с учетом пагинации.
     * Временное решение до добавления пагинации в репозиторий.
     */
    private Page<Partner> createPageFromList(List<Partner> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return Page.empty(pageable);
        }

        List<Partner> pageContent = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, list.size());
    }

    /**
     * Отображает список неактивных партнеров с пагинацией.
     *
     * @param page номер страницы
     * @param size количество элементов на странице
     * @param model модель для передачи данных в представление
     * @return шаблон списка неактивных партнеров
     */
    @GetMapping("/inactive")
    public String listInactivePartners(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_FIELD));
        Page<Partner> inactivePartnersPage = partnerService.findAllInactiveWithPagination(pageable);

        model.addAttribute("partnersPage", inactivePartnersPage);
        model.addAttribute("title", "Неактивные партнеры");
        model.addAttribute("showInactive", true);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("selectedStatus", "INACTIVE");

        return "admin/partners/list";
    }

    /**
     * Отображает список партнеров по типу с пагинацией.
     *
     * @param type тип партнера
     * @param page номер страницы
     * @param size количество элементов на странице
     * @param model модель для передачи данных в представление
     * @return шаблон списка партнеров по типу
     */
    @GetMapping("/type/{type}")
    public String listPartnersByType(@PathVariable String type,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     Model model) {
        PartnerType partnerType = PartnerType.fromString(type);
        if (partnerType == null) {
            return "redirect:/admin/partners";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_FIELD));
        Page<Partner> partnersPage = partnerService.findByTypeWithPagination(partnerType, pageable);

        model.addAttribute("partnersPage", partnersPage);
        model.addAttribute("title", "Партнеры: " + partnerType.getDisplayName());
        model.addAttribute("filterType", partnerType);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("selectedType", partnerType);

        return "admin/partners/list";
    }

    // ================== ПОИСК И ФИЛЬТРАЦИЯ (новые методы) ==================

    /**
     * Поиск партнеров по названию или описанию с пагинацией.
     *
     * @param searchTerm поисковый запрос
     * @param page номер страницы
     * @param size количество элементов на странице
     * @param model модель для передачи данных в представление
     * @return шаблон списка с результатами поиска
     */
    @GetMapping("/search")
    public String searchPartners(@RequestParam String searchTerm,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_FIELD));
        Page<Partner> searchResultsPage = searchPartnersWithPagination(searchTerm, pageable);

        model.addAttribute("partnersPage", searchResultsPage);
        model.addAttribute("title", "Результаты поиска: " + searchTerm);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("selectedSearch", searchTerm);

        return "admin/partners/list";
    }

    /**
     * Фильтрация партнеров по типу с пагинацией.
     *
     * @param type тип партнера
     * @param status статус активности
     * @param hasLogo наличие логотипа
     * @param page номер страницы
     * @param size количество элементов на странице
     * @param model модель для передачи данных в представление
     * @return шаблон списка с отфильтрованными партнерами
     */
    @GetMapping("/filter")
    public String filterByType(@RequestParam(required = false) PartnerType type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String hasLogo,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_FIELD));
        Page<Partner> partnersPage = filterPartners(type, status, hasLogo, pageable);

        String title;
        if (type != null) {
            title = "Партнеры: " + type.getDisplayName();
        } else if (status != null) {
            title = "Партнеры: " + ("ACTIVE".equals(status) ? "Активные" : "Неактивные");
        } else if (hasLogo != null) {
            title = "Партнеры: " + ("true".equals(hasLogo) ? "С логотипом" : "Без логотипа");
        } else {
            title = "Все партнеры";
        }

        model.addAttribute("partnersPage", partnersPage);
        model.addAttribute("title", title);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedHasLogo", hasLogo);

        return "admin/partners/list";
    }

    // ================== СОЗДАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму создания нового партнера.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон формы создания партнера
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("partner", new Partner());
        model.addAttribute("title", "Создание партнера");
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("allProjects", projectService.findAllActive());
        return "admin/partners/create";
    }

    /**
     * Обрабатывает создание нового партнера.
     *
     * @param partner создаваемый партнер
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров или форму с ошибками
     */
    @PostMapping("/create")
    public String createPartner(@Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Возвращаем на форму с ошибками
            return "admin/partners/create";
        }

        try {
            // Получаем файл логотипа из запроса
            if (request instanceof MultipartHttpServletRequest multipartRequest) {
                MultipartFile logoFile = multipartRequest.getFile("logoFile");

                if (logoFile != null && !logoFile.isEmpty()) {
                    partner.setLogoFile(logoFile);
                }
            }

            Partner savedPartner = partnerService.save(partner);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + savedPartner.getName() + "' успешно создан!");
            return "redirect:/admin/partners";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании партнера: " + e.getMessage());
            return "redirect:/admin/partners/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму редактирования партнера.
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы редактирования или редирект с ошибкой
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        model.addAttribute("partner", partner);
        model.addAttribute("title", "Редактирование: " + partner.getName());
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("allProjects", projectService.findAllActive());
        model.addAttribute("partnerProjects", partner.getProjects());

        return "admin/partners/edit";
    }

    /**
     * Обрабатывает обновление партнера.
     *
     * @param id идентификатор партнера
     * @param partner обновленные данные партнера
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updatePartner(@PathVariable Long id,
                                @Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            partner.setId(id); // Сохраняем ID для формы
            return "admin/partners/edit";
        }

        try {
            // Проверяем существование партнера
            Optional<Partner> existingPartnerOpt = partnerService.findById(id);
            if (existingPartnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/partners";
            }

            // Сохраняем существующие связи с проектами
            Partner existingPartner = existingPartnerOpt.get();
            partner.setProjects(existingPartner.getProjects());
            partner.setId(id); // Убедимся, что ID сохраняется

            // Получаем файл логотипа из запроса
            if (request instanceof MultipartHttpServletRequest multipartRequest) {
                MultipartFile logoFile = multipartRequest.getFile("logoFile");

                if (logoFile != null && !logoFile.isEmpty()) {
                    partner.setLogoFile(logoFile);
                }
            }

            Partner updatedPartner = partnerService.update(partner);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + updatedPartner.getName() + "' успешно обновлен!");
            return "redirect:/admin/partners";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении партнера: " + e.getMessage());
            return "redirect:/admin/partners/edit/" + id;
        }
    }

    // ================== УПРАВЛЕНИЕ АКТИВНОСТЬЮ ==================

    /**
     * Активирует партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
     */
    @PostMapping("/activate/{id}")
    public String activatePartner(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Partner activatedPartner = partnerService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + activatedPartner.getName() + "' активирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    /**
     * Деактивирует партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
     */
    @PostMapping("/deactivate/{id}")
    public String deactivatePartner(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Partner deactivatedPartner = partnerService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + deactivatedPartner.getName() + "' деактивирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    // ================== УДАЛЕНИЕ ПАРТНЕРА ==================

    /**
     * Отображает страницу подтверждения удаления партнера.
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон подтверждения удаления или редирект с ошибкой
     */
    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        model.addAttribute("partner", partner);
        model.addAttribute("title", "Удаление партнера: " + partner.getName());

        // Проверяем участие в проектах
        int projectCount = partner.getProjectsCount();
        model.addAttribute("projectCount", projectCount);

        return "admin/partners/delete";
    }

    /**
     * Обрабатывает удаление партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
     */
    @PostMapping("/delete/{id}")
    public String deletePartner(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(id);
            if (partnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/partners";
            }

            Partner partner = partnerOpt.get();
            String partnerName = partner.getName();

            // Удаляем связи с проектами перед удалением
            partner.getProjects().clear();
            partnerService.update(partner);

            // Удаляем партнера
            partnerService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + partnerName + "' успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    // ================== УПРАВЛЕНИЕ ПРОЕКТАМИ ==================

    /**
     * Отображает форму управления проектами партнера.
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон управления проектами или редирект с ошибкой
     */
    @GetMapping("/projects/{id}")
    public String manageProjects(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        List<Project> allProjects = projectService.findAllActive();
        List<Project> partnerProjects = List.copyOf(partner.getProjects());
        List<Project> availableProjects = allProjects.stream()
                .filter(project -> !partnerProjects.contains(project))
                .toList();

        model.addAttribute("partner", partner);
        model.addAttribute("title", "Управление проектами: " + partner.getName());
        model.addAttribute("partnerProjects", partnerProjects);
        model.addAttribute("availableProjects", availableProjects);

        return "admin/partners/projects";
    }

    /**
     * Добавляет проект к партнеру.
     *
     * @param partnerId идентификатор партнера
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{partnerId}/add")
    public String addProjectToPartner(@PathVariable Long partnerId,
                                      @RequestParam Long projectId,
                                      RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(partnerId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (partnerOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер или проект не найден");
                return "redirect:/admin/partners/projects/" + partnerId;
            }

            Partner partner = partnerOpt.get();
            Project project = projectOpt.get();

            partnerService.addProjectToPartner(partner, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' добавлен к партнеру '" +
                            partner.getName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении проекта: " + e.getMessage());
        }

        return "redirect:/admin/partners/projects/" + partnerId;
    }

    /**
     * Удаляет проект у партнера.
     *
     * @param partnerId идентификатор партнера
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{partnerId}/remove")
    public String removeProjectFromPartner(@PathVariable Long partnerId,
                                           @RequestParam Long projectId,
                                           RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(partnerId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (partnerOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер или проект не найден");
                return "redirect:/admin/partners/projects/" + partnerId;
            }

            Partner partner = partnerOpt.get();
            Project project = projectOpt.get();

            partnerService.removeProjectFromPartner(partner, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' удален у партнера '" +
                            partner.getName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении проекта: " + e.getMessage());
        }

        return "redirect:/admin/partners/projects/" + partnerId;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Метод для отладки - показывает все параметры запроса.
     */
    @ModelAttribute
    public void addAttributes(@RequestParam java.util.Map<String, String> allParams, Model model) {
        // Для отладки можно логировать параметры
        // System.out.println("Параметры запроса:+ allParams);
    }
}