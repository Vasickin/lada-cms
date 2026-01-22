package com.community.cms.domain.repository.page;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.enums.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью CustomPage в базе данных.
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов.
 *
 * <p>Расширен методами для работы с типами страниц и фильтрацией по статусу публикации.
 *
 * @author Vasickin
 * @version 1.2
 * @since 2025
 * @see CustomPage
 * @see PageType
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface CustomPageRepository extends JpaRepository<CustomPage, Long> {

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ

    Optional<CustomPage> findBySlug(String slug);
    List<CustomPage> findByPublishedTrueOrderByCreatedAtDesc();
    List<CustomPage> findByPublished(Boolean published);
    boolean existsBySlug(String slug);
    List<CustomPage> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT p FROM CustomPage p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<CustomPage> findByContentContaining(@Param("content") String content);

    long countByPublished(Boolean published);
    List<CustomPage> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT p FROM CustomPage p ORDER BY p.createdAt DESC LIMIT :limit")
    List<CustomPage> findRecentPages(@Param("limit") int limit);

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ТИПАМИ СТРАНИЦ

    /**
     * Находит страницу по slug ТОЛЬКО если она опубликована.
     * Используется для публичного доступа к контенту.
     *
     * @param slug уникальный идентификатор страницы
     * @param published статус публикации (true для опубликованных)
     * @return Optional содержащий страницу если найдена и опубликована
     */
    Optional<CustomPage> findBySlugAndPublished(String slug, Boolean published);

    /**
     * Находит страницы по типу и статусу публикации.
     *
     * @param pageType тип страницы для поиска
     * @param published статус публикации
     * @return список страниц с указанным типом и статусом
     */
    List<CustomPage> findByPageTypeAndPublished(PageType pageType, Boolean published);

    /**
     * Находит ОДНУ страницу по типу ТОЛЬКО если она опубликована.
     * Используется для получения уникальных основных страниц сайта.
     *
     * @param pageType тип страницы
     * @param published статус публикации (true для опубликованных)
     * @return Optional содержащий страницу если найдена и опубликована
     */
    Optional<CustomPage> findFirstByPageTypeAndPublished(PageType pageType, Boolean published);

    /**
     * Проверяет существование страницы определенного типа.
     *
     * @param pageType тип страницы для проверки
     * @return true если страница с таким типом существует, иначе false
     */
    boolean existsByPageType(PageType pageType);

    /**
     * Находит все страницы определенного типа.
     *
     * @param pageType тип страницы для поиска
     * @return список страниц указанного типа
     */
    List<CustomPage> findByPageType(PageType pageType);

    /**
     * Находит все страницы определенного типа, отсортированные по дате создания.
     *
     * @param pageType тип страницы для поиска
     * @return список страниц указанного типа (сначала новые)
     */
    List<CustomPage> findByPageTypeOrderByCreatedAtDesc(PageType pageType);

    /**
     * Находит все основные страницы сайта (исключая CUSTOM тип).
     * Использует оператор <> вместо != который не поддерживается в JPQL.
     *
     * @return список основных страниц сайта
     */
    @Query("SELECT p FROM CustomPage p WHERE p.pageType <> com.community.cms.domain.enums.PageType.CUSTOM")
    List<CustomPage> findAllSitePages();

    /**
     * Находит все опубликованные основные страницы сайта.
     * Использует оператор <> вместо != который не поддерживается в JPQL.
     *
     * @return список опубликованных основных страниц
     */
    @Query("SELECT p FROM CustomPage p WHERE p.pageType <> com.community.cms.domain.enums.PageType.CUSTOM AND p.published = true")
    List<CustomPage> findPublishedSitePages();

    /**
     * Подсчитывает количество страниц по типу.
     *
     * @param pageType тип страницы
     * @return количество страниц указанного типа
     */
    long countByPageType(PageType pageType);

    /**
     * Подсчитывает количество опубликованных страниц по типу.
     *
     * @param pageType тип страницы
     * @param published статус публикации
     * @return количество опубликованных страниц указанного типа
     */
    long countByPageTypeAndPublished(PageType pageType, Boolean published);
}