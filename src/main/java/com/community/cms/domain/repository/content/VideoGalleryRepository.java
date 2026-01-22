package com.community.cms.domain.repository.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.VideoGallery;
import com.community.cms.domain.model.content.VideoGallery.VideoType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoGalleryRepository extends JpaRepository<VideoGallery, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<VideoGallery> findByProject(Project project);
    List<VideoGallery> findByProjectOrderBySortOrderAsc(Project project);
    List<VideoGallery> findByProjectOrderByAddedAtDesc(Project project);

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project.id = :projectId")
    List<VideoGallery> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project.id = :projectId ORDER BY pv.sortOrder ASC")
    List<VideoGallery> findByProjectIdOrderBySortOrderAsc(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО ТИПУ ВИДЕО ==================

    List<VideoGallery> findByProjectAndVideoType(Project project, VideoType videoType);
    List<VideoGallery> findByProjectAndVideoTypeOrderBySortOrderAsc(Project project, VideoType videoType);

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project.id = :projectId AND pv.videoType = :videoType")
    List<VideoGallery> findByProjectIdAndVideoType(@Param("projectId") Long projectId, @Param("videoType") VideoType videoType);

    // ================== ПОИСК ПО ФЛАГАМ ==================

    Optional<VideoGallery> findByProjectAndIsMainTrue(Project project);

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project.id = :projectId AND pv.isMain = true")
    Optional<VideoGallery> findByProjectIdAndIsMainTrue(@Param("projectId") Long projectId);

    List<VideoGallery> findByProjectAndIsMainFalse(Project project);
    List<VideoGallery> findByProjectAndIsMainFalseOrderBySortOrderAsc(Project project);

    // ================== ПОИСК ПО URL И ID ВИДЕО ==================

    Optional<VideoGallery> findByVideoUrl(String videoUrl);
    List<VideoGallery> findByVideoId(String videoId);
    Optional<VideoGallery> findByProjectAndVideoId(Project project, String videoId);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM VideoGallery pv WHERE pv.project.id = :projectId AND pv.videoUrl = :videoUrl")
    boolean existsByProjectIdAndVideoUrl(@Param("projectId") Long projectId, @Param("videoUrl") String videoUrl);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM VideoGallery pv WHERE pv.project.id = :projectId AND pv.videoId = :videoId")
    boolean existsByProjectIdAndVideoId(@Param("projectId") Long projectId, @Param("videoId") String videoId);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);

    @Query("SELECT COUNT(pv) FROM VideoGallery pv WHERE pv.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    long countByProjectAndVideoType(Project project, VideoType videoType);
    long countByProjectAndIsMainTrue(Project project);

    default long countYouTubeVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.YOUTUBE);
    }

    default long countVimeoVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.VIMEO);
    }

    default long countRutubeVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.RUTUBE);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project = :project ORDER BY pv.sortOrder ASC")
    List<VideoGallery> findFirstNByProject(@Param("project") Project project, Pageable pageable);

    default List<VideoGallery> findFirstNByProject(Project project, int limit) {
        return findFirstNByProject(project, PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project.id = :projectId ORDER BY pv.sortOrder ASC")
    List<VideoGallery> findFirstNByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    default List<VideoGallery> findFirstNByProjectId(Long projectId, int limit) {
        return findFirstNByProjectId(projectId, PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.project = :project AND " +
            "(pv.description IS NULL OR TRIM(pv.description) = '')")
    List<VideoGallery> findWithoutDescriptionByProject(@Param("project") Project project);

    List<VideoGallery> findByProjectAndDurationSecondsIsNotNull(Project project);
    List<VideoGallery> findByProjectAndDurationSecondsIsNull(Project project);

    @Query("SELECT DISTINCT pv.project FROM VideoGallery pv")
    List<Project> findProjectsWithVideos();

    @Query("SELECT pv FROM VideoGallery pv ORDER BY pv.addedAt DESC")
    List<VideoGallery> findRecentVideos(Pageable pageable);

    default List<VideoGallery> findRecentVideos(int limit) {
        return findRecentVideos(PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM VideoGallery pv WHERE pv.videoType = :videoType ORDER BY pv.addedAt DESC")
    List<VideoGallery> findRecentVideosByType(@Param("videoType") VideoType videoType, Pageable pageable);

    default List<VideoGallery> findRecentVideosByType(VideoType videoType, int limit) {
        return findRecentVideosByType(videoType, PageRequest.of(0, limit));
    }

    // ================== УПРАВЛЕНИЕ ОСНОВНЫМ ВИДЕО ==================

    @Modifying
    @Query("UPDATE VideoGallery pv SET pv.isMain = false WHERE pv.project = :project")
    void resetMainVideoForProject(@Param("project") Project project);

    @Modifying
    @Query("UPDATE VideoGallery pv SET pv.isMain = false WHERE pv.project.id = :projectId")
    void resetMainVideoForProjectId(@Param("projectId") Long projectId);

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);

    @Modifying
    @Query("DELETE FROM VideoGallery pv WHERE pv.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    void deleteByProjectAndVideoType(Project project, VideoType videoType);
}