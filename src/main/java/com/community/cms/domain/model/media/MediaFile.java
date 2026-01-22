package com.community.cms.domain.model.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Универсальная сущность для медиафайлов (фото и видео).
 * Может использоваться как для фото-галереи, так и для видео-галереи.
 *
 * Universal entity for media files (photos and videos).
 * Can be used for both photo gallery and video gallery.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "media_files")
public class MediaFile {

    /**
     * Типы файлов для быстрой фильтрации и классификации.
     * File types for quick filtering and classification.
     */
    public enum FileType {
        IMAGE("Изображение", "Image"),
        VIDEO("Видео", "Video"),
        DOCUMENT("Документ", "Document"),
        AUDIO("Аудио", "Audio");

        private final String nameRu;
        private final String nameEn;

        FileType(String nameRu, String nameEn) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
        }

        public String getNameRu() {
            return nameRu;
        }

        public String getNameEn() {
            return nameEn;
        }

        /**
         * Определяет тип файла по MIME типу.
         * Determines file type by MIME type.
         *
         * @param mimeType MIME тип файла / file MIME type
         * @return тип файла / file type
         */
        public static FileType fromMimeType(String mimeType) {
            if (mimeType == null) {
                return DOCUMENT;
            }

            if (mimeType.startsWith("image/")) {
                return IMAGE;
            } else if (mimeType.startsWith("video/")) {
                return VIDEO;
            } else if (mimeType.startsWith("audio/")) {
                return AUDIO;
            } else {
                return DOCUMENT;
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя файла обязательно / File name is required")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "Путь к файлу обязателен / File path is required")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @NotBlank(message = "MIME тип обязателен / MIME type is required")
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType; // Например / e.g.: "image/jpeg", "image/png", "video/mp4"

    @NotNull(message = "Тип файла обязателен / File type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;

    @NotNull(message = "Размер файла обязателен / File size is required")
    @Min(value = 1, message = "Размер файла должен быть больше 0 байт / File size must be greater than 0 bytes")
    @Max(value = 52428800L, message = "Размер файла не должен превышать 50MB / File size must not exceed 50MB")
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // в байтах / in bytes

    @NotNull(message = "Статус основного файла обязателен / Primary file status is required")
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @NotNull(message = "Порядок сортировки обязателен / Sort order is required")
    @Min(value = 0, message = "Порядок сортировки должен быть не менее 0 / Sort order must be at least 0")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Конструктор по умолчанию.
     * Default constructor.
     */
    public MediaFile() {
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами.
     * Constructor with main parameters.
     *
     * @param fileName имя файла / file name
     * @param filePath путь к файлу / file path
     * @param mimeType MIME тип / MIME type
     * @param fileSize размер файла / file size
     */
    public MediaFile(String fileName, String filePath, String mimeType, Long fileSize) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileType = FileType.fromMimeType(mimeType);
        this.fileSize = fileSize;
    }

    /**
     * Конструктор с полным набором параметров.
     * Constructor with full set of parameters.
     *
     * @param fileName имя файла / file name
     * @param filePath путь к файлу / file path
     * @param mimeType MIME тип / MIME type
     * @param fileType тип файла / file type
     * @param fileSize размер файла / file size
     * @param isPrimary является ли основным / is primary
     * @param sortOrder порядок сортировки / sort order
     */
    public MediaFile(String fileName, String filePath, String mimeType, FileType fileType,
                     Long fileSize, Boolean isPrimary, Integer sortOrder) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }

    // Геттеры и сеттеры / Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        // Автоматически обновляем тип файла при изменении MIME типа
        // Automatically update file type when MIME type changes
        if (mimeType != null) {
            this.fileType = FileType.fromMimeType(mimeType);
        }
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public Boolean isPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Вспомогательные методы / Helper methods

    /**
     * Проверяет является ли файл изображением.
     * Checks if file is an image.
     *
     * @return true если файл изображение / true if file is image
     */
    public boolean isImage() {
        return FileType.IMAGE.equals(fileType);
    }

    /**
     * Проверяет является ли файл видео.
     * Checks if file is a video.
     *
     * @return true если файл видео / true if file is video
     */
    public boolean isVideo() {
        return FileType.VIDEO.equals(fileType);
    }

    /**
     * Проверяет является ли файл аудио.
     * Checks if file is audio.
     *
     * @return true если файл аудио / true if file is audio
     */
    public boolean isAudio() {
        return FileType.AUDIO.equals(fileType);
    }

    /**
     * Проверяет является ли файл документом.
     * Checks if file is a document.
     *
     * @return true если файл документ / true if file is document
     */
    public boolean isDocument() {
        return FileType.DOCUMENT.equals(fileType);
    }

    /**
     * Получает удобочитаемый размер файла.
     * Gets human readable file size.
     *
     * @return размер файла в формате "1.5 MB" / file size formatted as "1.5 MB"
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Получает расширение файла из имени.
     * Gets file extension from file name.
     *
     * @return расширение файла или пустая строка / file extension or empty string
     */
    public String getFileExtension() {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Получает только имя файла из полного пути.
     * Gets only filename from full path.
     *
     * @return имя файла / filename
     */
    public String getFilenameFromPath() {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // Находим последний разделитель пути
        int lastSlashIndex = filePath.lastIndexOf('/');
        int lastBackslashIndex = filePath.lastIndexOf('\\');
        int separatorIndex = Math.max(lastSlashIndex, lastBackslashIndex);

        // Если найден разделитель, возвращаем часть после него
        if (separatorIndex >= 0 && separatorIndex < filePath.length() - 1) {
            return filePath.substring(separatorIndex + 1);
        }

        // Если разделителей нет, возвращаем весь путь
        return filePath;
    }

    /**
     * Получает относительный веб-путь для изображения.
     * Gets relative web path for image.
     *
     * @return веб-путь / web path
     */
    public String getWebPath() {
        String filename = getFilenameFromPath();
        if (filename.isEmpty()) {
            return "";
        }
        return "/admin/photo-gallery/image/" + filename;
    }

    /**
     * Получает ПУБЛИЧНЫЙ веб-путь для изображения.
     * Gets PUBLIC web path for image.
     * Доступен без авторизации через статические ресурсы.
     * Accessible without authorization through static resources.
     *
     * @return публичный веб-путь / public web path
     */
    public String getPublicWebPath() {
        String filename = getFilenameFromPath();
        if (filename.isEmpty()) {
            return "";
        }
        return "/uploads/" + filename; // ← Новый метод для публичного доступа
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaFile mediaFile = (MediaFile) o;
        return Objects.equals(id, mediaFile.id) &&
                Objects.equals(fileName, mediaFile.fileName) &&
                Objects.equals(filePath, mediaFile.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, filePath);
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", fileSize=" + getFormattedFileSize() +
                ", isPrimary=" + isPrimary +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     * Pre-persist method before saving.
     */
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        // Автоматически определяем тип файла если не установлен
        // Automatically determine file type if not set
        if (fileType == null && mimeType != null) {
            fileType = FileType.fromMimeType(mimeType);
        }
    }
}