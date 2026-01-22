package com.community.cms.web.mvc.controller.public_page;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.enums.PageType;
import com.community.cms.domain.service.page.CustomPageService;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.people.TeamMemberService;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import com.community.cms.web.mvc.dto.people.TeamMemberDTO;
import com.community.cms.web.mvc.mapper.people.TeamMemberMapper;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.Project.ProjectStatus;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.content.PhotoGalleryService;
import com.community.cms.domain.service.people.PartnerService;
import com.community.cms.web.mvc.dto.content.ProjectDTO;
import com.community.cms.web.mvc.mapper.content.ProjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для главной страницы и публичных разделов сайта.
 */
@Controller
public class HomeController {

    private final CustomPageService pageService;
    private final TeamMemberService teamMemberService;
    private final TeamMemberMapper teamMemberMapper;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final PhotoGalleryService photoGalleryService;

    /**
     * Конструктор с внедрением зависимостей.
     */
    @Autowired
    public HomeController(CustomPageService pageService,
                          TeamMemberService teamMemberService,
                          TeamMemberMapper teamMemberMapper,
                          ProjectService projectService,
                          ProjectMapper projectMapper,
                          PhotoGalleryService photoGalleryService,
                          PartnerService partnerService) {
        this.pageService = pageService;
        this.teamMemberService = teamMemberService;
        this.teamMemberMapper = teamMemberMapper;
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.photoGalleryService = photoGalleryService;
    }

    // ================== СУЩЕСТВУЮЩИЕ МЕТОДЫ (БЕЗ ИЗМЕНЕНИЙ) ==================

    @GetMapping("/")
    public String home(Model model) {
        List<CustomPage> publishedPages = pageService.findAllPublishedPages();
        model.addAttribute("publishedPages", publishedPages);
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        Optional<CustomPage> aboutPage = pageService.findPublishedPageByType(PageType.ABOUT);
        model.addAttribute("hasContent", aboutPage.isPresent());
        aboutPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (aboutPage.isEmpty()) {
            model.addAttribute("pageTitle", "О нас");
            model.addAttribute("metaDescription", "Информация о нашей организации");
        }
        return "about";
    }

    // ================== НОВАЯ СТРАНИЦА "НАШИ ПРОЕКТЫ" ==================

