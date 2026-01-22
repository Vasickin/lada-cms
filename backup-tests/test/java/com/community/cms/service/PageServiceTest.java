package com.community.cms.service;

import com.community.cms.model.Page;
import com.community.cms.repository.PageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomPageService class.
 * Тесты для класса CustomPageService.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageService pageService;

    private Page testPage;
    private Page publishedPage;
    private Page draftPage;

    /**
     * Set up test data before each test.
     * Подготовка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testPage = new Page();
        testPage.setId(1L);
        testPage.setTitle("Test EasyPage");
        testPage.setContent("Test content");
        testPage.setSlug("test-page");
        testPage.setPublished(false);

        publishedPage = new Page();
        publishedPage.setId(2L);
        publishedPage.setTitle("Published EasyPage");
        publishedPage.setContent("Published content");
        publishedPage.setSlug("published-page");
        publishedPage.setPublished(true);

        draftPage = new Page();
        draftPage.setId(3L);
        draftPage.setTitle("Draft EasyPage");
        draftPage.setContent("Draft content");
        draftPage.setSlug("draft-page");
        draftPage.setPublished(false);
    }

    /**
     * Test finding all pages.
     * Тест поиска всех страниц.
     */
    @Test
    void findAllPages_ShouldReturnAllPages() {
        // Arrange
        List<Page> pages = Arrays.asList(testPage, publishedPage, draftPage);
        when(pageRepository.findAll()).thenReturn(pages);

        // Act
        List<Page> result = pageService.findAllPages();

        // Assert
        assertEquals(3, result.size());
        verify(pageRepository, times(1)).findAll();
    }

    /**
     * Test finding page by existing ID.
     * Тест поиска страницы по существующему ID.
     */
    @Test
    void findPageById_WhenPageExists_ShouldReturnPage() {
        // Arrange
        when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));

        // Act
        Optional<Page> result = pageService.findPageById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test EasyPage", result.get().getTitle());
        verify(pageRepository, times(1)).findById(1L);
    }

    /**
     * Test finding page by non-existing ID.
     * Тест поиска страницы по несуществующему ID.
     */
    @Test
    void findPageById_WhenPageNotExists_ShouldReturnEmpty() {
        // Arrange
        when(pageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Page> result = pageService.findPageById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(pageRepository, times(1)).findById(999L);
    }

    /**
     * Test finding page by existing slug.
     * Тест поиска страницы по существующему slug.
     */
    @Test
    void findPageBySlug_WhenPageExists_ShouldReturnPage() {
        // Arrange
        when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(testPage));

        // Act
        Optional<Page> result = pageService.findPageBySlug("test-page");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test-page", result.get().getSlug());
        verify(pageRepository, times(1)).findBySlug("test-page");
    }

    /**
     * Test saving a new page.
     * Тест сохранения новой страницы.
     */
    @Test
    void savePage_ShouldSavePage() {
        // Arrange
        when(pageRepository.save(any(Page.class))).thenReturn(testPage);

        // Act
        Page result = pageService.savePage(testPage);

        // Assert
        assertNotNull(result);
        assertEquals("Test EasyPage", result.getTitle());
        verify(pageRepository, times(1)).save(testPage);
    }

    /**
     * Test deleting an existing page.
     * Тест удаления существующей страницы.
     */
    @Test
    void deletePage_WhenPageExists_ShouldDeletePage() {
        // Arrange
        when(pageRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pageRepository).deleteById(1L);

        // Act
        pageService.deletePage(1L);

        // Assert
        verify(pageRepository, times(1)).existsById(1L);
        verify(pageRepository, times(1)).deleteById(1L);
    }

    /**
     * Test deleting a non-existing page.
     * Тест удаления несуществующей страницы.
     */
    @Test
    void deletePage_WhenPageNotExists_ShouldThrowException() {
        // Arrange
        when(pageRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            pageService.deletePage(999L);
        });
        verify(pageRepository, times(1)).existsById(999L);
        verify(pageRepository, never()).deleteById(any());
    }

    /**
     * Test publishing a page.
     * Тест публикации страницы.
     */
    @Test
    void publishPage_ShouldSetPublishedToTrue() {
        // Arrange
        when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));
        when(pageRepository.save(any(Page.class))).thenReturn(testPage);

        // Act
        pageService.publishPage(1L);

        // Assert
        assertTrue(testPage.getPublished());
        verify(pageRepository, times(1)).findById(1L);
        verify(pageRepository, times(1)).save(testPage);
    }

    /**
     * Test unpublishing a page.
     * Тест снятия страницы с публикации.
     */
    @Test
    void unpublishPage_ShouldSetPublishedToFalse() {
        // Arrange
        when(pageRepository.findById(2L)).thenReturn(Optional.of(publishedPage));
        when(pageRepository.save(any(Page.class))).thenReturn(publishedPage);

        // Act
        pageService.unpublishPage(2L);

        // Assert
        assertFalse(publishedPage.getPublished());
        verify(pageRepository, times(1)).findById(2L);
        verify(pageRepository, times(1)).save(publishedPage);
    }

    /**
     * Test finding recent pages with limit.
     * Тест поиска последних страниц с ограничением.
     */
    @Test
    void findRecentPages_ShouldReturnLimitedPages() {
        // Arrange
        List<Page> pages = Arrays.asList(testPage, publishedPage, draftPage);
        when(pageRepository.findRecentPages(5)).thenReturn(pages);  // Исправлено!

        // Act
        List<Page> result = pageService.findRecentPages(5);

        // Assert
        assertEquals(3, result.size());
        verify(pageRepository, times(1)).findRecentPages(5);  // Исправлено!
    }

    /**
     * Test getting page statistics.
     * Тест получения статистики страниц.
     */
    @Test
    void getPageStatistics_ShouldReturnCorrectCounts() {
        // Arrange
        when(pageRepository.count()).thenReturn(3L);           // Исправлено!
        when(pageRepository.countByPublished(true)).thenReturn(1L);  // Исправлено!
        when(pageRepository.countByPublished(false)).thenReturn(2L); // Исправлено!

        // Act
        var statistics = pageService.getPageStatistics();

        // Assert
        assertEquals(3, statistics.totalPages());
        assertEquals(1, statistics.publishedCount());
        assertEquals(2, statistics.draftCount());
        verify(pageRepository, times(1)).count();
        verify(pageRepository, times(1)).countByPublished(true);
        verify(pageRepository, times(1)).countByPublished(false);
    }
}
