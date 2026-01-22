package com.community.cms.domain.model.content;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.validation.VideoUrl;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Основная сущность проекта организации "ЛАДА".
 *
 * <p>Представляет проект или мероприятие организации с полной информацией,
 * включая даты проведения, статус, категорию, описание и медиа-контент.
 * Поддерживает гибкую систему секций для отображения разного контента
 * на странице проекта.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Уникальный slug для SEO-оптимизированных URL</li>
 *   <li>Несколько дат: начала, окончания и мероприятия</li>
 *   <li>Расширяемая система категорий</li>
 *   <li>Гибкие настройки отображения секций</li>
 *   <li>SEO мета-данные для поисковых систем</li>
 *   <li>Автоматическое отслеживание даты создания и обновления</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "projects")
public class Project {

    /**
     * Статусы проекта для управления жизненным циклом.
     * Project statuses for lifecycle management.
     */
    public enum ProjectStatus {
        ACTIVE("Активный", "Active"),
        ARCHIVED("Архивный", "Archived"),
        ANNUAL("Ежегодный", "Annual");

        private final String nameRu;
        private final String nameEn;

        ProjectStatus(String nameRu, String nameEn) {
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

    /**
     * Название проекта.
     * Должно быть уникальным и информативным.
     */
    @NotBlank(message = "Название проекта обязательно / Project title is required")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов / Title must be between 3 and 255 characters")
    @Column(nullable = false)
    private String title;

    /**
     * Уникальный человеко-понятный идентификатор (ЧПУ).
     * Используется для SEO-оптимизированных URL.
     */
    @NotBlank(message = "Slug обязателен / Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug может содержать только латинские буквы в нижнем регистре, цифры и дефисы / Slug can contain only lowercase letters, digits and hyphens")
    @Column(unique = true, nullable = false)
    private String slug;

    /**
     * Краткое описание проекта.
     * Отображается в карточках и превью.
     */
    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов / Short description must not exceed 500 characters")
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    /**
     * Полное описание проекта.
     * Поддерживает HTML разметку для форматирования.
     */
    @Lob
    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    /**
     * Дата начала проекта.
     * Используется для сортировки и фильтрации активных проектов.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Дата окончания проекта.
     * Если null - проект бессрочный.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Конкретная дата мероприятия.
     * Используется для разовых событий.
     */
    @Column(name = "event_date")
    private LocalDate eventDate;

    /**
     * Место проведения мероприятия.
     */
    @Size(max = 255, message = "Место проведения не должно превышать 255 символов / Location must not exceed 255 characters")
    private String location;

    /**
     * Ссылка на основное видео проекта.
     * Поддерживает YouTube, Vimeo, Rutube и другие видео-хостинги.
     * Используется для быстрого добавления видео прямо из формы проекта.
     *
     * @since 2025
     */
    @VideoUrl
    @Size(max = 2000, message = "Ссылка на видео не должна превышать 2000 символов")
    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    /**
     * Категория проекта (расширяемый список).
     * Примеры: "конкурс", "фестиваль", "благотворительность".
     */
    @NotBlank(message = "Категория обязательна / Category is required")
    @Size(max = 100, message = "Категория не должна превышать 100 символов / Category must not exceed 100 characters")
    @Column(nullable = false)
    private String category;

    /**
     * Статус проекта.
     * Определяет видимость и поведение проекта.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    // ================== ИСПРАВЛЕНИЕ: ДОБАВЛЕНО ПОЛЕ sortOrder ==================
    /**
     * Порядок сортировки в списке проектов.
     * Меньшее значение = выше в списке.
     * Используется для ручной сортировки в админке.
     * ВНИМАНИЕ: Это поле было добавлено для исправления ошибки компиляции,
     * когда ProjectRepository пытался сортировать по несуществующему полю.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    // ================== КОНЕЦ ИСПРАВЛЕНИЯ ==================

    /**
     * Путь к обложке проекта (featured image).
     * Используется для превью, карточек и OG-изображений.
     */
    @Column(name = "featured_image_path")
    private String featuredImagePath;

    // ================== ГИБКИЕ СЕКЦИИ ==================
    // Управление отображением различных блоков на странице проекта

    /**
     * Отображать секцию с описанием.
     */
    @Column(name = "show_description", nullable = false)
    private boolean showDescription = true;

    /**
     * Отображать секцию с фотогалереей.
     */
    @Column(name = "show_photos", nullable = false)
    private boolean showPhotos = true;

    /**
     * Отображать секцию с видео.
     */
    @Column(name = "show_videos", nullable = false)
    private boolean showVideos = true;

    /**
     * Отображать секцию с командой.
     */
    @Column(name = "show_team", nullable = false)
    private boolean showTeam = true;

    /**
     * Отображать секцию "Как участвовать".
     */
    @Column(name = "show_participation", nullable = false)
    private boolean showParticipation = true;

    /**
     * Отображать секцию с партнерами.
     */
    @Column(name = "show_partners", nullable = false)
    private boolean showPartners = true;

    /**
     * Отображать секцию "Похожие проекты".
     */
    @Column(name = "show_related", nullable = false)
    private boolean showRelated = true;

    /**
     * Порядок отображения секций.
     * Хранится в формате "description,photos,videos,team"
     */
    @Size(max = 255, message = "Порядок секций не должен превышать 255 символов / Sections order must not exceed 255 characters")
    @Column(name = "sections_order")
    private String sectionsOrder = "description,photos,videos,team,participation,partners,related";

    // ================== SEO МЕТА-ДАННЫЕ ==================

    /**
     * Meta title для SEO.
     * Если пустой, используется заголовок проекта.
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
     * Если пустой, используется featured image.
     */
    @Column(name = "og_image_path")
    private String ogImagePath;

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время создания записи.
     * Заполняется автоматически при первом сохранении.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления.
     * Обновляется автоматически при каждом изменении.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     * Требуется для JPA и фреймворков.
     */
    public Project() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами проекта.
     *
     * @param title название проекта
     * @param slug уникальный идентификатор для URL
     * @param category категория проекта
     */
    public Project(String title, String slug, String category) {
        this();
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.status = ProjectStatus.ACTIVE;
        this.sortOrder = 0; // ИСПРАВЛЕНИЕ: Установка значения по умолчанию
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Получает ссылку на видео проекта.
     *
     * @return ссылка на видео или null если видео не добавлено
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    // ================== ИСПРАВЛЕНИЕ: ГЕТТЕР И СЕТТЕР ДЛЯ sortOrder ==================
    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    // ================== КОНЕЦ ИСПРАВЛЕНИЯ ==================

    public String getFeaturedImagePath() {
        return featuredImagePath;
    }

    public void setFeaturedImagePath(String featuredImagePath) {
        this.featuredImagePath = featuredImagePath;
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


    // В разделе "ГЕТТЕРЫ И СЕТТЕРЫ" добавьте этот код (можно после featuredImagePath):

    /**
     * Список ID ключевых фотографий из галереи для проекта.
     * Хранит до 5 ID фотографий из PhotoGallery.
     * Key photo IDs from gallery for the project.
     * Stores up to 5 photo IDs from PhotoGallery.
     */
    @ElementCollection
    @CollectionTable(
            name = "project_key_photos",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "photo_gallery_item_id")
    @Size(max = 5, message = "Можно выбрать не более 5 ключевых фотографий / Maximum 5 key photos allowed")
    private List<Long> keyPhotoIds = new ArrayList<>();

// И добавьте getter и setter (в раздел геттеров/сеттеров):

    /**
     * Получает список ID ключевых фотографий.
     *
     * @return список ID фотографий из галереи
     */
    public List<Long> getKeyPhotoIds() {
        return keyPhotoIds;
    }

    /**
     * Устанавливает список ID ключевых фотографий.
     *
     * @param keyPhotoIds список ID фотографий из галереи (не более 5)
     */
    public void setKeyPhotoIds(List<Long> keyPhotoIds) {
        this.keyPhotoIds = keyPhotoIds != null ? keyPhotoIds : new ArrayList<>();
    }

    /**
     * Проверяет есть ли у проекта ключевые фотографии.
     *
     * @return true если есть хотя бы одна ключевая фотография
     */
    public boolean hasKeyPhotos() {
        return keyPhotoIds != null && !keyPhotoIds.isEmpty();
    }

    /**
     * Получает количество ключевых фотографий.
     *
     * @return количество ключевых фотографий (0-5)
     */
    public int getKeyPhotosCount() {
        return keyPhotoIds != null ? keyPhotoIds.size() : 0;
    }

    /**
     * Добавляет ID фотографии к списку ключевых.
     * Проверяет что не превышен лимит в 5 фото.
     *
     * @param photoId ID фотографии из галереи
     * @return true если фото было добавлено, false если лимит превышен
     */
    public boolean addKeyPhotoId(Long photoId) {
        if (keyPhotoIds == null) {
            keyPhotoIds = new ArrayList<>();
        }
        if (keyPhotoIds.size() >= 5) {
            return false; // Превышен лимит
        }
        if (photoId != null && !keyPhotoIds.contains(photoId)) {
            return keyPhotoIds.add(photoId);
        }
        return false;
    }

    /**
     * Удаляет ID фотографии из списка ключевых.
     *
     * @param photoId ID фотографии для удаления
     * @return true если фото было удалено
     */
    public boolean removeKeyPhotoId(Long photoId) {
        return keyPhotoIds != null && keyPhotoIds.remove(photoId);
    }

    /**
     * Очищает все ключевые фотографии.
     */
    public void clearKeyPhotos() {
        if (keyPhotoIds != null) {
            keyPhotoIds.clear();
        }
    }


    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет активен ли проект в данный момент.
     * Проект считается активным если:
     * 1. Статус ACTIVE или ANNUAL
     * 2. Текущая дата между startDate и endDate (если даты указаны)
     *
     * @return true если проект активен, иначе false
     */
    public boolean isCurrentlyActive() {
        if (status == ProjectStatus.ARCHIVED) {
            return false;
        }

        LocalDate now = LocalDate.now();

        // Если есть startDate, проверяем что проект уже начался
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }

        // Если есть endDate, проверяем что проект еще не закончился
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Проверяет является ли проект ежегодным.
     *
     * @return true если статус ANNUAL
     */
    public boolean isAnnual() {
        return status == ProjectStatus.ANNUAL;
    }

    /**
     * Проверяет является ли проект архивным.
     *
     * @return true если статус ARCHIVED
     */
    public boolean isArchived() {
        return status == ProjectStatus.ARCHIVED;
    }

    /**
     * Получает основную дату для отображения.
     * Приоритет: eventDate → startDate → createdDate.
     *
     * @return основная дата проекта
     */
    public LocalDate getDisplayDate() {
        if (eventDate != null) {
            return eventDate;
        }
        if (startDate != null) {
            return startDate;
        }
        return createdAt.toLocalDate();
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

    // Добавляем вспомогательный метод (можно в раздел вспомогательных методов):

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

    // ================== СВЯЗИ С КОМАНДОЙ ==================

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", sortOrder=" + sortOrder + // ИСПРАВЛЕНИЕ: Добавлен в toString
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     * Убеждается что все обязательные поля заполнены.
     */
    @PrePersist
    @PreUpdate
    protected void validate() {
        if (status == null) {
            status = ProjectStatus.ACTIVE;
        }
        // ================== ИСПРАВЛЕНИЕ: Установка значения по умолчанию для sortOrder ==================
        if (sortOrder == null) {
            sortOrder = 0;
        }
        // ================== КОНЕЦ ИСПРАВЛЕНИЯ ==================
        if (sectionsOrder == null || sectionsOrder.trim().isEmpty()) {
            sectionsOrder = "description,photos,videos,team,participation,partners,related";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @ManyToMany
    @JoinTable(
            name = "team_member_projects",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "team_member_id")
    )
    private Set<TeamMember> teamMembers = new HashSet<>();

    // И getter/setter:
    public Set<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(Set<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }


// В класс Project.java нужно добавить:

    /**
     * Партнеры, участвующие в проекте.
     * Связь многие-ко-многим через промежуточную таблицу project_partner_associations.
     */
    @ManyToMany(mappedBy = "projects")
    private Set<Partner> partners = new HashSet<>();

// И соответствующие геттеры/сеттеры:

    public Set<Partner> getPartners() {
        return partners;
    }

    public void setPartners(Set<Partner> partners) {
        this.partners = partners;
    }

    /**
     * Добавляет партнера к проекту.
     *
     * @param partner партнер для добавления
     */
    public void addPartner(Partner partner) {
        this.partners.add(partner);
        partner.getProjects().add(this);
    }

    /**
     * Удаляет партнера из проекта.
     *
     * @param partner партнер для удаления
     * @return true если партнер был удален, false если не найден
     */
    public boolean removePartner(Partner partner) {
        boolean removed = this.partners.remove(partner);
        if (removed) {
            partner.getProjects().remove(this);
        }
        return removed;
    }

    /**
     * Получает количество партнеров проекта.
     *
     * @return количество партнеров
     */
    public int getPartnersCount() {
        return partners != null ? partners.size() : 0;
    }

    // В Project.java добавляем новые методы:

    /**
     * Извлекает ID видео из URL для встраивания.
     * Использует ТОЧНО такую же логику как в админке.
     *
     * @return ID видео или null если не удалось извлечь
     */
    public String getVideoEmbedId() {
        if (!hasVideo()) {
            return null;
        }

        String url = this.videoUrl.trim();
        String platform = getVideoPlatform();

        try {
            switch (platform) {
                case "youtube":
                    // Паттерны из админки: extractYouTubeId()
                    if (url.contains("youtube.com/watch?v=")) {
                        return url.substring(url.indexOf("v=") + 2).split("&")[0];
                    } else if (url.contains("youtu.be/")) {
                        return url.substring(url.indexOf("youtu.be/") + 9).split("[?&]")[0];
                    } else if (url.contains("youtube.com/embed/")) {
                        return url.substring(url.indexOf("embed/") + 6).split("[?&]")[0];
                    } else if (url.contains("youtube.com/v/")) {
                        return url.substring(url.indexOf("v/") + 2).split("[?&]")[0];
                    }
                    break;

                case "vimeo":
                    // Паттерн из админки: extractVimeoId()
                    String vimeoId = url.replaceAll(".*vimeo\\.com/(\\d+).*", "$1");
                    if (!vimeoId.equals(url) && vimeoId.matches("\\d+")) {
                        return vimeoId;
                    }
                    break;

                case "rutube":
                    // Паттерны из админки: extractRutubeId()
                    if (url.contains("rutube.ru/video/")) {
                        String idPart = url.substring(url.indexOf("video/") + 6);
                        return idPart.split("/")[0].split("\\?")[0];
                    } else if (url.contains("rutube.ru/play/embed/")) {
                        String idPart = url.substring(url.indexOf("embed/") + 6);
                        return idPart.split("/")[0].split("\\?")[0];
                    } else if (url.contains("rutube.ru/shorts/")) {
                        String idPart = url.substring(url.indexOf("shorts/") + 7);
                        return idPart.split("/")[0].split("\\?")[0];
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при извлечении ID видео: " + e.getMessage());
        }

        return null;
    }

    /**
     * Получает embed URL для iframe (ТОЧНО как в админке).
     * @return embed URL или null если не удалось создать
     */
    public String getVideoEmbedUrl() {
        String platform = getVideoPlatform();
        String videoId = getVideoEmbedId();

        if (videoId == null) {
            return null;
        }

        switch (platform) {
            case "youtube":
                return "https://www.youtube.com/embed/" + videoId;
            case "vimeo":
                return "https://player.vimeo.com/video/" + videoId;
            case "rutube":
                return "https://rutube.ru/play/embed/" + videoId;
            default:
                return null;
        }
    }
}