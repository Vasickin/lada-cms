package com.community.cms.domain.model.page;

import com.community.cms.domain.enums.PageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая страницу в системе управления контентом.
 * Содержит основную информацию о веб-странице: заголовок, содержимое, URL-адрес и метаданные.
 *
 * <p>Расширена поддержкой типов страниц для основных разделов сайта:
 * <ul>
 *   <li>О нас (ABOUT)</li>
 *   <li>Наши проекты (PROJECTS)</li>
 *   <li>Галерея (GALLERY)</li>
 *   <li>Меценатам (PATRONS)</li>
 *   <li>Контакты (CONTACT)</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.1
 * @since 2025
 */
@Entity
@Table(name = "pages")
public class CustomPage {

    /**
     * Уникальный идентификатор страницы.
     * Генерируется автоматически базой данных при создании новой записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заголовок страницы, отображаемый пользователям.
     * Не может быть пустым и должен содержать от 3 до 255 символов.
     */
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 255, message = "Заголовок должен быть от 3 до 255 символов")
    @Column(nullable = false)
    private String title;

    /**
     * Основное содержимое страницы в формате HTML или plain text.
     * Хранится в поле типа TEXT для поддержки больших объемов данных.
     */
    @NotBlank(message = "Содержимое не может быть пустым")
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Уникальный человеко-понятный идентификатор страницы (ЧПУ).
     * Используется для построения SEO-дружественных URL.
     * Пример: "o-nas" вместо "page?id=1"
     */
    @NotBlank(message = "Slug не может быть пустым")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug может содержать только латинские буквы в нижнем регистре, цифры и дефисы")
    @Column(unique = true, nullable = false)
    private String slug;

    /**
     * Дата и время создания страницы.
     * Устанавливается автоматически при первом сохранении сущности.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления страницы.
     * Обновляется автоматически при каждом изменении сущности.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Флаг, указывающий статус публикации страницы.
     * true - страница опубликована и доступна пользователям
     * false - страница в черновике или снята с публикации
     */
    @Column(nullable = false)
    private Boolean published = false;

    /**
     * Тип страницы для определения её назначения и поведения.
     * CUSTOM - произвольные страницы (по умолчанию)
     * ABOUT, PROJECTS, etc. - основные страницы сайта
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false)
    private PageType pageType = PageType.CUSTOM;

    /**
     * Meta-описание для SEO оптимизации.
     * Используется в meta description теге для поисковых систем.
     * Необязательное поле, может быть пустым.
     */
    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    /**
     * Путь к главному изображению страницы.
     * Используется для превью в соцсетях, Open Graph разметки и SEO.
     * Необязательное поле, может быть пустым.
     */
    @Column(name = "featured_image")
    private String featuredImage;

    /**
     * Конструктор по умолчанию, требуемый JPA.
     */
    public CustomPage() {}

    /**
     * Конструктор для создания новой страницы с основными данными.
     *
     * @param title заголовок страницы
     * @param content содержимое страницы
     * @param slug уникальный идентификатор для URL
     */
    public CustomPage(String title, String content, String slug) {
        this.title = title;
        this.content = content;
        this.slug = slug;
        this.published = false;
        this.pageType = PageType.CUSTOM;
    }

    /**
     * Конструктор для создания страницы определенного типа.
     *
     * @param title заголовок страницы
     * @param content содержимое страницы
     * @param slug уникальный идентификатор для URL
     * @param pageType тип страницы
     */
    public CustomPage(String title, String content, String slug, PageType pageType) {
        this.title = title;
        this.content = content;
        this.slug = slug;
        this.published = false;
        this.pageType = pageType;
    }

    // Геттеры и сеттеры с Javadoc

    /**
     * Возвращает уникальный идентификатор страницы.
     *
     * @return идентификатор страницы
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор страницы.
     *
     * @param id идентификатор страницы
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает заголовок страницы.
     *
     * @return заголовок страницы
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает заголовок страницы.
     *
     * @param title заголовок страницы
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Возвращает содержимое страницы.
     *
     * @return содержимое страницы
     */
    public String getContent() {
        return content;
    }

    /**
     * Устанавливает содержимое страницы.
     *
     * @param content содержимое страницы
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Возвращает уникальный идентификатор для URL.
     *
     * @return slug страницы
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Устанавливает уникальный идентификатор для URL.
     *
     * @param slug slug страницы
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Возвращает дату и время создания страницы.
     *
     * @return дата создания
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания страницы.
     *
     * @param createdAt дата создания
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Возвращает дату и время последнего обновления страницы.
     *
     * @return дата обновления
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату и время последнего обновления страницы.
     *
     * @param updatedAt дата обновления
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Проверяет, опубликована ли страница.
     *
     * @return true если страница опубликована, иначе false
     */
    public Boolean getPublished() {
        return published;
    }

    /**
     * Устанавливает статус публикации страницы.
     *
     * @param published true для публикации, false для снятия с публикации
     */
    public void setPublished(Boolean published) {
        this.published = published;
    }

    /**
     * Возвращает тип страницы.
     *
     * @return тип страницы
     */
    public PageType getPageType() {
        return pageType;
    }

    /**
     * Устанавливает тип страницы.
     *
     * @param pageType тип страницы
     */
    public void setPageType(PageType pageType) {
        this.pageType = pageType;
    }

    /**
     * Возвращает meta-описание страницы.
     *
     * @return meta-описание
     */
    public String getMetaDescription() {
        return metaDescription;
    }

    /**
     * Устанавливает meta-описание страницы.
     *
     * @param metaDescription meta-описание
     */
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    /**
     * Возвращает путь к главному изображению страницы.
     *
     * @return путь к изображению
     */
    public String getFeaturedImage() {
        return featuredImage;
    }

    /**
     * Устанавливает путь к главному изображению страницы.
     *
     * @param featuredImage путь к изображению
     */
    public void setFeaturedImage(String featuredImage) {
        this.featuredImage = featuredImage;
    }

    /**
     * Проверяет, является ли страница одним из основных разделов сайта.
     *
     * @return true если страница является основным разделом, иначе false
     */
    public boolean isSitePage() {
        return this.pageType != PageType.CUSTOM;
    }

    /**
     * Проверяет, является ли страница опубликованной и основным разделом.
     *
     * @return true если страница опубликована и является основным разделом
     */
    public boolean isPublishedSitePage() {
        return this.published && this.isSitePage();
    }

    /**
     * Возвращает строковое представление страницы.
     * Не включает содержимое для безопасности и читаемости.
     *
     * @return строковое представление страницы
     */
    @Override
    public String toString() {
        return "CustomPage{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", pageType=" + pageType +
                ", published=" + published +
                ", createdAt=" + createdAt +
                '}';
    }
}