    /**
     * Новая красивая страница "Наши проекты" с фильтрацией.
     * URL: /projects
     */
    @GetMapping("/projects")
    public String showProjectsList(Model model,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   @RequestParam(required = false) String date) {

        try {
            // ================== ПОДГОТОВКА ДАННЫХ ДЛЯ ФИЛЬТРОВ ==================

            // Все категории для фильтра
            List<String> allCategories = projectService.findAllDistinctCategories();
            model.addAttribute("categories", allCategories);

            // Все статусы для фильтра
            List<ProjectStatus> allStatuses = Arrays.asList(ProjectStatus.values());
            model.addAttribute("statuses", allStatuses);

            // Все годы для фильтра (из событий проектов)
            List<Integer> allYears = projectService.findAll().stream()
                    .filter(p -> p.getEventDate() != null)
                    .map(p -> p.getEventDate().getYear())
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            model.addAttribute("years", allYears);

            // ================== КАРУСЕЛЬ СОБЫТИЙ ==================

            // Получаем все активные проекты для карусели
            List<Project> activeProjects = projectService.findAllActive();

            // Перемешиваем в случайном порядке
            List<Project> shuffledProjects = new ArrayList<>(activeProjects);
            Collections.shuffle(shuffledProjects);

            // Берем максимум 10 проектов для карусели
            List<Project> carouselProjects = shuffledProjects.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            // Преобразуем в DTO для карусели
            List<ProjectDTO> carouselDTOs = projectMapper.toPublicCarouselDTOList(carouselProjects);
            model.addAttribute("carouselProjects", carouselDTOs);

            // ================== ФИЛЬТРАЦИЯ И ПОИСК ПРОЕКТОВ ==================

            List<Project> filteredProjects = new ArrayList<>();

            // Базовый список всех проектов (для фильтрации в памяти как в админке)
            List<Project> allProjects = projectService.findAll();

            // Применяем фильтры последовательно
            for (Project project : allProjects) {
                boolean include = true;

                // Фильтр по статусу
                if (status != null && !status.isEmpty()) {
                    try {
                        ProjectStatus filterStatus = ProjectStatus.valueOf(status.toUpperCase());
                        if (project.getStatus() != filterStatus) {
                            include = false;
                        }
                    } catch (IllegalArgumentException e) {
                        // Неверный статус - игнорируем фильтр
                    }
                }

                // Фильтр по категории
                if (include && category != null && !category.isEmpty() && !category.equals("Все категории")) {
                    if (!category.equals(project.getCategory())) {
                        include = false;
                    }
                }

                // Фильтр по году
                if (include && year != null) {
                    if (project.getEventDate() == null || project.getEventDate().getYear() != year) {
                        include = false;
                    }
                }

                // Фильтр по конкретной дате
                if (include && date != null && !date.trim().isEmpty()) {
                    try {
                        // Парсим дату формата "dd.MM.yyyy"
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        LocalDate filterDate = LocalDate.parse(date, formatter);

                        if (project.getEventDate() == null || !project.getEventDate().equals(filterDate)) {
                            include = false;
                        }
                    } catch (Exception e) {
                        // Неверный формат даты - игнорируем фильтр
                        System.err.println("Неверный формат даты: " + date + ", ошибка: " + e.getMessage());
                    }
                }

                // Поиск по названию и описанию
                if (include && search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase().trim();
                    boolean foundInTitle = project.getTitle() != null &&
                            project.getTitle().toLowerCase().contains(searchLower);
                    boolean foundInDescription = project.getShortDescription() != null &&
                            project.getShortDescription().toLowerCase().contains(searchLower);
                    boolean foundInFullDescription = project.getFullDescription() != null &&
                            project.getFullDescription().toLowerCase().contains(searchLower);

                    if (!foundInTitle && !foundInDescription && !foundInFullDescription) {
                        include = false;
                    }
                }

                if (include) {
                    filteredProjects.add(project);
                }
            }

            // Сортируем по дате создания (новые сначала)
            filteredProjects.sort(Comparator
                    .comparing(Project::getEventDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Project::getCreatedAt, Comparator.reverseOrder()));

            // ================== ПАГИНАЦИЯ ==================

            int totalItems = filteredProjects.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);

            // Корректируем номер страницы
            if (page < 0) page = 0;
            if (page >= totalPages && totalPages > 0) page = totalPages - 1;

            int start = page * size;
            int end = Math.min(start + size, totalItems);

            List<Project> pageContent;
            if (start >= totalItems || filteredProjects.isEmpty()) {
                pageContent = Collections.emptyList();
            } else {
                pageContent = filteredProjects.subList(start, end);
            }

            // Преобразуем в DTO для отображения
            List<ProjectDTO> projectDTOs = projectMapper.toPublicCardDTOList(pageContent);

//            for (int i = 0; i < projectDTOs.size(); i++) {
//                ProjectDTO projectDTO = projectDTOs.get(i);
//                if (i < pageContent.size()) {
//                    Project project = pageContent.get(i);
//                    List<PhotoGalleryDTO> keyPhotos = projectMapper.loadKeyPhotosForProject(project);
//                    projectDTO.setKeyPhotos(keyPhotos);
//                }
//            }
//
//            // ============ ЗАГРУЗКА КЛЮЧЕВЫХ ФОТО ДЛЯ КАЖДОГО ПРОЕКТА ============
//            for (ProjectDTO projectDTO : projectDTOs) {
//                // Получаем проект по ID для загрузки фото
//                Optional<Project> projectOpt = projectService.findById(projectDTO.getId());
//                if (projectOpt.isPresent()) {
//                    List<PhotoGalleryDTO> keyPhotos = projectMapper.loadKeyPhotosForProject(projectOpt.get());
//                    projectDTO.setKeyPhotos(keyPhotos);
//                }
//            }

            // ================== ДОБАВЛЕНИЕ ДАННЫХ В МОДЕЛЬ ==================

            // Основные данные
            model.addAttribute("projects", projectDTOs);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            // Параметры фильтров (для сохранения состояния формы)
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedYear", year);
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedSearch", search);

            // ================== СТАТИСТИКА ПРОЕКТОВ ==================

            long totalProjectsCount = projectService.countAll();
            long activeProjectsCount = projectService.countActive(); // Уже есть метод
            long annualProjectsCount = projectService.countAnnual(); // Новый метод
            long archivedProjectsCount = projectService.countArchived(); // Новый метод

            model.addAttribute("totalProjectsCount", totalProjectsCount);
            model.addAttribute("activeProjectsCount", activeProjectsCount);
            model.addAttribute("annualProjectsCount", annualProjectsCount);
            model.addAttribute("archivedProjectsCount", archivedProjectsCount);

            // SEO мета-данные
            model.addAttribute("pageTitle", "Наши проекты");
            model.addAttribute("metaDescription", "Список всех проектов организации 'ЛАДА'. Фильтруйте по категориям, статусу и году.");
            model.addAttribute("hasContent", !projectDTOs.isEmpty());

            return "public/projects/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке списка проектов: " + e.getMessage());
            e.printStackTrace();

