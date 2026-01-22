package com.community.cms.domain.repository.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Project в базе данных.
 *
 * <p>Предоставляет методы для выполнения CRUD операций и пользовательских запросов
 * для проектов организации "ЛАДА". Расширяет стандартный JpaRepository и добавляет
 * специализированные методы для поиска, фильтрации и пагинации проектов.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Project
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // ================== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==================

    /**
     * Находит проект по уникальному slug.
     * Используется для публичного доступа к проектам по ЧПУ.
     *
     * @param slug уникальный идентификатор проекта
     * @return Optional содержащий проект если найден
     */
    Optional<Project> findBySlug(String slug);

    /**
     * Находит проект по slug ТОЛЬКО если он активен.
     * Используется для публичного доступа к активным проектам.
     *
     * @param slug уникальный идентификатор проекта
     * @param status статус проекта
     * @return Optional содержащий проект если найден и соответствует статусу
     */
    Optional<Project> findBySlugAndStatus(String slug, ProjectStatus status);

    /**
     * Проверяет существование проекта с указанным slug.
     * Используется для валидации при создании/обновлении проектов.
     *
     * @param slug slug для проверки
     * @return true если проект с таким slug существует, иначе false
     */
    boolean existsBySlug(String slug);

    // ================== ФИЛЬТРАЦИЯ ПО СТАТУСУ ==================

    /**
     * Находит все проекты с указанным статусом.
     *
     * @param status статус для фильтрации
     * @return список проектов с указанным статусом
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Находит все проекты с указанным статусом, отсортированные по дате создания.
     *
     * @param status статус для фильтрации
     * @return список проектов с указанным статусом (сначала новые)
     */
    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);

    /**
     * Находит все проекты с указанным статусом, отсортированные по порядку сортировки.
     *
     * @param status статус для фильтрации
     * @return список проектов с указанным статусом (по sortOrder)
     */
    List<Project> findByStatusOrderBySortOrderAsc(ProjectStatus status);

    // ================== ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ ==================

    /**
     * Находит все проекты указанной категории.
     *
     * @param category категория для фильтрации
     * @return список проектов указанной категории
     */
    List<Project> findByCategory(String category);

    /**
     * Находит все проекты указанной категории и статуса.
     *
     * @param category категория для фильтрации
     * @param status статус для фильтрации
     * @return список проектов соответствующих категории и статусу
     */
    List<Project> findByCategoryAndStatus(String category, ProjectStatus status);

    /**
     * Находит все проекты указанной категории, отсортированные по дате создания.
     *
     * @param category категория для фильтрации
     * @return список проектов указанной категории (сначала новые)
     */
    List<Project> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Находит все уникальные категории проектов.
     * Используется для фильтрации на сайте.
     * ИСПРАВЛЕНИЕ: Используем <> вместо !=
     *
     * @return список уникальных категорий
     */
    @Query("SELECT DISTINCT p.category FROM Project p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllDistinctCategories();

    // ================== ФИЛЬТРАЦИЯ ПО ДАТАМ ==================

    /**
     * Находит проекты, начавшиеся после указанной даты.
     *
     * @param date дата для фильтрации
     * @return список проектов начавшихся после указанной даты
     */
    List<Project> findByStartDateAfter(LocalDate date);

    /**
     * Находит проекты, закончившиеся до указанной даты.
     *
     * @param date дата для фильтрации
     * @return список проектов закончившихся до указанной даты
     */
    List<Project> findByEndDateBefore(LocalDate date);

    /**
     * Находит проекты, которые активны в указанный период.
     * Проект считается активным если:
     * - startDate ≤ date ≤ endDate (если даты указаны)
     * - status = ACTIVE или ANNUAL, или ARCHIVED
     *
     * @param date дата для проверки
     * @return список проектов активных на указанную дату
     */
    @Query("SELECT p FROM Project p WHERE " +
            "p.status IN ('ACTIVE', 'ANNUAL', 'ARCHIVED') AND " +
            "(p.startDate IS NULL OR p.startDate <= :date) AND " +
            "(p.endDate IS NULL OR p.endDate >= :date)")
    List<Project> findActiveOnDate(@Param("date") LocalDate date);

    /**
     * Находит проекты с событием в указанную дату.
     *
     * @param date дата события
     * @return список проектов с событием в указанную дату
     */
    List<Project> findByEventDate(LocalDate date);

    /**
     * Находит проекты с событием в указанный период.
     *
     * @param startDate начало периода
     * @param endDate конец периода
     * @return список проектов с событием в указанный период
     */
    List<Project> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    // ================== ПОИСК ПО НАЗВАНИЮ И ОПИСАНИЮ ==================

    /**
     * Находит проекты по части названия (без учета регистра).
     * Используется для поиска проектов на сайте.
     *
     * @param title фрагмент названия для поиска
     * @return список найденных проектов
     */
    List<Project> findByTitleContainingIgnoreCase(String title);

    /**
     * Находит проекты по части описания (без учета регистра).
     *
     * @param description фрагмент описания для поиска
     * @return список найденных проектов
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :description, '%')) " +
            "OR LOWER(p.fullDescription) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Project> findByDescriptionContaining(@Param("description") String description);

    /**
     * Находит проекты по части названия или описания (без учета регистра).
     * Комплексный поиск для пользовательского интерфейса.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных проектов
     */
    @Query("SELECT p FROM Project p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.fullDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Project> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит все проекты с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница проектов
     */
    Page<Project> findAll(Pageable pageable);

    /**
     * Находит проекты с указанным статусом с пагинацией.
     *
     * @param status статус для фильтрации
     * @param pageable объект пагинации
     * @return страница проектов с указанным статусом
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    /**
     * Находит проекты указанной категории с пагинацией.
     *
     * @param category категория для фильтрации
     * @param pageable объект пагинации
     * @return страница проектов указанной категории
     */
    Page<Project> findByCategory(String category, Pageable pageable);

    /**
     * Находит проекты с указанным статусом и категорией с пагинацией.
     *
     * @param status статус для фильтрации
     * @param category категория для фильтрации
     * @param pageable объект пагинации
     * @return страница проектов соответствующих статусу и категории
     */
    Page<Project> findByStatusAndCategory(ProjectStatus status, String category, Pageable pageable);

    // ================== СОРТИРОВКА ==================

    /**
     * Находит все проекты, отсортированные по дате создания (новые сначала).
     *
     * @return список проектов отсортированных по дате создания
     */
    List<Project> findAllByOrderByCreatedAtDesc();

    /**
     * Находит все проекты, отсортированные по дате события (ближайшие сначала).
     *
     * @return список проектов отсортированных по дате события
     */
    @Query("SELECT p FROM Project p WHERE p.eventDate IS NOT NULL ORDER BY p.eventDate ASC")
    List<Project> findAllByOrderByEventDateAsc();

    /**
     * Находит все проекты, отсортированные по названию (A-Z).
     *
     * @return список проектов отсортированных по названию
     */
    List<Project> findAllByOrderByTitleAsc();

    /**
     * Находит все проекты, отсортированные по порядку сортировки.
     * Используется для ручной сортировки в админке.
     *
     * @return список проектов отсортированных по sortOrder
     */
    @Query("SELECT p FROM Project p ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Project> findAllByOrderBySortOrderAsc();

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    /**
     * Подсчитывает количество проектов по статусу.
     *
     * @param status статус для подсчета
     * @return количество проектов с указанным статусом
     */
    long countByStatus(ProjectStatus status);

    /**
     * Подсчитывает количество проектов по категории.
     *
     * @param category категория для подсчета
     * @return количество проектов указанной категории
     */
    long countByCategory(String category);

    /**
     * Подсчитывает количество проектов по статусу и категории.
     *
     * @param status статус для подсчета
     * @param category категория для подсчета
     * @return количество проектов соответствующих статусу и категории
     */
    long countByStatusAndCategory(ProjectStatus status, String category);

    /**
     * Находит последние N проектов.
     * Используется для отображения на главной странице.
     * ИСПРАВЛЕНИЕ: Используем Pageable для limit и <> вместо !=
     *
     * @param pageable объект пагинации с limit
     * @return страница последних проектов
     */
    @Query("SELECT p FROM Project p WHERE p.status <> 'ARCHIVED' ORDER BY p.createdAt DESC")
    Page<Project> findRecentProjects(Pageable pageable);

    /**
     * Находит последние N проектов (удобный метод).
     * ИСПРАВЛЕНИЕ: Обертка над методом с Pageable
     *
     * @param limit количество проектов
     * @return список последних проектов
     */
    default List<Project> findRecentProjects(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findRecentProjects(pageable).getContent();
    }

    /**
     * Находит избранные проекты (с обложкой).
     * Используется для слайдера на главной странице.
     * ИСПРАВЛЕНИЕ: Используем Pageable для limit и <> вместо !=
     *
     * @param pageable объект пагинации с limit
     * @return страница избранных проектов
     */
    @Query("SELECT p FROM Project p WHERE p.featuredImagePath IS NOT NULL AND p.status <> 'ARCHIVED' " +
            "ORDER BY p.createdAt DESC")
    Page<Project> findFeaturedProjects(Pageable pageable);

    /**
     * Находит избранные проекты (удобный метод).
     * ИСПРАВЛЕНИЕ: Обертка над методом с Pageable
     *
     * @param limit количество проектов
     * @return список избранных проектов
     */
    default List<Project> findFeaturedProjects(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findFeaturedProjects(pageable).getContent();
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит проекты без обложки.
     * Используется для уведомлений в админке.
     *
     * @return список проектов без featuredImagePath
     */
    List<Project> findByFeaturedImagePathIsNull();

    /**
     * Находит проекты без даты начала.
     * Используется для проверки заполненности данных.
     *
     * @return список проектов без startDate
     */
    List<Project> findByStartDateIsNull();

    /**
     * Находит проекты без описания.
     * Используется для проверки заполненности данных.
     *
     * @return список проектов без fullDescription
     */
    @Query("SELECT p FROM Project p WHERE p.fullDescription IS NULL OR TRIM(p.fullDescription) = ''")
    List<Project> findProjectsWithoutDescription();

    /**
     * Находит похожие проекты по категории.
     * Используется для секции "Похожие проекты".
     * ИСПРАВЛЕНИЕ: Используем Pageable для limit и <> вместо !=
     *
     * @param category категория для поиска
     * @param excludeId ID проекта для исключения
     * @param pageable объект пагинации с limit
     * @return страница похожих проектов
     */
    @Query("SELECT p FROM Project p WHERE p.category = :category AND p.id <> :excludeId AND p.status <> 'ARCHIVED' " +
            "ORDER BY p.createdAt DESC")
    Page<Project> findSimilarProjects(@Param("category") String category,
                                      @Param("excludeId") Long excludeId,
                                      Pageable pageable);

    /**
     * Похожие проекты для публичной части (все статусы)
     */
    @Query("SELECT p FROM Project p WHERE p.category = :category AND p.id <> :excludeId " +
            "ORDER BY p.createdAt DESC")
    Page<Project> findSimilarProjectsAllStatuses(@Param("category") String category,
                                                 @Param("excludeId") Long excludeId,
                                                 Pageable pageable);

    /**
     * Находит похожие проекты по категории (удобный метод).
     * ИСПРАВЛЕНИЕ: Обертка над методом с Pageable
     *
     * @param category категория для поиска
     * @param excludeId ID проекта для исключения
     * @param limit количество проектов
     * @return список похожих проектов
     */
    default List<Project> findSimilarProjects(String category, Long excludeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findSimilarProjects(category, excludeId, pageable).getContent();
    }

    default List<Project> findSimilarProjectsAllStatuses(String category, Long excludeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findSimilarProjectsAllStatuses(category, excludeId, pageable).getContent();
    }

    /**
     * Находит проекты с событиями в будущем.
     * Используется для календаря мероприятий.
     * ИСПРАВЛЕНИЕ: Используем <> вместо !=
     *
     * @param date текущая дата
     * @return список проектов с будущими событиями
     */
    @Query("SELECT p FROM Project p WHERE p.eventDate > :date AND p.status <> 'ARCHIVED' ORDER BY p.eventDate ASC")
    List<Project> findUpcomingEvents(@Param("date") LocalDate date);

    /**
     * Находит проекты с событиями в прошлом.
     * Используется для архива мероприятий.
     * ИСПРАВЛЕНИЕ: Используем <> вместо !=
     *
     * @param date текущая дата
     * @return список проектов с прошедшими событиями
     */
    @Query("SELECT p FROM Project p WHERE p.eventDate < :date AND p.status <> 'ARCHIVED' ORDER BY p.eventDate DESC")
    List<Project> findPastEvents(@Param("date") LocalDate date);
}