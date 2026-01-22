package com.community.cms.domain.model.content;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.regex.Matcher;


/**
 * Сущность для хранения видео проектов.
 *
 * <p>Хранит только ссылки на внешние видеохостинги (YouTube, Vimeo, Rutube).
 * Видеофайлы НЕ хранятся на сервере - только embed коды и метаданные.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Только ссылки на YouTube/Vimeo/Rutube</li>
 *   <li>Поддержка основного и дополнительных видео</li>
 *   <li>Автоматическое извлечение ID видео из URL</li>
 *   <li>Генерация embed кода для вставки в HTML</li>
 *   <li>Порядок сортировки в списке видео</li>
 * </ul>
 *
 * <p>Безопасность: Видео загружаются только через доверенные источники
 * (YouTube, Vimeo, Rutube) для предотвращения XSS атак.</p>
 *
 * @author Community CMS
 * @version 1.1
 * @since 2025
 */
@Entity
@Table(name = "project_videos")
public class VideoGallery {

    /**
     * Типы видеохостингов, поддерживаемых системой.
     * Видео типы для определения источника видео.
     */
    public enum VideoType {
        YOUTUBE("YouTube"),
        VIMEO("Vimeo"),
        RUTUBE("Rutube");

        private final String displayName;

        VideoType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Определяет тип видеохостинга по URL.
         *
         * @param url URL видео
         * @return тип видеохостинга или null если не поддерживается
         */
        public static VideoType fromUrl(String url) {
            if (url == null) {
                return null;
            }

            url = url.toLowerCase();
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                return YOUTUBE;
            } else if (url.contains("vimeo.com")) {
                return VIMEO;
            } else if (url.contains("rutube.ru")) {
                return RUTUBE;
            }
            return null;
        }

        /**
         * Извлекает ID видео из URL.
         *
         * @param url URL видео
         * @return ID видео или null если не удалось извлечь
         */
        public static String extractVideoId(String url) {
            if (url == null) {
                return null;
            }

            VideoType type = fromUrl(url);
            if (type == null) {
                return null;
            }

            return switch (type) {
                case YOUTUBE -> extractYouTubeId(url);
                case VIMEO -> extractVimeoId(url);
                case RUTUBE -> extractRutubeId(url);
            };
        }

        /**
         * Извлекает ID YouTube видео из URL.
         */
        private static String extractYouTubeId(String url) {
            // Форматы:
            // https://www.youtube.com/watch?v=VIDEO_ID
            // https://youtu.be/VIDEO_ID
            // https://www.youtube.com/embed/VIDEO_ID

            String videoId = null;

            if (url.contains("youtube.com/watch")) {
                // Извлекаем из параметра v=
                int start = url.indexOf("v=");
                if (start != -1) {
                    start += 2;
                    int end = url.indexOf('&', start);
                    videoId = end == -1 ? url.substring(start) : url.substring(start, end);
                }
            } else if (url.contains("youtu.be/")) {
                // Извлекаем из короткой ссылки
                int start = url.indexOf("youtu.be/") + 9;
                int end = url.indexOf('?', start);
                videoId = end == -1 ? url.substring(start) : url.substring(start, end);
            } else if (url.contains("youtube.com/embed/")) {
                // Извлекаем из embed ссылки
                int start = url.indexOf("embed/") + 6;
                int end = url.indexOf('?', start);
                videoId = end == -1 ? url.substring(start) : url.substring(start, end);
            }

            // Очищаем ID от лишних символов
            if (videoId != null && videoId.length() > 11) {
                videoId = videoId.substring(0, 11);
            }

            return videoId;
        }

