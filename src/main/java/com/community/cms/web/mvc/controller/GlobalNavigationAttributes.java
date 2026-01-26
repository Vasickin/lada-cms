package com.community.cms.web.mvc.controller;

import com.community.cms.domain.service.content.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalNavigationAttributes {

    @Autowired
    private ProjectService projectService;

    @ModelAttribute("projectCategories")
    public List<String> getProjectCategories() {
        try {
            List<String> categories = projectService.findAllDistinctCategories();

            if (categories != null) {
                return categories.stream()
                        .filter(category -> category != null && !category.trim().isEmpty())
                        .distinct()
                        .sorted()
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении категорий проектов: " + e.getMessage());
        }

        return java.util.Collections.emptyList();
    }
}
