package com.community.cms.web.mvc.controller.admin.content;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.repository.content.ProjectRepository;
import com.community.cms.domain.service.content.PhotoGalleryService;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.TeamMemberService;
import com.community.cms.domain.service.people.PartnerService;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;
    private final PartnerService partnerService;
    private final ProjectRepository projectRepository;

    @Autowired
    private PhotoGalleryService photoGalleryService;

    @Autowired
    public ProjectAdminController(ProjectService projectService,
                                  TeamMemberService teamMemberService,
                                  PartnerService partnerService,
                                  ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
        this.partnerService = partnerService;
        this.projectRepository = projectRepository;
    }

    // ================== СПИСОК ПРОЕКТОВ ==================
    @GetMapping
    public String listProjects(Model model,
                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer year) {

        Page<Project> projectsPage;

        try {
            // ================== СПЕЦИАЛЬНАЯ ОБРАБОТКА ПОИСКА ==================
            if (search != null && !search.trim().isEmpty()) {
                List<Project> searchResults = projectRepository.findByTitleContainingIgnoreCase(search.trim());

                // Дополнительная фильтрация поисковых результатов
                List<Project> filteredProjects = new ArrayList<>(searchResults);

                // ФИЛЬТРАЦИЯ ПО СТАТУСУ
                if (status != null && !status.trim().isEmpty() && !filteredProjects.isEmpty()) {
                    try {
                        Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                        filteredProjects = filteredProjects.stream()
                                .filter(p -> p.getStatus() == projectStatus)
                                .collect(Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Некорректный статус: " + status);
                    }
                }

                // ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ
                if (category != null && !category.trim().isEmpty() && !category.equals("Все категории") && !filteredProjects.isEmpty()) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> category.equals(p.getCategory()))
                            .collect(Collectors.toList());
                }

                // ФИЛЬТРАЦИЯ ПО ГОДУ СОБЫТИЯ
                if (year != null && !filteredProjects.isEmpty()) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> p.getEventDate() != null && p.getEventDate().getYear() == year)
                            .collect(Collectors.toList());
                }

                // ПАГИНАЦИЯ
                filteredProjects.sort(Comparator.comparing(Project::getCreatedAt).reversed());
                int start = (int) pageable.getOffset();
                int totalItems = filteredProjects.size();
                if (start > totalItems) start = 0;
                int end = Math.min((start + pageable.getPageSize()), totalItems);

                List<Project> pageContent = (start >= totalItems || filteredProjects.isEmpty())
                        ? Collections.emptyList()
                        : filteredProjects.subList(start, end);

                projectsPage = new PageImpl<>(pageContent, pageable, totalItems);

            } else {
                // ================== БЕЗ ПОИСКА ==================
                List<Project> allProjects = projectRepository.findAll();
                List<Project> filteredProjects = new ArrayList<>(allProjects);

                // ФИЛЬТРАЦИЯ ПО СТАТУСУ
                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                        filteredProjects = filteredProjects.stream()
                                .filter(p -> p.getStatus() == projectStatus)
                                .collect(Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Некорректный статус: " + status);
                    }
                }

                // ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ
                if (category != null && !category.trim().isEmpty() && !category.equals("Все категории")) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> category.equals(p.getCategory()))
                            .collect(Collectors.toList());
                }

                // ФИЛЬТРАЦИЯ ПО ГОДУ СОБЫТИЯ
                if (year != null) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> p.getEventDate() != null && p.getEventDate().getYear() == year)
                            .collect(Collectors.toList());
                }

                // ПАГИНАЦИЯ
                filteredProjects.sort(Comparator.comparing(Project::getCreatedAt).reversed());
                int start = (int) pageable.getOffset();
                int totalItems = filteredProjects.size();
                if (start > totalItems) start = 0;
                int end = Math.min((start + pageable.getPageSize()), totalItems);

                List<Project> pageContent = (start >= totalItems || filteredProjects.isEmpty())
                        ? Collections.emptyList()
                        : filteredProjects.subList(start, end);

                projectsPage = new PageImpl<>(pageContent, pageable, totalItems);
            }

        } catch (Exception e) {
            System.err.println("ОШИБКА при фильтрации проектов: " + e.getMessage());
            projectsPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            model.addAttribute("errorMessage", "Ошибка при поиске: " + e.getMessage());
        }

        // ================== ПОДГОТОВКА ДАННЫХ ДЛЯ ШАБЛОНА ==================
        model.addAttribute("projectsPage", projectsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());

        List<Integer> years = projectRepository.findAll().stream()
                .filter(p -> p.getEventDate() != null)
                .map(p -> p.getEventDate().getYear())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
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
        model.addAttribute("statuses", Project.ProjectStatus.values());

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
                                @RequestParam(value = "videoUrl", required = false) String videoUrl) {

        // ===== ВАЛИДАЦИЯ ДАТ =====
        if (project.getStartDate() != null && project.getEndDate() != null &&
                project.getStartDate().isAfter(project.getEndDate())) {
            bindingResult.rejectValue("startDate", "error.project", "Дата начала не может быть позже даты окончания");
        }

        if (project.getEventDate() != null && project.getStartDate() != null && project.getEndDate() != null &&
                (project.getEventDate().isBefore(project.getStartDate()) ||
                        project.getEventDate().isAfter(project.getEndDate()))) {
            bindingResult.rejectValue("eventDate", "error.project", "Дата события должна быть в рамках проекта");
        }

        // Восстанавливаем данные для формы
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());
        model.addAttribute("allPartners", partnerService.findActiveByNameContaining(""));

        // ===== ОБРАБОТКА КАТЕГОРИИ =====
        if ("__NEW__".equals(project.getCategory())) {
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                bindingResult.rejectValue("category", "error.project", "Введите название новой категории");
                return "admin/projects/create";
            } else {
                String cleanedCategory = newCategoryName.trim();
                project.setCategory(cleanedCategory);

                // Проверка уникальности
                List<String> allCategories = projectRepository.findAllDistinctCategories();
                for (String cat : allCategories) {
                    if (cat != null && cleanedCategory != null) {
                        String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                        String normalizedNew = cleanedCategory.trim().toLowerCase().replaceAll("\\s+", " ");
                        if (normalizedExisting.equals(normalizedNew)) {
                            bindingResult.rejectValue("category", "error.project",
                                    "Категория \"" + cat + "\" уже существует");
                            return "admin/projects/create";
                        }
                    }
                }
            }
        } else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "error.project", "Выберите категорию проекта");
            return "admin/projects/create";
        }

        if (bindingResult.hasErrors()) {
            return "admin/projects/create";
        }

        // Проверка уникальности slug
        if (projectService.existsBySlug(project.getSlug())) {
            bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
            return "admin/projects/create";
        }

        // ===== ОБРАБОТКА ВИДЕО URL =====
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            project.setVideoUrl(videoUrl.trim());
        }

        try {
            // Инициализация коллекций
            if (project.getTeamMembers() == null) project.setTeamMembers(new HashSet<>());
            if (project.getPartners() == null) project.setPartners(new HashSet<>());

            // Сохранение проекта
            Project savedProject = projectService.save(project);

            // ===== ОБРАБОТКА КОМАНДЫ =====
            if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
                String[] ids = selectedTeamMemberIds.split(",");
                for (String idStr : ids) {
                    try {
                        Long memberId = Long.parseLong(idStr.trim());
                        teamMemberService.findById(memberId).ifPresent(member -> {
                            if (member.getProjects() == null) member.setProjects(new HashSet<>());
                            member.getProjects().add(savedProject);
                            savedProject.getTeamMembers().add(member);
                            teamMemberService.save(member);
                        });
                    } catch (NumberFormatException ignored) {}
                }
                projectService.save(savedProject);
            }


            // ===== ОБРАБОТКА ПАРТНЁРОВ =====
            if (selectedPartnerIds != null && !selectedPartnerIds.trim().isEmpty()) {
                String[] ids = selectedPartnerIds.split(",");
                for (String idStr : ids) {
                    try {
                        Long partnerId = Long.parseLong(idStr.trim());
                        partnerService.findById(partnerId).ifPresent(partner -> {
                            // Инициализируем коллекцию если null
                            if (partner.getProjects() == null) {
                                partner.setProjects(new HashSet<>());
                            }
                            // Добавляем проект к партнёру
                            partner.getProjects().add(savedProject);
                            // Добавляем партнёра к проекту
                            savedProject.getPartners().add(partner);
                            // Сохраняем партнёра
                            partnerService.save(partner);
                        });
                    } catch (NumberFormatException ignored) {}
                }
                // СОХРАНЯЕМ проект после добавления партнёров!
                projectService.save(savedProject);
            }

            // ===== ОБРАБОТКА ФОТО =====
            if (selectedPhotoIds != null && !selectedPhotoIds.trim().isEmpty()) {
                try {
                    List<Long> photoIds = Arrays.stream(selectedPhotoIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .limit(5)
                            .collect(Collectors.toList());
                    savedProject.setKeyPhotoIds(photoIds);
                    projectService.save(savedProject);
                } catch (Exception ignored) {}
            }

            // Формирование сообщения об успехе
            String successMessage = getSuccessMessage(selectedTeamMemberIds, selectedPartnerIds, selectedPhotoIds);

            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/projects";

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            bindingResult.reject("error.project", "Ошибка при создании проекта: " + e.getMessage());
            return "admin/projects/create";
        }
    }

    @Nonnull
    private static String getSuccessMessage(String selectedTeamMemberIds, String selectedPartnerIds, String selectedPhotoIds) {
        String successMessage = "Проект успешно создан";
        if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
            successMessage += " с командой из " + selectedTeamMemberIds.split(",").length + " человек";
        }
        if (selectedPartnerIds != null && !selectedPartnerIds.trim().isEmpty()) {
            successMessage += ", партнерами: " + selectedPartnerIds.split(",").length;
        }
        if (selectedPhotoIds != null && !selectedPhotoIds.trim().isEmpty()) {
            successMessage += " и " + selectedPhotoIds.split(",").length + " фото";
        }
        return successMessage;
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
                    model.addAttribute("statuses", Project.ProjectStatus.values());
                    model.addAttribute("allTeamMembers", allTeamMembers);
                    model.addAttribute("projectTeamMembers", projectTeamMembers);
                    model.addAttribute("availableMembers", availableMembers);
                    model.addAttribute("videoUrl", project.getVideoUrl());

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
                                @RequestParam(value = "videoUrl", required = false) String videoUrl) {

        // ===== ВАЛИДАЦИЯ ДАТ =====
        if (project.getStartDate() != null && project.getEndDate() != null &&
                project.getStartDate().isAfter(project.getEndDate())) {
            bindingResult.rejectValue("startDate", "error.project", "Дата начала не может быть позже даты окончания");
        }

        if (project.getEventDate() != null && project.getStartDate() != null && project.getEndDate() != null &&
                (project.getEventDate().isBefore(project.getStartDate()) ||
                        project.getEventDate().isAfter(project.getEndDate()))) {
            bindingResult.rejectValue("eventDate", "error.project", "Дата события должна быть в рамках проекта");
        }

        // Восстанавливаем данные для формы (на случай ошибки)
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());

        // Получаем текущий проект для восстановления данных формы
        Project existingProjectForForm = projectService.findById(id).orElse(null);
        if (existingProjectForForm != null) {
            // Команда
            List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProjectForForm);
            List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
            List<TeamMember> availableMembers = allTeamMembers.stream()
                    .filter(member -> !projectTeamMembers.contains(member))
                    .collect(Collectors.toList());

            model.addAttribute("allTeamMembers", allTeamMembers);
            model.addAttribute("projectTeamMembers", projectTeamMembers);
            model.addAttribute("availableMembers", availableMembers);

            // Партнёры
            List<Partner> projectPartners = partnerService.findByProject(existingProjectForForm);
            List<Partner> allPartners = partnerService.findByNameContaining("");
            List<Partner> availablePartners = allPartners.stream()
                    .filter(partner -> !projectPartners.contains(partner))
                    .collect(Collectors.toList());

            model.addAttribute("allPartners", allPartners);
            model.addAttribute("projectPartners", projectPartners);
            model.addAttribute("availablePartners", availablePartners);
            List<Partner> partnersInProject = partnerService.findByProject(existingProjectForForm);
            model.addAttribute("projectPartnersCount", partnersInProject.size());

            // Видео URL
            model.addAttribute("videoUrl", existingProjectForForm.getVideoUrl());
        } else {
            // Если проект не найден, показываем пустые списки
            model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());
            model.addAttribute("allPartners", partnerService.findByNameContaining(""));
        }

        // ===== ОБРАБОТКА КАТЕГОРИИ =====
        if ("__NEW__".equals(project.getCategory())) {
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                bindingResult.rejectValue("category", "error.project", "Введите название новой категории");
                return "admin/projects/edit";
            } else {
                String cleanedCategory = newCategoryName.trim();
                project.setCategory(cleanedCategory);

                // Проверка уникальности
                List<String> allCategories = projectService.findAllDistinctCategories();
                for (String cat : allCategories) {
                    if (cat != null && cleanedCategory != null) {
                        String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                        String normalizedNew = cleanedCategory.trim().toLowerCase().replaceAll("\\s+", " ");
                        if (normalizedExisting.equals(normalizedNew)) {
                            bindingResult.rejectValue("category", "error.project",
                                    "Категория \"" + cat + "\" уже существует");
                            return "admin/projects/edit";
                        }
                    }
                }
            }
        } else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "error.project", "Выберите категорию проекта");
            return "admin/projects/edit";
        }

        if (bindingResult.hasErrors()) {
            return "admin/projects/edit";
        }

        // Проверка уникальности slug
        projectService.findBySlug(project.getSlug())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
                });

        if (bindingResult.hasErrors()) {
            return "admin/projects/edit";
        }

        try {
            // Получаем существующий проект из БД
            Project existingProject = projectService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Проект не найден"));

            // Обновляем основные поля проекта
            existingProject.setTitle(project.getTitle());
            existingProject.setSlug(project.getSlug());
            existingProject.setCategory(project.getCategory());
            existingProject.setStatus(project.getStatus());
            existingProject.setShortDescription(project.getShortDescription());
            existingProject.setFullDescription(project.getFullDescription());
            existingProject.setStartDate(project.getStartDate());
            existingProject.setEndDate(project.getEndDate());
            existingProject.setEventDate(project.getEventDate());
            existingProject.setLocation(project.getLocation());
            existingProject.setShowDescription(project.isShowDescription());
            existingProject.setShowPhotos(project.isShowPhotos());
            existingProject.setShowVideos(project.isShowVideos());
            existingProject.setShowTeam(project.isShowTeam());
            existingProject.setShowParticipation(project.isShowParticipation());
            existingProject.setShowPartners(project.isShowPartners());
            existingProject.setShowRelated(project.isShowRelated());

            // ===== ОБРАБОТКА ВИДЕО URL =====
            if (videoUrl != null) {
                existingProject.setVideoUrl(videoUrl.trim().isEmpty() ? null : videoUrl.trim());
            }

            // ===== ОБРАБОТКА ОБНОВЛЕНИЯ КОМАНДЫ =====
                        if (selectedTeamMemberIds != null) {
                // 1. Удаляем всех текущих членов из проекта
                List<TeamMember> currentMembers = teamMemberService.findByProject(existingProject);
                for (TeamMember member : currentMembers) {
                    if (member.getProjects() != null) {
                        member.getProjects().remove(existingProject);
                        teamMemberService.save(member);
                    }
                }
                existingProject.getTeamMembers().clear();

                // 2. Добавляем новых членов (если есть)
                if (!selectedTeamMemberIds.trim().isEmpty()) {
                    String[] ids = selectedTeamMemberIds.split(",");
                    for (String idStr : ids) {
                        try {
                            Long memberId = Long.parseLong(idStr.trim());
                            teamMemberService.findById(memberId).ifPresent(member -> {
                                if (member.getProjects() == null) {
                                    member.setProjects(new HashSet<>());
                                }
                                if (!member.getProjects().contains(existingProject)) {
                                    member.getProjects().add(existingProject);
                                }
                                existingProject.getTeamMembers().add(member);
                                teamMemberService.save(member);
                            });
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }


            // ===== ОБРАБОТКА ОБНОВЛЕНИЯ ПАРТНЁРОВ  =====
            if (selectedPartnerIds != null) {
                // 1. Получаем текущих партнёров проекта
                List<Partner> currentPartners = partnerService.findByProject(existingProject);

                // 2. Удаляем проект у текущих партнёров
                for (Partner partner : currentPartners) {
                    partner.getProjects().remove(existingProject);
                    partnerService.save(partner);
                }

                // 3. ОЧИЩАЕМ коллекцию у проекта
                existingProject.getPartners().clear();

                // 4. Сохраняем проект С ПУСТОЙ коллекцией
                projectService.save(existingProject);

                // 5. Добавляем новых партнёров (если есть)
                if (!selectedPartnerIds.trim().isEmpty()) {
                    String[] ids = selectedPartnerIds.split(",");
                    for (String idStr : ids) {
                        try {
                            Long partnerId = Long.parseLong(idStr.trim());
                            partnerService.findById(partnerId).ifPresent(partner -> {
                                // Инициализируем коллекцию если null
                                if (partner.getProjects() == null) {
                                    partner.setProjects(new HashSet<>());
                                }
                                // Добавляем проект к партнёру
                                partner.getProjects().add(existingProject);
                                // Добавляем партнёра к проекту
                                existingProject.getPartners().add(partner);
                                // Сохраняем партнёра
                                partnerService.save(partner);
                            });
                        } catch (NumberFormatException ignored) {}
                    }
                    // Сохраняем проект с новыми партнёрами
                    projectService.save(existingProject);
                }
            }

            // ===== ОБРАБОТКА ФОТО =====
            if (selectedPhotoIds != null) {
                try {
                    List<Long> photoIds = Arrays.stream(selectedPhotoIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .limit(5)
                            .collect(Collectors.toList());
                    existingProject.setKeyPhotoIds(photoIds);
                } catch (Exception ignored) {}
            }

            // Сохраняем проект
            projectService.save(existingProject);

            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно обновлен");
            return "redirect:/admin/projects";

        } catch (Exception e) {
            System.err.println("ERROR saving project: " + e.getMessage());
            e.printStackTrace();

            bindingResult.reject("error.project", "Ошибка при обновлении проекта: " + e.getMessage());
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
                    Map<String, Object> photoMap = new HashMap<>();
                    photoMap.put("id", photo.getId()); // ← ВАЖНО: id, а не photoId!
                    photoMap.put("fileName", photo.getFileName());
                    photoMap.put("webPath", photo.getWebPath());
                    photoMap.put("thumbnailPath", photo.getWebPath());
                    photoMap.put("galleryId", gallery.getId());
                    photoMap.put("galleryTitle", gallery.getTitle());
                    photoMap.put("galleryYear", gallery.getYear());
                    photoMap.put("isPrimary", photo.getIsPrimary());
                    result.add(photoMap);
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
}