package com.community.cms.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @PostConstruct
    public void init() {
        System.out.println("=== StaticResourceConfig INIT ===");

        // Проверим путь
        Path uploadsDir = Paths.get("uploads").toAbsolutePath();
        System.out.println("Uploads directory: " + uploadsDir);
        System.out.println("Directory exists: " + java.nio.file.Files.exists(uploadsDir));
        System.out.println("Directory is readable: " + java.nio.file.Files.isReadable(uploadsDir));

        // Проверим конкретный файл
        Path testFile = uploadsDir.resolve("61a4c362-18bd-4241-88a7-3bbbce120384.png");
        System.out.println("Test file: " + testFile);
        System.out.println("Test file exists: " + java.nio.file.Files.exists(testFile));
        if (java.nio.file.Files.exists(testFile)) {
            try {
                System.out.println("Test file size: " + java.nio.file.Files.size(testFile) + " bytes");
            } catch (Exception e) {
                System.out.println("Error getting file size: " + e.getMessage());
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsDir = Paths.get("uploads").toAbsolutePath();

        System.out.println("=== Adding resource handler for uploads ===");
        System.out.println("URL Pattern: /uploads/**");
        System.out.println("File Location: file:" + uploadsDir + "/");

        // Добавляем хендлер ТОЛЬКО для uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsDir + "/")
                .setCachePeriod(3600);

        System.out.println("Resource handler added successfully!");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
