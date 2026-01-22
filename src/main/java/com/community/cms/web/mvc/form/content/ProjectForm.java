package com.community.cms.web.mvc.form.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.validation.VideoUrl;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования проекта.
 *
 * <p>Используется в административной панели для валидации данных проекта
 * перед сохранением в базу данных. Отделяет слой представления от сущностей JPA.
 * Соответствует реальной структуре сущности Project.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Валидация входных данных</li>
 *   <li>Защита от Over-Posting атак</li>
 *   <li>Преобразование дат в правильный формат</li>
 *   <li>Ограничение доступных для редактирования полей</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Project
 */
public class ProjectForm {

    // ================== ОСНОВНЫЕ ДАННЫЕ ==================

    private Long id;

    /**
     * Название проекта.
     * Обязательное поле, от 3 до 255 символов.
     */
    @NotBlank(message = "Название проекта обязательно")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов")
    private String title;

    /**
     * URL-идентификатор проекта (slug).
     * Должен содержать только латинские буквы в нижнем регистре, цифры и дефисы.
     */
    @NotBlank(message = "Slug обязателен")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug может содержать только латинские буквы в нижнем регистре, цифры и дефисы")
    @Size(min = 3, max = 100, message = "Slug должен быть от 3 до 100 символов")
    private String slug;

    /**
     * Краткое описание проекта.
     * Максимум 500 символов, используется для карточек и превью.
     */
    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов")
    private String shortDescription;

    /**
     * Полное описание проекта.
     * Поддерживает HTML разметку, используется на детальной странице.
     */
    private String fullDescription;

    /**
     * Место проведения мероприятия.
     * Максимум 255 символов.
     */
    @Size(max = 255, message = "Место проведения не должно превышать 255 символов")
    private String location;

    /**
     * Ссылка на видео проекта.
     * Поддерживает YouTube, Vimeo, Rutube.
     * Используется для быстрого добавления видео без перехода в отдельный интерфейс.
     *
     * @since 2025
     */
    @VideoUrl(message = "Некорректная ссылка на видео. Поддерживаются: YouTube, Vimeo, Rutube")
    @Size(max = 2000, message = "Ссылка на видео не должна превышать 2000 символов")
    private String videoUrl;

    // ================== КАТЕГОРИЯ И СТАТУС ==================

    /**
     * Категория проекта (строка).
     * Примеры: "конкурс", "фестиваль", "благотворительность".
     */
    @NotBlank(message = "Категория проекта обязательна")
    @Size(max = 100, message = "Категория не должна превышать 100 символов")
    private String category;

    /**
     * Статус проекта.
     * Определяет видимость и поведение проекта на сайте.
     */
    @NotNull(message = "Статус проекта обязателен")
    private Project.ProjectStatus status;

    /**
     * Порядок сортировки в списке проектов.
     * Меньшее значение = выше в списке.
     */
    @NotNull(message = "Порядок сортировки обязателен")
    @Min(value = 0, message = "Порядок сортировки не может быть отрицательным")
    private Integer sortOrder = 0;

    // ================== ДАТЫ ПРОЕКТА ==================

    /**
     * Дата начала проекта.
     * Формат: ГГГГ-ММ-ДД
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * Дата окончания проекта.
     * Формат: ГГГГ-ММ-ДД
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * Дата проведения мероприятия.
     * Формат: ГГГГ-ММ-ДД
     * Используется для разовых событий.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    // ================== НАСТРОЙКИ СЕКЦИЙ ==================

    /**
     * Показывать секцию с описанием на сайте.
     */
    private boolean showDescription = true;

    /**
     * Показывать фотогалерею проекта.
     */
    private boolean showPhotos = true;

    /**
     * Показывать видео проекта.
     */
    private boolean showVideos = true;

    /**
     * Показывать команду проекта.
     */
    private boolean showTeam = true;

