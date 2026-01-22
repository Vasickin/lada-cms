package com.community.cms.web.mvc.dto.content;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.web.mvc.dto.people.TeamMemberDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO для публичного отображения проектов.
 * Содержит все данные, необходимые для фронтенда.
 */
public class ProjectDTO {

    // ================== ОСНОВНЫЕ ПОЛЯ ==================
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String fullDescription;

    // ================== ДАТЫ ==================
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate eventDate;
    private String location;

    // Вычисляемые поля для дат (для календаря и фильтров)
    private Integer eventYear;
    private Integer eventMonth;
    private Integer eventDay;

    // ================== МЕДИА ==================
    private String featuredImagePath;
    private List<Long> keyPhotoIds;
    private String videoUrl;
    private String videoPlatform; // "youtube", "vimeo", "rutube", "none", "unknown"
    private String videoEmbedUrl;

    // ================== КАТЕГОРИЯ И СТАТУС ==================
    private String category;
    private String status; // "ACTIVE", "ARCHIVED", "ANNUAL"

    // ================== СЕКЦИИ (для детальной страницы) ==================
    private boolean showDescription;
    private boolean showPhotos;
    private boolean showVideos;
    private boolean showTeam;
    private boolean showParticipation;
    private boolean showPartners;
    private boolean showRelated;

    // ================== SEO МЕТА-ДАННЫЕ ==================
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogImagePath;

    // ================== ВЫЧИСЛЯЕМЫЕ ПОЛЯ ==================
    private boolean currentlyActive;
    private boolean annual;
    private boolean archived;
    private boolean hasVideo;
    private boolean hasFeaturedImage;
    private boolean hasKeyPhotos;
    private LocalDate displayDate;

    // ================== СВЯЗАННЫЕ ДАННЫЕ ==================
    private List<TeamMemberDTO> teamMembers;
    private Set<Partner> partners; // Используем сущность Partner напрямую
    private List<PhotoGalleryDTO> keyPhotos;

    // ================== ДЛЯ НАВИГАЦИИ ==================
    private String detailUrl; // Формат: "/projects/{slug}"

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     */
    public ProjectDTO() {
    }

    /**
     * Конструктор с основными параметрами.
     */
    public ProjectDTO(Long id, String title, String slug, String category) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.detailUrl = "/projects/" + slug;
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
        this.detailUrl = "/projects/" + slug;
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

        // Автоматически заполняем поля для календаря
        if (eventDate != null) {
            this.eventYear = eventDate.getYear();
            this.eventMonth = eventDate.getMonthValue();
            this.eventDay = eventDate.getDayOfMonth();
        } else {
            this.eventYear = null;
            this.eventMonth = null;
            this.eventDay = null;
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getEventYear() {
        return eventYear;
    }

    public void setEventYear(Integer eventYear) {
        this.eventYear = eventYear;
    }

    public Integer getEventMonth() {
        return eventMonth;
    }

    public void setEventMonth(Integer eventMonth) {
        this.eventMonth = eventMonth;
    }

    public Integer getEventDay() {
        return eventDay;
    }

    public void setEventDay(Integer eventDay) {
        this.eventDay = eventDay;
    }

    public String getFeaturedImagePath() {
        return featuredImagePath;
    }

    public void setFeaturedImagePath(String featuredImagePath) {
        this.featuredImagePath = featuredImagePath;
        this.hasFeaturedImage = featuredImagePath != null && !featuredImagePath.trim().isEmpty();
    }

    public List<Long> getKeyPhotoIds() {
        return keyPhotoIds;
    }

    public void setKeyPhotoIds(List<Long> keyPhotoIds) {
        this.keyPhotoIds = keyPhotoIds;
        this.hasKeyPhotos = keyPhotoIds != null && !keyPhotoIds.isEmpty();
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
        this.hasVideo = videoUrl != null && !videoUrl.trim().isEmpty();
    }

    public String getVideoPlatform() {
        return videoPlatform;
    }

    public void setVideoPlatform(String videoPlatform) {
        this.videoPlatform = videoPlatform;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    public void setCurrentlyActive(boolean currentlyActive) {
        this.currentlyActive = currentlyActive;
    }

    public boolean isAnnual() {
        return annual;
    }

    public void setAnnual(boolean annual) {
        this.annual = annual;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public boolean isHasFeaturedImage() {
        return hasFeaturedImage;
    }

    public void setHasFeaturedImage(boolean hasFeaturedImage) {
        this.hasFeaturedImage = hasFeaturedImage;
    }

    public boolean isHasKeyPhotos() {
        return hasKeyPhotos;
    }

    public void setHasKeyPhotos(boolean hasKeyPhotos) {
        this.hasKeyPhotos = hasKeyPhotos;
    }

    public LocalDate getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(LocalDate displayDate) {
        this.displayDate = displayDate;
    }

    public List<TeamMemberDTO> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMemberDTO> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public Set<Partner> getPartners() {
        return partners;
    }

    public void setPartners(Set<Partner> partners) {
        this.partners = partners;
    }

    public List<PhotoGalleryDTO> getKeyPhotos() {
        return keyPhotos;
    }

    public void setKeyPhotos(List<PhotoGalleryDTO> keyPhotos) {
        this.keyPhotos = keyPhotos;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
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

    public String getVideoEmbedUrl() {
        return videoEmbedUrl;
    }

    public void setVideoEmbedUrl(String videoEmbedUrl) {
        this.videoEmbedUrl = videoEmbedUrl;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, есть ли у проекта обложка.
     *
     * @return true если есть обложка, иначе false
     */
    public boolean hasFeaturedImage() {
        return hasFeaturedImage;
    }

    /**
     * Проверяет, есть ли у проекта ключевые фотографии.
     *
     * @return true если есть ключевые фото, иначе false
     */
    public boolean hasKeyPhotos() {
        return hasKeyPhotos;
    }

    /**
     * Проверяет, есть ли у проекта видео.
     *
     * @return true если есть видео, иначе false
     */
    public boolean hasVideo() {
        return hasVideo;
    }

    /**
     * Получает эффективный meta title.
     * Если metaTitle не задан, возвращает заголовок проекта.
     *
     * @return meta title для использования
     */
    public String getEffectiveMetaTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : title;
    }

    /**
     * Получает эффективный meta description.
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
     * Получает эффективный путь к OG изображению.
     * Если ogImagePath не задан, возвращает featuredImagePath.
     *
     * @return путь к OG изображению
     */
    public String getEffectiveOgImagePath() {
        return ogImagePath != null && !ogImagePath.trim().isEmpty() ? ogImagePath : featuredImagePath;
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", detailUrl='" + detailUrl + '\'' +
                '}';
    }
}
