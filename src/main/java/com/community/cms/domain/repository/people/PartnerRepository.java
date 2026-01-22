package com.community.cms.domain.repository.people;

import com.community.cms.domain.enums.PartnerType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Partner в базе данных.
 *
 * <p>Предоставляет методы для выполнения CRUD операций и пользовательских запросов
 * для партнеров организации "ЛАДА". Расширяет стандартный JpaRepository и добавляет
 * специализированные методы для работы с партнерами проектов.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    // ================== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==================

    /**
     * Находит всех активных партнеров.
     * Используется для отображения партнеров на сайте.
     *
     * @return список активных партнеров
     */
    List<Partner> findByActiveTrue();

    /**
     * Находит всех активных партнеров, отсортированных по порядку сортировки.
     *
     * @return список активных партнеров (по sortOrder)
     */
    List<Partner> findByActiveTrueOrderBySortOrderAsc();

    /**
     * Находит всех активных партнеров, отсортированных по названию.
     *
     * @return список активных партнеров (A-Z по названию)
     */
    List<Partner> findByActiveTrueOrderByNameAsc();

    /**
     * Находит всех неактивных партнеров.
     * Используется в админке для управления партнерами.
     *
     * @return список неактивных партнеров
     */
    List<Partner> findByActiveFalse();

    // ================== ПОИСК ПО НАЗВАНИЮ И ТИПУ ==================

    /**
     * Находит партнеров по части названия (без учета регистра).
     * Используется для поиска в админке.
     *
     * @param name фрагмент названия для поиска
     * @return список найденных партнеров
     */
    List<Partner> findByNameContainingIgnoreCase(String name);

    /**
     * Находит партнеров по части названия (без учета регистра) среди активных.
     *
     * @param name фрагмент названия для поиска
     * @return список найденных активных партнеров
     */
    List<Partner> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    /**
     * Находит партнеров по типу партнерства.
     *
     * @param type тип партнера для поиска
     * @return список партнеров указанного типа
     */
    List<Partner> findByType(PartnerType type);

    /**
     * Находит партнеров по типу партнерства среди активных.
     *
     * @param type тип партнера для поиска
     * @return список активных партнеров указанного типа
     */
    List<Partner> findByTypeAndActiveTrue(PartnerType type);

    /**
     * Находит партнеров по части названия или описания (без учета регистра).
     * Комплексный поиск для админки.
     *
     *
     * @return список найденных партнеров
     */
    @Query("SELECT p FROM Partner p WHERE " +
            "p.name LIKE %:searchTerm% OR " +
            "p.description LIKE %:searchTerm%")
    List<Partner> searchByNameOrDescription(@Param("searchTerm") String searchTerm);


    /**
     * Находит партнеров по части названия или описания (без учета регистра) среди активных.
     *
     *
     * @return список найденных активных партнеров
     */
    @Query("SELECT p FROM Partner p WHERE p.active = true AND (" +
            "p.name LIKE %:searchTerm% OR " +
            "p.description LIKE %:searchTerm%)")
    List<Partner> searchActiveByNameOrDescription(@Param("searchTerm") String searchTerm);

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    /**
     * Находит партнеров, участвующих в указанном проекте.
     *
     * @param project проект для поиска
     * @return список партнеров участвующих в проекте
     */
    @Query("SELECT p FROM Partner p JOIN p.projects proj WHERE proj = :project AND p.active = true")
    List<Partner> findByProject(@Param("project") Project project);

    /**
     * Находит партнеров, участвующих в указанном проекте, отсортированных по sortOrder.
     *
     * @param project проект для поиска
     * @return список партнеров участвующих в проекте (по sortOrder)
     */
    @Query("SELECT p FROM Partner p JOIN p.projects proj WHERE proj = :project AND p.active = true " +
            "ORDER BY p.sortOrder ASC, p.name ASC")
    List<Partner> findByProjectOrderBySortOrder(@Param("project") Project project);

    /**
     * Находит партнеров, участвующих в проекте по ID проекта.
     *
     * @param projectId ID проекта
     * @return список партнеров участвующих в проекте
     */
    @Query("SELECT p FROM Partner p JOIN p.projects proj WHERE proj.id = :projectId AND p.active = true")
    List<Partner> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Находит партнеров, НЕ участвующих в указанном проекте.
     * Используется для добавления партнеров в проект.
     *
     * @param project проект для исключения
     * @return список партнеров не участвующих в проекте
     */
    @Query("SELECT p FROM Partner p WHERE p.active = true AND p NOT IN " +
            "(SELECT p2 FROM Partner p2 JOIN p2.projects proj WHERE proj = :project)")
    List<Partner> findNotInProject(@Param("project") Project project);

    /**
     * Находит партнеров, не имеющих проектов.
     * Используется для оптимизации базы партнеров.
     *
     * @return список партнеров без проектов
     */
    @Query("SELECT p FROM Partner p WHERE p.active = true AND SIZE(p.projects) = 0")
    List<Partner> findWithoutProjects();

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит всех активных партнеров с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница активных партнеров
     */
    Page<Partner> findByActiveTrue(Pageable pageable);

    /**
     * Находит всех партнеров с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница всех партнеров
     */
    Page<Partner> findAll(@Nonnull Pageable pageable);

    // ================== СОРТИРОВКА ==================

    /**
     * Находит всех партнеров, отсортированных по названию (A-Z).
     *
     * @return список всех партнеров отсортированных по названию
     */
    List<Partner> findAllByOrderByNameAsc();

    /**
     * Находит всех партнеров, отсортированных по порядку сортировки.
     *
     * @return список всех партнеров отсортированных по sortOrder
     */
    List<Partner> findAllByOrderBySortOrderAsc();

    /**
     * Находит всех партнеров, отсортированных по дате создания (новые сначала).
     *
     * @return список всех партнеров отсортированных по дате создания
     */
    List<Partner> findAllByOrderByCreatedAtDesc();

    /**
     * Находит всех партнеров, отсортированных по типу партнерства.
     *
     * @return список всех партнеров отсортированных по типу
     */
    List<Partner> findAllByOrderByTypeAsc();

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    /**
     * Подсчитывает количество активных партнеров.
     *
     * @return количество активных партнеров
     */
    long countByActiveTrue();

    /**
     * Подсчитывает количество неактивных партнеров.
     *
     * @return количество неактивных партнеров
     */
    long countByActiveFalse();

    /**
     * Подсчитывает количество партнеров указанного типа.
     *
     * @param type тип партнера
     * @return количество партнеров указанного типа
     */
    long countByType(PartnerType type);

    /**
     * Подсчитывает количество активных партнеров указанного типа.
     *
     * @param type тип партнера
     * @return количество активных партнеров указанного типа
     */
    long countByTypeAndActiveTrue(PartnerType type);

    /**
     * Находит партнеров с логотипом.
     * Используется для проверки заполненности данных.
     *
     * @return список партнеров с logoUrl
     */
    List<Partner> findByLogoUrlIsNotNull();

    /**
     * Находит партнеров без логотипа.
     * Используется для уведомлений в админке.
     *
     * @return список партнеров без logoUrl
     */
    List<Partner> findByLogoUrlIsNull();

    /**
     * Находит партнеров с описанием.
     *
     * @return список партнеров с description
     */
    @Query("SELECT p FROM Partner p WHERE p.description IS NOT NULL AND TRIM(p.description) != ''")
    List<Partner> findWithDescription();

    /**
     * Находит партнеров без описания.
     * Используется для уведомлений в админке.
     *
     * @return список партнеров без description
     */
    @Query("SELECT p FROM Partner p WHERE p.description IS NULL OR TRIM(p.description) = ''")
    List<Partner> findWithoutDescription();

    /**
     * Находит партнеров с указанным веб-сайтом.
     *
     * @return список партнеров с website
     */
    @Query("SELECT p FROM Partner p WHERE p.website IS NOT NULL AND TRIM(p.website) != ''")
    List<Partner> findWithWebsite();

    /**
     * Находит партнеров без веб-сайта.
     *
     * @return список партнеров без website
     */
    @Query("SELECT p FROM Partner p WHERE p.website IS NULL OR TRIM(p.website) = ''")
    List<Partner> findWithoutWebsite();

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит последних N добавленных партнеров.
     * Используется для виджета "Новые партнеры".
     *
     * @param limit количество партнеров
     * @return список последних добавленных партнеров
     */
    @Query(value = "SELECT p FROM Partner p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Partner> findRecentPartners(@Param("limit") int limit);

    /**
     * Находит ключевых партнеров (с высокой позицией в сортировке).
     * Используется для выделения главных партнеров.
     *
     * @param limit количество партнеров
     * @return список ключевых партнеров
     */
    @Query("SELECT p FROM Partner p WHERE p.active = true AND p.sortOrder < 10 ORDER BY p.sortOrder ASC")
    List<Partner> findKeyPartners(@Param("limit") int limit);

    /**
     * Находит спонсоров (партнеров типа SPONSOR).
     *
     * @return список спонсоров
     */
    default List<Partner> findSponsors() {
        return findByType(PartnerType.SPONSOR);
    }

    /**
     * Находит активных спонсоров.
     *
     * @return список активных спонсоров
     */
    default List<Partner> findActiveSponsors() {
        return findByTypeAndActiveTrue(PartnerType.SPONSOR);
    }

    /**
     * Находит информационных партнеров.
     *
     * @return список информационных партнеров
     */
    default List<Partner> findInformationPartners() {
        return findByType(PartnerType.INFORMATION);
    }

    /**
     * Находит активных информационных партнеров.
     *
     * @return список активных информационных партнеров
     */
    default List<Partner> findActiveInformationPartners() {
        return findByTypeAndActiveTrue(PartnerType.INFORMATION);
    }

    /**
     * Находит технических партнеров.
     *
     * @return список технических партнеров
     */
    default List<Partner> findTechnicalPartners() {
        return findByType(PartnerType.TECHNICAL);
    }

    /**
     * Находит активных технических партнеров.
     *
     * @return список активных технических партнеров
     */
    default List<Partner> findActiveTechnicalPartners() {
        return findByTypeAndActiveTrue(PartnerType.TECHNICAL);
    }

    /**
     * Проверяет существование партнера с указанным email.
     *
     * @param email email для проверки
     * @return true если партнер с таким email существует, иначе false
     */
    boolean existsByContactEmail(String email);

    /**
     * Находит партнера по email.
     *
     * @param email email для поиска
     * @return Optional содержащий партнера если найден
     */
    Optional<Partner> findByContactEmail(String email);

    /**
     * Находит партнеров с указанным email среди активных.
     *
     * @param email email для поиска
     * @return Optional содержащий активного партнера если найден
     */
    Optional<Partner> findByContactEmailAndActiveTrue(String email);

    /**
     * Проверяет существование партнера с указанным сайтом.
     *
     * @param website сайт для проверки
     * @return true если партнер с таким сайтом существует, иначе false
     */
    boolean existsByWebsite(String website);

    /**
     * Находит партнеров по контактному лицу (без учета регистра).
     *
     * @param contactPerson имя контактного лица для поиска
     * @return список партнеров с указанным контактным лицом
     */
    List<Partner> findByContactPersonContainingIgnoreCase(String contactPerson);

    // ================== ПАГИНАЦИЯ С ФИЛЬТРАМИ ==================

    /**
     * Находит всех неактивных партнеров с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница неактивных партнеров
     */
    Page<Partner> findByActiveFalse(Pageable pageable);

    /**
     * Находит партнеров по типу с пагинацией.
     *
     * @param type тип партнера
     * @param pageable объект пагинации
     * @return страница партнеров указанного типа
     */
    Page<Partner> findByType(PartnerType type, Pageable pageable);

    /**
     * Находит партнеров по типу среди активных с пагинацией.
     *
     * @param type тип партнера
     * @param pageable объект пагинации
     * @return страница активных партнеров указанного типа
     */
    Page<Partner> findByTypeAndActiveTrue(PartnerType type, Pageable pageable);

//    /**
//     * Комплексный поиск партнеров по названию или описанию с пагинацией.
//     *
//     * @param searchTerm поисковый запрос
//     * @param pageable объект пагинации
//     * @return страница найденных партнеров
//     */
//    @Query("SELECT p FROM Partner p WHERE " +
//            "LOWER(p.name) LIKE CONCAT('%', LOWER(:searchTerm), '%') OR " +
//            "LOWER(p.description) LIKE CONCAT('%', LOWER(:searchTerm), '%')")
//    List<Partner> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
//
//    /**
//     * Комплексный поиск активных партнеров по названию или описанию с пагинацией.
//     *
//     * @param searchTerm поисковый запрос
//     * @param pageable объект пагинации
//     * @return страница найденных активных партнеров
//     */
//    @Query("SELECT p FROM Partner p WHERE p.active = true AND (" +
//            "LOWER(p.name) LIKE CONCAT('%', LOWER(:searchTerm), '%') OR " +
//            "LOWER(p.description) LIKE CONCAT('%', LOWER(:searchTerm), '%'))")
//    List<Partner> searchActiveByNameOrDescription(@Param("searchTerm") String searchTerm);
}