    /**
     * Показывать секцию "Как участвовать".
     */
    private boolean showParticipation = true;

    /**
     * Показывать партнеров проекта.
     */
    private boolean showPartners = true;

    /**
     * Показывать похожие проекты.
     */
    private boolean showRelated = true;

    /**
     * Порядок отображения секций.
     * Хранится в формате "description,photos,videos,team"
     */
    @Size(max = 255, message = "Порядок секций не должен превышать 255 символов")
    private String sectionsOrder = "description,photos,videos,team,participation,partners,related";

    // ================== ИЗОБРАЖЕНИЯ ==================

    /**
     * Путь к обложке проекта (featured image).
     * Используется для превью, карточек и OG-изображений.
     */
    private String featuredImagePath;

    /**
     * Путь к OG-изображению для соцсетей.
     * Если пустой, используется featured image.
     */
    private String ogImagePath;

    // ================== SEO ОПТИМИЗАЦИЯ ==================

    /**
     * Meta title для SEO.
     * Если не указан, используется title проекта.
     */
    @Size(max = 255, message = "Meta title не должен превышать 255 символов")
    private String metaTitle;

    /**
     * Meta description для SEO.
     * Используется поисковыми системами для сниппетов.
     */
    @Size(max = 500, message = "Meta description не должен превышать 500 символов")
    private String metaDescription;

    /**
     * Meta keywords для SEO.
     * Ключевые слова через запятую.
     */
    @Size(max = 500, message = "Meta keywords не должен превышать 500 символов")
    private String metaKeywords;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public ProjectForm() {
        // Устанавливаем значения по умолчанию
        this.status = Project.ProjectStatus.ACTIVE;
        this.sortOrder = 0;
        this.sectionsOrder = "description,photos,videos,team,participation,partners,related";
        this.showDescription = true;
        this.showPhotos = true;
        this.showVideos = true;
        this.showTeam = true;
        this.showParticipation = true;
        this.showPartners = true;
        this.showRelated = true;
    }

