package com.community.cms.controller;

import com.community.cms.model.Page;
import com.community.cms.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HomeController class.
 * Тесты для класса HomeController.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private Page publishedPage1;
    private Page publishedPage2;

    /**
     * Set up test data before each test.
     * Подготовка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        publishedPage1 = new Page();
        publishedPage1.setId(1L);
        publishedPage1.setTitle("About Our Community");
        publishedPage1.setSlug("about-community");
        publishedPage1.setContent("Content about our community");
        publishedPage1.setPublished(true);

        publishedPage2 = new Page();
        publishedPage2.setId(2L);
        publishedPage2.setTitle("Latest News");
        publishedPage2.setSlug("latest-news");
        publishedPage2.setContent("Latest community news");
        publishedPage2.setPublished(true);
    }

    /**
     * Test home page with published pages.
     * Тест главной страницы с опубликованными страницами.
     */
    @Test
    void home_WithPublishedPages_ShouldReturnHomeWithPages() {
        // Arrange
        List<Page> publishedPages = Arrays.asList(publishedPage1, publishedPage2);
        when(pageService.findAllPublishedPages()).thenReturn(publishedPages);

        // Act
        String viewName = homeController.home(model);

        // Assert
        assertEquals("index", viewName);
        verify(pageService, times(1)).findAllPublishedPages();
        verify(model, times(1)).addAttribute("publishedPages", publishedPages);
    }

    /**
     * Test home page with empty published pages list.
     * Тест главной страницы с пустым списком опубликованных страниц.
     */
    @Test
    void home_WithEmptyPublishedPages_ShouldReturnHomeWithEmptyList() {
        // Arrange
        List<Page> publishedPages = Arrays.asList();
        when(pageService.findAllPublishedPages()).thenReturn(publishedPages);

        // Act
        String viewName = homeController.home(model);

        // Assert
        assertEquals("index", viewName);
        verify(pageService, times(1)).findAllPublishedPages();
        verify(model, times(1)).addAttribute("publishedPages", publishedPages);
    }

    /**
     * Test home page with single published page.
     * Тест главной страницы с одной опубликованной страницей.
     */
    @Test
    void home_WithSinglePublishedPage_ShouldReturnHomeWithPage() {
        // Arrange
        List<Page> publishedPages = Arrays.asList(publishedPage1);
        when(pageService.findAllPublishedPages()).thenReturn(publishedPages);

        // Act
        String viewName = homeController.home(model);

        // Assert
        assertEquals("index", viewName);
        verify(pageService, times(1)).findAllPublishedPages();
        verify(model, times(1)).addAttribute("publishedPages", publishedPages);
    }

    /**
     * Test about page.
     * Тест страницы "О нас".
     */
    @Test
    void about_ShouldReturnAboutPage() {
        // Act
        String viewName = homeController.about();

        // Assert
        assertEquals("about", viewName);
        // Verify no service calls for static pages
        verifyNoInteractions(pageService);
    }

    /**
     * Test contact page.
     * Тест страницы "Контакты".
     */
    @Test
    void contact_ShouldReturnContactPage() {
        // Act
        String viewName = homeController.contact();

        // Assert
        assertEquals("contact", viewName);
        // Verify no service calls for static pages
        verifyNoInteractions(pageService);
    }

    /**
     * Test home page service exception handling.
     * Тест обработки исключений сервиса на главной странице.
     */


    /**
     * Test that about page doesn't call any services.
     * Тест что страница "О нас" не вызывает сервисы.
     */
    @Test
    void about_ShouldNotCallAnyServices() {
        // Act
        String viewName = homeController.about();

        // Assert
        assertEquals("about", viewName);
        verifyNoInteractions(pageService);
        verifyNoInteractions(model);
    }

    /**
     * Test that contact page doesn't call any services.
     * Тест что страница "Контакты" не вызывает сервисы.
     */
    @Test
    void contact_ShouldNotCallAnyServices() {
        // Act
        String viewName = homeController.contact();

        // Assert
        assertEquals("contact", viewName);
        verifyNoInteractions(pageService);
        verifyNoInteractions(model);
    }
}
