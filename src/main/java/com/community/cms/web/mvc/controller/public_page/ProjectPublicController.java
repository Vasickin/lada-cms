package com.community.cms.web.mvc.controller.public_page;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.web.mvc.dto.content.ProjectDTO;
import com.community.cms.web.mvc.mapper.content.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Публичный контроллер для работы с проектами.
 *
 * <p>Обеспечивает отображение списка проектов с фильтрацией,
 * пагинацией и сортировкой, а также детальных страниц проектов.
 * Вся фильтрация выполняется на уровне базы данных через JPA Specifications.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/projects")
public class ProjectPublicController {

    private static final Logger log = LoggerFactory.getLogger(ProjectPublicController.class);

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectPublicController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    // ================== СПИСОК ПРОЕКТОВ ==================

    /**
     * Отображает страницу со списком проектов с применением фильтров.
     *
     * <p>Поддерживаемые фильтры:
     * <ul>
     *   <li>status - статус проекта (UPCOMING, ACTIVE, COMPLETED, ANNUAL, ARCHIVED)</li>
     *   <li>category - категория проекта</li>
     *   <li>year - год события</li>
     *   <li>date - конкретная дата события (формат dd.MM.yyyy)</li>
     *   <li>search - поиск по названию и описанию</li>
     * </ul>
     * </p>
     *
     * @param status фильтр по статусу
     * @param category фильтр по категории
     * @param year фильтр по году события
     * @param date фильтр по дате события
     * @param search поисковый запрос
     * @param pageable параметры пагинации
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона public/projects/list
     */
    @GetMapping
    public String listProjects(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 12) Pageable pageable,
            Model model) {

        log.info("Запрос списка проектов с фильтрами: status={}, category={}, year={}, date={}, search={}, page={}",
                status, category, year, date, search, pageable.getPageNumber());

        // ===== ПРЕОБРАЗОВАНИЕ СТАТУСА В ENUM =====
        ProjectStatusType statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = ProjectStatusType.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Некорректный статус: {}, игнорируем", status);
            }
        }

        // ===== ПАГИНАЦИЯ БЕЗ СОРТИРОВКИ (сортировка будет в SQL-запросах) =====
        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        // ===== ПОЛУЧЕНИЕ ОТФИЛЬТРОВАННЫХ ПРОЕКТОВ ЧЕРЕЗ БД =====
        Page<Project> projectsPage = projectService.findFilteredProjects(
                statusEnum, category, year, date, search, customPageable
        );

        // ===== ПРЕОБРАЗОВАНИЕ В DTO =====
        Page<ProjectDTO> dtoPage = projectsPage.map(projectMapper::toPublicCardDTO);

        // ===== ДАННЫЕ ДЛЯ ФОРМЫ ФИЛЬТРАЦИИ =====
        // Статусы для выпадающего списка
        model.addAttribute("statuses", ProjectStatusType.values());

        // Категории для выпадающего списка
        List<String> categories = projectService.findAllDistinctCategories();
        model.addAttribute("categories", categories);

        // Годы для выпадающего списка (все года из БД)
        List<Integer> years = projectService.findAllDistinctEventYears();
        model.addAttribute("years", years);

        // ===== ДАННЫЕ ДЛЯ ПАГИНАЦИИ =====
        model.addAttribute("projects", dtoPage.getContent());
        model.addAttribute("currentPage", dtoPage.getNumber());
        model.addAttribute("totalPages", dtoPage.getTotalPages());
        model.addAttribute("pageSize", dtoPage.getSize());
        model.addAttribute("totalItems", dtoPage.getTotalElements());

        // ===== СОХРАНЕНИЕ ВЫБРАННЫХ ФИЛЬТРОВ =====
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedDate", date != null ? date.toString() : null);
        model.addAttribute("selectedSearch", search);

        // ===== КАРУСЕЛЬ СОБЫТИЙ (до 5 проектов) =====
        List<Project> carouselProjects = projectService.findProjectsForCarousel(5);
        List<ProjectDTO> carouselDTOs = projectMapper.toPublicCarouselDTOList(carouselProjects);
        model.addAttribute("carouselProjects", carouselDTOs);

        // ===== СТАТИСТИКА =====
        model.addAttribute("totalProjectsCount", projectService.countAll());
        model.addAttribute("upcomingProjectsCount", projectService.countUpcoming());
        model.addAttribute("activeProjectsCount", projectService.countActive());
        model.addAttribute("completedProjectsCount", projectService.countCompleted());
        model.addAttribute("annualProjectsCount", projectService.countAnnual());
        model.addAttribute("archivedProjectsCount", projectService.countArchived());

        // ===== СТАТИСТИКА ПО КАТЕГОРИЯМ =====
        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (String cat : categories) {
            long count = projectService.countByCategory(cat);
            categoryStats.put(cat, count);
        }
        model.addAttribute("categoryStats", categoryStats);

        log.info("Найдено проектов: {} (страница {} из {})",
                dtoPage.getTotalElements(),
                dtoPage.getNumber() + 1,
                dtoPage.getTotalPages());

        return "public/projects/list";
    }

    // ================== ДЕТАЛЬНАЯ СТРАНИЦА ПРОЕКТА ==================

    /**
     * Отображает детальную страницу проекта по его slug.
     *
     * @param slug уникальный идентификатор проекта
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона public/projects/detail
     */
    @GetMapping("/{slug}")
    public String projectDetail(@PathVariable String slug, Model model) {
        log.info("Запрос детальной страницы проекта: slug={}", slug);

        // Поиск проекта по slug
        Optional<Project> projectOpt = projectService.findBySlug(slug);

        if (projectOpt.isEmpty()) {
            log.warn("Проект не найден: slug={}", slug);
            return "redirect:/projects";
        }

        Project project = projectOpt.get();

        // Преобразование в полный DTO с фото, командой, партнёрами
        ProjectDTO projectDTO = projectMapper.toPublicDetailDTO(project);
        model.addAttribute("project", projectDTO);

        // Похожие проекты (по категории, исключая текущий)
        List<Project> similarProjects = findSimilarProjects(project);
        List<ProjectDTO> similarProjectDTOs = projectMapper.toPublicCardDTOList(similarProjects);
        model.addAttribute("similarProjects", similarProjectDTOs);

        log.info("Детальная страница проекта: id={}, title={}, похожих проектов: {}",
                project.getId(), project.getTitle(), similarProjects.size());

        return "public/projects/detail";
    }

    /**
     * Вспомогательный метод для поиска похожих проектов.
     * Временно реализован через фильтрацию в памяти.
     * Будет заменён на запрос к БД на Этапе 7.
     *
     * @param currentProject текущий проект
     * @return список похожих проектов (до 3)
     */
    private List<Project> findSimilarProjects(Project currentProject) {
        String category = currentProject.getCategory();
        Long excludeId = currentProject.getId();

        // Используем метод, который НЕ исключает архивные проекты
        return projectService.findSimilarProjectsAllStatuses(category, excludeId, 30);
    }
}
