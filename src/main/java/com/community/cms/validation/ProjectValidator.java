package com.community.cms.validation;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.service.content.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;

/**
 * Валидатор для проектов.
 * Вынесен из ProjectAdminController для соблюдения принципа единственной ответственности.
 */
@Component
public class ProjectValidator {



    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Проверяет корректность дат проекта.
     */
    public void validateDates(Project project, BindingResult bindingResult) {
        // Дата начала не может быть позже даты окончания
        if (project.getStartDate() != null && project.getEndDate() != null
                && project.getStartDate().isAfter(project.getEndDate())) {
            bindingResult.rejectValue("startDate", "error.project",
                    "Дата начала не может быть позже даты окончания");
        }

        // Дата события не может быть раньше даты начала
        if (project.getStartDate() != null && project.getEventDate() != null
                && project.getEventDate().isBefore(project.getStartDate())) {
            bindingResult.rejectValue("eventDate", "error.project",
                    "Дата события не может быть раньше даты начала проекта");
        }

        // Дата события должна быть в рамках периода
        if (project.getEventDate() != null && project.getStartDate() != null && project.getEndDate() != null
                && (project.getEventDate().isBefore(project.getStartDate())
                || project.getEventDate().isAfter(project.getEndDate()))) {
            bindingResult.rejectValue("eventDate", "error.project",
                    "Дата события должна быть в рамках проекта");
        }
    }

    /**
     * Проверяет корректность выбранного статуса относительно дат проекта.
     */
    public void validateProjectStatus(Project project, BindingResult bindingResult) {
        ProjectStatusType status = project.getStatus();
        LocalDate today = LocalDate.now();

        if (status == ProjectStatusType.ANNUAL || status == ProjectStatusType.ARCHIVED) {
            return;
        }

        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();
        LocalDate eventDate = project.getEventDate();

        if (status == ProjectStatusType.UPCOMING) {
            if (startDate != null && !startDate.isAfter(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Ближайшие' для проекта, дата начала которого уже наступила");
                return;
            }
            if (startDate == null && eventDate != null && !eventDate.isAfter(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Ближайшие' для проекта, дата события которого уже наступила");
            }
        }

        if (status == ProjectStatusType.ACTIVE) {
            if (endDate != null && endDate.isBefore(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Активные' для проекта, дата окончания которого уже прошла");
                return;
            }
            if (endDate == null && eventDate != null && eventDate.isBefore(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Активные' для проекта, дата события которого уже прошла");
            }
        }

        if (status == ProjectStatusType.COMPLETED) {
            if (startDate != null && !startDate.isBefore(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Завершённые' для проекта, который еще не завершился");
                return;
            }
            if (startDate == null && eventDate != null && !eventDate.isBefore(today)) {
                bindingResult.rejectValue("status", "error.project",
                        "Нельзя выбрать статус 'Завершённые' для проекта, дата события которого еще не прошла");
            }
        }
    }

    /**
     * Проверяет уникальность slug при создании нового проекта.
     */
    public void validateSlugUniquenessForCreate(Project project, BindingResult bindingResult) {
        if (projectService != null && projectService.existsBySlug(project.getSlug())) {
            bindingResult.rejectValue("slug", "error.project",
                    "Проект с таким URL уже существует");
        }
    }

    /**
     * Проверяет уникальность slug при редактировании проекта.
     *
     * @param project проект с новым slug
     * @param excludeId ID текущего проекта (исключаем из проверки)
     */
    public void validateSlugUniquenessForUpdate(Project project, Long excludeId, BindingResult bindingResult) {
        if (projectService == null) return;

        projectService.findBySlug(project.getSlug())
                .filter(p -> !p.getId().equals(excludeId))
                .ifPresent(p -> bindingResult.rejectValue("slug", "error.project",
                        "Проект с таким URL уже существует"));
    }

    /**
     * Проверяет и обрабатывает категорию проекта.
     * Поддерживает создание новой категории через спецзначение "__NEW__".
     *
     * @param project проект с выбранной категорией
     * @param newCategoryName название новой категории (если выбрано "__NEW__")
     * @param existingCategories список существующих категорий
     * @param bindingResult для регистрации ошибок
     * @return true если есть ошибка валидации, false если всё ок
     */
    public boolean validateAndProcessCategory(Project project, String newCategoryName,
                                              List<String> existingCategories,
                                              BindingResult bindingResult) {
        // Создание новой категории
        if ("__NEW__".equals(project.getCategory())) {
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                bindingResult.rejectValue("category", "error.project",
                        "Введите название новой категории");
                return true;
            }

            String cleanedCategory = newCategoryName.trim();
            project.setCategory(cleanedCategory);

            // Проверка на дубликат с существующей категорией
            for (String cat : existingCategories) {
                if (cat != null && cat.trim().equalsIgnoreCase(cleanedCategory)) {
                    bindingResult.rejectValue("category", "error.project",
                            "Категория \"" + cat + "\" уже существует");
                    return true;
                }
            }
            return false;
        }

        // Выбор существующей категории
        if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "error.project",
                    "Выберите категорию проекта");
            return true;
        }

        return false;
    }
}