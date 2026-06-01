package com.community.cms.web.mvc.controller.admin.content;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.domain.repository.media.MediaFileRepository;
import com.community.cms.validation.ProjectValidator;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.service.content.PhotoGalleryService;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.TeamMemberService;
import com.community.cms.domain.service.people.PartnerService;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для админ-панели управления проектами.
 */
@Controller
@RequestMapping("/admin/projects")
public class ProjectAdminController {

    private static final Logger log = LoggerFactory.getLogger(ProjectAdminController.class);

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;
    private final PartnerService partnerService;
    private final ProjectValidator projectValidator;
    private final MediaFileRepository mediaFileRepository;

    @Autowired
    private PhotoGalleryService photoGalleryService;

    @Autowired
    public ProjectAdminController(ProjectService projectService,
                                  TeamMemberService teamMemberService,
                                  PartnerService partnerService,
                                  ProjectValidator projectValidator,
                                  MediaFileRepository mediaFileRepository) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
        this.partnerService = partnerService;
        this.projectValidator = projectValidator;
        this.mediaFileRepository = mediaFileRepository;
    }

    // ================== СПИСОК ПРОЕКТОВ ==================
    @GetMapping
    public String listProjects(Model model,
                               @PageableDefault(size = 20) Pageable pageable,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer year) {

        Page<Project> projectsPage;

        // ===== ПРЕОБРАЗОВАНИЕ СТАТУСА В ENUM =====
        ProjectStatusType statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = ProjectStatusType.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Некорректный статус: {}", status);
            }
        }

        // ===== ПАГИНАЦИЯ БЕЗ СОРТИРОВКИ (сортировка в БД через Pageable по умолчанию) =====
        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // ===== ФИЛЬТРАЦИЯ ЧЕРЕЗ БД =====
        projectsPage = projectService.findFilteredProjects(
                statusEnum, category, year, null, search, customPageable
        );

        // ===== ПОДГОТОВКА ДАННЫХ ДЛЯ ШАБЛОНА =====
        model.addAttribute("projectsPage", projectsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", ProjectStatusType.values());

        // Годы из БД (отдельный запрос)
        List<Integer> years = projectService.findAllDistinctEventYears();
        model.addAttribute("years", years);

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSearch", search);

        return "admin/projects/list";
    }

    // ================== СОЗДАНИЕ ПРОЕКТА ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Project project = new Project();
        model.addAttribute("project", project);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", ProjectStatusType.values());
        model.addAttribute("forceShowInCarousel", false);

        // Члены команды
        List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
        model.addAttribute("allTeamMembers", allTeamMembers);

        // Партнёры
        List<Partner> allPartners = partnerService.findActiveByNameContaining("");
        model.addAttribute("allPartners", allPartners);

        return "admin/projects/create";
    }


    @PostMapping("/create")
    @Transactional
    public String createProject(@Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds,
                                @RequestParam(value = "selectedPartnerIds", required = false) String selectedPartnerIds,
                                @RequestParam(value = "selectedPhotoIds", required = false) String selectedPhotoIds,
                                @RequestParam(value = "videoUrl", required = false) String videoUrl,
                                @RequestParam(value = "forceShowInCarousel", required = false) String[] forceShowInCarouselParam) {

        // Обработка флага карусели
        project.setForceShowInCarousel(parseForceShowInCarousel(forceShowInCarouselParam));

        // Валидация
        projectValidator.validateProjectStatus(project, bindingResult);
        projectValidator.validateDates(project, bindingResult);

        // Данные для формы
        prepareFormData(model);

        // Обработка категории
        List<String> allCategories = projectService.findAllDistinctCategories();
        if (projectValidator.validateAndProcessCategory(project, newCategoryName, allCategories, bindingResult)) {
            return "admin/projects/create";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedTeamMemberIds", selectedTeamMemberIds);
            model.addAttribute("selectedPartnerIds", selectedPartnerIds);
            model.addAttribute("selectedPhotoIds", selectedPhotoIds);
            return "admin/projects/create";
        }

        // Проверка уникальности slug
        projectValidator.validateSlugUniquenessForCreate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            return "admin/projects/create";
        }

        try {
            processVideoUrl(project, videoUrl);
            Project savedProject = projectService.save(project);
            processTeamMembers(savedProject, selectedTeamMemberIds);
            processPartners(savedProject, selectedPartnerIds);
            processPhotos(savedProject, selectedPhotoIds);
            projectService.save(savedProject);

            String successMessage = buildSuccessMessage(selectedTeamMemberIds, selectedPartnerIds, selectedPhotoIds);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/projects";

        } catch (Exception e) {
            log.error("Ошибка при создании проекта: {}", e.getMessage(), e);
            bindingResult.reject("error.project", "Ошибка при создании проекта: " + e.getMessage());
            return "admin/projects/create";
        }
    }


    // ================== РЕДАКТИРОВАНИЕ ПРОЕКТА ==================
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    // Получаем всех активных членов команды
                    List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                    List<TeamMember> projectTeamMembers = teamMemberService.findByProject(project);
                    List<TeamMember> availableMembers = allTeamMembers.stream()
                            .filter(member -> !projectTeamMembers.contains(member))
                            .collect(Collectors.toList());

                    // Получаем всех активных партнёров
                    List<Partner> allPartners = partnerService.findActiveByNameContaining("");
                    List<Partner> projectPartners = partnerService.findByProject(project);
                    List<Partner> availablePartners = allPartners.stream()
                            .filter(partner -> !projectPartners.contains(partner))
                            .collect(Collectors.toList());

                    List<Partner> partnersInProject = partnerService.findByProject(project);
                    long projectPartnersCount = partnersInProject.size();

                    model.addAttribute("project", project);
                    model.addAttribute("categories", projectService.findAllDistinctCategories());
                    model.addAttribute("statuses", ProjectStatusType.values());
                    model.addAttribute("allTeamMembers", allTeamMembers);
                    model.addAttribute("projectTeamMembers", projectTeamMembers);
                    model.addAttribute("availableMembers", availableMembers);
                    model.addAttribute("videoUrl", project.getVideoUrl());
                    model.addAttribute("forceShowInCarousel", project.isForceShowInCarousel());

                    // Партнёры
                    model.addAttribute("allPartners", allPartners);
                    model.addAttribute("projectPartners", projectPartners);
                    model.addAttribute("availablePartners", availablePartners);
                    model.addAttribute("projectPartnersCount", projectPartnersCount);

                    return "admin/projects/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds,
                                @RequestParam(value = "selectedPartnerIds", required = false) String selectedPartnerIds,
                                @RequestParam(value = "selectedPhotoIds", required = false) String selectedPhotoIds,
                                @RequestParam(value = "videoUrl", required = false) String videoUrl,
                                @RequestParam(value = "forceShowInCarousel", required = false) String[] forceShowInCarouselParam) {

        // Обработка флага карусели
        boolean forceShowInCarousel = parseForceShowInCarousel(forceShowInCarouselParam);

        // Валидация
        projectValidator.validateProjectStatus(project, bindingResult);
        projectValidator.validateDates(project, bindingResult);

        // Данные для формы
        prepareFormData(model);
        model.addAttribute("videoUrl", videoUrl);
        model.addAttribute("forceShowInCarousel", forceShowInCarousel);

        // Обработка категории
        if (processCategory(project, newCategoryName, bindingResult)) {
            restoreSelectedData(model, selectedTeamMemberIds, selectedPartnerIds, selectedPhotoIds);
            return "admin/projects/edit";
        }

        // Проверка уникальности slug
        projectValidator.validateSlugUniquenessForUpdate(project, id, bindingResult);

        // ✅ ОСНОВНОЙ БЛОК ОБРАБОТКИ ОШИБОК
        if (bindingResult.hasErrors()) {
            restoreSelectedData(model, selectedTeamMemberIds, selectedPartnerIds, selectedPhotoIds);
            return "admin/projects/edit";
        }

        try {
            Project existingProject = projectService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Проект не найден"));

            // Обновление основных полей
            updateProjectFields(existingProject, project, forceShowInCarousel);
            processVideoUrl(existingProject, videoUrl);

            // Обновление связей
            updateTeamMembers(existingProject, selectedTeamMemberIds);
            updatePartners(existingProject, selectedPartnerIds);
            processPhotos(existingProject, selectedPhotoIds);

            projectService.save(existingProject);

            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно обновлен");
            return "redirect:/admin/projects";

        } catch (Exception e) {
            log.error("Ошибка при обновлении проекта: {}", e.getMessage(), e);
            bindingResult.reject("error.project", "Ошибка при обновлении проекта: " + e.getMessage());
            restoreSelectedData(model, selectedTeamMemberIds, selectedPartnerIds, selectedPhotoIds);
            return "admin/projects/edit";
        }
    }

    // ================== ПРОСМОТР ПРОЕКТА ==================
    @GetMapping("/view/{id}")
    public String viewProject(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    model.addAttribute("project", project);
                    return "admin/projects/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    // ================== УДАЛЕНИЕ ПРОЕКТА ==================
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении проекта: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    // ================== УПРАВЛЕНИЕ СТАТУСОМ ==================
    @PostMapping("/activate/{id}")
    public String activateProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.activate(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект активирован");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при активации проекта: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    @PostMapping("/archive/{id}")
    public String archiveProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.archive(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект архивирован");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при архивации проекта: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    @PostMapping("/mark-annual/{id}")
    public String markAsAnnual(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.markAsAnnual(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект помечен как ежегодный");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    // ================== МАССОВЫЕ ОПЕРАЦИИ ==================
    @PostMapping("/batch")
    public String batchOperation(@RequestParam String action,
                                 @RequestParam("ids") List<Long> ids,
                                 RedirectAttributes redirectAttributes) {

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не выбрано ни одного проекта");
            return "redirect:/admin/projects";
        }

        int successCount = 0;
        int errorCount = 0;

        for (Long id : ids) {
            try {
                switch (action) {
                    case "activate":
                        projectService.findById(id).ifPresent(projectService::activate);
                        successCount++;
                        break;
                    case "archive":
                        projectService.findById(id).ifPresent(projectService::archive);
                        successCount++;
                        break;
                    case "delete":
                        projectService.deleteById(id);
                        successCount++;
                        break;
                    default:
                        errorCount++;
                }
            } catch (Exception e) {
                errorCount++;
            }
        }

        if (successCount > 0) {
            String message = switch (action) {
                case "activate" -> "Активировано проектов: " + successCount;
                case "archive" -> "Архивировано проектов: " + successCount;
                case "delete" -> "Удалено проектов: " + successCount;
                default -> "Выполнено операций: " + successCount;
            };
            redirectAttributes.addFlashAttribute("successMessage", message);
        }

        if (errorCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибок: " + errorCount);
        }

        return "redirect:/admin/projects";
    }

    // ================== ОЧИСТКА КЭША ==================
    @PostMapping("/clear-cache")
    public String clearCache(RedirectAttributes redirectAttributes) {
        try {
            projectService.clearAllCache();
            redirectAttributes.addFlashAttribute("successMessage", "Кэш проектов очищен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при очистке кэша: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    // ================== УПРАВЛЕНИЕ КОМАНДОЙ ПРОЕКТА ==================
    @GetMapping("/{id}/team-management")
    public String manageProjectTeam(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
        List<TeamMember> projectTeamMembers = teamMemberService.findByProject(project);
        List<TeamMember> availableTeamMembers = allTeamMembers.stream()
                .filter(member -> !projectTeamMembers.contains(member))
                .collect(Collectors.toList());

        model.addAttribute("project", project);
        model.addAttribute("availableMembers", availableTeamMembers);
        model.addAttribute("projectMembers", projectTeamMembers);
        model.addAttribute("allTeamMembers", allTeamMembers);
        return "admin/projects/project-team-management";
    }

    @PostMapping("/{id}/team-management/update")
    public String updateProjectTeam(@PathVariable Long id,
                                    @RequestParam(value = "teamMemberIds", required = false) List<Long> teamMemberIds,
                                    RedirectAttributes redirectAttributes) {
        try {
            Project project = projectService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));

            List<TeamMember> currentMembers = teamMemberService.findByProject(project);

            // Удаляем всех текущих членов из проекта
            for (TeamMember member : currentMembers) {
                member.getProjects().remove(project);
                teamMemberService.save(member);
            }

            // Добавляем новых членов
            if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
                for (Long memberId : teamMemberIds) {
                    TeamMember member = teamMemberService.findById(memberId)
                            .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));
                    member.getProjects().add(project);
                    teamMemberService.save(member);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Команда проекта успешно обновлена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении команды: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + id + "/team-management";
    }

    // ================== УПРАВЛЕНИЕ ФОТО ИЗ ГАЛЕРЕЙ ==================
    @GetMapping("/available-photos")
    @ResponseBody
    public List<Map<String, Object>> getAvailablePhotos() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            List<PhotoGallery> galleries = photoGalleryService.getAllPhotoGalleryItems();

            for (PhotoGallery gallery : galleries) {
                if (gallery.getPublished() != null && !gallery.getPublished()) {
                    continue;
                }

                List<MediaFile> photos = gallery.getImages();
                for (MediaFile photo : photos) {
                    result.add(getStringObjectMap(photo, gallery));
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки фото: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/available-galleries")
    @ResponseBody
    public List<PhotoGalleryDTO> getAvailableGalleries() {
        List<PhotoGalleryDTO> result = new ArrayList<>();

        try {
            List<PhotoGallery> galleries = photoGalleryService.getAllPhotoGalleryItems();

            for (PhotoGallery gallery : galleries) {
                if (gallery.getPublished() != null && !gallery.getPublished()) {
                    continue;
                }

                PhotoGalleryDTO dto = new PhotoGalleryDTO(
                        gallery.getId(),
                        gallery.getTitle(),
                        gallery.getYear(),
                        gallery.getDescription(),
                        gallery.getImagesCount(),
                        gallery.getPublished()
                );
                result.add(dto);
            }

            result.sort((a, b) -> b.getYear().compareTo(a.getYear()));

        } catch (Exception e) {
            System.err.println("Ошибка получения списка галерей: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/gallery/{galleryId}/photos")
    @ResponseBody
    public List<Map<String, Object>> getGalleryPhotos(@PathVariable Long galleryId) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            PhotoGallery gallery = photoGalleryService.getPhotoGalleryItemById(galleryId);
            List<MediaFile> photos = gallery.getImages();

            for (MediaFile photo : photos) {
                Map<String, Object> photoMap = getStringObjectMap(photo, gallery);
                result.add(photoMap);
            }

        } catch (Exception e) {
            System.err.println("Ошибка получения фото галереи " + galleryId + ": " + e.getMessage());
        }

        return result;
    }

    @Nonnull
    private static Map<String, Object> getStringObjectMap(MediaFile photo, PhotoGallery gallery) {
        Map<String, Object> photoMap = new HashMap<>();
        photoMap.put("id", photo.getId()); // ← ОБЯЗАТЕЛЬНО id для фронтенда!
        photoMap.put("photoId", photo.getId()); // и photoId для совместимости
        photoMap.put("fileName", photo.getFileName());
        photoMap.put("webPath", photo.getWebPath());
        photoMap.put("thumbnailPath", photo.getWebPath());
        photoMap.put("galleryId", gallery.getId());
        photoMap.put("galleryTitle", gallery.getTitle());
        photoMap.put("galleryYear", gallery.getYear());
        photoMap.put("isPrimary", photo.getIsPrimary());
        return photoMap;
    }

    @PostMapping("/photos-info")
    @ResponseBody
    public List<Map<String, Object>> getPhotosInfo(@RequestBody List<Long> photoIds) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Long id : photoIds) {
            mediaFileRepository.findById(id).ifPresent(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("title", p.getFileName());
                map.put("webPath", p.getWebPath());
                map.put("galleryTitle", "Фото");
                result.add(map);
            });
        }
        return result;
    }

    @GetMapping("/debug-search")
    @ResponseBody
    public Map<String, Object> debugSearch(@RequestParam String search) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Project> serviceResults = projectService.search(search);
            List<Map<String, Object>> serviceData = new ArrayList<>();

            for (Project p : serviceResults) {
                Map<String, Object> projectData = new HashMap<>();
                projectData.put("id", p.getId());
                projectData.put("title", p.getTitle());
                projectData.put("slug", p.getSlug());
                projectData.put("shortDescription", p.getShortDescription());
                serviceData.add(projectData);
            }

            result.put("searchQuery", search);
            result.put("serviceResults", serviceData);
            result.put("serviceCount", serviceResults.size());
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Предоставляет мапу соответствий между именами констант статусов
     * и их отображаемыми названиями на русском языке.
     * <p>
     * Используется в шаблонах для единообразного отображения статусов
     * в выпадающих списках, фильтрах и бейджах.
     * </p>
     *
     * @return LinkedHashMap с парами "ИМЯ_КОНСТАНТЫ" -> "Русское название"
     */
    @ModelAttribute("statusLabels")
    public Map<String, String> getStatusLabels() {
        Map<String, String> labels = new LinkedHashMap<>();

        // Порядок важен: будем использовать LinkedHashMap, чтобы сохранить
        // логическую последовательность статусов
        labels.put("UPCOMING", "Ближайшие");
        labels.put("ACTIVE", "Активные");
        labels.put("COMPLETED", "Завершённые");
        labels.put("ANNUAL", "Ежегодные");
        labels.put("ARCHIVED", "Архивные");

        return labels;
    }

    // ================== ПРИВАТНЫЕ МЕТОДЫ-ПОМОЩНИКИ ==================

    private boolean parseForceShowInCarousel(String[] param) {
        boolean result = param != null && Arrays.asList(param).contains("true");
        log.debug("forceShowInCarousel: {}", result);
        return result;
    }

    private boolean processCategory(Project project, String newCategoryName,
                                    BindingResult bindingResult) {
        if ("__NEW__".equals(project.getCategory())) {
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                bindingResult.rejectValue("category", "error.project", "Введите название новой категории");
                return true;
            }
            String cleanedCategory = newCategoryName.trim();
            project.setCategory(cleanedCategory);

            List<String> allCategories = projectService.findAllDistinctCategories();
            for (String cat : allCategories) {
                if (cat != null) {
                    String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                    String normalizedNew = cleanedCategory.toLowerCase().replaceAll("\\s+", " ");
                    if (normalizedExisting.equals(normalizedNew)) {
                        bindingResult.rejectValue("category", "error.project",
                                "Категория \"" + cat + "\" уже существует");
                        return true;
                    }
                }
            }
        } else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "error.project", "Выберите категорию проекта");
            return true;
        }
        return false;
    }

    private void processVideoUrl(Project project, String videoUrl) {
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            project.setVideoUrl(videoUrl.trim());
        }
    }

    private void processTeamMembers(Project project, String selectedTeamMemberIds) {
        if (selectedTeamMemberIds == null || selectedTeamMemberIds.trim().isEmpty()) {
            return;
        }
        if (project.getTeamMembers() == null) {
            project.setTeamMembers(new HashSet<>());
        }
        String[] ids = selectedTeamMemberIds.split(",");
        for (String idStr : ids) {
            try {
                Long memberId = Long.parseLong(idStr.trim());
                teamMemberService.findById(memberId).ifPresent(member -> {
                    if (member.getProjects() == null) {
                        member.setProjects(new HashSet<>());
                    }
                    member.getProjects().add(project);
                    project.getTeamMembers().add(member);
                    teamMemberService.save(member);
                });
            } catch (NumberFormatException e) {
                logInvalidId("члена команды", idStr);
            }
        }
        projectService.save(project);
    }

    private void processPartners(Project project, String selectedPartnerIds) {
        if (selectedPartnerIds == null || selectedPartnerIds.trim().isEmpty()) {
            return;
        }
        if (project.getPartners() == null) {
            project.setPartners(new HashSet<>());
        }
        String[] ids = selectedPartnerIds.split(",");
        for (String idStr : ids) {
            try {
                Long partnerId = Long.parseLong(idStr.trim());
                partnerService.findById(partnerId).ifPresent(partner -> {
                    if (partner.getProjects() == null) {
                        partner.setProjects(new HashSet<>());
                    }
                    partner.getProjects().add(project);
                    project.getPartners().add(partner);
                    partnerService.save(partner);
                });
            } catch (NumberFormatException e) {
                logInvalidId("партнёра", idStr);
            }
        }
        projectService.save(project);
    }

    private void processPhotos(Project project, String selectedPhotoIds) {
        if (selectedPhotoIds == null || selectedPhotoIds.trim().isEmpty()) {
            return;
        }
        try {
            List<Long> photoIds = Arrays.stream(selectedPhotoIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .limit(10)
                    .collect(Collectors.toList());
            project.setKeyPhotoIds(photoIds);
        } catch (Exception e) {
            log.warn("Ошибка при обработке фото: {}", e.getMessage());
        }
    }

    private void prepareFormData(Model model) {
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", ProjectStatusType.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());
        model.addAttribute("allPartners", partnerService.findActiveByNameContaining(""));
    }

    private String buildSuccessMessage(String teamIds, String partnerIds, String photoIds) {
        String msg = "Проект успешно создан";
        if (teamIds != null && !teamIds.trim().isEmpty()) {
            msg += " с командой из " + teamIds.split(",").length + " человек";
        }
        if (partnerIds != null && !partnerIds.trim().isEmpty()) {
            msg += ", партнерами: " + partnerIds.split(",").length;
        }
        if (photoIds != null && !photoIds.trim().isEmpty()) {
            msg += " и " + photoIds.split(",").length + " фото";
        }
        return msg;
    }

    private void updateProjectFields(Project existing, Project source, boolean forceShowInCarousel) {
        existing.setTitle(source.getTitle());
        existing.setSlug(source.getSlug());
        existing.setCategory(source.getCategory());
        existing.setStatus(source.getStatus());
        existing.setShortDescription(source.getShortDescription());
        existing.setFullDescription(source.getFullDescription());
        existing.setStartDate(source.getStartDate());
        existing.setEndDate(source.getEndDate());
        existing.setEventDate(source.getEventDate());
        existing.setLocation(source.getLocation());
        existing.setShowDescription(source.isShowDescription());
        existing.setShowPhotos(source.isShowPhotos());
        existing.setShowVideos(source.isShowVideos());
        existing.setShowTeam(source.isShowTeam());
        existing.setShowParticipation(source.isShowParticipation());
        existing.setShowPartners(source.isShowPartners());
        existing.setShowRelated(source.isShowRelated());
        existing.setForceShowInCarousel(forceShowInCarousel);
    }

    private void updateTeamMembers(Project project, String selectedTeamMemberIds) {
        if (selectedTeamMemberIds == null) {
            return;
        }
        List<TeamMember> currentMembers = teamMemberService.findByProject(project);
        for (TeamMember member : currentMembers) {
            if (member.getProjects() != null) {
                member.getProjects().remove(project);
                teamMemberService.save(member);
            }
        }
        project.getTeamMembers().clear();

        if (!selectedTeamMemberIds.trim().isEmpty()) {
            String[] ids = selectedTeamMemberIds.split(",");
            for (String idStr : ids) {
                try {
                    Long memberId = Long.parseLong(idStr.trim());
                    teamMemberService.findById(memberId).ifPresent(member -> {
                        if (member.getProjects() == null) {
                            member.setProjects(new HashSet<>());
                        }
                        member.getProjects().add(project);
                        project.getTeamMembers().add(member);
                        teamMemberService.save(member);
                    });
                } catch (NumberFormatException e) {
                    log.warn("Некорректный ID члена команды: {}", idStr);
                }
            }
        }
    }

    private void updatePartners(Project project, String selectedPartnerIds) {
        if (selectedPartnerIds == null) {
            return;
        }
        List<Partner> currentPartners = partnerService.findByProject(project);
        for (Partner partner : currentPartners) {
            partner.getProjects().remove(project);
            partnerService.save(partner);
        }
        project.getPartners().clear();
        projectService.save(project);

        if (!selectedPartnerIds.trim().isEmpty()) {
            String[] ids = selectedPartnerIds.split(",");
            for (String idStr : ids) {
                try {
                    Long partnerId = Long.parseLong(idStr.trim());
                    partnerService.findById(partnerId).ifPresent(partner -> {
                        if (partner.getProjects() == null) {
                            partner.setProjects(new HashSet<>());
                        }
                        partner.getProjects().add(project);
                        project.getPartners().add(partner);
                        partnerService.save(partner);
                    });
                } catch (NumberFormatException e) {
                    log.warn("Некорректный ID партнёра: {}", idStr);
                }
            }
            projectService.save(project);
        }
    }

    private void logInvalidId(String entityType, String idStr) {
        log.warn("Некорректный ID {}: {}", entityType, idStr);
    }

// ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ВОССТАНОВЛЕНИЯ ==================

    /**
     * Восстанавливает выбранные данные после ошибок валидации
     */
    private void restoreSelectedData(Model model,
                                     String selectedTeamMemberIds,
                                     String selectedPartnerIds,
                                     String selectedPhotoIds) {
        // Восстанавливаем команду проекта
        if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
            model.addAttribute("selectedTeamMemberIds", selectedTeamMemberIds);

            // Обновляем списки для корректного отображения в UI
            List<Long> selectedIds = parseIds(selectedTeamMemberIds);
            List<TeamMember> allMembers = teamMemberService.findAllActiveOrderBySortOrder();

            List<TeamMember> projectMembers = allMembers.stream()
                    .filter(m -> selectedIds.contains(m.getId()))
                    .collect(Collectors.toList());

            List<TeamMember> availableMembers = allMembers.stream()
                    .filter(m -> !selectedIds.contains(m.getId()))
                    .collect(Collectors.toList());

            model.addAttribute("projectTeamMembers", projectMembers);
            model.addAttribute("availableMembers", availableMembers);
        }

        // Восстанавливаем партнёров
        if (selectedPartnerIds != null && !selectedPartnerIds.trim().isEmpty()) {
            model.addAttribute("selectedPartnerIds", selectedPartnerIds);

            List<Long> selectedIds = parseIds(selectedPartnerIds);
            List<Partner> allPartners = partnerService.findActiveByNameContaining("");

            List<Partner> projectPartners = allPartners.stream()
                    .filter(p -> selectedIds.contains(p.getId()))
                    .collect(Collectors.toList());

            List<Partner> availablePartners = allPartners.stream()
                    .filter(p -> !selectedIds.contains(p.getId()))
                    .collect(Collectors.toList());

            model.addAttribute("projectPartners", projectPartners);
            model.addAttribute("availablePartners", availablePartners);
            model.addAttribute("projectPartnersCount", projectPartners.size());
        }

        // Восстанавливаем фото
        if (selectedPhotoIds != null && !selectedPhotoIds.trim().isEmpty()) {
            model.addAttribute("selectedPhotoIds", selectedPhotoIds);
        }
    }

    /**
     * Парсит строку с ID разделёнными запятыми в список Long
     */
    private List<Long> parseIds(String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * Проверка уникальности slug на лету (для клиентской валидации)
     */
    @GetMapping("/check-slug")
    @ResponseBody
    public Map<String, Boolean> checkSlug(@RequestParam String slug,
                                          @RequestParam(required = false) Long excludeId) {
        Map<String, Boolean> response = new HashMap<>();

        if (excludeId != null) {
            // Режим редактирования: исключаем текущий проект
            boolean exists = projectService.findBySlug(slug)
                    .filter(p -> !p.getId().equals(excludeId))
                    .isPresent();
            response.put("exists", exists);
        } else {
            // Режим создания
            response.put("exists", projectService.existsBySlug(slug));
        }

        return response;
    }
}