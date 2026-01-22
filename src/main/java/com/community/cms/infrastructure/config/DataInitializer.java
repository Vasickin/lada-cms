package com.community.cms.infrastructure.config;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.model.people.User;
import com.community.cms.domain.service.people.UserService;
import com.community.cms.domain.service.page.CustomPageService;
import com.community.cms.domain.service.media.PublicationCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Компонент для инициализации начальных данных в базе данных при запуске приложения.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Создание административных учетных записей при первом запуске</li>
 *   <li>Создание основных страниц сайта по умолчанию</li>
 *   <li>Создание категорий публикации для системы галереи</li>
 *   <li>Настройка ролей и прав доступа по умолчанию</li>
 *   <li>Обеспечение работоспособности системы после развертывания</li>
 * </ul>
 *
 * <p>Учетные записи, страницы и категории создаются только если они еще не существуют в базе данных.
 * Это предотвращает дублирование при повторных запусках приложения.</p>
 *
 * @author Vasickin
 * @version 1.2
 * @since 2025
 * @see UserService
 * @see CustomPageService
 * @see PublicationCategoryService
 * @see User
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final CustomPageService pageService;
    private final PasswordEncoder passwordEncoder;

    // ДОБАВЛЕН НОВЫЙ СЕРВИС
    private PublicationCategoryService publicationCategoryService;

    /**
     * Конструктор с внедрением зависимостей (ОСНОВНОЙ конструктор).
     * Используется Spring'ом для инъекции обязательных зависимостей.
     *
     * @param userService сервис для работы с пользователями
     * @param pageService сервис для работы со страницами
     * @param passwordEncoder кодировщик паролей
     */
    @Autowired
    public DataInitializer(UserService userService, CustomPageService pageService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.pageService = pageService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ДОПОЛНИТЕЛЬНЫЙ сеттер для опциональной зависимости.
     * Используется Spring'ом для инъекции PublicationCategoryService если он существует.
     * Если сервис не создан - метод не вызывается, и поле остается null.
     *
     * @param publicationCategoryService сервис для работы с категориями публикации
     */
    @Autowired(required = false)
    public void setPublicationCategoryService(PublicationCategoryService publicationCategoryService) {
        this.publicationCategoryService = publicationCategoryService;
    }

    /**
     * Метод, выполняемый при запуске приложения.
     * Создает начальные учетные записи пользователей и страницы если они не существуют.
     *
     * @param args аргументы командной строки (не используются)
     * @throws Exception если произошла ошибка при инициализации данных
     */
    @Override
    public void run(String... args) throws Exception {
        createDefaultUsers();
        createDefaultPages();

        // ДОБАВЛЕН НОВЫЙ МЕТОД - вызывается только если сервис существует
        if (publicationCategoryService != null) {
            createDefaultPublicationCategories();
        } else {
            System.out.println("⚠️  PublicationCategoryService не найден. Пропускаем инициализацию категорий.");
        }
    }

    /**
     * Создает учетные записи пользователей по умолчанию.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createDefaultUsers() {
        createAdminUser();
        createEditorUser();
        createTestUser();
    }

    /**
     * Создает основные страницы сайта по умолчанию.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createDefaultPages() {
        System.out.println("🔄 Проверка основных страниц сайта...");

        // Используем метод из CustomPageService для создания страниц
        var createdPages = pageService.initializeSitePages();

        if (!createdPages.isEmpty()) {
            System.out.println("✅ Созданы основные страницы сайта:");
            for (var page : createdPages) {
                System.out.println("   📄 " + page.getTitle() + " (" + page.getSlug() + ")");
            }
        } else {
            System.out.println("✅ Все основные страницы сайта уже существуют");
        }

        // Создаем пример произвольной страницы для демонстрации
        createSampleCustomPage();
    }

    /**
     * НОВЫЙ МЕТОД: Создает категории публикации по умолчанию для системы галереи.
     * Вызывается только если PublicationCategoryService существует.
     */
    private void createDefaultPublicationCategories() {
        System.out.println("\n🔄 Проверка категорий публикации...");

        try {
            // Инициализируем стандартные категории
            publicationCategoryService.initializeDefaultCategories();

            // Получаем количество созданных категорий
            long categoryCount = publicationCategoryService.getCount();

            if (categoryCount > 0) {
                System.out.println("✅ Категории публикации инициализированы: " + categoryCount + " категорий");

                // Показываем список созданных категорий
                var categories = publicationCategoryService.getAllCategories();
                for (var category : categories) {
                    System.out.println("   📂 " + category.getName() +
                            " - " + (category.getDescription() != null && category.getDescription().length() > 50
                            ? category.getDescription().substring(0, 50) + "..."
                            : category.getDescription()));
                }
            } else {
                System.out.println("⚠️  Категории публикации не были созданы");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании категорий публикации: " + e.getMessage());
            // Не бросаем исключение дальше, чтобы не ломать инициализацию других данных
        }
    }

    /**
     * Создает учетную запись администратора системы.
     * Администратор имеет полные права доступа ко всем функциям системы.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createAdminUser() {
        // Проверяем, существует ли уже пользователь с именем admin
        if (userService.userExistsByUsername("admin")) {
            System.out.println("✅ Администратор уже существует");
            return;
        }

        try {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@lada-org.ru");
            admin.setPassword("admin123"); // Пароль будет зашифрован автоматически
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            admin.addRole("ADMIN");
            admin.addRole("EDITOR");
            admin.addRole("USER");

            userService.saveUser(admin);
            System.out.println("✅ Создан администратор: admin / admin123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании администратора: " + e.getMessage());
        }
    }

    /**
     * Создает учетную запись редактора контента.
     * Редактор имеет права на управление контентом, но не на управление пользователями.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createEditorUser() {
        // Проверяем, существует ли уже пользователь с именем editor
        if (userService.userExistsByUsername("editor")) {
            System.out.println("✅ Редактор уже существует");
            return;
        }

        try {
            User editor = new User();
            editor.setUsername("editor");
            editor.setEmail("editor@lada-org.ru");
            editor.setPassword("editor123"); // Пароль будет зашифрован автоматически
            editor.setEnabled(true);
            editor.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            editor.addRole("EDITOR");
            editor.addRole("USER");

            userService.saveUser(editor);
            System.out.println("✅ Создан редактор: editor / editor123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании редактора: " + e.getMessage());
        }
    }

    /**
     * Создает тестовую учетную запись обычного пользователя.
     * Обычный пользователь имеет ограниченные права доступа.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createTestUser() {
        // Проверяем, существует ли уже пользователь с именем user
        if (userService.userExistsByUsername("user")) {
            System.out.println("✅ Тестовый пользователь уже существует");
            return;
        }

        try {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@lada-org.ru");
            user.setPassword("user123"); // Пароль будет зашифрован автоматически
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            user.addRole("USER");

            userService.saveUser(user);
            System.out.println("✅ Создан тестовый пользователь: user / user123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании тестового пользователя: " + e.getMessage());
        }
    }

    /**
     * Создает пример произвольной страницы для демонстрации функциональности.
     * НЕ ИЗМЕНЯЛ - рабочий метод.
     */
    private void createSampleCustomPage() {
        String sampleSlug = "primer-stranicy";
        if (!pageService.pageExistsBySlug(sampleSlug)) {
            var samplePage = new CustomPage(
                    "Пример страницы",
                    """
                    <h2>Добро пожаловать на пример страницы!</h2>
                    <p>Это демонстрационная страница, созданная автоматически при первом запуске приложения.</p>
                    
                    <h3>Что вы можете делать:</h3>
                    <ul>
                        <li>Создавать новые страницы через административную панель</li>
                        <li>Редактировать содержимое страниц с помощью WYSIWYG редактора</li>
                        <li>Публиковать и снимать с публикации страницы</li>
                        <li>Управлять пользователями и их правами доступа</li>
                    </ul>
                    
                    <div class="alert alert-info">
                        <strong>Совет:</strong> Для начала работы перейдите в административную панель 
                        и создайте свои собственные страницы!
                    </div>
                    """,
                    sampleSlug
            );
            samplePage.setMetaDescription("Пример страницы для демонстрации функциональности CMS системы");
            samplePage.setPublished(true);

            pageService.savePage(samplePage);
            System.out.println("✅ Создана демонстрационная страница: " + samplePage.getTitle());
        }
    }

}