    /**
     * Конструктор на основе существующего проекта.
     * Используется для редактирования проекта.
     *
     * @param project существующий проект
     */
    public ProjectForm(Project project) {
        this();
        this.id = project.getId();
        this.title = project.getTitle();
        this.slug = project.getSlug();
        this.shortDescription = project.getShortDescription();
        this.fullDescription = project.getFullDescription();
        this.location = project.getLocation();
        this.videoUrl = project.getVideoUrl();
        this.category = project.getCategory();
        this.status = project.getStatus();
        this.sortOrder = project.getSortOrder();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.eventDate = project.getEventDate();
        this.showDescription = project.isShowDescription();
        this.showPhotos = project.isShowPhotos();
        this.showVideos = project.isShowVideos();
        this.showTeam = project.isShowTeam();
        this.showParticipation = project.isShowParticipation();
        this.showPartners = project.isShowPartners();
        this.showRelated = project.isShowRelated();
        this.sectionsOrder = project.getSectionsOrder();
        this.featuredImagePath = project.getFeaturedImagePath();
        this.ogImagePath = project.getOgImagePath();
        this.metaTitle = project.getMetaTitle();
        this.metaDescription = project.getMetaDescription();
        this.metaKeywords = project.getMetaKeywords();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Получает ссылку на видео проекта.
     *
     * @return ссылка на видео или null
     * @since 2025
     */
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * Устанавливает ссылку на видео проекта.
     *
     * @param videoUrl ссылка на видео (YouTube, Vimeo, Rutube)
     * @since 2025
     */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Project.ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(Project.ProjectStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isShowDescription() {
        return showDescription;
    }

    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }

    public boolean isShowPhotos() {
        return showPhotos;
    }

    public void setShowPhotos(boolean showPhotos) {
        this.showPhotos = showPhotos;
    }

    public boolean isShowVideos() {
        return showVideos;
    }

    public void setShowVideos(boolean showVideos) {
        this.showVideos = showVideos;
    }

    public boolean isShowTeam() {
        return showTeam;
    }

    public void setShowTeam(boolean showTeam) {
        this.showTeam = showTeam;
    }

    public boolean isShowParticipation() {
        return showParticipation;
    }

    public void setShowParticipation(boolean showParticipation) {
        this.showParticipation = showParticipation;
    }

    public boolean isShowPartners() {
        return showPartners;
    }

    public void setShowPartners(boolean showPartners) {
        this.showPartners = showPartners;
    }

    public boolean isShowRelated() {
        return showRelated;
    }

    public void setShowRelated(boolean showRelated) {
        this.showRelated = showRelated;
    }

    public String getSectionsOrder() {
        return sectionsOrder;
    }

    public void setSectionsOrder(String sectionsOrder) {
        this.sectionsOrder = sectionsOrder;
    }

    public String getFeaturedImagePath() {
        return featuredImagePath;
    }

    public void setFeaturedImagePath(String featuredImagePath) {
        this.featuredImagePath = featuredImagePath;
    }

    public String getOgImagePath() {
        return ogImagePath;
    }

    public void setOgImagePath(String ogImagePath) {
        this.ogImagePath = ogImagePath;
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

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, является ли проект долгосрочным (имеет даты начала и окончания).
     *
     * @return true если проект имеет и startDate, и endDate
     */
    public boolean isLongTermProject() {
        return startDate != null && endDate != null;
    }

    /**
     * Проверяет, является ли проект разовым мероприятием (только eventDate).
     *
     * @return true если проект имеет только eventDate
     */
    public boolean isSingleEventProject() {
        return eventDate != null && startDate == null && endDate == null;
    }

    /**
     * Получает отображаемое имя статуса на русском.
     *
     * @return русское название статуса
     */
    public String getStatusDisplayNameRu() {
        return status != null ? status.getNameRu() : "";
    }

    /**
     * Получает отображаемое имя статуса на английском.
     *
     * @return английское название статуса
     */
    public String getStatusDisplayNameEn() {
        return status != null ? status.getNameEn() : "";
    }

    /**
     * Проверяет, является ли проект активным.
     *
     * @return true если статус проекта ACTIVE
     */
    public boolean isActive() {
        return status == Project.ProjectStatus.ACTIVE;
    }

    /**
     * Проверяет, является ли проект архивным.
     *
     * @return true если статус проекта ARCHIVED
     */
    public boolean isArchived() {
        return status == Project.ProjectStatus.ARCHIVED;
    }

    /**
     * Проверяет, является ли проект ежегодным.
     *
     * @return true если статус проекта ANNUAL
     */
    public boolean isAnnual() {
        return status == Project.ProjectStatus.ANNUAL;
    }

    /**
     * Проверяет, есть ли у проекта добавленное видео.
     *
     * @return true если videoUrl не пустой, иначе false
     * @since 2025
     */
    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.trim().isEmpty();
    }

    /**
     * Получает тип видео-хостинга по URL.
     *
     * @return "youtube", "vimeo", "rutube" или "unknown"
     * @since 2025
     */
    public String getVideoPlatform() {
        if (!hasVideo()) {
            return "none";
        }

        String url = videoUrl.toLowerCase();
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return "youtube";
        } else if (url.contains("vimeo.com")) {
            return "vimeo";
        } else if (url.contains("rutube.ru")) {
            return "rutube";
        } else {
            return "unknown";
        }
    }

    /**
     * Получает основную дату для отображения.
     * Приоритет: eventDate > endDate > startDate
     *
     * @return основная дата проекта
     */
    public LocalDate getDisplayDate() {
        if (eventDate != null) {
            return eventDate;
        } else if (endDate != null) {
            return endDate;
        } else {
            return startDate;
        }
    }

