package com.community.cms.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./data/uploads}")  // ← ДОБАВИЛИ ЭТУ СТРОКУ!
    private String uploadDir;

    @PostConstruct
    public void init() {
        System.out.println("=== StaticResourceConfig INIT ===");

        // Проверим путь
        Path uploadsDir = Paths.get("/data/uploads").toAbsolutePath();
        System.out.println("Uploads directory: " + uploadsDir);
        System.out.println("Directory exists: " + java.nio.file.Files.exists(uploadsDir));
        System.out.println("Directory is readable: " + java.nio.file.Files.isReadable(uploadsDir));

        // Проверим конкретный файл
        Path testFile = uploadsDir.resolve("0d140be2-96c6-4693-b977-cb52155069a9.jpg");
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
        Path uploadsDir = Paths.get(uploadDir).toAbsolutePath();

        System.out.println("=== Adding resource handler for data/uploads ===");
        System.out.println("URL Pattern: /data/uploads/**");
        System.out.println("File Location: file:" + uploadsDir + "/");

        // Добавляем хендлер ТОЛЬКО для uploads
        registry.addResourceHandler("/data/uploads/**")
                .addResourceLocations("file:" + uploadsDir + "/")
                .setCachePeriod(3600);

        System.out.println("Resource handler added successfully!");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