            // Возвращаем пустые данные при ошибке
            model.addAttribute("projects", new ArrayList<ProjectDTO>());
            model.addAttribute("carouselProjects", new ArrayList<ProjectDTO>());
            model.addAttribute("categories", new ArrayList<String>());
            model.addAttribute("statuses", Arrays.asList(ProjectStatus.values()));
            model.addAttribute("years", new ArrayList<Integer>());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            model.addAttribute("pageTitle", "Наши проекты");
            model.addAttribute("metaDescription", "Список проектов организации 'ЛАДА'");

            return "public/projects/list";
        }
    }

    // ================== ДЕТАЛЬНАЯ СТРАНИЦА ПРОЕКТА ==================

    /**
     * Детальная страница проекта.
     * URL: /projects/{slug}
     */
    @GetMapping("/projects/{slug}")
    public String showProjectDetail(@PathVariable String slug, Model model) {
        try {
            Optional<Project> projectOpt = projectService.findBySlug(slug);

            if (projectOpt.isEmpty()) {
                model.addAttribute("errorTitle", "Проект не найден");
                model.addAttribute("errorMessage", "Запрошенный проект не существует или недоступен.");
                return "error/404";
            }

            Project project = projectOpt.get();

            // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД для детальной страницы
            ProjectDTO projectDTO = projectMapper.toPublicDetailDTO(project);

            // ЗАГРУЗКА ПОХОЖИХ ПРОЕКТОВ
            List<Project> similarProjects = projectService.findSimilarProjectsAllStatuses(
                    project.getCategory(),
                    project.getId(),
                    30
            );
            List<ProjectDTO> similarProjectDTOs = projectMapper.toPublicCardDTOList(similarProjects);

            // ЗАГРУЖАЕМ КЛЮЧЕВЫЕ ФОТО ДЛЯ ПОХОЖИХ ПРОЕКТОВ
//            for (ProjectDTO similarDTO : similarProjectDTOs) {
//                Optional<Project> similarProjectOpt = projectService.findById(similarDTO.getId());
//                if (similarProjectOpt.isPresent()) {
//                    List<PhotoGalleryDTO> keyPhotos = projectMapper.loadKeyPhotosForProject(similarProjectOpt.get());
//                    similarDTO.setKeyPhotos(keyPhotos);
//                }
//            }

            // ДОБАВЛЯЕМ В МОДЕЛЬ
            model.addAttribute("project", projectDTO);
            model.addAttribute("similarProjects", similarProjectDTOs);

            // SEO мета-данные
            model.addAttribute("pageTitle", projectDTO.getEffectiveMetaTitle());
            model.addAttribute("metaDescription", projectDTO.getEffectiveMetaDescription());
            model.addAttribute("metaKeywords", projectDTO.getMetaKeywords());
            model.addAttribute("image", projectDTO.getEffectiveOgImagePath());

            return "public/projects/detail";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке детальной страницы проекта: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorTitle", "Ошибка загрузки");
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке страницы проекта.");
            return "error/500";
        }
    }

    // ================== API ДЛЯ КАЛЕНДАРЯ ==================

    /**
     * API для получения событий календаря по месяцу.
     * URL: /api/calendar/events?year=2024&month=5
     * month: 1-12
     */
    @GetMapping("/api/calendar/events")
    @ResponseBody
    public ResponseEntity<?> getCalendarEvents(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String status) {

        try {
            // Рассчитываем диапазон дат для месяца
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            // Получаем проекты в этом диапазоне через сервис
            List<Project> projects = projectService.findByEventDateBetween(startDate, endDate);

            // Фильтруем по статусу если указан
            if (status != null && !status.isEmpty()) {
                try {
                    ProjectStatus filterStatus = ProjectStatus.valueOf(status.toUpperCase());
                    projects = projects.stream()
                            .filter(p -> p.getStatus() == filterStatus)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Неверный статус - игнорируем фильтр
                }
            }

            // Преобразуем в DTO для календаря (упрощенный DTO)
            List<Map<String, Object>> calendarEvents = new ArrayList<>();
            for (Project project : projects) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", project.getId());
                event.put("title", project.getTitle());
                event.put("eventDate", project.getEventDate().toString()); // ISO формат: "2024-05-15"
                event.put("category", project.getCategory());
                event.put("status", project.getStatus().name());
                event.put("slug", project.getSlug());
                event.put("detailUrl", "/projects/" + project.getSlug());

                // Определяем тип события: past, current, future
                LocalDate today = LocalDate.now();
                if (project.getEventDate().isBefore(today)) {
                    event.put("type", "past");
                } else if (project.getEventDate().isEqual(today)) {
                    event.put("type", "current");
                } else {
                    event.put("type", "future");
                }

                calendarEvents.add(event);
            }

            return ResponseEntity.ok(calendarEvents);

        } catch (Exception e) {
            System.err.println("Ошибка при получении событий календаря: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении событий календаря"));
        }
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        // Перенаправляем на новую фото-галерею
        return "redirect:/photo-gallery";
    }

    // В класс HomeController ДОБАВЛЯЕМ эти методы:

