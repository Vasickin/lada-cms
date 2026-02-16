package com.community.cms.infrastructure.scheduler;

import com.community.cms.domain.service.content.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик задач для автоматического обновления статусов проектов.
 * <p>
 * Запускает проверку статусов проектов по расписанию и обновляет их
 * в зависимости от текущей даты (UPCOMING → ACTIVE → COMPLETED).
 * </p>
 */
@Component
public class ProjectStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProjectStatusScheduler.class);

    private final ProjectService projectService;

    public ProjectStatusScheduler(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Ежедневное обновление статусов проектов.
     * <p>
     * Запускается каждый день в 00:05 ночи.
     * Этого достаточно, так как статусы зависят только от даты,
     * а не от времени суток.
     * </p>
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void updateProjectStatusesDaily() {
        log.info("⏰ Запуск ежедневного обновления статусов проектов (00:05)");
        long startTime = System.currentTimeMillis();

        try {
            projectService.updateProjectStatusesAutomatically();

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Ежедневное обновление статусов завершено за {} мс", duration);

        } catch (Exception e) {
            log.error("❌ Ошибка при ежедневном обновлении статусов проектов", e);
        }
    }

//    /**
//     * Дополнительная проверка в рабочее время (опционально).
//     * <p>
//     * Запускается каждый час с 9 до 18 в будние дни.
//     * Можно раскомментировать, если нужно более частое обновление.
//     * </p>
//     */
//    /*
//    @Scheduled(cron = "0 0 9-18 * * MON-FRI")
//    public void updateProjectStatusesHourly() {
//        log.debug("Почасовая проверка статусов проектов");
//        projectService.updateProjectStatusesAutomatically();
//    }
//    */
//
//    /**
//     * Экстренное обновление при запуске приложения (опционально).
//     * <p>
//     * Запускается через 1 минуту после старта приложения.
//     * Нужно, чтобы привести статусы в порядок после возможного простоя.
//     * </p>
//     */
//    /*
//    @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE)
//    public void updateProjectStatusesOnStartup() {
//        log.info("🚀 Первоначальное обновление статусов после запуска");
//        projectService.updateProjectStatusesAutomatically();
//    }
//    */
}
