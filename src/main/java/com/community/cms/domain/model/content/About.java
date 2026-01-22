package com.community.cms.domain.model.content;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность статьи/новости проекта организации "ЛАДА".
 *
 * <p>Представляет статью, новость или обновление, связанное с конкретным проектом.
 * Может использоваться для блога проекта, новостной ленты или обновлений статуса.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Заголовок и содержимое статьи с поддержкой HTML</li>
 *   <li>Дата публикации и обновления</li>
 *   <li>Изображение для превью статьи</li>
 *   <li>Статус публикации (черновик/опубликовано)</li>
 *   <li>SEO мета-данные для каждой статьи</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "project_articles")
public class About {

    /**
     * Статусы статьи для управления публикацией.
     * Article statuses for publication management.
     */
    public enum ArticleStatus {
        DRAFT("Черновик", "Draft"),
        PUBLISHED("Опубликовано", "Published"),
        ARCHIVED("Архив", "Archived");

        private final String nameRu;
        private final String nameEn;

        ArticleStatus(String nameRu, String nameEn) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
        }

        public String getNameRu() {
            return nameRu;
        }

        public String getNameEn() {
            return nameEn;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================== СВЯЗИ ==================

    /**
     * Проект, к которому относится статья.
     * Одна статья принадлежит только одному проекту.
     */
    @NotNull(message = "Проект обязателен / Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ================== ОСНОВНЫЕ ДАННЫЕ ==================

    /**
     * Заголовок статьи.
     * Должен быть кратким и информативным.
     */
    @NotBlank(message = "Заголовок статьи обязателен / Article title is required")
    @Size(min = 3, max = 255, message = "Заголовок должен быть от 3 до 255 символов / Title must be between 3 and 255 characters")
    @Column(nullable = false)
    private String title;

    /**
     * Содержимое статьи.
     * Поддерживает HTML разметку для форматирования текста.
     */
    @NotBlank(message = "Содержимое статьи обязательно / Article content is required")
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Краткое описание/аннотация статьи.
     * Используется для превью в списках статей.
     */
    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов / Short description must not exceed 500 characters")
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    /**
     * Путь к изображению для превью статьи.
     * Отображается в списках статей и соцсетях.
     */
    @Column(name = "featured_image_path", length = 500)
    private String featuredImagePath;

    /**
     * Автор статьи (опционально).
     * Если не указан, используется "Команда проекта".
     */
    @Size(max = 100, message = "Автор не должен превышать 100 символов / Author must not exceed 100 characters")
    private String author;

    // ================== СТАТУС И ДАТЫ ==================

    /**
     * Статус статьи.
     * Определяет видимость статьи на сайте.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    /**
     * Дата и время публикации статьи.
     * Для черновиков может быть null.
     */
    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    /**
     * Дата и время создания статьи.
     * Заполняется автоматически.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления статьи.
     * Обновляется автоматически при изменениях.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================== SEO МЕТА-ДАННЫЕ ==================

    /**
     * Meta title для SEO.
     * Если пустой, используется заголовок статьи.
     */
    @Size(max = 255, message = "Meta title не должен превышать 255 символов / Meta title must not exceed 255 characters")
    @Column(name = "meta_title")
    private String metaTitle;

    /**
     * Meta description для SEO.
     * Используется в description теге поисковых систем.
     */
    @Size(max = 500, message = "Meta description не должен превышать 500 символов / Meta description must not exceed 500 characters")
    @Column(name = "meta_description")
    private String metaDescription;

    /**
     * Meta keywords для SEO.
     * Ключевые слова через запятую.
     */
    @Size(max = 500, message = "Meta keywords не должны превышать 500 символов / Meta keywords must not exceed 500 characters")
    @Column(name = "meta_keywords")
    private String metaKeywords;

    /**
     * Путь к OG-изображению для соцсетей.
     * Если пустой, используется featuredImagePath.
     */
    @Column(name = "og_image_path", length = 500)
    private String ogImagePath;

    /**
     * Slug статьи для SEO-оптимизированных URL.
     * Генерируется из заголовка, но может быть переопределен.
     */
    @Size(max = 200, message = "Slug не должен превышать 200 символов / Slug must not exceed 200 characters")
    @Column(unique = true)
    private String slug;

    /**
     * Количество просмотров статьи.
     * Увеличивается при каждом просмотре.
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * Порядок сортировки в списке статей.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Конструктор по умолчанию.
     */
    public About() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param project проект
     * @param title заголовок статьи
     * @param content содержимое статьи
     */
    public About(Project project, String title, String content) {
        this();
        this.project = project;
        this.title = title;
        this.content = content;
        this.status = ArticleStatus.DRAFT;
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFeaturedImagePath() {
        return featuredImagePath;
    }

    public void setFeaturedImagePath(String featuredImagePath) {
        this.featuredImagePath = featuredImagePath;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getOgImagePath() {
        return ogImagePath;
    }

    public void setOgImagePath(String ogImagePath) {
        this.ogImagePath = ogImagePath;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет опубликована ли статья.
     *
     * @return true если статус PUBLISHED и publishedDate не в будущем
     */
    public boolean isPublished() {
        if (status != ArticleStatus.PUBLISHED) {
            return false;
        }

        // Проверяем что дата публикации не в будущем
        if (publishedDate != null && publishedDate.isAfter(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    /**
     * Проверяет является ли статья черновиком.
     *
     * @return true если статус DRAFT
     */
    public boolean isDraft() {
        return status == ArticleStatus.DRAFT;
    }

    /**
     * Проверяет является ли статья архивной.
     *
     * @return true если статус ARCHIVED
     */
    public boolean isArchived() {
        return status == ArticleStatus.ARCHIVED;
    }

    /**
     * Публикует статью.
     * Устанавливает статус PUBLISHED и текущую дату как publishedDate.
     */
    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
        this.publishedDate = LocalDateTime.now();
    }

    /**
     * Переводит статью в черновик.
     * Сбрасывает дату публикации.
     */
    public void unpublish() {
        this.status = ArticleStatus.DRAFT;
        this.publishedDate = null;
    }

    /**
     * Архивирует статью.
     */
    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }

    /**
     * Увеличивает счетчик просмотров на 1.
     */
    public void incrementViewCount() {
        if (viewCount == null) {
            viewCount = 0;
        }
        viewCount++;
    }

    /**
     * Получает meta title для SEO.
     * Если metaTitle не задан, возвращает заголовок статьи.
     *
     * @return meta title для использования
     */
    public String getEffectiveMetaTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : title;
    }

    /**
     * Получает meta description для SEO.
     * Если metaDescription не задан, возвращает короткое описание или начало контента.
     *
     * @return meta description для использования
     */
    public String getEffectiveMetaDescription() {
        if (metaDescription != null && !metaDescription.trim().isEmpty()) {
            return metaDescription;
        }
        if (shortDescription != null && !shortDescription.trim().isEmpty()) {
            return shortDescription;
        }
        // Берем первые 150 символов контента (без HTML тегов)
        String plainContent = content.replaceAll("<[^>]*>", "").trim();
        if (plainContent.length() > 150) {
            return plainContent.substring(0, 147) + "...";
        }
        return plainContent;
    }

    /**
     * Получает путь к OG изображению.
     * Если ogImagePath не задан, возвращает featuredImagePath.
     *
     * @return путь к OG изображению
     */
    public String getEffectiveOgImagePath() {
        return ogImagePath != null && !ogImagePath.trim().isEmpty() ? ogImagePath : featuredImagePath;
    }

    /**
     * Получает автора для отображения.
     * Если автор не указан, возвращает "Команда проекта".
     *
     * @return отображаемое имя автора
     */
    public String getDisplayAuthor() {
        return author != null && !author.trim().isEmpty() ? author : "Команда проекта";
    }

    /**
     * Проверяет имеет ли статья изображение для превью.
     *
     * @return true если featuredImagePath не пустой, иначе false
     */
    public boolean hasFeaturedImage() {
        return featuredImagePath != null && !featuredImagePath.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли статья короткое описание.
     *
     * @return true если shortDescription не пустой, иначе false
     */
    public boolean hasShortDescription() {
        return shortDescription != null && !shortDescription.trim().isEmpty();
    }

    /**
     * Получает отображаемую дату статьи.
     * Приоритет: publishedDate → createdAt.
     *
     * @return дата для отображения
     */
    public LocalDateTime getDisplayDate() {
        return publishedDate != null ? publishedDate : createdAt;
    }

    /**
     * Получает ID проекта для быстрого доступа.
     *
     * @return ID проекта или null если проект не установлен
     */
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    /**
     * Генерирует читаемый URL для статьи.
     * Использует slug или генерирует из заголовка.
     *
     * @return URL путь к статье
     */
    public String getArticleUrl() {
        if (slug != null && !slug.trim().isEmpty()) {
            return "/projects/" + project.getSlug() + "/articles/" + slug;
        }

        // Генерируем slug из заголовка
        String generatedSlug = title.toLowerCase()
                .replaceAll("[^a-zа-я0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        return "/projects/" + project.getSlug() + "/articles/" + generatedSlug + "-" + id;
    }

    /**
     * Получает первые 200 символов контента без HTML тегов.
     * Используется для превью статьи.
     *
     * @return очищенный текст для превью
     */
    public String getContentPreview() {
        if (content == null) {
            return "";
        }

        // Удаляем HTML теги
        String plainContent = content.replaceAll("<[^>]*>", "").trim();

        // Берем первые 200 символов
        if (plainContent.length() > 200) {
            return plainContent.substring(0, 197) + "...";
        }

        return plainContent;
    }

    @Override
    public String toString() {
        return "About{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", publishedDate=" + publishedDate +
                ", viewCount=" + viewCount +
                ", projectId=" + getProjectId() +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     */
    @PrePersist
    @PreUpdate
    protected void validate() {
        if (status == null) {
            status = ArticleStatus.DRAFT;
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        // Если статья публикуется впервые, устанавливаем дату публикации
        if (status == ArticleStatus.PUBLISHED && publishedDate == null) {
            publishedDate = LocalDateTime.now();
        }
    }
}