// ================== ФОТО-ГАЛЕРЕЯ ==================

    /**
     * Новая красивая страница "Фото-галерея" с фильтрацией по году и категориям.
     * URL: /photo-gallery
     */
    @GetMapping("/photo-gallery")
    public String showPhotoGallery(Model model,
                                   @RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size) {

        try {
            List<PhotoGallery> galleries;

            // Фильтрация по году если указан
            if (year != null) {
                galleries = photoGalleryService.getPublishedPhotoGalleryItemsByYear(year);
            } else {
                galleries = photoGalleryService.getPublishedPhotoGalleryItems();
            }

            // Фильтрация по категории если указана
            List<PhotoGalleryDTO> galleryDTOs = galleries.stream()
                    .map(gallery -> {
                        PhotoGalleryDTO dto = new PhotoGalleryDTO(
                                gallery.getId(),
                                gallery.getTitle(),
                                gallery.getYear(),
                                gallery.getDescription(),
                                gallery.getImagesCount(),
                                gallery.getPublished()
                        );

                        // ДОБАВЛЯЕМ изображения
                        if (gallery.getImages() != null && !gallery.getImages().isEmpty()) {
                            List<PhotoGalleryDTO> imageDTOs = gallery.getImages().stream()
                                    .map(photo -> {
                                        // Создаем DTO
                                        PhotoGalleryDTO photoDTO = new PhotoGalleryDTO(
                                                photo.getId(),
                                                photo.getFileName(),
                                                photo.getWebPath(),   // webPath для админки
                                                photo.getWebPath(),   // thumbnailPath для админки
                                                photo.getFileName(),
                                                gallery.getId(),
                                                gallery.getTitle(),
                                                gallery.getYear(),
                                                photo.getIsPrimary()
                                        );

                                        // ДОБАВЛЯЕМ публичный путь
                                        String publicPath = photo.getWebPath();
                                        if (publicPath.startsWith("/admin/photo-gallery/image/")) {
                                            String filename = publicPath.substring("/admin/photo-gallery/image/".length());
                                            photoDTO.setPublicWebPath("/uploads/" + filename);
                                        } else {
                                            photoDTO.setPublicWebPath(publicPath);
                                        }

                                        return photoDTO;
                                    })
                                    .collect(Collectors.toList());
                            dto.setImages(imageDTOs);
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            int totalPhotos = galleries.stream()
                    .mapToInt(PhotoGallery::getImagesCount)
                    .sum();

            // ПАГИНАЦИЯ
            int totalItems = galleryDTOs.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);


            // Корректируем номер страницы
            if (page < 0) page = 0;
            if (page >= totalPages && totalPages > 0) page = totalPages - 1;

            int start = page * size;
            int end = Math.min(start + size, totalItems);

            List<PhotoGalleryDTO> pageContent;
            if (start >= totalItems || galleryDTOs.isEmpty()) {
                pageContent = Collections.emptyList();
            } else {
                pageContent = galleryDTOs.subList(start, end);
            }

            // Получаем доступные года для фильтра
            List<Integer> availableYears = photoGalleryService.getAvailableYears();

            // Получаем все категории из галерей
            List<String> allCategories = galleries.stream()
                    .flatMap(gallery -> gallery.getCategoryNames().stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Добавляем данные в модель
            model.addAttribute("galleries", pageContent);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalPhotos", totalPhotos);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            model.addAttribute("availableYears", availableYears);
            model.addAttribute("allCategories", allCategories);
            model.addAttribute("selectedYear", year);
            model.addAttribute("selectedCategory", category);

            // SEO мета-данные
            model.addAttribute("pageTitle", "Фото-галерея");
            model.addAttribute("metaDescription",
                    "Фотогалерея организации 'ЛАДА'. Все фотографии с мероприятий отсортированы по годам и категориям.");
            model.addAttribute("hasContent", !pageContent.isEmpty());

            return "public/photo-gallery/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке фото-галереи: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("galleries", new ArrayList<PhotoGalleryDTO>());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            model.addAttribute("pageTitle", "Фото-галерея");
            model.addAttribute("metaDescription", "Фотогалерея организации 'ЛАДА'");

            return "public/photo-gallery/list";
        }
    }

    /**
     * Детальная страница фото-галереи.
     * URL: /photo-gallery/{id}
     */
    @GetMapping("/photo-gallery/{id}")
    public String showGalleryDetail(@PathVariable Long id, Model model) {
        try {
            PhotoGallery gallery = photoGalleryService.getPhotoGalleryItemById(id);

            // Проверяем что галерея опубликована
            if (gallery.getPublished() == null || !gallery.getPublished()) {
                model.addAttribute("errorTitle", "Галерея не найдена");
                model.addAttribute("errorMessage", "Запрошенная галерея не существует или недоступна.");
                return "error/404";
            }

            // Создаем DTO галереи
            PhotoGalleryDTO galleryDTO = new PhotoGalleryDTO(
                    gallery.getId(),
                    gallery.getTitle(),
                    gallery.getYear(),
                    gallery.getDescription(),
                    gallery.getImagesCount(),
                    gallery.getPublished()
            );

            // Добавляем изображения в DTO
            if (gallery.getImages() != null && !gallery.getImages().isEmpty()) {
                List<PhotoGalleryDTO> imageDTOs = gallery.getImages().stream()
                        .map(photo -> {
                            // Создаем DTO
                            PhotoGalleryDTO photoDTO = new PhotoGalleryDTO(
                                    photo.getId(),
                                    photo.getFileName(),
                                    photo.getWebPath(),
                                    photo.getWebPath(),
                                    photo.getFileName(),
                                    gallery.getId(),
                                    gallery.getTitle(),
                                    gallery.getYear(),
                                    photo.getIsPrimary()
                            );

                            // ДОБАВЛЯЕМ публичный путь
                            String publicPath = photo.getWebPath();
                            if (publicPath != null && publicPath.startsWith("/admin/photo-gallery/image/")) {
                                String filename = publicPath.substring("/admin/photo-gallery/image/".length());
                                photoDTO.setPublicWebPath("/uploads/" + filename);
                            } else {
                                photoDTO.setPublicWebPath(publicPath);
                            }

                            return photoDTO;
                        })
                        .collect(Collectors.toList());
                galleryDTO.setImages(imageDTOs);
            }

            // SEO мета-данные
            model.addAttribute("gallery", galleryDTO);
            model.addAttribute("pageTitle", gallery.getTitle() + " (" + gallery.getYear() + ")");
            model.addAttribute("metaDescription",
                    gallery.getDescription() != null && !gallery.getDescription().isEmpty() ?
                            gallery.getDescription() :
                            "Фото-галерея '" + gallery.getTitle() + "' за " + gallery.getYear() + " год."
            );
            model.addAttribute("hasContent", true);

            return "public/photo-gallery/detail";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке детальной страницы галереи: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorTitle", "Ошибка загрузки");
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке страницы галереи.");
            return "error/500";
        }
    }


    @GetMapping("/patrons")
    public String patrons(Model model) {
        Optional<CustomPage> patronsPage = pageService.findPublishedPageByType(PageType.PATRONS);
        model.addAttribute("hasContent", patronsPage.isPresent());
        patronsPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (patronsPage.isEmpty()) {
            model.addAttribute("pageTitle", "Меценатам");
            model.addAttribute("metaDescription", "Информация для меценатов и партнеров");
        }
        return "patrons";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        Optional<CustomPage> contactPage = pageService.findPublishedPageByType(PageType.CONTACT);
        model.addAttribute("hasContent", contactPage.isPresent());
        contactPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (contactPage.isEmpty()) {
            model.addAttribute("pageTitle", "Контакты");
            model.addAttribute("metaDescription", "Контактная информация организации");
        }
        return "contact";
    }

    @GetMapping("/pages/{slug}")
    public String showPublicPage(@PathVariable String slug, Model model) {
        Optional<CustomPage> page = pageService.findPageBySlugAndPublished(slug, true);
        if (page.isPresent()) {
            CustomPage foundPage = page.get();
            model.addAttribute("page", foundPage);
            model.addAttribute("pageTitle", foundPage.getTitle());
            model.addAttribute("metaDescription", foundPage.getMetaDescription());
            model.addAttribute("hasContent", true);
            return "pages/view";
        } else {
            return "error/404";
        }
    }

    @GetMapping("/test-fragments")
    public String testFragments() {
        return "test-fragments";
    }

    @GetMapping("/sitemap")
    public String sitemap(Model model) {
        List<CustomPage> sitePages = pageService.findPublishedSitePages();
        model.addAttribute("sitePages", sitePages);
        return "sitemap";
    }



//    @Nonnull
//    private static List<PhotoGalleryDTO> getPhotoGalleryDTOS(ProjectDTO projectDTO) {
//        List<PhotoGalleryDTO> keyPhotos = new ArrayList<>();
//
//        // Используем PhotoGalleryService для получения фото (как в ProjectAdminController)
//        // TODO: Реализовать получение PhotoGalleryDTO через существующий сервис
//        for (Long photoId : projectDTO.getKeyPhotoIds()) {
//            PhotoGalleryDTO photoDTO = new PhotoGalleryDTO();
//            photoDTO.setPhotoId(photoId);
//            photoDTO.setFileName("photo-" + photoId + ".jpg");
//            photoDTO.setWebPath("/images/projects/" + photoId + ".jpg");
//            photoDTO.setThumbnailPath("/images/projects/thumbnails/" + photoId + ".jpg");
//            keyPhotos.add(photoDTO);
//        }
//        return keyPhotos;
//    }

    // ================== СУЩЕСТВУЮЩИЕ МЕТОДЫ ДЛЯ КОМАНДЫ (БЕЗ ИЗМЕНЕНИЙ) ==================

    @GetMapping("/team")
    public String showTeamPage(Model model) {
        try {
            List<TeamMember> activeMembers = teamMemberService.findAllActiveOrderBySortOrder();
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(activeMembers);

            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Наша команда");
            model.addAttribute("metaDescription", "Команда организации 'ЛАДА' - талантливые специалисты, художники и организаторы");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке страницы команды: " + e.getMessage());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            return "error/500";
        }
    }

    @GetMapping("/team/{id}")
    public String showTeamMember(@PathVariable Long id, Model model) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

            if (teamMemberOpt.isEmpty()) {
                model.addAttribute("errorTitle", "Член команды не найден");
                model.addAttribute("errorMessage", "Запрошенный участник команды не существует.");
                return "error/404";
            }

            TeamMember teamMember = teamMemberOpt.get();
            if (!teamMember.isActive()) {
                model.addAttribute("errorTitle", "Член команды не доступен");
                model.addAttribute("errorMessage", "Данный участник команды временно не отображается на сайте.");
                return "error/404";
            }

            TeamMemberDTO memberDTO = teamMemberMapper.toDTO(teamMember);

            model.addAttribute("member", memberDTO);
            model.addAttribute("pageTitle", memberDTO.getFullName() + " - " + memberDTO.getPosition());
            model.addAttribute("metaDescription",
                    memberDTO.getPosition() + " организации 'ЛАДА'. " +
                            (memberDTO.getBio() != null && memberDTO.getBio().length() > 150 ?
                                    memberDTO.getBio().substring(0, 150) + "..." :
                                    "Член команды организации 'ЛАДА'."));
            model.addAttribute("hasContent", true);

            return "public/team/detail";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке страницы члена команды: " + e.getMessage());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            return "error/500";
        }
    }

    @GetMapping("/team/sorted-by-name")
    public String showTeamSortedByName(Model model) {
        try {
            List<TeamMember> activeMembers = teamMemberService.findAllActiveOrderByName();
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(activeMembers);

            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Наша команда (по алфавиту)");
            model.addAttribute("metaDescription", "Команда организации 'ЛАДА' в алфавитном порядке");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("sortedByName", true);
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке команды по имени: " + e.getMessage());
            return "redirect:/team";
        }
    }

    @GetMapping("/team/key-members")
    public String showKeyTeamMembers(Model model) {
        try {
            List<TeamMember> keyMembers = teamMemberService.findKeyTeamMembers(10);
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(keyMembers);

            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Ключевая команда");
            model.addAttribute("metaDescription", "Руководство и ключевые специалисты организации 'ЛАДА'");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("keyMembers", true);
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке ключевых членов команды: " + e.getMessage());
            return "redirect:/team";
        }
    }
}