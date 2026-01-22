package com.community.cms.domain.service.content;

import com.community.cms.domain.model.content.About;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.About.ArticleStatus;
import com.community.cms.domain.repository.content.AboutRepository;
import com.community.cms.domain.repository.content.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления статьями/новостями проектов организации "ЛАДА".
 *
 * <p>Предоставляет бизнес-логику для работы со статьями проектов, включая создание,
 * публикацию, архивацию, поиск и фильтрацию. Статьи используются для блогов проектов,
 * новостных лент и обновлений статуса.</p>
 *
 * <p>Основные возможности:
 * <ul>
 *   <li>Управление жизненным циклом статей (черновик/опубликовано/архив)</li>
 *   <li>Привязка статей к конкретным проектам</li>
 *   <li>Поиск и фильтрация по заголовку, содержанию, автору</li>
 *   <li>Пагинация для списков статей</li>
 *   <li>Управление счетчиком просмотров</li>
 *   <li>SEO оптимизация (мета-теги, slug)</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see About
 * @see AboutRepository
 */
@Service
@Transactional
public class AboutService {

    private final AboutRepository articleRepository;
    private final ProjectRepository projectRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param articleRepository репозиторий для работы со статьями
     * @param projectRepository репозиторий для работы с проектами
     */
    @Autowired
    public AboutService(AboutRepository articleRepository,
                        ProjectRepository projectRepository) {
        this.articleRepository = articleRepository;
        this.projectRepository = projectRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет новую статью.
     *
     * @param article статья для сохранения
     * @return сохраненная статья
     */
    public About save(About article) {
        validateArticle(article);
        return articleRepository.save(article);
    }

    /**
     * Обновляет существующую статью.
     * Очищает кэш для этой статьи.
     *
     * @param article статья для обновления
     * @return обновленная статья
     */
    @Caching(evict = {
            @CacheEvict(value = "article-by-id", key = "#article.id"),
            @CacheEvict(value = "article-by-slug", key = "#article.slug")
    })
    public About update(About article) {
        validateArticle(article);
        return articleRepository.save(article);
    }

    /**
     * Находит статью по ID.
     *
     * @param id идентификатор статьи
     * @return Optional с статьей, если найдена
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "article-by-id", key = "#id", unless = "#result == null")
    public Optional<About> findById(Long id) {
        return articleRepository.findById(id);
    }

    /**
     * Находит опубликованную статью по slug.
     * Используется для публичного доступа.
     *
     * @param slug уникальный идентификатор статьи
     * @return Optional с опубликованной статьей, если найдена
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "article-by-slug", key = "#slug", unless = "#result == null")
    public Optional<About> findPublishedBySlug(String slug) {
        return articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED);
    }

    /**
     * Увеличивает счетчик просмотров статьи на 1.
     *
     * @param article статья для обновления
     */
    @CacheEvict(value = "article-by-id", key = "#article.id")
    public void incrementViewCount(About article) {
        article.incrementViewCount();
        articleRepository.save(article);
    }

    /**
     * Удаляет статью по ID.
     * Очищает кэш для этой статьи.
     *
     * @param id идентификатор статьи для удаления
     */
    @CacheEvict(value = "article-by-id", key = "#id")
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    /**
     * Проверяет существование статьи с указанным slug.
     *
     * @param slug slug для проверки
     * @return true если статья существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return articleRepository.existsBySlug(slug);
    }

    // ================== УПРАВЛЕНИЕ СТАТУСОМ СТАТЬИ ==================

    /**
     * Публикует статью (меняет статус на PUBLISHED).
     * Устанавливает текущую дату как дату публикации.
     *
     * @param article статья для публикации
     * @return опубликованная статья
     */
    @CacheEvict(value = "article-by-id", key = "#article.id")
    public About publish(About article) {
        article.publish();
        return articleRepository.save(article);
    }

    /**
     * Переводит статью в черновик (меняет статус на DRAFT).
     * Сбрасывает дату публикации.
     *
     * @param article статья для перевода в черновик
     * @return статья со статусом черновика
     */
    @CacheEvict(value = "article-by-id", key = "#article.id")
    public About unpublish(About article) {
        article.unpublish();
        return articleRepository.save(article);
    }

    /**
     * Архивирует статью (меняет статус на ARCHIVED).
     *
     * @param article статья для архивации
     * @return архивированная статья
     */
    @CacheEvict(value = "article-by-id", key = "#article.id")
    public About archive(About article) {
        article.archive();
        return articleRepository.save(article);
    }

    /**
     * Публикует статью по ID.
     *
     * @param id идентификатор статьи
     * @return опубликованная статья
     * @throws IllegalArgumentException если статья не найдена
     */
    public About publishById(Long id) {
        About article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Статья с ID " + id + " не найдена"));
        return publish(article);
    }

    // ================== ПОИСК СТАТЕЙ ПО ПРОЕКТУ ==================

    /**
     * Находит все статьи указанного проекта.
     *
     * @param project проект
     * @return список статей проекта
     */
    @Transactional(readOnly = true)
    public List<About> findByProject(Project project) {
        return articleRepository.findByProject(project);
    }

    /**
     * Находит все статьи проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return список статей проекта
     */
    @Transactional(readOnly = true)
    public List<About> findByProjectId(Long projectId) {
        return articleRepository.findByProjectId(projectId);
    }

    /**
     * Находит опубликованные статьи указанного проекта.
     *
     * @param project проект
     * @return список опубликованных статей проекта
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-articles", key = "#project.id + '-published'")
    public List<About> findPublishedByProject(Project project) {
        return articleRepository.findPublishedArticlesByProject(project);
    }

    /**
     * Находит опубликованные статьи по ID проекта.
     *
     * @param projectId ID проекта
     * @return список опубликованных статей проекта
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-articles", key = "#projectId + '-published'")
    public List<About> findPublishedByProjectId(Long projectId) {
        return articleRepository.findPublishedArticlesByProjectId(projectId);
    }

    /**
     * Находит опубликованные статьи проекта с пагинацией.
     *
     * @param project проект
     * @param pageable параметры пагинации
     * @return страница опубликованных статей проекта
     */
    @Transactional(readOnly = true)
    public Page<About> findPublishedByProject(Project project, Pageable pageable) {
        return articleRepository.findPublishedArticlesByProject(project, pageable);
    }

    // ================== ПОИСК И ФИЛЬТРАЦИЯ СТАТЕЙ ==================

    /**
     * Находит все опубликованные статьи.
     *
     * @return список всех опубликованных статей
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "articles", key = "'published'")
    public List<About> findAllPublished() {
        return articleRepository.findPublishedArticles();
    }

    /**
     * Находит все статьи по статусу.
     *
     * @param status статус для фильтрации
     * @return список статей с указанным статусом
     */
    // Для простых случаев без пагинации
    @Transactional(readOnly = true)
    public List<About> findByStatus(ArticleStatus status) {
        return articleRepository.findByStatus(status, Pageable.unpaged()).getContent();
    }

    /**
     * Находит статьи по автору (без учета регистра).
     *
     * @param author автор для поиска
     * @return список статей указанного автора
     */
    @Transactional(readOnly = true)
    public List<About> findByAuthor(String author) {
        return articleRepository.findByAuthorContainingIgnoreCase(author);
    }

    /**
     * Находит статьи по заголовку (без учета регистра).
     *
     * @param title фрагмент заголовка для поиска
     * @return список найденных статей
     */
    @Transactional(readOnly = true)
    public List<About> searchByTitle(String title) {
        return articleRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Комплексный поиск статей по заголовку, содержанию и краткому описанию.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных статей
     */
    @Transactional(readOnly = true)
    public List<About> search(String searchTerm) {
        return articleRepository.searchByTitleOrContent(searchTerm);
    }

    /**
     * Комплексный поиск опубликованных статей.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных опубликованных статей
     */
    @Transactional(readOnly = true)
    public List<About> searchPublished(String searchTerm) {
        return articleRepository.searchPublishedByTitleOrContent(searchTerm);
    }

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит все опубликованные статьи с пагинацией.
     * Используется для новостной ленты.
     *
     * @param pageable параметры пагинации
     * @return страница опубликованных статей
     */
    @Transactional(readOnly = true)
    public Page<About> findPublished(Pageable pageable) {
        return articleRepository.findPublishedArticles(pageable);
    }

    /**
     * Находит все статьи с пагинацией.
     * Используется в админке.
     *
     * @param pageable параметры пагинации
     * @return страница всех статей
     */
    @Transactional(readOnly = true)
    public Page<About> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    /**
     * Находит статьи по статусу с пагинацией.
     *
     * @param status статус для фильтрации
     * @param pageable параметры пагинации
     * @return страница статей с указанным статусом
     */
    @Transactional(readOnly = true)
    public Page<About> findByStatus(ArticleStatus status, Pageable pageable) {
        return articleRepository.findByStatus(status, pageable);
    }

    // ================== ПОЛУЧЕНИЕ ПОПУЛЯРНЫХ И ПОСЛЕДНИХ СТАТЕЙ ==================

    /**
     * Находит последние N опубликованных статей.
     * Используется для виджета "Последние новости".
     *
     * @param limit количество статей
     * @return список последних опубликованных статей
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "articles", key = "'recent-' + #limit")
    public List<About> findRecentArticles(int limit) {
        return articleRepository.findRecentArticles(limit);
    }

    /**
     * Находит последние N опубликованных статей проекта.
     *
     * @param project проект
     * @param limit количество статей
     * @return список последних статей проекта
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "project-articles", key = "#project.id + '-recent-' + #limit")
    public List<About> findRecentArticlesByProject(Project project, int limit) {
        return articleRepository.findRecentArticlesByProject(project, limit);
    }

    /**
     * Находит самую популярную статью (по количеству просмотров).
     *
     * @return Optional с самой популярной статьей, если есть
     */
    @Transactional(readOnly = true)
    public Optional<About> findMostPopularArticle() {
        return articleRepository.findMostPopularArticle();
    }

    /**
     * Находит похожие статьи по содержанию.
     * Используется для рекомендаций в конце статьи.
     *
     * @param project проект
     * @param excludeId ID статьи для исключения
     * @param searchTerms поисковые термины (из текущей статьи)
     * @param limit количество статей
     * @return список похожих статей
     */
    @Transactional(readOnly = true)
    public List<About> findSimilarArticles(Project project, Long excludeId, String searchTerms, int limit) {
        return articleRepository.findSimilarArticles(project, excludeId, searchTerms, limit);
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество статей проекта.
     *
     * @param project проект
     * @return количество статей проекта
     */
    @Transactional(readOnly = true)
    public long countByProject(Project project) {
        return articleRepository.countByProject(project);
    }

    /**
     * Подсчитывает количество статей по ID проекта.
     *
     * @param projectId ID проекта
     * @return количество статей проекта
     */
    @Transactional(readOnly = true)
    public long countByProjectId(Long projectId) {
        return articleRepository.countByProjectId(projectId);
    }

    /**
     * Подсчитывает количество опубликованных статей.
     *
     * @return количество опубликованных статей
     */
    @Transactional(readOnly = true)
    public long countPublished() {
        return articleRepository.countPublishedArticles();
    }

    /**
     * Подсчитывает количество статей по статусу.
     *
     * @param status статус для подсчета
     * @return количество статей с указанным статусом
     */
    @Transactional(readOnly = true)
    public long countByStatus(ArticleStatus status) {
        return articleRepository.countByStatus(status);
    }

    /**
     * Получает общее количество просмотров всех статей.
     *
     * @return общее количество просмотров
     */
    @Transactional(readOnly = true)
    public Long getTotalViewCount() {
        return articleRepository.sumViewCount();
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит запланированные статьи (дата публикации в будущем).
     *
     * @return список запланированных статей
     */
    @Transactional(readOnly = true)
    public List<About> findScheduledArticles() {
        return articleRepository.findScheduledArticles(LocalDateTime.now());
    }

    /**
     * Находит статьи без изображения для превью.
     * Используется для уведомлений в админке.
     *
     * @return список статей без featuredImagePath
     */
    @Transactional(readOnly = true)
    public List<About> findArticlesWithoutFeaturedImage() {
        return articleRepository.findByFeaturedImagePathIsNull();
    }

    /**
     * Находит статьи без краткого описания.
     * Используется для уведомлений в админке.
     *
     * @return список статей без shortDescription
     */
    @Transactional(readOnly = true)
    public List<About> findArticlesWithoutShortDescription() {
        return articleRepository.findWithoutShortDescription();
    }

    /**
     * Находит статьи без meta description.
     * Используется для SEO оптимизации.
     *
     * @return список статей без metaDescription
     */
    @Transactional(readOnly = true)
    public List<About> findArticlesWithoutMetaDescription() {
        return articleRepository.findWithoutMetaDescription();
    }

    // ================== УДАЛЕНИЕ ПО СВЯЗЯМ ==================

    /**
     * Удаляет все статьи указанного проекта.
     *
     * @param project проект
     */
    public void deleteByProject(Project project) {
        articleRepository.deleteByProject(project);
    }

    /**
     * Удаляет все статьи по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void deleteByProjectId(Long projectId) {
        articleRepository.deleteByProjectId(projectId);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для статьи.
     *
     * @param article статья для валидации
     * @throws IllegalArgumentException если статья невалидна
     */
    private void validateArticle(About article) {
        if (article == null) {
            throw new IllegalArgumentException("Статья не может быть null");
        }

        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок статьи обязателен");
        }

        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Содержимое статьи обязательно");
        }

        if (article.getProject() == null) {
            throw new IllegalArgumentException("Статья должна быть привязана к проекту");
        }

        // Проверка что проект существует
        Long projectId = article.getProject().getId();
        if (projectId != null && !projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Проект с ID " + projectId + " не существует");
        }

        // Автоматическая генерация slug если не указан
        if (article.getSlug() == null || article.getSlug().trim().isEmpty()) {
            article.setSlug(generateSlugFromTitle(article.getTitle()));
        }
    }

    /**
     * Генерирует slug из заголовка статьи.
     *
     * @param title заголовок статьи
     * @return сгенерированный slug
     */
    private String generateSlugFromTitle(String title) {
        if (title == null) {
            return "";
        }

        // Транслитерация кириллицы в латиницу
        String transliterated = title.toLowerCase()
                .replace("а", "a").replace("б", "b").replace("в", "v").replace("г", "g")
                .replace("д", "d").replace("е", "e").replace("ё", "yo").replace("ж", "zh")
                .replace("з", "z").replace("и", "i").replace("й", "y").replace("к", "k")
                .replace("л", "l").replace("м", "m").replace("н", "n").replace("о", "o")
                .replace("п", "p").replace("р", "r").replace("с", "s").replace("т", "t")
                .replace("у", "u").replace("ф", "f").replace("х", "h").replace("ц", "ts")
                .replace("ч", "ch").replace("ш", "sh").replace("щ", "sch").replace("ъ", "")
                .replace("ы", "y").replace("ь", "").replace("э", "e").replace("ю", "yu")
                .replace("я", "ya");

        // Оставляем только латинские буквы, цифры и дефисы
        return transliterated.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    /**
     * Создает новую статью для проекта.
     *
     * @param project проект
     * @param title заголовок статьи
     * @param content содержимое статьи
     * @return созданная статья
     */
    public About createArticleForProject(Project project, String title, String content) {
        About article = new About(project, title, content);
        return save(article);
    }

    /**
     * Очищает кэш статей.
     * Используется при массовых обновлениях.
     */
    @CacheEvict(value = {"articles", "article-by-id", "article-by-slug", "project-articles"}, allEntries = true)
    public void clearAllCache() {
        // Метод аннотирован @CacheEvict, кэш будет очищен автоматически
    }
}