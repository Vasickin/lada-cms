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
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminPageCustomController class.
 * Тесты для класса AdminPageCustomController.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PageController pageController;

    private Page testPage;

    /**
     * Set up test data before each test.
     * Подготовка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testPage = new Page();
        testPage.setId(1L);
        testPage.setTitle("Test EasyPage");
        testPage.setSlug("test-page");
        testPage.setContent("Test content");
        testPage.setPublished(true);
    }

    /**
     * Test listing all pages.
     * Тест отображения списка всех страниц.
     */
    @Test
    void listPages_ShouldReturnPageList() {
        // Arrange
        List<Page> pages = Arrays.asList(testPage);
        when(pageService.findAllPages()).thenReturn(pages);

        // Act
        String viewName = pageController.listPages(model);

        // Assert
        assertEquals("pages/list", viewName);
        verify(pageService, times(1)).findAllPages();
        verify(model, times(1)).addAttribute("pages", pages);
    }

    /**
     * Test showing page creation form.
     * Тест отображения формы создания страницы.
     */
    @Test
    void showCreateForm_ShouldReturnCreateForm() {
        // Act
        String viewName = pageController.showCreateForm(model);

        // Assert
        assertEquals("pages/create", viewName);
        verify(model, times(1)).addAttribute(eq("page"), any(Page.class));
    }

    /**
     * Test creating page with valid data.
     * Тест создания страницы с валидными данными.
     */
    @Test
    void createPage_WithValidData_ShouldRedirectToList() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(pageService.savePage(any(Page.class))).thenReturn(testPage);

        // Act
        String viewName = pageController.createPage(testPage, bindingResult, model);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).savePage(testPage);
        verify(bindingResult, times(1)).hasErrors();
    }

    /**
     * Test creating page with validation errors.
     * Тест создания страницы с ошибками валидации.
     */
    @Test
    void createPage_WithValidationErrors_ShouldReturnForm() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = pageController.createPage(testPage, bindingResult, model);

        // Assert
        assertEquals("pages/create", viewName);
        verify(bindingResult, times(1)).hasErrors();
        verify(pageService, never()).savePage(any());
    }

    /**
     * Test creating page with service exception.
     * Тест создания страницы с исключением сервиса.
     */
    @Test
    void createPage_WithServiceException_ShouldReturnFormWithError() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(pageService.savePage(any(Page.class))).thenThrow(new IllegalArgumentException("Slug already exists"));

        // Act
        String viewName = pageController.createPage(testPage, bindingResult, model);

        // Assert
        assertEquals("pages/create", viewName);
        verify(pageService, times(1)).savePage(testPage);
        verify(model, times(1)).addAttribute("error", "Slug already exists");
    }

    /**
     * Test showing edit form for existing page.
     * Тест отображения формы редактирования существующей страницы.
     */
    @Test
    void showEditForm_WhenPageExists_ShouldReturnEditForm() {
        // Arrange
        when(pageService.findPageById(1L)).thenReturn(Optional.of(testPage));

        // Act
        String viewName = pageController.showEditForm(1L, model);

        // Assert
        assertEquals("pages/edit", viewName);
        verify(pageService, times(1)).findPageById(1L);
        verify(model, times(1)).addAttribute("page", testPage);
    }

    /**
     * Test showing edit form for non-existing page.
     * Тест отображения формы редактирования несуществующей страницы.
     */
    @Test
    void showEditForm_WhenPageNotExists_ShouldRedirectToList() {
        // Arrange
        when(pageService.findPageById(999L)).thenReturn(Optional.empty());

        // Act
        String viewName = pageController.showEditForm(999L, model);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).findPageById(999L);
        verify(model, never()).addAttribute(eq("page"), any());
    }

    /**
     * Test updating page with valid data.
     * Тест обновления страницы с валидными данными.
     */
    @Test
    void updatePage_WithValidData_ShouldRedirectToList() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(pageService.savePage(any(Page.class))).thenReturn(testPage);

        // Act
        String viewName = pageController.updatePage(1L, testPage, bindingResult, model);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).savePage(testPage);
        assertEquals(1L, testPage.getId()); // Проверяем что ID установлен
        verify(bindingResult, times(1)).hasErrors();
    }

    /**
     * Test updating page with validation errors.
     * Тест обновления страницы с ошибками валидации.
     */
    @Test
    void updatePage_WithValidationErrors_ShouldReturnEditForm() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = pageController.updatePage(1L, testPage, bindingResult, model);

        // Assert
        assertEquals("pages/edit", viewName);
        verify(bindingResult, times(1)).hasErrors();
        verify(pageService, never()).savePage(any());
    }

    /**
     * Test updating page with service exception.
     * Тест обновления страницы с исключением сервиса.
     */
    @Test
    void updatePage_WithServiceException_ShouldReturnEditFormWithError() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(pageService.savePage(any(Page.class))).thenThrow(new IllegalArgumentException("Update failed"));

        // Act
        String viewName = pageController.updatePage(1L, testPage, bindingResult, model);

        // Assert
        assertEquals("pages/edit", viewName);
        verify(pageService, times(1)).savePage(testPage);
        verify(model, times(1)).addAttribute("error", "Update failed");
    }

    /**
     * Test deleting existing page.
     * Тест удаления существующей страницы.
     */
    @Test
    void deletePage_ShouldRedirectToList() {
        // Act
        String viewName = pageController.deletePage(1L);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).deletePage(1L);
    }

    /**
     * Test deleting non-existing page.
     * Тест удаления несуществующей страницы.
     */
    @Test
    void deletePage_WhenPageNotExists_ShouldRedirectToList() {
        // Arrange
        doThrow(new IllegalArgumentException("EasyPage not found")).when(pageService).deletePage(999L);

        // Act
        String viewName = pageController.deletePage(999L);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).deletePage(999L);
    }

    /**
     * Test publishing page.
     * Тест публикации страницы.
     */
    @Test
    void publishPage_ShouldRedirectToList() {
        // Act
        String viewName = pageController.publishPage(1L);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).publishPage(1L);
    }

    /**
     * Test unpublishing page.
     * Тест снятия страницы с публикации.
     */
    @Test
    void unpublishPage_ShouldRedirectToList() {
        // Act
        String viewName = pageController.unpublishPage(1L);

        // Assert
        assertEquals("redirect:/pages", viewName);
        verify(pageService, times(1)).unpublishPage(1L);
    }

    /**
     * Test showing public published page.
     * Тест отображения опубликованной публичной страницы.
     */
    @Test
    void showPublicPage_WhenPageExistsAndPublished_ShouldReturnView() {
        // Arrange
        testPage.setPublished(true);
        when(pageService.findPageBySlug("test-page")).thenReturn(Optional.of(testPage));

        // Act
        String viewName = pageController.showPublicPage("test-page", model);

        // Assert
        assertEquals("pages/view", viewName);
        verify(pageService, times(1)).findPageBySlug("test-page");
        verify(model, times(1)).addAttribute("page", testPage);
    }

    /**
     * Test showing public unpublished page.
     * Тест отображения неопубликованной публичной страницы.
     */
    @Test
    void showPublicPage_WhenPageExistsButNotPublished_ShouldReturn404() {
        // Arrange
        testPage.setPublished(false);
        when(pageService.findPageBySlug("unpublished-page")).thenReturn(Optional.of(testPage));

        // Act
        String viewName = pageController.showPublicPage("unpublished-page", model);

        // Assert
        assertEquals("error/404", viewName);
        verify(pageService, times(1)).findPageBySlug("unpublished-page");
        verify(model, never()).addAttribute(eq("page"), any());
    }

    /**
     * Test showing non-existing public page.
     * Тест отображения несуществующей публичной страницы.
     */
    @Test
    void showPublicPage_WhenPageNotExists_ShouldReturn404() {
        // Arrange
        when(pageService.findPageBySlug("non-existing")).thenReturn(Optional.empty());

        // Act
        String viewName = pageController.showPublicPage("non-existing", model);

        // Assert
        assertEquals("error/404", viewName);
        verify(pageService, times(1)).findPageBySlug("non-existing");
        verify(model, never()).addAttribute(eq("page"), any());
    }
}
