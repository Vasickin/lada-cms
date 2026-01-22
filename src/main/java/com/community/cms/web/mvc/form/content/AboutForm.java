package com.community.cms.web.mvc.form.content;

import com.community.cms.domain.model.content.About;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования статей проектов.
 *
 * <p>Используется в административной панели для валидации данных статьи проекта
 * перед сохранением в базу данных. Отделяет слой представления от сущностей JPA.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Валидация входных данных (обязательные поля, размеры)</li>
 *   <li>Защита от Over-Posting атак</li>
 *   <li>Преобразование дат в правильный формат</li>
 *   <li>Ограничение доступных для редактирования полей</li>
 *   <li>Генерация slug из заголовка при необходимости</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see About
 */
public class AboutForm {

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * ID статьи.
     * Используется только при редактировании существующей статьи.
     */
    private Long id;

    // ================== СВЯЗЬ С ПРОЕКТОМ ==================

    /**
     * ID проекта, к которому относится статья.
     * Обязательное поле.
     */
    @NotNull(message = "ID проекта обязателен / Project ID is required")
    private Long projectId;

    // ================== ОСНОВНЫЕ ДАННЫЕ СТАТЬИ ==================

    /**
     * Заголовок статьи.
     * Обязательное поле, от 3 до 255 символов.
     * Должен быть кратким и информативным.
     */
    @NotBlank(message = "Заголовок статьи обязателен / Article title is required")
    @Size(min = 3, max = 255, message = "Заголовок должен быть от 3 до 255 символов / Title must be between 3 and 255 characters")
    private String title;

    /**
     * Содержимое статьи.
     * Обязательное поле. Поддерживает HTML разметку для форматирования текста.
     * Используется для отображения полного текста статьи.
     */
    @NotBlank(message = "Содержимое статьи обязательно / Article content is required")
    private String content;

    /**
     * Краткое описание/аннотация статьи.
     * Максимум 500 символов. Используется для превью в списках статей
     * и для SEO описания, если metaDescription не указан.
     */
    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов / Short description must not exceed 500 characters")
    private String shortDescription;

    // ================== ИЗОБРАЖЕНИЕ ДЛЯ ПРЕВЬЮ ==================

    /**
     * Путь к изображению для превью статьи.
     * Используется в списках статей и карточках.
     * Если не указан, статья будет отображаться без изображения.
     */
    private String featuredImagePath;

    // ================== ИНФОРМАЦИЯ ОБ АВТОРЕ ==================

    /**
     * Автор статьи (опционально).
     * Максимум 100 символов.
     * Если не указан, используется "Команда проекта".
     */
    @Size(max = 100, message = "Автор не должен превышать 100 символов / Author must not exceed 100 characters")
    private String author;

    // ================== СТАТУС И ДАТЫ ПУБЛИКАЦИИ ==================

    /**
     * Статус статьи.
     * Определяет видимость статьи на сайте:
     * DRAFT - черновик (только в админке),
     * PUBLISHED - опубликовано (видно на сайте),
     * ARCHIVED - архив (скрыто с сайта).
     */
    private About.ArticleStatus status = About.ArticleStatus.DRAFT;

    /**
     * Дата и время публикации статьи.
     * Формат: "yyyy-MM-dd'T'HH:mm" (например, "2025-01-15T14:30").
     * Используется для запланированной публикации.
     * Если null и статус PUBLISHED, используется текущая дата.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime publishedDate;

    // ================== SEO ОПТИМИЗАЦИЯ ==================

    /**
     * Meta title для SEO.
     * Максимум 255 символов.
     * Если не указан, используется заголовок статьи.
     */
    @Size(max = 255, message = "Meta title не должен превышать 255 символов / Meta title must not exceed 255 characters")
    private String metaTitle;

    /**
     * Meta description для SEO.
     * Максимум 500 символов.
     * Используется поисковыми системами для сниппетов.
     * Если не указан, используется shortDescription или начало content.
     */
    @Size(max = 500, message = "Meta description не должен превышать 500 символов / Meta description must not exceed 500 characters")
    private String metaDescription;

    /**
     * Meta keywords для SEO.
     * Максимум 500 символов.
     * Ключевые слова через запятую.
     */
    @Size(max = 500, message = "Meta keywords не должны превышать 500 символов / Meta keywords must not exceed 500 characters")
    private String metaKeywords;

    /**
     * Путь к OG-изображению для соцсетей.
     * Если не указан, используется featuredImagePath.
     */
    private String ogImagePath;

    // ================== URL СТАТЬИ ==================

    /**
     * Slug статьи для SEO-оптимизированных URL.
     * Максимум 200 символов.
     * Должен содержать только латинские буквы, цифры и дефисы.
     * Если не указан, генерируется из заголовка.
     */
    @Size(max = 200, message = "Slug не должен превышать 200 символов / Slug must not exceed 200 characters")
    private String slug;

    // ================== НАСТРОЙКИ СОРТИРОВКИ ==================

    /**
     * Порядок сортировки в списке статей.
     * Меньшее значение = выше в списке.
     * По умолчанию: 0.
     */
    private Integer sortOrder = 0;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public AboutForm() {
        this.status = About.ArticleStatus.DRAFT;
        this.sortOrder = 0;
    }

