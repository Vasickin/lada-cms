package com.community.cms.domain.model.content;

import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.domain.model.media.PublicationCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

/**
 * Сущность элемента фото-галереи.
 * Содержит только фото, может быть опубликована в нескольких категориях.
 *
 * Photo gallery item entity.
 * Contains only photos, can be published in multiple categories.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "photo_gallery_items")
public class PhotoGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название элемента обязательно / Title is required")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов / Title must be between 3 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Год обязателен / Year is required")
    @Min(value = 2000, message = "Год должен быть не ранее 2000 / Year must be no earlier than 2000")
    @Max(value = 2100, message = "Год должен быть не позднее 2100 / Year must be no later than 2100")
    @Column(nullable = false)
    private Integer year;

    @Size(max = 2000, message = "Описание не должно превышать 2000 символов / Description must not exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "photo_item_categories",
            joinColumns = @JoinColumn(name = "photo_item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<PublicationCategory> categories = new HashSet<>();

    @Size(max = 15, message = "Максимальное количество изображений: 15 / Maximum number of images: 15")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_item_id")
    @OrderBy("isPrimary DESC, sortOrder ASC")
    private List<MediaFile> images = new ArrayList<>();

    @NotNull(message = "Статус публикации обязателен / Publication status is required")
    @Column(nullable = false)
    private Boolean published = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     * Default constructor.
     */
    public PhotoGallery() {
        this.categories = new HashSet<>();
        this.images = new ArrayList<>();
        this.published = true;
    }

    /**
     * Конструктор с основными параметрами.
     * Constructor with main parameters.
     *
     * @param title название / title
     * @param year год / year
     * @param description описание / description
     */
    public PhotoGallery(String title, Integer year, String description) {
        this();
        this.title = title;
        this.year = year;
        this.description = description;
    }

    /**
     * Конструктор с полным набором параметров.
     * Constructor with full set of parameters.
     *
     * @param title название / title
     * @param year год / year
     * @param description описание / description
     * @param categories категории / categories
     * @param images изображения / images
     * @param published статус публикации / publication status
     */
    public PhotoGallery(String title, Integer year, String description,
                        Set<PublicationCategory> categories, List<MediaFile> images,
                        Boolean published) {
        this();
        this.title = title;
        this.year = year;
        this.description = description;

        if (categories != null) {
            this.categories = new HashSet<>(categories);
        }

        if (images != null) {
            this.images = new ArrayList<>(images);
        }

        if (published != null) {
            this.published = published;
        }
    }

    // Геттеры и сеттеры / Getters and Setters

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PublicationCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<PublicationCategory> categories) {
        this.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();
    }

    public List<MediaFile> getImages() {
        return images;
    }

    public void setImages(List<MediaFile> images) {
        if (images != null) {
            this.images = new ArrayList<>(images);
            // Проверяем что все файлы являются изображениями
            // Check that all files are images
            for (MediaFile file : this.images) {
                if (file != null && !file.isImage()) {
                    throw new IllegalArgumentException("Все файлы должны быть изображениями / All files must be images");
                }
            }
        } else {
            this.images = new ArrayList<>();
        }
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
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

    // Методы для работы с категориями / Methods for working with categories

    /**
     * Добавляет категорию к элементу.
     * Adds category to item.
     *
     * @param category категория для добавления / category to add
     */
    public void addCategory(PublicationCategory category) {
        if (category != null) {
            categories.add(category);
        }
    }

    /**
     * Удаляет категорию из элемента.
     * Removes category from item.
     *
     * @param category категория для удаления / category to remove
     * @return true если категория была удалена / true if category was removed
     */
    public boolean removeCategory(PublicationCategory category) {
        return categories.remove(category);
    }

    /**
     * Удаляет категорию по ID.
     * Removes category by ID.
     *
     * @param categoryId ID категории / category ID
     * @return true если категория была удалена / true if category was removed
     */
    public boolean removeCategoryById(Long categoryId) {
        if (categoryId != null) {
            return categories.removeIf(category -> category != null && categoryId.equals(category.getId()));
        }
        return false;
    }

    /**
     * Проверяет содержит ли элемент указанную категорию.
     * Checks if item contains specified category.
     *
     * @param categoryName название категории / category name
     * @return true если содержит категорию / true if contains category
     */
    public boolean hasCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        return categories.stream()
                .anyMatch(category -> category != null && categoryName.equals(category.getName()));
    }

    /**
     * Проверяет содержит ли элемент категорию по ID.
     * Checks if item contains category by ID.
     *
     * @param categoryId ID категории / category ID
     * @return true если содержит категорию / true if contains category
     */
    public boolean hasCategoryById(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        return categories.stream()
                .anyMatch(category -> category != null && categoryId.equals(category.getId()));
    }

    /**
     * Очищает все категории элемента.
     * Clears all categories of item.
     */
    public void clearCategories() {
        categories.clear();
    }

    // Методы для работы с изображениями / Methods for working with images

    /**
     * Добавляет изображение к элементу.
     * Adds image to item.
     *
     * @param image изображение для добавления / image to add
     * @throws IllegalArgumentException если файл не является изображением / if file is not an image
     */
    public void addImage(MediaFile image) {
        if (image != null) {
            if (!image.isImage()) {
                throw new IllegalArgumentException("Файл должен быть изображением / File must be an image");
            }

            images.add(image);

            // Если это первое изображение, устанавливаем его как основное
            // If this is first image, set it as primary
            if (images.size() == 1) {
                image.setIsPrimary(true);
            }
        }
    }

    /**
     * Добавляет несколько изображений к элементу.
     * Adds multiple images to item.
     *
     * @param images список изображений / list of images
     */
    public void addImages(List<MediaFile> images) {
        if (images != null) {
            for (MediaFile image : images) {
                addImage(image);
            }
        }
    }

    /**
     * Удаляет изображение из элемента.
     * Removes image from item.
     *
     * @param image изображение для удаления / image to remove
     * @return true если изображение было удалено / true if image was removed
     */
    public boolean removeImage(MediaFile image) {
        if (image != null && images.contains(image)) {
            boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
            boolean removed = images.remove(image);

            if (removed) {
                // Если удалили основное изображение и остались другие изображения
                // If primary image was removed and there are other images left
                if (wasPrimary && !images.isEmpty()) {
                    // Делаем первое изображение основным
                    // Make first image primary
                    images.get(0).setIsPrimary(true);
                }
            }
            return removed;
        }
        return false;
    }

    /**
     * Удаляет изображение по ID.
     * Removes image by ID.
     *
     * @param imageId ID изображения / image ID
     * @return true если изображение было удалено / true if image was removed
     */
    public boolean removeImageById(Long imageId) {
        if (imageId != null) {
            return images.removeIf(image -> image != null && imageId.equals(image.getId()));
        }
        return false;
    }

    /**
     * Очищает все изображения элемента.
     * Clears all images of item.
     */
    public void clearImages() {
        images.clear();
    }

    /**
     * Устанавливает основное изображение.
     * Sets primary image.
     *
     * @param image изображение для установки как основного / image to set as primary
     * @throws IllegalArgumentException если изображение не принадлежит элементу / if image doesn't belong to item
     */
    public void setPrimaryImage(MediaFile image) {
        if (image == null) {
            throw new IllegalArgumentException("Изображение не может быть null / Image cannot be null");
        }

        if (!images.contains(image)) {
            throw new IllegalArgumentException("Изображение не принадлежит этому элементу / Image doesn't belong to this item");
        }

        // Снимаем статус основного со всех изображений
        // Remove primary status from all images
        for (MediaFile img : images) {
            if (img != null) {
                img.setIsPrimary(false);
            }
        }

        // Устанавливаем новое основное изображение
        // Set new primary image
        image.setIsPrimary(true);
    }

    /**
     * Устанавливает основное изображение по ID.
     * Sets primary image by ID.
     *
     * @param imageId ID изображения / image ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryImageById(Long imageId) {
        if (imageId != null) {
            for (MediaFile image : images) {
                if (image != null && imageId.equals(image.getId())) {
                    setPrimaryImage(image);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Получает основное изображение элемента.
     * Gets primary image of item.
     *
     * @return основное изображение или null / primary image or null
     */
    public MediaFile getPrimaryImage() {
        // Ищем явно помеченное как основное
        // Look for explicitly marked as primary
        for (MediaFile image : images) {
            if (image != null && Boolean.TRUE.equals(image.getIsPrimary())) {
                return image;
            }
        }

        // Если нет явно помеченного, возвращаем первое изображение
        // If no explicitly marked, return first image
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * Получает количество изображений.
     * Gets number of images.
     *
     * @return количество изображений / number of images
     */
    public int getImagesCount() {
        return images.size();
    }

    /**
     * Проверяет содержит ли элемент изображения.
     * Checks if item contains images.
     *
     * @return true если содержит изображения / true if contains images
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * Получает изображение по ID.
     * Gets image by ID.
     *
     * @param imageId ID изображения / image ID
     * @return изображение или null / image or null
     */
    public MediaFile getImageById(Long imageId) {
        if (imageId != null) {
            return images.stream()
                    .filter(image -> image != null && imageId.equals(image.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    // Бизнес-методы / Business methods

    /**
     * Проверяет опубликован ли элемент на главной странице.
     * Checks if item is published on homepage.
     *
     * @return true если опубликован на главной / true if published on homepage
     */
    public boolean isPublishedOnHomepage() {
        return published && hasCategory("Главная");
    }

    /**
     * Проверяет опубликован ли элемент в галерее.
     * Checks if item is published in gallery.
     *
     * @return true если опубликован в галерее / true if published in gallery
     */
    public boolean isPublishedInGallery() {
        return published && hasCategory("Галерея");
    }

    /**
     * Проверяет опубликован ли элемент в разделе "О нас".
     * Checks if item is published in "About us" section.
     *
     * @return true если опубликован в "О нас" / true if published in "About us"
     */
    public boolean isPublishedInAboutUs() {
        return published && hasCategory("О нас");
    }

    /**
     * Проверяет опубликован ли элемент в разделе проектов.
     * Checks if item is published in projects section.
     *
     * @return true если опубликован в проектах / true if published in projects
     */
    public boolean isPublishedInProjects() {
        return published && hasCategory("Наши проекты");
    }

    /**
     * Получает список категорий в виде строк.
     * Gets categories as string list.
     *
     * @return список названий категорий / list of category names
     */
    public List<String> getCategoryNames() {
        return categories.stream()
                .filter(Objects::nonNull)
                .map(PublicationCategory::getName)
                .toList();
    }

    /**
     * Получает строку с категориями через запятую.
     * Gets comma-separated string of categories.
     *
     * @return строка с категориями / string with categories
     */
    public String getCategoriesString() {
        return String.join(", ", getCategoryNames());
    }

    // Методы жизненного цикла / Lifecycle methods

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (published == null) {
            published = true;
        }
        if (categories == null) {
            categories = new HashSet<>();
        }
        if (images == null) {
            images = new ArrayList<>();
        }

        // Проверяем что все файлы являются изображениями
        // Check that all files are images
        for (MediaFile file : images) {
            if (file != null && !file.isImage()) {
                throw new IllegalStateException("Все файлы должны быть изображениями / All files must be images");
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Проверяем что все файлы являются изображениями
        // Check that all files are images
        for (MediaFile file : images) {
            if (file != null && !file.isImage()) {
                throw new IllegalStateException("Все файлы должны быть изображениями / All files must be images");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoGallery that = (PhotoGallery) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PhotoGallery{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", published=" + published +
                ", categories=" + getCategoryNames() +
                ", imagesCount=" + images.size() +
                '}';
    }
}