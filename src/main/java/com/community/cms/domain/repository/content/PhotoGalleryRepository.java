package com.community.cms.domain.repository.content;

import com.community.cms.domain.model.content.PhotoGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с элементами фото-галереи в базе данных.
 * Repository for working with photo gallery items in the database.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Repository
public interface PhotoGalleryRepository extends JpaRepository<PhotoGallery, Long> {

    /**
     * Находит опубликованные элементы, отсортированные по дате создания (новые первыми).
     * Finds published items, sorted by creation date (newest first).
     *
     * @return список элементов / list of items
     */
    List<PhotoGallery> findByPublishedTrueOrderByCreatedAtDesc();

    /**
     * Находит опубликованные элементы по году, отсортированные по дате создания.
     * Finds published items by year, sorted by creation date.
     *
     * @param year год для фильтрации / year for filtering
     * @return список элементов / list of items
     */
    List<PhotoGallery> findByYearAndPublishedTrueOrderByCreatedAtDesc(Integer year);

    /**
     * Находит все элементы (включая неопубликованные) для админки, отсортированные по дате создания.
     * Finds all items (including unpublished) for admin panel, sorted by creation date.
     *
     * @return список элементов / list of items
     */
    List<PhotoGallery> findAllByOrderByCreatedAtDesc();

    /**
     * Находит элемент по ID только если он опубликован.
     * Finds item by ID only if it's published.
     *
     * @param id идентификатор элемента / item identifier
     * @return Optional с элементом / Optional with item
     */
    Optional<PhotoGallery> findByIdAndPublishedTrue(Long id);

    /**
     * Проверяет существование элемента по названию.
     * Checks if item exists by title.
     *
     * @param title название элемента / item title
     * @return true если элемент существует / true if item exists
     */
    boolean existsByTitle(String title);

    /**
     * Находит элементы по части названия (без учета регистра).
     * Finds items by title part (case insensitive).
     *
     * @param title часть названия для поиска / title part for search
     * @return список найденных элементов / list of found items
     */
    List<PhotoGallery> findByTitleContainingIgnoreCase(String title);

    /**
     * Находит опубликованные элементы с определенной категорией.
     * Finds published items with specific category.
     *
     * @param categoryName название категории / category name
     * @return список элементов / list of items
     */
    @Query("SELECT pgi FROM PhotoGallery pgi JOIN pgi.categories pc WHERE pc.name = :categoryName AND pgi.published = true ORDER BY pgi.createdAt DESC")
    List<PhotoGallery> findByCategoryNameAndPublishedTrue(@Param("categoryName") String categoryName);

    /**
     * Находит уникальные года из опубликованных элементов (для фильтров).
     * Finds distinct years from published items (for filters).
     *
     * @return список уникальных годов по убыванию / list of distinct years in descending order
     */
    @Query("SELECT DISTINCT pgi.year FROM PhotoGallery pgi WHERE pgi.published = true ORDER BY pgi.year DESC")
    List<Integer> findDistinctYears();

    /**
     * Подсчитывает количество элементов по статусу публикации.
     * Counts items by publication status.
     *
     * @param published статус публикации / publication status
     * @return количество элементов / items count
     */
    long countByPublished(Boolean published);

    /**
     * Находит элементы по диапазону годов.
     * Finds items by year range.
     *
     * @param startYear начальный год / start year
     * @param endYear конечный год / end year
     * @return список элементов в диапазоне / list of items in range
     */
    @Query("SELECT pgi FROM PhotoGallery pgi WHERE pgi.year BETWEEN :startYear AND :endYear AND pgi.published = true ORDER BY pgi.year DESC, pgi.createdAt DESC")
    List<PhotoGallery> findByYearRangeAndPublishedTrue(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    /**
     * Находит последние добавленные элементы (ограниченное количество).
     * Finds recently added items (limited number).
     *
     * @param limit ограничение количества / items limit
     * @return список последних элементов / list of recent items
     */
    @Query(value = "SELECT * FROM photo_gallery_items WHERE published = true ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<PhotoGallery> findRecentItems(@Param("limit") int limit);

    /**
     * Подсчитывает общее количество изображений во всех элементах.
     * Counts total number of images in all items.
     *
     * @return общее количество изображений / total number of images
     */
    @Query("SELECT COUNT(img) FROM PhotoGallery pgi JOIN pgi.images img")
    long countTotalImages();

    /**
     * Находит элементы без категорий.
     * Finds items without categories.
     *
     * @return список элементов без категорий / list of items without categories
     */
    @Query("SELECT pgi FROM PhotoGallery pgi WHERE pgi.categories IS EMPTY")
    List<PhotoGallery> findItemsWithoutCategories();

    /**
     * Находит элементы без изображений.
     * Finds items without images.
     *
     * @return список элементов без изображений / list of items without images
     */
    @Query("SELECT pgi FROM PhotoGallery pgi WHERE pgi.images IS EMPTY")
    List<PhotoGallery> findItemsWithoutImages();

    /**
     * Получает все неопубликованные элементы фото-галереи (черновики).
     * Элементы возвращаются отсортированными по дате создания в порядке убывания:
     * сначала самые новые, затем более старые.
     * Используется для отображения черновиков в административной панели.
     *
     * @return список элементов фото-галереи со статусом "черновик"
     */
    List<PhotoGallery> findByPublishedFalseOrderByCreatedAtDesc();
}
