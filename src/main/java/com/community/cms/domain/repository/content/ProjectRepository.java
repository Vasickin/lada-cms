package com.community.cms.domain.repository.content;

import com.community.cms.domain.enums.ProjectStatusType;
import com.community.cms.domain.model.content.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

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
    Optional<Project> findBySlugAndStatus(String slug, ProjectStatusType status);

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
    List<Project> findByStatus(ProjectStatusType status);

    // ================== ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ ==================

    /**
     * Находит все проекты указанной категории и статуса.
     *
     * @param category категория для фильтрации
     * @param status статус для фильтрации
     * @return список проектов соответствующих категории и статусу
     */
    List<Project> findByCategoryAndStatus(String category, ProjectStatusType status);

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
    @SuppressWarnings("NullableProblems")
    Page<Project> findAll(Pageable pageable);

    /**
     * Находит проекты с указанным статусом с пагинацией.
     *
     * @param status статус для фильтрации
     * @param pageable объект пагинации
     * @return страница проектов с указанным статусом
     */
    Page<Project> findByStatus(ProjectStatusType status, Pageable pageable);

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
    Page<Project> findByStatusAndCategory(ProjectStatusType status, String category, Pageable pageable);


    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    /**
     * Подсчитывает количество проектов по статусу.
     *
     * @param status статус для подсчета
     * @return количество проектов с указанным статусом
     */
    long countByStatus(ProjectStatusType status);

    /**
     * Подсчитывает количество проектов по категории.
     *
     * @param category категория для подсчета
     * @return количество проектов указанной категории
     */
    long countByCategory(String category);

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
     * Находит похожие проекты по категории (включая архивные).
     * Использует нативный запрос PostgreSQL для корректной сортировки с NULLS LAST.
     *
     * @param category категория для поиска
     * @param excludeId ID проекта для исключения
     * @param pageable параметры пагинации с лимитом
     * @return страница похожих проектов
     */
    @Query(value = "SELECT * FROM projects p WHERE p.category = :category AND p.id != :excludeId " +
            "ORDER BY p.event_date DESC NULLS LAST, p.created_at DESC",
            nativeQuery = true)
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

    /**
     * Находит проекты со статусом UPCOMING, которые должны стать ACTIVE.
     * <p>
     * Условия выборки:
     * <ul>
     *     <li>Статус проекта = UPCOMING</li>
     *     <li>И (дата начала не пустая И дата начала <= сегодня)
     *         ИЛИ (дата начала пустая И дата события не пустая И дата события <= сегодня)</li>
     * </ul>
     * </p>
     *
     * @return список проектов для активации
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'UPCOMING' AND " +
            "((p.startDate IS NOT NULL AND p.startDate <= CURRENT_DATE) OR " +
            "(p.startDate IS NULL AND p.eventDate IS NOT NULL AND p.eventDate <= CURRENT_DATE))")
    List<Project> findUpcomingProjectsToActivate();

    /**
     * Находит проекты со статусом ACTIVE, которые должны стать COMPLETED.
     * <p>
     * Условия выборки:
     * <ul>
     *     <li>Статус проекта = ACTIVE</li>
     *     <li>И (дата окончания не пустая И дата окончания < сегодня)
     *         ИЛИ (дата окончания пустая И дата события не пустая И дата события < сегодня)</li>
     * </ul>
     * </p>
     *
     * @return список проектов для завершения
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' AND " +
            "((p.endDate IS NOT NULL AND p.endDate < CURRENT_DATE) OR " +
            "(p.endDate IS NULL AND p.eventDate IS NOT NULL AND p.eventDate < CURRENT_DATE))")
    List<Project> findActiveProjectsToComplete();

    /**
     * Находит проекты с некорректным статусом UPCOMING (которые уже должны быть активны).
     * <p>
     * Используется для валидации и исправления возможных ошибок.
     * </p>
     *
     * @return список проектов с некорректным статусом
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'UPCOMING' AND " +
            "p.startDate IS NOT NULL AND p.startDate < CURRENT_DATE")
    List<Project> findInvalidUpcomingProjects();

    /**
     * Находит проекты, которые должны быть обновлены на текущую дату.
     * <p>
     * Объединяет оба условия для UPCOMING и ACTIVE в одном запросе.
     * Может быть полезно для массовой обработки.
     * </p>
     *
     * @return список проектов, требующих обновления статуса
     */
    @Query("SELECT p FROM Project p WHERE " +
            "(p.status = 'UPCOMING' AND " +
            "((p.startDate IS NOT NULL AND p.startDate <= CURRENT_DATE) OR " +
            "(p.startDate IS NULL AND p.eventDate IS NOT NULL AND p.eventDate <= CURRENT_DATE))) " +
            "OR " +
            "(p.status = 'ACTIVE' AND " +
            "((p.endDate IS NOT NULL AND p.endDate < CURRENT_DATE) OR " +
            "(p.endDate IS NULL AND p.eventDate IS NOT NULL AND p.eventDate < CURRENT_DATE)))")
    List<Project> findProjectsToUpdate();

    /**
     * Проверяет, есть ли активные проекты на указанную дату.
     * Используется для календаря и статистики.
     *
     * @param date дата для проверки
     * @return true если есть активные проекты
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE " +
            "p.status = 'ACTIVE' AND " +
            "((p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)) OR " +
            "(p.startDate IS NULL AND p.eventDate = :date))")
    boolean hasActiveProjectsOnDate(@Param("date") LocalDate date);

    /**
     * Находит все проекты, активные на указанную дату.
     *
     * @param date дата для проверки
     * @return список активных проектов
     */
    @Query("SELECT p FROM Project p WHERE " +
            "p.status = 'ACTIVE' AND " +
            "((p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)) OR " +
            "(p.startDate IS NULL AND p.eventDate = :date))")
    List<Project> findActiveProjectsOnDate(@Param("date") LocalDate date);

    /**
     * Находит проекты, которые завершатся в ближайшие N дней.
     * Полезно для уведомлений.
     *
     * @param days количество дней
     * @return список проектов, завершающихся скоро
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' AND " +
            "p.endDate IS NOT NULL AND " +
            "p.endDate BETWEEN CURRENT_DATE AND CURRENT_DATE + :days")
    List<Project> findProjectsEndingSoon(@Param("days") int days);

    /**
     * Находит проекты для отображения в карусели событий на публичной странице.
     * <p>
     * В карусель попадают проекты, которые:
     * <ul>
     *     <li>Имеют принудительное включение (forceShowInCarousel = true) ИЛИ</li>
     *     <li>Имеют дату события в будущем (eventDate > CURRENT_DATE)</li>
     * </ul>
     * Результат сортируется по дате события (от ближайших к более поздним),
     * при этом проекты с forceShowInCarousel = true идут в начале списка.
     * </p>
     *
     * @return список проектов для карусели, отсортированный по приоритету и дате
     */
    @Query("SELECT p FROM Project p WHERE " +
            "p.forceShowInCarousel = true OR " +  // Ручное включение — всегда
            "(p.eventDate IS NOT NULL AND p.eventDate >= CURRENT_DATE) OR " +  // eventDate сегодня или позже
            "(p.startDate IS NOT NULL AND p.endDate IS NOT NULL AND " +
            "   p.startDate <= CURRENT_DATE AND p.endDate >= CURRENT_DATE) OR " +  // проект активен
            "(p.endDate IS NOT NULL AND p.endDate >= CURRENT_DATE) " +  // проект ещё не закончился
            "ORDER BY p.forceShowInCarousel DESC, " +
            "CASE WHEN p.eventDate IS NOT NULL AND p.eventDate >= CURRENT_DATE THEN p.eventDate ELSE p.endDate END ASC")
    List<Project> findProjectsForCarousel();

    /**
     * Находит проекты для отображения в карусели с ограничением по количеству.
     *
     * @param limit максимальное количество проектов
     * @return список проектов для карусели (не больше limit)
     */
    default List<Project> findProjectsForCarousel(int limit) {
        return findProjectsForCarousel().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Находит все уникальные года событий проектов.
     * Используется для фильтра в публичной части.
     *
     * @return список уникальных годов событий, отсортированных по убыванию
     */
    @Query("SELECT DISTINCT YEAR(p.eventDate) FROM Project p WHERE p.eventDate IS NOT NULL ORDER BY YEAR(p.eventDate) DESC")
    List<Integer> findAllDistinctEventYears();


    /**
     * Поиск проектов по названию и описанию с использованием ILIKE.
     * Использует нативный запрос PostgreSQL для регистронезависимого поиска.
     *
     * @param searchTerm поисковый запрос
     * @param pageable параметры пагинации
     * @return страница найденных проектов
     */
    @Query(value = "SELECT * FROM projects p WHERE " +
            "p.title ILIKE CONCAT('%', :searchTerm, '%') OR " +
            "p.short_description ILIKE CONCAT('%', :searchTerm, '%') OR " +
            "p.full_description ILIKE CONCAT('%', :searchTerm, '%') " +
            "ORDER BY p.event_date DESC NULLS LAST, p.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM projects p WHERE " +
                    "p.title ILIKE CONCAT('%', :searchTerm, '%') OR " +
                    "p.short_description ILIKE CONCAT('%', :searchTerm, '%') OR " +
                    "p.full_description ILIKE CONCAT('%', :searchTerm, '%')",
            nativeQuery = true)
    Page<Project> searchByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

}