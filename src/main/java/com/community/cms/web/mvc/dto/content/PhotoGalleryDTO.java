package com.community.cms.web.mvc.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhotoGalleryDTO {

    // ================== ПОЛЯ ДЛЯ РАЗНЫХ СЦЕНАРИЕВ ==================

    // Для галереи (старый GalleryDTO)
    private Long id;
    private String title;
    private Integer year;
    private String description;
    private Integer photoCount;
    private Boolean published;

    // Для фото (старый PhotoDTO)
    private Long photoId;           // ID фото
    private String fileName;
    private String webPath;
    private String thumbnailPath;
    private Boolean isPrimary;

    // Связь фото с галереей
    private Long galleryId;
    private String galleryTitle;
    private Integer galleryYear;

    // Для списка изображений в галерее
    private List<PhotoGalleryDTO> images;

    // Для отображения изображений в проектах на публичной странице
    private String publicWebPath;

    // ================== КОНСТРУКТОРЫ ==================

    public PhotoGalleryDTO() {}

    // Конструктор для галереи (старый GalleryDTO)
    public PhotoGalleryDTO(Long id, String title, Integer year, Integer photoCount) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.photoCount = photoCount;
    }

    // Конструктор для галереи с полным набором
    public PhotoGalleryDTO(Long id, String title, Integer year, String description,
                           Integer photoCount, Boolean published) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.description = description;
        this.photoCount = photoCount;
        this.published = published;
    }

    // Конструктор для фото (старый PhotoDTO) - упрощенный
    public PhotoGalleryDTO(Long id, String fileName, String webPath,
                           Long galleryId, String galleryTitle) {
        this.photoId = id;
        this.fileName = fileName;
        this.webPath = webPath;
        this.galleryId = galleryId;
        this.galleryTitle = galleryTitle;
    }

    // Конструктор для фото с полным набором (старый PhotoDTO)
    public PhotoGalleryDTO(Long id, String fileName, String webPath, String thumbnailPath,
                           String title, Long galleryId, String galleryTitle,
                           Integer galleryYear, Boolean isPrimary) {
        this.photoId = id;
        this.fileName = fileName;
        this.webPath = webPath;
        this.thumbnailPath = thumbnailPath;
        this.title = title; // Здесь title это заголовок фото
        this.galleryId = galleryId;
        this.galleryTitle = galleryTitle;
        this.galleryYear = galleryYear;
        this.isPrimary = isPrimary;
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    // Геттеры/сеттеры для полей галереи
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPhotoCount() { return photoCount; }
    public void setPhotoCount(Integer photoCount) { this.photoCount = photoCount; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    // Геттеры/сеттеры для полей фото
    public Long getPhotoId() { return photoId; }
    public void setPhotoId(Long photoId) { this.photoId = photoId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getWebPath() { return webPath; }
    public void setWebPath(String webPath) { this.webPath = webPath; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    // Геттеры/сеттеры для связи
    public Long getGalleryId() { return galleryId; }
    public void setGalleryId(Long galleryId) { this.galleryId = galleryId; }

    public String getGalleryTitle() { return galleryTitle; }
    public void setGalleryTitle(String galleryTitle) { this.galleryTitle = galleryTitle; }

    public Integer getGalleryYear() { return galleryYear; }
    public void setGalleryYear(Integer galleryYear) { this.galleryYear = galleryYear; }

    public List<PhotoGalleryDTO> getImages() { return images; }
    public void setImages(List<PhotoGalleryDTO> images) { this.images = images; }

    // ================== МЕТОДЫ ДЛЯ РАБОТЫ С ИЗОБРАЖЕНИЯМИ В ПРОЕКТАХ ==================

    /**
     * Получает путь к первому изображению галереи.
     * Используется в карточках проектов и карусели.
     */
    public String getFirstImagePath() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getWebPath();
        }
        return "/images/placeholder.jpg";
    }

    /**
     * Проверяет, есть ли у галереи изображения.
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * Получает количество изображений в галерее.
     */
    public int getImagesCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * Получает изображение по ID.
     */
    public PhotoGalleryDTO getImageById(Long imageId) {
        if (images != null && imageId != null) {
            for (PhotoGalleryDTO image : images) {
                if (imageId.equals(image.getPhotoId())) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * Получает путь к изображению по ID.
     */
    public String getImagePathById(Long imageId) {
        PhotoGalleryDTO image = getImageById(imageId);
        return image != null ? image.getWebPath() : null;
    }

    /**
     * Метод для изображений на публичной странице проекты
     */
    public String getPublicWebPath() {
        return publicWebPath;
    }

    /**
     * Метод для изображений на публичной странице проекты
     */
    public void setPublicWebPath(String publicWebPath) {
        this.publicWebPath = publicWebPath;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Для обратной совместимости - возвращает ID в зависимости от контекста.
     */
    public Long getEffectiveId() {
        return photoId != null ? photoId : id;
    }

    /**
     * Получает отображаемое название.
     */
    public String getDisplayName() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return fileName;
    }

    /**
     * Проверяет, является ли этот DTO галереей.
     */
    public boolean isGallery() {
        return title != null && year != null;
    }

    /**
     * Проверяет, является ли этот DTO фото.
     */
    public boolean isPhoto() {
        return fileName != null && webPath != null;
    }

    @Override
    public String toString() {
        if (isPhoto()) {
            return "PhotoGalleryDTO{" +
                    "photoId=" + photoId +
                    ", fileName='" + fileName + '\'' +
                    ", galleryId=" + galleryId +
                    ", galleryTitle='" + galleryTitle + '\'' +
                    '}';
        } else {
            return "PhotoGalleryDTO{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", year=" + year +
                    ", photoCount=" + photoCount +
                    ", published=" + published +
                    '}';
        }
    }
}
