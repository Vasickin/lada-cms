package com.community.cms.domain.repository.people;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью TeamMember в базе данных.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // ================== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==================

    /**
     * Находит всех активных членов команды.
     */
    List<TeamMember> findByActiveTrue();

    /**
     * Находит всех активных членов команды, отсортированных по порядку сортировки.
     */
    List<TeamMember> findByActiveTrueOrderBySortOrderAsc();

    /**
     * Находит всех активных членов команды, отсортированных по имени.
     */
    List<TeamMember> findByActiveTrueOrderByFullNameAsc();

    /**
     * Находит всех неактивных членов команды.
     */
    List<TeamMember> findByActiveFalse();

    // ================== ПОИСК ПО ИМЕНИ И ДОЛЖНОСТИ ==================

    /**
     * Находит членов команды по части имени (без учета регистра).
     */
    List<TeamMember> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Находит членов команды по части имени (без учета регистра) среди активных.
     */
    List<TeamMember> findByFullNameContainingIgnoreCaseAndActiveTrue(String fullName);

    /**
     * Находит членов команды по должности (без учета регистра).
     */
    List<TeamMember> findByPositionContainingIgnoreCase(String position);

    /**
     * Находит членов команды по должности (без учета регистра) среди активных.
     */
    List<TeamMember> findByPositionContainingIgnoreCaseAndActiveTrue(String position);

    /**
     * Находит членов команды по части имени или должности (без учета регистра).
     */
    @Query("SELECT tm FROM TeamMember tm WHERE " +
            "LOWER(tm.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TeamMember> searchByNameOrPosition(@Param("searchTerm") String searchTerm);

    /**
     * Находит членов команды по части имени или должности (без учета регистра) среди активных.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND (" +
            "LOWER(tm.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<TeamMember> searchActiveByNameOrPosition(@Param("searchTerm") String searchTerm);

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    /**
     * Находит членов команды, участвующих в указанном проекте.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p = :project AND tm.active = true")
    List<TeamMember> findByProject(@Param("project") Project project);

    /**
     * Находит членов команды, участвующих в указанном проекте, отсортированных по sortOrder.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p = :project AND tm.active = true " +
            "ORDER BY tm.sortOrder ASC, tm.fullName ASC")
    List<TeamMember> findByProjectOrderBySortOrder(@Param("project") Project project);

    /**
     * Находит членов команды, участвующих в проекте по ID проекта.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p.id = :projectId AND tm.active = true")
    List<TeamMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Находит членов команды, НЕ участвующих в указанном проекте.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND tm NOT IN " +
            "(SELECT tm2 FROM TeamMember tm2 JOIN tm2.projects p WHERE p = :project)")
    List<TeamMember> findNotInProject(@Param("project") Project project);

    /**
     * Находит членов команды, не имеющих проектов.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND SIZE(tm.projects) = 0")
    List<TeamMember> findWithoutProjects();

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит всех активных членов команды с пагинацией.
     */
    Page<TeamMember> findByActiveTrue(Pageable pageable);

    /**
     * Находит всех членов команды с пагинацией.
     */
    Page<TeamMember> findAll(Pageable pageable);

    /**
     * Находит всех неактивных членов команды с пагинацией.
     */
    Page<TeamMember> findByActiveFalse(Pageable pageable);

    /**
     * Находит членов команды с аватаркой с пагинацией.
     */
    Page<TeamMember> findByAvatarPathIsNotNull(Pageable pageable);

    /**
     * Находит членов команды без аватарки с пагинацией.
     */
    Page<TeamMember> findByAvatarPathIsNull(Pageable pageable);

    /**
     * Поиск членов команды по имени или должности с пагинацией.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE " +
            "(LOWER(tm.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<TeamMember> searchByNameOrPosition(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ================== СОРТИРОВКА ==================

    /**
     * Находит всех членов команды, отсортированных по имени (A-Z).
     */
    List<TeamMember> findAllByOrderByFullNameAsc();

    /**
     * Находит всех членов команды, отсортированных по порядку сортировки.
     */
    List<TeamMember> findAllByOrderBySortOrderAsc();

    /**
     * Находит всех членов команды, отсортированных по дате создания (новые сначала).
     */
    List<TeamMember> findAllByOrderByCreatedAtDesc();

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    /**
     * Подсчитывает количество активных членов команды.
     */
    long countByActiveTrue();

    /**
     * Подсчитывает количество неактивных членов команды.
     */
    long countByActiveFalse();

    /**
     * Находит членов команды с аватаркой.
     */
    List<TeamMember> findByAvatarPathIsNotNull();

    /**
     * Находит членов команды без аватарки.
     */
    List<TeamMember> findByAvatarPathIsNull();

    /**
     * Находит членов команды с биографией.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.bio IS NOT NULL AND TRIM(tm.bio) != ''")
    List<TeamMember> findWithBio();

    /**
     * Находит членов команды без биографии.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.bio IS NULL OR TRIM(tm.bio) = ''")
    List<TeamMember> findWithoutBio();

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит последних N добавленных членов команды.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true ORDER BY tm.createdAt DESC")
    List<TeamMember> findRecentTeamMembers(@Param("limit") int limit);

    /**
     * Находит ключевых членов команды (с высокой позицией в сортировке).
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND tm.sortOrder < 10 ORDER BY tm.sortOrder ASC")
    List<TeamMember> findKeyTeamMembers(@Param("limit") int limit);

    /**
     * Находит членов команды с указанной должностью.
     */
    List<TeamMember> findByPosition(String position);

    /**
     * Находит членов команды с указанной должностью среди активных.
     */
    List<TeamMember> findByPositionAndActiveTrue(String position);

    /**
     * Проверяет существование члена команды с указанным email.
     */
    boolean existsByEmail(String email);

    /**
     * Находит члена команды по email.
     */
    Optional<TeamMember> findByEmail(String email);

    /**
     * Находит членов команды с указанным email среди активных.
     */
    Optional<TeamMember> findByEmailAndActiveTrue(String email);
}