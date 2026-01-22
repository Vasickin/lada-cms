package com.community.cms.domain.service.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.Project.ProjectStatus;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.repository.content.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления проектами организации "ЛАДА".
 *
 * <p>Предоставляет бизнес-логику для работы с проектами, включая создание,
 * обновление, удаление, поиск и фильтрацию. Интегрирует кэширование для
 * повышения производительности публичной части сайта.</p>
 *
 * <p>Основные возможности:
 * <ul>
 *   <li>Полное управление жизненным циклом проектов</li>
 *   <li>Гибкая фильтрация по статусу, категории, датам</li>
 *   <li>Поиск по названию и описанию</li>
 *   <li>Пагинация для списков проектов</li>
 *   <li>Автоматическое кэширование часто запрашиваемых данных</li>
 *   <li>Проверка бизнес-правил и валидация</li>
 * </ul>
 *
 * <p>Кэширование:
 * <ul>
 *   <li>projects-list: кэширует списки проектов</li>
 *   <li>project-by-id: кэширует отдельные проекты по ID</li>
 *   <li>project-by-slug: кэширует проекты по slug для публичного доступа</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Project
 * @see ProjectRepository
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectRepository репозиторий для работы с проектами
     */
    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет новый проект.
     * Очищает кэш списков проектов.
     *
     * @param project проект для сохранения
     * @return сохраненный проект
     */
    @Caching(evict = {
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true),
            @CacheEvict(value = "project-categories", allEntries = true)
    })
    public Project save(Project project) {
        validateProject(project);
        return projectRepository.save(project);
    }

    /**
     * Обновляет существующий проект.
     * Очищает кэш для этого проекта и списков.
     *
     * @param project проект для обновления
     * @return обновленный проект
     */
    @Caching(evict = {
            @CacheEvict(value = "project-by-id", key = "#project.id"),
            @CacheEvict(value = "project-by-slug", key = "#project.slug"),
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true),
            @CacheEvict(value = "project-categories", allEntries = true)
    })
    public Project update(Project project) {
        validateProject(project);
        return projectRepository.save(project);
    }

    /**
     * Находит проект по ID.
     * Использует кэширование для повторных запросов.
     *
     * @param id идентификатор проекта
     * @return Optional с проектом, если найден
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-by-id", key = "#id", unless = "#result == null")
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Находит проект по slug для публичного доступа.
     * Использует кэширование и проверяет активность проекта.
     *
     * @param slug уникальный идентификатор проекта
     * @return Optional с проектом, если найден и активен
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-by-slug", key = "#slug", unless = "#result == null")
    public Optional<Project> findBySlugForPublic(String slug) {
        return projectRepository.findBySlugAndStatus(slug, ProjectStatus.ACTIVE);
    }

    /**
     * Находит проект по slug (админский доступ).
     * Не кэшируется для админки.
     *
     * @param slug уникальный идентификатор проекта
     * @return Optional с проектом, если найден
     */
    @Transactional(readOnly = true)
    public Optional<Project> findBySlug(String slug) {
        return projectRepository.findBySlug(slug);
    }

    /**
     * Удаляет проект по ID.
     * Очищает все связанные кэши.
     * <p>
     * ВНИМАНИЕ: Перед удалением проекта очищаются все связи ManyToMany
     * с партнерами и командой, чтобы избежать нарушения ограничений внешнего ключа.
     * </p>
     *
     * @param id идентификатор проекта для удаления
     * @throws EntityNotFoundException если проект не найден
     */
    @Caching(evict = {
            @CacheEvict(value = "project-by-id", key = "#id"),
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true),
            @CacheEvict(value = "project-categories", allEntries = true)
    })
    public void deleteById(Long id) {
        // 1. Находим проект
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Проект не найден с ID: " + id));

        // 2. Получаем slug для очистки кэша (до изменения проекта)
        String slug = project.getSlug();

        // 3. Очищаем связь с партнерами (основная проблема!)
        if (project.getPartners() != null && !project.getPartners().isEmpty()) {
            // Используем итератор для безопасного удаления
            Iterator<Partner> partnerIterator = project.getPartners().iterator();
            while (partnerIterator.hasNext()) {
                Partner partner = partnerIterator.next();
                // Удаляем проект из коллекции партнера
                if (partner.getProjects() != null) {
                    partner.getProjects().remove(project);
                }
                // Удаляем партнера из коллекции проекта
                partnerIterator.remove();
            }
        }

        // 4. Очищаем связь с командой
        if (project.getTeamMembers() != null && !project.getTeamMembers().isEmpty()) {
            Iterator<TeamMember> memberIterator = project.getTeamMembers().iterator();
            while (memberIterator.hasNext()) {
                TeamMember member = memberIterator.next();
                // Удаляем проект из коллекции члена команды
                if (member.getProjects() != null) {
                    member.getProjects().remove(project);
                }
                // Удаляем члена команды из коллекции проекта
                memberIterator.remove();
            }
        }

        // 5. Очищаем ключевые фото (ElementCollection)
        if (project.getKeyPhotoIds() != null) {
            project.getKeyPhotoIds().clear();
        }

        // 6. Сохраняем проект с очищенными коллекциями
        projectRepository.save(project);

        // 7. Очищаем кэш по slug (если нужно)
        // @CacheEvict уже обработает это

        // 8. Теперь удаляем сам проект
        projectRepository.deleteById(id);
    }

    /**
     * Проверяет существование проекта с указанным slug.
     *
     * @param slug slug для проверки
     * @return true если проект существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return projectRepository.existsBySlug(slug);
    }

    // ================== СПИСКИ ПРОЕКТОВ (С КЭШИРОВАНИЕМ) ==================

    /**
     * Находит все активные проекты.
     * Используется для публичной части сайта.
     *
     * @return список активных проектов
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "projects-list", key = "'active'")
    public List<Project> findAllActive() {
        return projectRepository.findByStatus(ProjectStatus.ACTIVE);
    }

    /**
     * Находит все проекты (без фильтрации).
     * Используется в админке.
     *
     * @return список всех проектов
     */
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /**
     * Находит все проекты с пагинацией.
     * Используется для админки с большим количеством проектов.
     *
     * @param pageable параметры пагинации
     * @return страница проектов
     */
    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    /**
     * Находит проекты по статусу.
     *
     * @param status статус для фильтрации
     * @return список проектов с указанным статусом
     */
    @Transactional(readOnly = true)
    public List<Project> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    /**
     * Находит активные проекты по категории.
     * Используется для фильтрации на сайте.
     *
     * @param category категория для фильтрации
     * @return список активных проектов указанной категории
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "projects-by-category", key = "#category")
    public List<Project> findActiveByCategory(String category) {
        return projectRepository.findByCategoryAndStatus(category, ProjectStatus.ACTIVE);
    }

    // ================== ФИЛЬТРАЦИЯ И ПОИСК ==================

    /**
     * Находит проекты по названию (поиск без учета регистра).
     *
     * @param title фрагмент названия для поиска
     * @return список найденных проектов
     */
    @Transactional(readOnly = true)
    public List<Project> searchByTitle(String title) {
        return projectRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Комплексный поиск проектов по названию и описанию.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных проектов
     */
    @Transactional(readOnly = true)
    public List<Project> search(String searchTerm) {
        return projectRepository.searchByTitleOrDescription(searchTerm);
    }

    /**
     * Находит проекты с событиями в будущем.
     * Используется для календаря мероприятий.
     *
     * @param date текущая дата
     * @return список проектов с будущими событиями
     */
    @Transactional(readOnly = true)
    public List<Project> findUpcomingEvents(LocalDate date) {
        return projectRepository.findUpcomingEvents(date);
    }

    /**
     * Находит проекты с событиями в прошлом.
     * Используется для архива мероприятий.
     *
     * @param date текущая дата
     * @return список проектов с прошедшими событиями
     */
    @Transactional(readOnly = true)
    public List<Project> findPastEvents(LocalDate date) {
        return projectRepository.findPastEvents(date);
    }

    /**
     * Находит проекты, активные на указанную дату.
     *
     * @param date дата для проверки
     * @return список проектов активных на указанную дату
     */
    @Transactional(readOnly = true)
    public List<Project> findActiveOnDate(LocalDate date) {
        return projectRepository.findActiveOnDate(date);
    }

    /**
     * Находит все уникальные категории проектов (нормализованные).
     * Используется для проверки уникальности.
     *
     * @return множество нормализованных названий категорий
     */
    @Transactional(readOnly = true)
    public Set<String> findAllNormalizedCategories() {
        List<String> categories = projectRepository.findAllDistinctCategories();
        return categories.stream()
                .map(this::normalizeCategoryName)
                .collect(Collectors.toSet());
    }

    /**
     * Нормализует название категории для сравнения.
     * Убирает пробелы, приводит к нижнему регистру.
     *
     * @param categoryName исходное название категории
     * @return нормализованное название
     */
    private String normalizeCategoryName(String categoryName) {
        if (categoryName == null) {
            return "";
        }
        return categoryName.trim()
                .toLowerCase()
                .replaceAll("\\s+", " "); // заменяем множественные пробелы на один
    }

    // ================== ПАГИНАЦИЯ С ФИЛьТРАЦИЕЙ ==================

    /**
     * Находит активные проекты с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница активных проектов
     */
    @Transactional(readOnly = true)
    public Page<Project> findActive(Pageable pageable) {
        return projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);
    }

    /**
     * Находит проекты по статусу с пагинацией.
     *
     * @param status статус для фильтрации
     * @param pageable параметры пагинации
     * @return страница проектов с указанным статусом
     */
    @Transactional(readOnly = true)
    public Page<Project> findByStatus(ProjectStatus status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable);
    }

    /**
     * Находит проекты по категории с пагинацией.
     *
     * @param category категория для фильтрации
     * @param pageable параметры пагинации
     * @return страница проектов указанной категории
     */
    @Transactional(readOnly = true)
    public Page<Project> findByCategory(String category, Pageable pageable) {
        return projectRepository.findByCategory(category, pageable);
    }

    /**
     * Находит активные проекты по категории с пагинацией.
     *
     * @param category категория для фильтрации
     * @param pageable параметры пагинации
     * @return страница активных проектов указанной категории
     */
    @Transactional(readOnly = true)
    public Page<Project> findActiveByCategory(String category, Pageable pageable) {
        return projectRepository.findByStatusAndCategory(ProjectStatus.ACTIVE, category, pageable);
    }

    // ================== ПОЛУЧЕНИЕ УНИКАЛЬНЫХ КАТЕГОРИЙ ==================

    /**
     * Находит все уникальные категории проектов.
     * Используется для фильтров на сайте.
     *
     * @return список уникальных категорий
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-categories")
    public List<String> findAllDistinctCategories() {
        return projectRepository.findAllDistinctCategories();
    }

    // ================== ПОЛУЧЕНИЕ ПОПУЛЯРНЫХ И ИЗБРАННЫХ ПРОЕКТОВ ==================

    // Добавить эти методы в ProjectService.java:

    /**
     * Подсчитывает общее количество проектов.
     * Используется для статистики на сайте.
     *
     * @return общее количество проектов
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return projectRepository.count();
    }

    /**
     * Подсчитывает количество ежегодных проектов.
     *
     * @return количество ежегодных проектов
     */
    @Transactional(readOnly = true)
    public long countAnnual() {
        return countByStatus(ProjectStatus.ANNUAL);
    }

    /**
     * Подсчитывает количество архивных проектов.
     *
     * @return количество архивных проектов
     */
    @Transactional(readOnly = true)
    public long countArchived() {
        return countByStatus(ProjectStatus.ARCHIVED);
    }

    /**
     * Подсчитывает количество неархивных проектов (активные + ежегодные).
     *
     * @return количество неархивных проектов
     */
    @Transactional(readOnly = true)
    public long countNonArchived() {
        return countByStatus(ProjectStatus.ACTIVE) + countByStatus(ProjectStatus.ANNUAL);
    }

    /**
     * Находит последние N активных проектов.
     * Используется для главной страницы.
     *
     * @param limit количество проектов
     * @return список последних проектов
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "projects-list", key = "'recent-' + #limit")
    public List<Project> findRecentProjects(int limit) {
        return projectRepository.findRecentProjects(limit);
    }

    /**
     * Находит избранные проекты (с обложкой).
     * Используется для слайдера на главной странице.
     *
     * @param limit количество проектов
     * @return список избранных проектов
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "projects-list", key = "'featured-' + #limit")
    public List<Project> findFeaturedProjects(int limit) {
        return projectRepository.findFeaturedProjects(limit);
    }

    /**
     * Находит похожие проекты по категории.
     * Используется для секции "Похожие проекты".
     *
     * @param category категория для поиска
     * @param excludeId ID проекта для исключения
     * @param limit количество проектов
     * @return список похожих проектов
     */
    @Transactional(readOnly = true)
    public List<Project> findSimilarProjects(String category, Long excludeId, int limit) {
        return projectRepository.findSimilarProjects(category, excludeId, limit);
    }

    @Transactional(readOnly = true)
    public List<Project> findSimilarProjectsAllStatuses(String category, Long excludeId, int limit) {
        return projectRepository.findSimilarProjectsAllStatuses(category, excludeId, limit);
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество проектов по статусу.
     *
     * @param status статус для подсчета
     * @return количество проектов с указанным статусом
     */
    @Transactional(readOnly = true)
    public long countByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    /**
     * Подсчитывает количество активных проектов.
     *
     * @return количество активных проектов
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return projectRepository.countByStatus(ProjectStatus.ACTIVE);
    }

    /**
     * Подсчитывает количество проектов по категории.
     *
     * @param category категория для подсчета
     * @return количество проектов указанной категории
     */
    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return projectRepository.countByCategory(category);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Активирует проект (меняет статус на ACTIVE).
     * Очищает кэш для этого проекта.
     *
     * @param project проект для активации
     * @return активированный проект
     */
    @Caching(evict = {
            @CacheEvict(value = "project-by-id", key = "#project.id"),
            @CacheEvict(value = "project-by-slug", key = "#project.slug"),
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true)
    })
    public Project activate(Project project) {
        project.setStatus(ProjectStatus.ACTIVE);
        return projectRepository.save(project);
    }

    /**
     * Архивирует проект (меняет статус на ARCHIVED).
     * Очищает кэш для этого проекта.
     *
     * @param project проект для архивации
     * @return архивированный проект
     */
    @Caching(evict = {
            @CacheEvict(value = "project-by-id", key = "#project.id"),
            @CacheEvict(value = "project-by-slug", key = "#project.slug"),
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true)
    })
    public Project archive(Project project) {
        project.setStatus(ProjectStatus.ARCHIVED);
        return projectRepository.save(project);
    }

    /**
     * Помечает проект как ежегодный (меняет статус на ANNUAL).
     * Очищает кэш для этого проекта.
     *
     * @param project проект для изменения
     * @return измененный проект
     */
    @Caching(evict = {
            @CacheEvict(value = "project-by-id", key = "#project.id"),
            @CacheEvict(value = "project-by-slug", key = "#project.slug"),
            @CacheEvict(value = {"projects-list", "projects-by-category"}, allEntries = true)
    })
    public Project markAsAnnual(Project project) {
        project.setStatus(ProjectStatus.ANNUAL);
        return projectRepository.save(project);
    }

    /**
     * Проверяет бизнес-правила для проекта.
     *
     * @param project проект для валидации
     * @throws IllegalArgumentException если проект невалиден
     */
    private void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Проект не может быть null");
        }

        if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название проекта обязательно");
        }

        if (project.getSlug() == null || project.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Slug проекта обязателен");
        }

        if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Категория проекта обязательна");
        }

        // Проверка дат
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getStartDate().isAfter(project.getEndDate())) {
                throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
            }
        }
    }

    /**
     * Очищает весь кэш проектов.
     * Используется при массовых обновлениях.
     */
    @CacheEvict(value = {"projects-list", "project-by-id", "project-by-slug", "projects-by-category", "project-categories"}, allEntries = true)
    public void clearAllCache() {
        // Метод аннотирован @CacheEvict, кэш будет очищен автоматически
    }

    /**
     * Находит проекты по диапазону дат событий.
     * Используется для календаря мероприятий.
     *
     * @param startDate начало периода
     * @param endDate конец периода
     * @return список проектов с событиями в указанный период
     */
    @Transactional(readOnly = true)
    public List<Project> findByEventDateBetween(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findByEventDateBetween(startDate, endDate);
    }
}