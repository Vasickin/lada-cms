package com.community.cms.controller;

import com.community.cms.dto.PageStatistics;
import com.community.cms.model.Page;
import com.community.cms.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminController class.
 * Тесты для класса AdminController.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private GrantedAuthority authority1;

    @Mock
    private GrantedAuthority authority2;

    @InjectMocks
    private AdminController adminController;

    private Page testPage;
    private PageStatistics statistics;

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

        statistics = new PageStatistics(10L, 7L, 3L);
    }

    /**
     * Test dashboard with authenticated user and roles.
     * Тест дашборда с аутентифицированным пользователем и ролями.
     */
    @Test
    void dashboard_WithAuthenticatedUser_ShouldReturnDashboardWithUserInfo() {
        // Arrange
        List<Page> recentPages = Arrays.asList(testPage);
        Collection<GrantedAuthority> authorities = Arrays.asList(authority1, authority2);

        when(pageService.getPageStatistics()).thenReturn(statistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authority1.getAuthority()).thenReturn("ROLE_ADMIN");
        when(authority2.getAuthority()).thenReturn("ROLE_EDITOR");

        // Act
        String viewName = adminController.dashboard(model, authentication);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify model attributes for statistics
        verify(model, times(1)).addAttribute("totalPages", 10L);
        verify(model, times(1)).addAttribute("publishedCount", 7L);
        verify(model, times(1)).addAttribute("draftCount", 3L);
        verify(model, times(1)).addAttribute("recentPages", recentPages);

        // Verify user info
        verify(model, times(1)).addAttribute("currentUsername", "admin");
        verify(model, times(1)).addAttribute("currentUserRoles", Arrays.asList("ROLE_ADMIN", "ROLE_EDITOR"));
        verify(model, times(1)).addAttribute("isAuthenticated", true);
    }

    /**
     * Test dashboard with authenticated user but empty authorities.
     * Тест дашборда с аутентифицированным пользователем без ролей.
     */
    @Test
    void dashboard_WithAuthenticatedUserNoAuthorities_ShouldReturnDashboard() {
        // Arrange
        List<Page> recentPages = Arrays.asList(testPage);
        Collection<GrantedAuthority> authorities = Arrays.asList();

        when(pageService.getPageStatistics()).thenReturn(statistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String viewName = adminController.dashboard(model, authentication);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify user info with empty roles
        verify(model, times(1)).addAttribute("currentUsername", "user");
        verify(model, times(1)).addAttribute("currentUserRoles", Arrays.asList());
        verify(model, times(1)).addAttribute("isAuthenticated", true);
    }

    /**
     * Test dashboard with null authentication.
     * Тест дашборда без аутентификации (null authentication).
     */
    @Test
    void dashboard_WithNullAuthentication_ShouldReturnDashboardWithGuestInfo() {
        // Arrange
        List<Page> recentPages = Arrays.asList(testPage);

        when(pageService.getPageStatistics()).thenReturn(statistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);

        // Act
        String viewName = adminController.dashboard(model, null);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify guest info
        verify(model, times(1)).addAttribute("isAuthenticated", false);
        verify(model, times(1)).addAttribute("currentUsername", "Гость");
        verify(model, times(1)).addAttribute("currentUserRoles", List.of("ROLE_ANONYMOUS"));
    }

    /**
     * Test dashboard with unauthenticated user.
     * Тест дашборда с неаутентифицированным пользователем.
     */
    @Test
    void dashboard_WithUnauthenticatedUser_ShouldReturnDashboardWithGuestInfo() {
        // Arrange
        List<Page> recentPages = Arrays.asList(testPage);

        when(pageService.getPageStatistics()).thenReturn(statistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        String viewName = adminController.dashboard(model, authentication);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify guest info
        verify(model, times(1)).addAttribute("isAuthenticated", false);
        verify(model, times(1)).addAttribute("currentUsername", "Гость");
        verify(model, times(1)).addAttribute("currentUserRoles", List.of("ROLE_ANONYMOUS"));
    }

    /**
     * Test dashboard with empty recent pages.
     * Тест дашборда с пустым списком последних страниц.
     */
    @Test
    void dashboard_WithEmptyRecentPages_ShouldReturnDashboard() {
        // Arrange
        List<Page> recentPages = Arrays.asList();
        Collection<GrantedAuthority> authorities = Arrays.asList(authority1);

        when(pageService.getPageStatistics()).thenReturn(statistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authority1.getAuthority()).thenReturn("ROLE_ADMIN");

        // Act
        String viewName = adminController.dashboard(model, authentication);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify model attributes
        verify(model, times(1)).addAttribute("totalPages", 10L);
        verify(model, times(1)).addAttribute("publishedCount", 7L);
        verify(model, times(1)).addAttribute("draftCount", 3L);
        verify(model, times(1)).addAttribute("recentPages", recentPages);
    }

    /**
     * Test dashboard with zero statistics.
     * Тест дашборда с нулевой статистикой.
     */
    @Test
    void dashboard_WithZeroStatistics_ShouldReturnDashboard() {
        // Arrange
        PageStatistics zeroStatistics = new PageStatistics(0L, 0L, 0L);
        List<Page> recentPages = Arrays.asList();
        Collection<GrantedAuthority> authorities = Arrays.asList(authority1);

        when(pageService.getPageStatistics()).thenReturn(zeroStatistics);
        when(pageService.findRecentPages(5)).thenReturn(recentPages);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authority1.getAuthority()).thenReturn("ROLE_ADMIN");

        // Act
        String viewName = adminController.dashboard(model, authentication);

        // Assert
        assertEquals("admin/dashboard", viewName);

        // Verify statistics
        verify(pageService, times(1)).getPageStatistics();
        verify(pageService, times(1)).findRecentPages(5);

        // Verify model attributes with zero values
        verify(model, times(1)).addAttribute("totalPages", 0L);
        verify(model, times(1)).addAttribute("publishedCount", 0L);
        verify(model, times(1)).addAttribute("draftCount", 0L);
        verify(model, times(1)).addAttribute("recentPages", recentPages);
    }
}
