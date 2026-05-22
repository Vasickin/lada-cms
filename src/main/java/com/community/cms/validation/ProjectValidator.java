package com.community.cms.validation;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

/**
 * Валидатор для проектов.
 * Вынесен из ProjectAdminController для соблюдения принципа единственной ответственности.
 */
@Component
public class ProjectValidator {

    private static final Logger log = LoggerFactory.getLogger(ProjectValidator.class);

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

        //  Дата события не может быть раньше даты начала
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
}