    /**
     * Преобразует ProjectForm в сущность Project.
     * Не заполняет связи (videos, partners, teamMembers и т.д.).
     *
     * @return сущность Project с заполненными базовыми полями
     */
    public Project toEntity() {
        Project project = new Project();
        project.setId(this.id);
        project.setTitle(this.title);
        project.setSlug(this.slug);
        project.setShortDescription(this.shortDescription);
        project.setFullDescription(this.fullDescription);
        project.setLocation(this.location);
        project.setCategory(this.category);
        project.setStatus(this.status);
        project.setSortOrder(this.sortOrder);
        project.setStartDate(this.startDate);
        project.setEndDate(this.endDate);
        project.setEventDate(this.eventDate);
        project.setShowDescription(this.showDescription);
        project.setShowPhotos(this.showPhotos);
        project.setShowVideos(this.showVideos);
        project.setVideoUrl(this.videoUrl);
        project.setShowTeam(this.showTeam);
        project.setShowParticipation(this.showParticipation);
        project.setShowPartners(this.showPartners);
        project.setShowRelated(this.showRelated);
        project.setSectionsOrder(this.sectionsOrder);
        project.setFeaturedImagePath(this.featuredImagePath);
        project.setOgImagePath(this.ogImagePath);
        project.setMetaTitle(this.metaTitle);
        project.setMetaDescription(this.metaDescription);
        project.setMetaKeywords(this.metaKeywords);

        return project;
    }

    /**
     * Обновляет существующую сущность Project данными из формы.
     * Не обновляет связи (videos, partners, teamMembers и т.д.).
     *
     * @param project сущность для обновления
     */
    public void updateEntity(Project project) {
        project.setTitle(this.title);
        project.setSlug(this.slug);
        project.setShortDescription(this.shortDescription);
        project.setFullDescription(this.fullDescription);
        project.setLocation(this.location);
        project.setCategory(this.category);
        project.setStatus(this.status);
        project.setSortOrder(this.sortOrder);
        project.setStartDate(this.startDate);
        project.setEndDate(this.endDate);
        project.setEventDate(this.eventDate);
        project.setShowDescription(this.showDescription);
        project.setShowPhotos(this.showPhotos);
        project.setShowVideos(this.showVideos);
        project.setVideoUrl(this.videoUrl);
        project.setShowTeam(this.showTeam);
        project.setShowParticipation(this.showParticipation);
        project.setShowPartners(this.showPartners);
        project.setShowRelated(this.showRelated);
        project.setSectionsOrder(this.sectionsOrder);
        project.setFeaturedImagePath(this.featuredImagePath);
        project.setOgImagePath(this.ogImagePath);
        project.setMetaTitle(this.metaTitle);
        project.setMetaDescription(this.metaDescription);
        project.setMetaKeywords(this.metaKeywords);
    }

    /**
     * Возвращает массив секций в правильном порядке.
     * Используется для отображения секций на странице.
     *
     * @return массив имен секций в порядке отображения
     */
    public String[] getOrderedSections() {
        if (sectionsOrder == null || sectionsOrder.trim().isEmpty()) {
            return new String[]{"description", "photos", "videos", "team", "participation", "partners", "related"};
        }
        return sectionsOrder.split(",");
    }

    /**
     * Получает meta title для SEO.
     * Если metaTitle не задан, возвращает заголовок проекта.
     *
     * @return meta title для использования
     */
    public String getEffectiveMetaTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : title;
    }

    /**
     * Получает meta description для SEO.
     * Если metaDescription не задан, возвращает короткое описание.
     *
     * @return meta description для использования
     */
    public String getEffectiveMetaDescription() {
        if (metaDescription != null && !metaDescription.trim().isEmpty()) {
            return metaDescription;
        }
        return shortDescription != null ? shortDescription : "";
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

    @Override
    public String toString() {
        return "ProjectForm{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                ", showDescription=" + showDescription +
                ", showPhotos=" + showPhotos +
                ", showVideos=" + showVideos +
                '}';
    }
}