        /**
         * Извлекает ID Vimeo видео из URL.
         */
        private static String extractVimeoId(String url) {
            // Форматы:
            // https://vimeo.com/VIDEO_ID
            // https://vimeo.com/channels/xxxx/VIDEO_ID
            // https://vimeo.com/groups/xxxx/videos/VIDEO_ID

            // Ищем последний сегмент URL, который является числом
            String[] parts = url.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i].matches("\\d+")) {
                    return parts[i];
                }
            }
            return null;
        }

        /**
         * Извлекает ID Rutube видео из URL.
         */
        private static String extractRutubeId(String url) {
            // Форматы:
            // https://rutube.ru/video/VIDEO_ID/
            // https://rutube.ru/play/embed/VIDEO_ID
            // https://rutube.ru/video/private/VIDEO_ID/?t=123

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "rutube\\.ru/(?:video/|play/embed/)([a-zA-Z0-9_-]+)"
            );
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================== СВЯЗИ ==================

    /**
     * Проект, к которому принадлежит видео.
     */
    @NotNull(message = "Проект обязателен / Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ================== ОСНОВНЫЕ ДАННЫЕ ==================

    /**
     * Название видео.
     * Отображается в интерфейсе пользователя.
     */
    @NotBlank(message = "Название видео обязательно / Video title is required")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов / Title must be between 3 and 255 characters")
    @Column(nullable = false)
    private String title;

    /**
     * Описание видео (опционально).
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов / Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL видео на внешнем видеохостинге.
     * Только YouTube, Vimeo или Rutube.
     */
    @NotBlank(message = "URL видео обязателен / Video URL is required")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com|rutube\\.ru)/.+$",
            message = "Поддерживаются только ссылки на YouTube, Vimeo и Rutube / Only YouTube, Vimeo and Rutube links are supported")
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    /**
     * Тип видеохостинга.
     * Определяется автоматически из URL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "video_type", nullable = false, length = 20)
    private VideoType videoType;

    /**
     * ID видео на внешнем хостинге.
     * Извлекается автоматически из URL.
     */
    @Column(name = "video_id", length = 50)
    private String videoId;

    /**
     * Флаг основного видео проекта.
     * Основное видео отображается в начале страницы.
     */
    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    /**
     * Длительность видео в секундах (опционально).
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Порядок сортировки в списке видео.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время добавления видео.
     */
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    /**
     * Дата и время последнего обновления.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     */
    public VideoGallery() {
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param project проект
     * @param title название видео
     * @param videoUrl URL видео
     */
    public VideoGallery(Project project, String title, String videoUrl) {
        this();
        this.project = project;
        this.title = title;
        setVideoUrl(videoUrl); // Используем сеттер для автоматического определения типа
        this.sortOrder = 0;
        this.isMain = false;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;

        // Автоматически определяем тип и ID видео
        this.videoType = VideoType.fromUrl(videoUrl);
        if (this.videoType != null) {
            this.videoId = VideoType.extractVideoId(videoUrl);
        }
    }

    public VideoType getVideoType() {
        return videoType;
    }

    public void setVideoType(VideoType videoType) {
        this.videoType = videoType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Генерирует embed код для вставки видео в HTML.
     *
     * @return HTML код для встраивания видео
     */
    public String getEmbedCode() {
        if (videoType == null || videoId == null) {
            return "";
        }

        return switch (videoType) {
            case YOUTUBE -> generateYouTubeEmbedCode();
            case VIMEO -> generateVimeoEmbedCode();
            case RUTUBE -> generateRutubeEmbedCode();
        };
    }

    /**
     * Генерирует embed код для YouTube.
     */
    private String generateYouTubeEmbedCode() {
        return String.format(
                "<iframe width=\"560\" height=\"315\" " +
                        "src=\"https://www.youtube.com/embed/%s\" " +
                        "title=\"%s\" " +
                        "frameborder=\"0\" " +
                        "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" " +
                        "allowfullscreen></iframe>",
                videoId, title
        );
    }

    /**
     * Генерирует embed код для Vimeo.
     */
    private String generateVimeoEmbedCode() {
        return String.format(
                "<iframe src=\"https://player.vimeo.com/video/%s\" " +
                        "width=\"560\" height=\"315\" " +
                        "frameborder=\"0\" " +
                        "allow=\"autoplay; fullscreen; picture-in-picture\" " +
                        "allowfullscreen " +
                        "title=\"%s\"></iframe>",
                videoId, title
        );
    }

    /**
     * Генерирует embed код для Rutube.
     */
    private String generateRutubeEmbedCode() {
        return String.format(
                "<iframe src=\"https://rutube.ru/play/embed/%s\" " +
                        "width=\"560\" height=\"315\" " +
                        "frameborder=\"0\" " +
                        "allow=\"autoplay; fullscreen; picture-in-picture\" " +
                        "allowfullscreen " +
                        "title=\"%s\"></iframe>",
                videoId, title
        );
    }

    /**
     * Генерирует URL для превью видео (миниатюры).
     *
     * @return URL превью или null если не поддерживается
     */
    public String getThumbnailUrl() {
        if (videoType == null || videoId == null) {
            return null;
        }

        return switch (videoType) {
            case YOUTUBE -> String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", videoId);
            case VIMEO -> {
                // Для Vimeo нужно API запрос, возвращаем заглушку
                yield "https://vimeo.com/" + videoId;
            }
            case RUTUBE -> String.format("https://rutube.ru/video/thumb/%s.jpg", videoId);
        };
    }

    /**
     * Получает отформатированную длительность видео.
     * Пример: 125 → "2:05"
     *
     * @return отформатированная длительность или пустая строка
     */
    public String getFormattedDuration() {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Проверяет является ли видео с YouTube.
     *
     * @return true если видео с YouTube, иначе false
     */
    public boolean isYouTubeVideo() {
        return videoType == VideoType.YOUTUBE;
    }

    /**
     * Проверяет является ли видео с Vimeo.
     *
     * @return true если видео с Vimeo, иначе false
     */
    public boolean isVimeoVideo() {
        return videoType == VideoType.VIMEO;
    }

    /**
     * Проверяет является ли видео с Rutube.
     *
     * @return true если видео с Rutube, иначе false
     */
    public boolean isRutubeVideo() {
        return videoType == VideoType.RUTUBE;
    }

    /**
     * Проверяет является ли видео основным для проекта.
     *
     * @return true если это основное видео, иначе false
     */
    public boolean isMainVideo() {
        return isMain;
    }

    /**
     * Получает короткий URL (для отображения в интерфейсе).
     *
     * @return короткая версия URL
     */
    public String getShortUrl() {
        if (videoUrl == null) {
            return "";
        }

        if (videoUrl.length() > 50) {
            return videoUrl.substring(0, 47) + "...";
        }
        return videoUrl;
    }

    /**
     * Проверяет валидность URL видео.
     *
     * @return true если URL валиден и поддерживается, иначе false
     */
    public boolean isValidVideoUrl() {
        return videoType != null && videoId != null;
    }

    @Override
    public String toString() {
        return "VideoGallery{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoType=" + videoType +
                ", videoId='" + videoId + '\'' +
                ", isMain=" + isMain +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     */
    @PrePersist
    @PreUpdate
    protected void validate() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        // Автоматически определяем тип и ID если не установлены
        if (videoUrl != null && (videoType == null || videoId == null)) {
            setVideoUrl(videoUrl);
        }
    }
}