    /**
     * Конструктор на основе существующей статьи проекта.
     * Используется для редактирования статьи.
     *
     * @param article существующая статья проекта
     */
    public AboutForm(About article) {
        this();
        this.id = article.getId();
        this.projectId = article.getProject() != null ? article.getProject().getId() : null;
        this.title = article.getTitle();
        this.content = article.getContent();
        this.shortDescription = article.getShortDescription();
        this.featuredImagePath = article.getFeaturedImagePath();
        this.author = article.getAuthor();
        this.status = article.getStatus();
        this.publishedDate = article.getPublishedDate();
        this.metaTitle = article.getMetaTitle();
        this.metaDescription = article.getMetaDescription();
        this.metaKeywords = article.getMetaKeywords();
        this.ogImagePath = article.getOgImagePath();
        this.slug = article.getSlug();
        this.sortOrder = article.getSortOrder();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public About.ArticleStatus getStatus() {
        return status;
    }

    public void setStatus(About.ArticleStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, является ли статья черновиком.
     *
     * @return true если статус DRAFT, иначе false
     */
    public boolean isDraft() {
        return status == About.ArticleStatus.DRAFT;
    }

    /**
     * Проверяет, опубликована ли статья.
     *
     * @return true если статус PUBLISHED, иначе false
     */
    public boolean isPublished() {
        return status == About.ArticleStatus.PUBLISHED;
    }

    /**
     * Проверяет, является ли статья архивной.
     *
     * @return true если статус ARCHIVED, иначе false
     */
    public boolean isArchived() {
        return status == About.ArticleStatus.ARCHIVED;
    }

    /**
     * Проверяет, имеет ли статья изображение для превью.
     *
     * @return true если featuredImagePath не пустой, иначе false
     */
    public boolean hasFeaturedImage() {
        return featuredImagePath != null && !featuredImagePath.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли статья краткое описание.
     *
     * @return true если shortDescription не пустой, иначе false
     */
    public boolean hasShortDescription() {
        return shortDescription != null && !shortDescription.trim().isEmpty();
    }

    /**
     * Проверяет, является ли статья запланированной (дата публикации в будущем).
     *
     * @return true если статус PUBLISHED и publishedDate в будущем, иначе false
     */
    public boolean isScheduled() {
        if (status != About.ArticleStatus.PUBLISHED || publishedDate == null) {
            return false;
        }
        return publishedDate.isAfter(LocalDateTime.now());
    }

    /**
     * Получает отображаемое имя автора.
     * Если автор не указан, возвращает "Команда проекта".
     *
     * @return отображаемое имя автора
     */
    public String getDisplayAuthor() {
        return author != null && !author.trim().isEmpty() ? author : "Команда проекта";
    }

    /**
     * Получает дату для отображения.
     * Приоритет: publishedDate → null (для черновиков).
     *
     * @return дата для отображения или null
     */
    public LocalDateTime getDisplayDate() {
        return publishedDate;
    }

    /**
     * Генерирует читаемый URL для статьи.
     * Используется для предпросмотра ссылки в админке.
     *
     * @param projectSlug slug проекта
     * @return URL путь к статье
     */
    public String generateArticleUrl(String projectSlug) {
        if (slug != null && !slug.trim().isEmpty()) {
            return "/projects/" + projectSlug + "/articles/" + slug;
        }

        // Генерируем slug из заголовка, если не указан
        String generatedSlug = generateSlugFromTitle();
        return "/projects/" + projectSlug + "/articles/" + generatedSlug + (id != null ? "-" + id : "");
    }

    /**
     * Генерирует slug из заголовка статьи.
     * Используется, если slug не указан вручную.
     *
     * @return сгенерированный slug
     */
    private String generateSlugFromTitle() {
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
     * Преобразует AboutForm в сущность About.
     * Не заполняет связь с проектом (нужно установить отдельно).
     *
     * @return сущность About с заполненными базовыми полями
     */
    public About toEntity() {
        About article = new About();
        article.setId(this.id);
        article.setTitle(this.title);
        article.setContent(this.content);
        article.setShortDescription(this.shortDescription);
        article.setFeaturedImagePath(this.featuredImagePath);
        article.setAuthor(this.author);
        article.setStatus(this.status);
        article.setPublishedDate(this.publishedDate);
        article.setMetaTitle(this.metaTitle);
        article.setMetaDescription(this.metaDescription);
        article.setMetaKeywords(this.metaKeywords);
        article.setOgImagePath(this.ogImagePath);
        article.setSlug(this.slug);
        article.setSortOrder(this.sortOrder);

        // Проект устанавливается отдельно по projectId
        // article.setProject(project);

        return article;
    }

    /**
     * Обновляет существующую сущность About данными из формы.
     * Не обновляет связь с проектом.
     *
     * @param article сущность для обновления
     */
    public void updateEntity(About article) {
        article.setTitle(this.title);
        article.setContent(this.content);
        article.setShortDescription(this.shortDescription);
        article.setFeaturedImagePath(this.featuredImagePath);
        article.setAuthor(this.author);
        article.setStatus(this.status);
        article.setPublishedDate(this.publishedDate);
        article.setMetaTitle(this.metaTitle);
        article.setMetaDescription(this.metaDescription);
        article.setMetaKeywords(this.metaKeywords);
        article.setOgImagePath(this.ogImagePath);
        article.setSlug(this.slug);
        article.setSortOrder(this.sortOrder);
    }

    @Override
    public String toString() {
        return "AboutForm{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", publishedDate=" + publishedDate +
                ", hasFeaturedImage=" + hasFeaturedImage() +
                '}';
    }
}
