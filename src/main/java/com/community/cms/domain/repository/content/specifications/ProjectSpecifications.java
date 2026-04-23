package com.community.cms.domain.repository.content.specifications;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.Project;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Спецификации для фильтрации проектов на уровне базы данных.
 *
 * <p>Используется для динамического построения запросов к БД
 * без загрузки всех данных в память. Все фильтры применяются
 * на стороне PostgreSQL.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
public class ProjectSpecifications {

    /**
     * Фильтрация по статусу проекта.
     *
     * @param status статус для фильтрации (может быть null)
     * @return спецификация для фильтрации по статусу
     */
    public static Specification<Project> hasStatus(ProjectStatusType status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Фильтрация по категории проекта.
     *
     * @param category категория для фильтрации (может быть null или пустой)
     * @return спецификация для фильтрации по категории
     */
    public static Specification<Project> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null || category.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    /**
     * Фильтрация по году даты события.
     * Использует функцию date_part для получения года из даты в PostgreSQL.
     *
     * @param year год для фильтрации (может быть null)
     * @return спецификация для фильтрации по году
     */
    public static Specification<Project> hasYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return null;
            }

            // Используем date_part('year', event_date) для PostgreSQL
            Expression<Integer> yearExpression = criteriaBuilder.function(
                    "date_part",
                    Integer.class,
                    criteriaBuilder.literal("year"),
                    root.get("eventDate")
            );

            return criteriaBuilder.equal(yearExpression, year);
        };
    }

    /**
     * Фильтрация по точной дате события.
     *
     * @param date дата для фильтрации (может быть null)
     * @return спецификация для фильтрации по дате
     */
    public static Specification<Project> hasDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("eventDate"), date);
        };
    }

    /**
     * Поиск по названию и описанию.
     * НЕ ИСПОЛЬЗУЕТСЯ в текущей реализации - поиск выполняется через нативный запрос.
     *
     * @param searchTerm поисковый запрос
     * @return null (спецификация не применяется)
     */
    public static Specification<Project> searchByTerm(String searchTerm) {
        return null;
    }

    /**
     * Комбинирует несколько спецификаций через AND.
     * Все переданные спецификации должны быть выполнены.
     *
     * @param specifications массив спецификаций для комбинации
     * @return комбинированная спецификация
     */
    @SafeVarargs
    public static Specification<Project> and(Specification<Project>... specifications) {
        return (root, query, criteriaBuilder) -> {
            Predicate combinedPredicate = criteriaBuilder.conjunction();
            for (Specification<Project> spec : specifications) {
                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                    if (predicate != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, predicate);
                    }
                }
            }
            return combinedPredicate;
        };
    }
}