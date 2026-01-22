package com.community.cms.domain.service.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.VideoGallery;
import com.community.cms.domain.model.content.VideoGallery.VideoType;
import com.community.cms.domain.repository.content.VideoGalleryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления видео проектов.
 *
 * <p>Предоставляет бизнес-логику для работы с видео проектов.
 * Видео хранятся только как ссылки на внешние видеохостинги (YouTube, Vimeo, Rutube).</p>
 *
 * @author Community CMS
 * @version 1.1
 * @since 2025
 * @see VideoGallery
 * @see VideoGalleryRepository
 */
@Service
@Transactional
public class VideoGalleryService {

    private final VideoGalleryRepository videoGalleryRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param videoGalleryRepository репозиторий для работы с видео проектов
     */
    @Autowired
    public VideoGalleryService(VideoGalleryRepository videoGalleryRepository) {
        this.videoGalleryRepository = videoGalleryRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет видео проекта.
     *
     * @param videoGallery видео проекта для сохранения
     * @return сохраненное видео проекта
     */
    public VideoGallery save(VideoGallery videoGallery) {
        validateProjectVideo(videoGallery);
        return videoGalleryRepository.save(videoGallery);
    }

    /**
     * Обновляет существующее видео проекта.
     *
     * @param videoGallery видео проекта для обновления
     * @return обновленное видео проекта
     */
    public VideoGallery update(VideoGallery videoGallery) {
        validateProjectVideo(videoGallery);
        return videoGalleryRepository.save(videoGallery);
    }

    /**
     * Находит видео проекта по ID.
     *
     * @param id идентификатор видео проекта
     * @return Optional с видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<VideoGallery> findById(Long id) {
        return videoGalleryRepository.findById(id);
    }

    /**
     * Удаляет видео проекта по ID.
     *
     * @param id идентификатор видео проекта для удаления
     */
    public void deleteById(Long id) {
        videoGalleryRepository.deleteById(id);
    }

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    /**
     * Находит все видео указанного проекта.
     *
     * @param project проект
     * @return список видео проекта
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProject(Project project) {
        return videoGalleryRepository.findByProject(project);
    }

    /**
     * Находит все видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return список видео проекта
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectId(Long projectId) {
        return videoGalleryRepository.findByProjectId(projectId);
    }

    /**
     * Находит все видео проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectOrderBySortOrder(Project project) {
        return videoGalleryRepository.findByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Находит все видео проекта по ID проекта, отсортированные по порядку сортировки.
     *
     * @param projectId ID проекта
     * @return список видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectIdOrderBySortOrder(Long projectId) {
        return videoGalleryRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    // ================== ПОИСК ПО ТИПУ ВИДЕО ==================

    /**
     * Находит видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectAndVideoType(Project project, VideoType videoType) {
        return videoGalleryRepository.findByProjectAndVideoType(project, videoType);
    }

    /**
     * Находит видео проекта по ID проекта и типу видеохостинга.
     *
     * @param projectId ID проекта
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectIdAndVideoType(Long projectId, VideoType videoType) {
        return videoGalleryRepository.findByProjectIdAndVideoType(projectId, videoType);
    }

    /**
     * Находит видео проекта по типу видеохостинга, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByProjectAndVideoTypeOrderBySortOrder(Project project, VideoType videoType) {
        return videoGalleryRepository.findByProjectAndVideoTypeOrderBySortOrderAsc(project, videoType);
    }

    // ================== ПОИСК ПО ФЛАГАМ ==================

    /**
     * Находит основное видео проекта.
     *
     * @param project проект
     * @return Optional с основным видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<VideoGallery> findMainVideoByProject(Project project) {
        return videoGalleryRepository.findByProjectAndIsMainTrue(project);
    }

    /**
     * Находит основное видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return Optional с основным видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<VideoGallery> findMainVideoByProjectId(Long projectId) {
        return videoGalleryRepository.findByProjectIdAndIsMainTrue(projectId);
    }

    /**
     * Находит не основные видео проекта.
     *
     * @param project проект
     * @return список не основных видео проекта
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findNonMainVideosByProject(Project project) {
        return videoGalleryRepository.findByProjectAndIsMainFalse(project);
    }

    /**
     * Находит не основные видео проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список не основных видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findNonMainVideosByProjectOrderBySortOrder(Project project) {
        return videoGalleryRepository.findByProjectAndIsMainFalseOrderBySortOrderAsc(project);
    }

    // ================== ПОИСК ПО URL И ID ВИДЕО ==================

    /**
     * Находит видео по URL.
     *
     * @param videoUrl URL видео
     * @return Optional с видео, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<VideoGallery> findByVideoUrl(String videoUrl) {
        return videoGalleryRepository.findByVideoUrl(videoUrl);
    }

    /**
     * Находит видео по ID видео на внешнем хостинге.
     *
     * @param videoId ID видео на внешнем хостинге
     * @return список видео с указанным ID
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findByVideoId(String videoId) {
        return videoGalleryRepository.findByVideoId(videoId);
    }

    /**
     * Находит видео проекта по проекту и ID видео на внешнем хостинге.
     *
     * @param project проект
     * @param videoId ID видео на внешнем хостинге
     * @return Optional с видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<VideoGallery> findByProjectAndVideoId(Project project, String videoId) {
        return videoGalleryRepository.findByProjectAndVideoId(project, videoId);
    }

    /**
     * Проверяет существование видео по ID проекта и URL.
     *
     * @param projectId ID проекта
     * @param videoUrl URL видео
     * @return true если видео существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndVideoUrl(Long projectId, String videoUrl) {
        return videoGalleryRepository.existsByProjectIdAndVideoUrl(projectId, videoUrl);
    }

    /**
     * Проверяет существование видео по ID проекта и ID видео на внешнем хостинга.
     *
     * @param projectId ID проекта
     * @param videoId ID видео на внешнем хостинге
     * @return true если видео существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndVideoId(Long projectId, String videoId) {
        return videoGalleryRepository.existsByProjectIdAndVideoId(projectId, videoId);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит первые N видео проекта.
     *
     * @param project проект
     * @param limit количество видео
     * @return список первых N видео проекта
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findFirstNByProject(Project project, int limit) {
        return videoGalleryRepository.findFirstNByProject(project, limit);
    }

    /**
     * Находит первые N видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @param limit количество видео
     * @return список первых N видео проекта
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findFirstNByProjectId(Long projectId, int limit) {
        return videoGalleryRepository.findFirstNByProjectId(projectId, limit);
    }

    /**
     * Находит видео проекта без описания.
     *
     * @param project проект
     * @return список видео проекта без описания
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findWithoutDescriptionByProject(Project project) {
        return videoGalleryRepository.findWithoutDescriptionByProject(project);
    }

    /**
     * Находит видео проекта с указанной длительностью.
     *
     * @param project проект
     * @return список видео проекта с указанной длительностью
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findWithDurationByProject(Project project) {
        return videoGalleryRepository.findByProjectAndDurationSecondsIsNotNull(project);
    }

    /**
     * Находит видео проекта без указанной длительности.
     *
     * @param project проект
     * @return список видео проекта без указанной длительности
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findWithoutDurationByProject(Project project) {
        return videoGalleryRepository.findByProjectAndDurationSecondsIsNull(project);
    }

    /**
     * Находит последние N добавленных видео.
     *
     * @param limit количество видео
     * @return список последних добавленных видео
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findRecentVideos(int limit) {
        return videoGalleryRepository.findRecentVideos(limit);
    }

    /**
     * Находит последние N добавленных видео по типу видеохостинга.
     *
     * @param videoType тип видеохостинга
     * @param limit количество видео
     * @return список последних добавленных видео указанного типа
     */
    @Transactional(readOnly = true)
    public List<VideoGallery> findRecentVideosByType(VideoType videoType, int limit) {
        return videoGalleryRepository.findRecentVideosByType(videoType, limit);
    }

    /**
     * Находит проекты, у которых есть видео.
     *
     * @return список проектов с видео
     */
    @Transactional(readOnly = true)
    public List<Project> findProjectsWithVideos() {
        return videoGalleryRepository.findProjectsWithVideos();
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество видео проекта.
     *
     * @param project проект
     * @return количество видео проекта
     */
    @Transactional(readOnly = true)
    public long countByProject(Project project) {
        return videoGalleryRepository.countByProject(project);
    }

    /**
     * Подсчитывает количество видео по ID проекта.
     *
     * @param projectId ID проекта
     * @return количество видео проекта
     */
    @Transactional(readOnly = true)
    public long countByProjectId(Long projectId) {
        return videoGalleryRepository.countByProjectId(projectId);
    }

    /**
     * Подсчитывает количество видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return количество видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public long countByProjectAndVideoType(Project project, VideoType videoType) {
        return videoGalleryRepository.countByProjectAndVideoType(project, videoType);
    }

    /**
     * Подсчитывает количество основных видео проекта.
     *
     * @param project проект
     * @return количество основных видео проекта
     */
    @Transactional(readOnly = true)
    public long countMainVideosByProject(Project project) {
        return videoGalleryRepository.countByProjectAndIsMainTrue(project);
    }

    /**
     * Подсчитывает количество YouTube видео проекта.
     *
     * @param project проект
     * @return количество YouTube видео проекта
     */
    @Transactional(readOnly = true)
    public long countYouTubeVideosByProject(Project project) {
        return videoGalleryRepository.countYouTubeVideosByProject(project);
    }

    /**
     * Подсчитывает количество Vimeo видео проекта.
     *
     * @param project проект
     * @return количество Vimeo видео проекта
     */
    @Transactional(readOnly = true)
    public long countVimeoVideosByProject(Project project) {
        return videoGalleryRepository.countVimeoVideosByProject(project);
    }

    /**
     * Подсчитывает количество Rutube видео проекта.
     *
     * @param project проект
     * @return количество Rutube видео проекта
     */
    @Transactional(readOnly = true)
    public long countRutubeVideosByProject(Project project) {
        return videoGalleryRepository.countRutubeVideosByProject(project);
    }

    // ================== УПРАВЛЕНИЕ ОСНОВНЫМ ВИДЕО ==================

    /**
     * Устанавливает видео как основное для проекта.
     * Сбрасывает флаг основного видео с других видео проекта.
     *
     * @param videoGallery видео для установки как основного
     * @return обновленное видео проекта
     */
    public VideoGallery setAsMainVideo(VideoGallery videoGallery) {
        // Сбрасываем флаг основного видео со всех видео проекта
        videoGalleryRepository.resetMainVideoForProject(videoGallery.getProject());

        // Устанавливаем флаг основного видео для указанного видео
        videoGallery.setMain(true);
        return videoGalleryRepository.save(videoGallery);
    }

    /**
     * Устанавливает видео как основное для проекта по ID видео.
     *
     * @param videoId ID видео
     * @return обновленное видео проекта
     * @throws IllegalArgumentException если видео не найдено
     */
    public VideoGallery setAsMainVideoById(Long videoId) {
        VideoGallery videoGallery = videoGalleryRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Видео с ID " + videoId + " не найдено"));
        return setAsMainVideo(videoGallery);
    }

    /**
     * Сбрасывает флаг основного видео с указанного видео.
     *
     * @param videoGallery видео для сброса флага
     * @return обновленное видео проекта
     */
    public VideoGallery removeMainVideo(VideoGallery videoGallery) {
        videoGallery.setMain(false);
        return videoGalleryRepository.save(videoGallery);
    }

    /**
     * Сбрасывает флаг основного видео со всех видео проекта.
     *
     * @param project проект
     */
    public void resetMainVideoForProject(Project project) {
        videoGalleryRepository.resetMainVideoForProject(project);
    }

    /**
     * Сбрасывает флаг основного видео со всех видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void resetMainVideoForProjectId(Long projectId) {
        videoGalleryRepository.resetMainVideoForProjectId(projectId);
    }

    // ================== УДАЛЕНИЕ ПО СВЯЗЯМ ==================

    /**
     * Удаляет все видео указанного проекта.
     *
     * @param project проект
     */
    public void deleteByProject(Project project) {
        videoGalleryRepository.deleteByProject(project);
    }

    /**
     * Удаляет все видео по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void deleteByProjectId(Long projectId) {
        videoGalleryRepository.deleteByProjectId(projectId);
    }

    /**
     * Удаляет видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     */
    public void deleteByProjectAndVideoType(Project project, VideoType videoType) {
        videoGalleryRepository.deleteByProjectAndVideoType(project, videoType);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для видео проекта.
     *
     * @param videoGallery видео проекта для валидации
     * @throws IllegalArgumentException если видео проекта невалидно
     */
    private void validateProjectVideo(VideoGallery videoGallery) {
        if (videoGallery == null) {
            throw new IllegalArgumentException("Видео проекта не может быть null");
        }

        if (videoGallery.getProject() == null) {
            throw new IllegalArgumentException("Видео должно быть привязано к проекту");
        }

        if (videoGallery.getTitle() == null || videoGallery.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название видео обязательно");
        }

        if (videoGallery.getVideoUrl() == null || videoGallery.getVideoUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("URL видео обязателен");
        }

        // Автоматическое определение типа и ID видео при сохранении
        videoGallery.setVideoUrl(videoGallery.getVideoUrl());

        if (videoGallery.getVideoType() == null) {
            throw new IllegalArgumentException("Не удалось определить тип видеохостинга. Поддерживаются только YouTube, Vimeo и Rutube");
        }

        if (videoGallery.getVideoId() == null || videoGallery.getVideoId().trim().isEmpty()) {
            throw new IllegalArgumentException("Не удалось извлечь ID видео из URL");
        }

        if (videoGallery.getSortOrder() == null) {
            videoGallery.setSortOrder(0);
        }
    }

    /**
     * Создает новое видео проекта.
     *
     * @param project проект
     * @param title название видео
     * @param videoUrl URL видео
     * @param description описание (опционально)
     * @param isMain флаг основного видео
     * @param durationSeconds длительность в секундах (опционально)
     * @param sortOrder порядок сортировки
     * @return созданное видео проекта
     */
    public VideoGallery createProjectVideo(Project project, String title, String videoUrl,
                                           String description, boolean isMain,
                                           Integer durationSeconds, Integer sortOrder) {
        VideoGallery videoGallery = new VideoGallery(project, title, videoUrl);
        videoGallery.setDescription(description);
        videoGallery.setMain(isMain);
        videoGallery.setDurationSeconds(durationSeconds);
        videoGallery.setSortOrder(sortOrder != null ? sortOrder : 0);

        return save(videoGallery);
    }

    /**
     * Проверяет валидность URL видео.
     *
     * @param videoUrl URL видео для проверки
     * @return true если URL валиден и поддерживается, иначе false
     */
    public boolean isValidVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return false;
        }

        VideoType videoType = VideoType.fromUrl(videoUrl);
        if (videoType == null) {
            return false;
        }

        String videoId = VideoType.extractVideoId(videoUrl);
        return videoId != null && !videoId.trim().isEmpty();
    }

    /**
     * Получает embed код для вставки видео в HTML.
     *
     * @param videoGallery видео проекта
     * @return HTML embed код
     */
    public String getEmbedCode(VideoGallery videoGallery) {
        return videoGallery.getEmbedCode();
    }

    /**
     * Получает URL превью видео (миниатюры).
     *
     * @param videoGallery видео проекта
     * @return URL превью видео
     */
    public String getThumbnailUrl(VideoGallery videoGallery) {
        return videoGallery.getThumbnailUrl();
    }

    /**
     * Обновляет порядок сортировки видео проекта.
     *
     * @param videoGalleries список видео с обновленными sortOrder
     * @return список обновленных видео
     */
    public List<VideoGallery> updateSortOrder(List<VideoGallery> videoGalleries) {
        return videoGalleryRepository.saveAll(videoGalleries);
    }
}