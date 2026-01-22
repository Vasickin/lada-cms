package com.community.cms.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Конфигурация для включения JPA Auditing функциональности.
 *
 * <p>Аннотация {@link EnableJpaAuditing} активирует автоматическое заполнение
 * полей с аннотациями {@link org.springframework.data.annotation.CreatedDate}
 * и {@link org.springframework.data.annotation.LastModifiedDate}.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see org.springframework.data.jpa.repository.config.EnableJpaAuditing
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.community.cms.domain.repository")
@EntityScan(basePackages = "com.community.cms.domain.model")
@EnableTransactionManagement  // Включает управление транзакциями
public class JpaConfig {
    /**
     * // Конфигурация включается через аннотацию @EnableJpaAuditing
     *     // Конфигурация для JPA и Hibernate
     *     // @EnableJpaRepositories - говорит Spring где искать репозитории
     *     // @EntityScan - говорит Spring где искать entity-классы
     *     // @EnableJpaAuditing - включает авто-заполнение дат (@CreatedDate, @LastModifiedD
     */

    // Явное создание менеджера транзакций (опционально)
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
