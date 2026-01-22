package com.community.cms.domain.repository.content;

import com.community.cms.domain.model.content.About;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.About.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AboutRepository extends JpaRepository<About, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<About> findByProject(Project project);
    List<About> findByProjectOrderByPublishedDateDesc(Project project);
    List<About> findByProjectOrderBySortOrderAsc(Project project);

    @Query("SELECT pa FROM About pa WHERE pa.project.id = :projectId")
    List<About> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pa FROM About pa WHERE pa.project.id = :projectId ORDER BY pa.publishedDate DESC")
    List<About> findByProjectIdOrderByPublishedDateDesc(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО СТАТУСУ ==================

    List<About> findByProjectAndStatus(Project project, ArticleStatus status);
    List<About> findByProjectAndStatusOrderByPublishedDateDesc(Project project, ArticleStatus status);

    default List<About> findPublishedByProject(Project project) {
        return findByProjectAndStatusOrderByPublishedDateDesc(project, ArticleStatus.PUBLISHED);
    }

    @Query("SELECT pa FROM About pa WHERE pa.project.id = :projectId AND pa.status = :status")
    List<About> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ArticleStatus status);

    // ================== ПОИСК ОПУБЛИКОВАННЫХ СТАТЕЙ ==================

    List<About> findByStatusOrderByPublishedDateDesc(ArticleStatus status);

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<About> findPublishedArticles();

    @Query("SELECT pa FROM About pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<About> findPublishedArticlesByProject(@Param("project") Project project);

    @Query("SELECT pa FROM About pa WHERE pa.project.id = :projectId AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<About> findPublishedArticlesByProjectId(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО ЗАГОЛОВКУ И СОДЕРЖИМОМУ ==================

    List<About> findByTitleContainingIgnoreCase(String title);
    List<About> findByTitleContainingIgnoreCaseAndStatus(String title, ArticleStatus status);

    @Query("SELECT pa FROM About pa WHERE LOWER(pa.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<About> findByContentContaining(@Param("content") String content);

    @Query("SELECT pa FROM About pa WHERE " +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<About> searchByTitleOrContent(@Param("searchTerm") String searchTerm);

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' AND (" +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<About> searchPublishedByTitleOrContent(@Param("searchTerm") String searchTerm);

    // ================== ПОИСК ПО АВТОРУ И SLUG ==================

    List<About> findByAuthorContainingIgnoreCase(String author);
    Optional<About> findBySlug(String slug);
    Optional<About> findBySlugAndStatus(String slug, ArticleStatus status);
    boolean existsBySlug(String slug);

    // ================== ПАГИНАЦИЯ ==================

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findPublishedArticles(Pageable pageable);

    @Query("SELECT pa FROM About pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findPublishedArticlesByProject(@Param("project") Project project, Pageable pageable);

    @Query("SELECT pa FROM About pa WHERE pa.project.id = :projectId AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findPublishedArticlesByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    Page<About> findAll(Pageable pageable);
    Page<About> findByStatus(ArticleStatus status, Pageable pageable);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);

    @Query("SELECT COUNT(pa) FROM About pa WHERE pa.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    long countByProjectAndStatus(Project project, ArticleStatus status);

    @Query("SELECT COUNT(pa) FROM About pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP)")
    long countPublishedArticles();

    long countByStatus(ArticleStatus status);

    @Query("SELECT SUM(pa.viewCount) FROM About pa")
    Long sumViewCount();

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' ORDER BY pa.viewCount DESC")
    Optional<About> findMostPopularArticle();

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findRecentArticles(Pageable pageable);

    default List<About> findRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedDate"));
        return findRecentArticles(pageable).getContent();
    }

    @Query("SELECT pa FROM About pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findRecentArticlesByProject(@Param("project") Project project, Pageable pageable);

    default List<About> findRecentArticlesByProject(Project project, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedDate"));
        return findRecentArticlesByProject(project, pageable).getContent();
    }

    @Query("SELECT pa FROM About pa WHERE pa.status = 'PUBLISHED' AND pa.publishedDate > :currentTime")
    List<About> findScheduledArticles(@Param("currentTime") LocalDateTime currentTime);

    List<About> findByFeaturedImagePathIsNull();

    @Query("SELECT pa FROM About pa WHERE pa.shortDescription IS NULL OR TRIM(pa.shortDescription) = ''")
    List<About> findWithoutShortDescription();

    @Query("SELECT pa FROM About pa WHERE pa.metaDescription IS NULL OR TRIM(pa.metaDescription) = ''")
    List<About> findWithoutMetaDescription();

    // ИСПРАВЛЕНИЕ: Заменяем != на <> в JPQL
    @Query("SELECT pa FROM About pa WHERE pa.project = :project AND pa.id <> :excludeId AND " +
            "pa.status = 'PUBLISHED' AND (" +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerms, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerms, '%'))) " +
            "ORDER BY pa.publishedDate DESC")
    Page<About> findSimilarArticles(@Param("project") Project project,
                                    @Param("excludeId") Long excludeId,
                                    @Param("searchTerms") String searchTerms,
                                    Pageable pageable);

    default List<About> findSimilarArticles(Project project, Long excludeId, String searchTerms, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findSimilarArticles(project, excludeId, searchTerms, pageable).getContent();
    }

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);

    @Modifying
    @Query("DELETE FROM About pa WHERE pa.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    void deleteByProjectAndStatus(Project project, ArticleStatus status);
}