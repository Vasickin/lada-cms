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

//    @Value("${file.upload-dir:./data/uploads}")  // ← ДОБАВИЛИ ЭТУ СТРОКУ!
    private String uploadDir;

    @PostConstruct
    public void init() {
        // ТА ЖЕ ЛОГИКА
        String userDir = System.getProperty("user.dir");

        if ("/app".equals(userDir)) {
            uploadDir = "/data/uploads";
        } else {
            uploadDir = "./data/uploads";
        }

        System.out.println("=== StaticResourceConfig INIT ===");
        System.out.println("Upload directory: " + uploadDir);

        java.nio.file.Path uploadsPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath();
        System.out.println("Absolute uploads path: " + uploadsPath);

        try {
            java.nio.file.Files.createDirectories(uploadsPath);
            System.out.println("Uploads directory created/verified: " + uploadsPath);
            System.out.println("Directory exists: " + java.nio.file.Files.exists(uploadsPath));
            System.out.println("Directory is readable: " + java.nio.file.Files.isReadable(uploadsPath));
            System.out.println("Directory is writable: " + java.nio.file.Files.isWritable(uploadsPath));
        } catch (Exception e) {
            System.err.println("ERROR creating uploads directory: " + e.getMessage());
            e.printStackTrace();
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
