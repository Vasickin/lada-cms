# 🏗️ РЕФАКТОРИНГ АРХИТЕКТУРЫ — COMMUNITY CMS

**🔖 Идентификатор:** `REFACTOR-ARCH-001`  
**📌 Версия:** `1.0.0`  
**📅 Дата создания:** `[ДД.ММ.ГГГГ]`  
**👤 Автор:** `[ВАШЕ ИМЯ]`  
**🌿 Ветка:** `refactor/architecture-cleanup`  
**🎯 Целевая ветка:** `main`  
**📊 Статус:** `🟡 В РАБОТЕ`

---

## 📋 СОДЕРЖАНИЕ

1. [📖 Краткое описание](#-краткое-описание)
2. [🎯 Цели и задачи](#-цели-и-задачи)
3. [📁 Область применения](#-область-применения)
4. [⏳ Сроки и этапы](#-сроки-и-этапы)
5. [🏛️ Целевая архитектура](#️-целевая-архитектура)
    - [5.1 Диаграмма слоев](#51-диаграмма-слоев)
    - [5.2 Детальная структура](#52-детальная-структура)
6. [🔧 Подробный план работ](#-подробный-план-работ)
    - [6.1 Этап 1: Подготовка](#61-этап-1-подготовка)
    - [6.2 Этап 2: Domain слой](#62-этап-2-domain-слой)
    - [6.3 Этап 3: MVC слой — Проекты](#63-этап-3-mvc-слой--проекты)
    - [6.4 Этап 4: MVC слой — Остальные модули](#64-этап-4-mvc-слой--остальные-модули)
    - [6.5 Этап 5: Infrastructure слой](#65-этап-5-infrastructure-слой)
    - [6.6 Этап 6: Завершение](#66-этап-6-завершение)
7. [✅ Критерии приемки](#-критерии-приемки)
8. [⚠️ Риски и митигация](#️-риски-и-митигация)
9. [🛠️ Инструменты и команды](#️-инструменты-и-команды)
10. [📊 Трекер прогресса](#-трекер-прогресса)
11. [📝 Приложения](#-приложения)

---

## 📖 КРАТКОЕ ОПИСАНИЕ

> **Рефакторинг текущей "плоской" архитектуры проекта в многослойную (layered architecture) для улучшения поддерживаемости, тестируемости и подготовки к масштабированию.**

**Текущее состояние:**
- ❌ Все классы в одном пакете `com.community.cms`
- ❌ Смешение ответственности в контроллерах
- ❌ Бизнес-логика размазана по слоям
- ❌ Нет четкого разделения на Domain/Web/Infrastructure

**Целевое состояние:**
- ✅ Четкое разделение слоев (Domain ← Web ← Infrastructure)
- ✅ "Тонкие" контроллеры, "толстые" сервисы
- ✅ Возможность легко добавить REST API
- ✅ Профессиональная enterprise-структура

---

## 🎯 ЦЕЛИ И ЗАДАЧИ

### 🎯 **Основные цели:**
1. **Архитектурная чистота** — разделение ответственности между слоями
2. **Поддерживаемость** — легкость внесения изменений одним разработчиком
3. **Масштабируемость** — подготовка к добавлению API/мобильного приложения
4. **Тестируемость** — изоляция бизнес-логики для unit-тестов

### 📋 **Конкретные задачи:**
- [ ] Реорганизовать пакетную структуру согласно DDD/Onion Architecture
- [ ] Выделить чистый Domain слой (без зависимостей от Spring)
- [ ] Разделить Web слой на MVC (Thymeleaf) и API (для будущего)
- [ ] Вынести инфраструктурные компоненты в отдельный слой
- [ ] Создать мапперы для преобразования между слоями
- [ ] Написать базовые интеграционные тесты

---

## 📁 ОБЛАСТЬ ПРИМЕНЕНИЯ

**Входит в scope:**
- ✅ Реструктуризация Java-пакетов
- ✅ Рефакторинг контроллеров, сервисов, репозиториев
- ✅ Создание DTO, мапперов, валидаторов
- ✅ Обновление импортов и конфигураций
- ✅ Написание smoke-тестов

**Не входит в scope:**
- ❌ Изменение бизнес-логики
- ❌ Рефакторинг Thymeleaf шаблонов (кроме минимальных правок путей)
- ❌ Изменение базы данных или миграции
- ❌ Добавление новой функциональности

---

## ⏳ СРОКИ И ЭТАПЫ

**Общая продолжительность:** `4–6 недель`  
**Дата начала:** `[ДД.ММ.ГГГГ]`  
**Планируемая дата завершения:** `[ДД.ММ.ГГГГ + 6 недель]`

### 📊 **Дорожная карта:**

# 📦 СТРУКТУРА ПРОЕКТА:

```shell
📦📦 community-cms/
├── 📁 src/main/
│   ├── 🏛️ java/com/community/cms/
│   │   │
│   │   ├── 🚀 CmsApplication.java                                 # ✅ Точка входа
│   │   │
│   │   ├── 📁 domain/                                             # ✅ ЯДРО - бизнес-логика
│   │   │   │
│   │   │   ├── 🎭 enums/                                          # ✅ ПЕРЕЧИСЛЕНИЯ
│   │   │   │   ├── 🗃️ MediaType.java                              # ✅ PHOTO, VIDEO, DOCUMENT
│   │   │   │   └── 🗃️ PageType.java                               # ✅ ABOUT, PROJECTS, GALLERY, etc.
│   │   │   │
│   │   │   ├── 🧩 model/                                          # ✅ СУЩНОСТИ (Entity)
│   │   │   │   ├── 📁 content/                                    # ✅ КОНТЕНТ
│   │   │   │   │   ├── 🗃️ About.java                              # ✅ Страница "О нас"
│   │   │   │   │   ├── 🗃️ PhotoGallery.java                       # ✅ Фото-галереи (альбомы)
│   │   │   │   │   ├── 🗃️ Project.java                            # 🔗 Проекты организации
│   │   │   │   │   └── 🗃️ VideoGallery.java                       # ✅ Видео-галереи (альбомы)
│   │   │   │   │
│   │   │   │   ├── 📁 media/                                      # ✅ МЕДИА ФАЙЛЫ
│   │   │   │   │   ├── 🗃️ MediaFile.java                          # ✅ Универсальный медиа-файл
│   │   │   │   │   └── 🗃️ PublicationCategory.java                # ✅ Категории публикаций
│   │   │   │   │
│   │   │   │   ├── 📁 page/                                       # ✅ СТРАНИЦЫ
│   │   │   │   │   └── 🗃️ CustomPage.java                         # ✅ Кастомные страницы
│   │   │   │   │
│   │   │   │   ├── 📁 people/                                     # ✅ ЛЮДИ
│   │   │   │   │   ├── 🗃️ User.java                               # ✅ Пользователи системы
│   │   │   │   │   ├── 🗃️ TeamMember.java                         # ✅ Члены команды
│   │   │   │   │   └── 🗃️ Partner.java                            # 🔗 | 🔨 Партнеры организации
│   │   │   │   │
│   │   │   │   └── 📁 shared/                                     # ⬜ ПУСТО (можно добавить общие компоненты)
│   │   │   │
│   │   │   ├── 💾 repository/                                     # ✅ РЕПОЗИТОРИИ (интерфейсы JPA)
│   │   │   │   ├── 📁 content/                                    # ✅ 1:1 с моделями content
│   │   │   │   │   ├── 🗃️ AboutRepository.java
│   │   │   │   │   ├── 🗃️ PhotoGalleryRepository.java
│   │   │   │   │   ├── 🗃️ ProjectRepository.java
│   │   │   │   │   └── 🗃️ VideoGalleryRepository.java
│   │   │   │   │
│   │   │   │   ├── 📁 media/                                      # ✅ 1:1 с моделями media
│   │   │   │   │   ├── 🗃️ MediaFileRepository.java
│   │   │   │   │   └── 🗃️ PublicationCategoryRepository.java
│   │   │   │   │
│   │   │   │   ├── 📁 page/                                        # ✅ 1:1 с моделями page
│   │   │   │   │   └── 🗃️ CustomPageRepository.java
│   │   │   │   │
│   │   │   │   └── 📁 people/                                      # ✅ 1:1 с моделями people
│   │   │   │       ├── 🗃️ PartnerRepository.java
│   │   │   │       ├── 🗃️ TeamMemberRepository.java
│   │   │   │       └── 🗃️ UserRepository.java
│   │   │   │
│   │   │   └── ⚙️ service/                                         # ✅ СЕРВИСЫ (бизнес-логика)
│   │   │       ├── 📁 content/                                     # ✅ 1:1 с репозиториями content
│   │   │       │   ├── 🗃️ AboutService.java
│   │   │       │   ├── 🗃️ PhotoGalleryService.java
│   │   │       │   ├── 🗃️ ProjectService.java
│   │   │       │   └── 🗃️ VideoGalleryService.java
│   │   │       │
│   │   │       ├── 📁 media/                                       # ✅ 1:1 с репозиториями media
│   │   │       │   └── 🗃️ PublicationCategoryService.java
│   │   │       │
│   │   │       ├── 📁 page/                                        # ✅ 1:1 с репозиториями page
│   │   │       │   └── 🗃️ CustomPageService.java
│   │   │       │
│   │   │       └── 📁 people/                                      # ✅ 1:1 с репозиториями people
│   │   │           ├── 🗃️ PartnerService.java
│   │   │           ├── 🗃️ TeamMemberService.java
│   │   │           └── 🗃️ UserService.java
│   │   │
│   │   ├── 🏗️ infrastructure/                                      # 🏗️ ИНФРАСТРУКТУРА
│   │   │   ├── 📁 config/                                          # ✅ КОНФИГУРАЦИИ
│   │   │   │   ├── 🗃️ DataInitializer.java                         # ✅ Инициализация тестовых данных
│   │   │   │   ├── 🗃️ JpaConfig.java                               # ⚠️ Нужно добавить @EnableJpaRepositories
│   │   │   │   └── 🗃️ SecurityConfig.java                          # ✅ Конфигурация безопасности
│   │   │   │
│   │   │   ├── 📁 persistence/                                     # ⬜ ПУСТО (можно для JPA реализаций)
│   │   │   │
│   │   │   └── 📁 storage/                                         # ✅ ФАЙЛОВОЕ ХРАНИЛИЩЕ
│   │   │       └── 🗃️ FileStorageService.java                      # ✅ Управление загрузкой файлов
│   │   │
│   │   ├── 📁 util/                                                # ⬜ ПУСТО (можно для утилит)
│   │   │
│   │   ├── 📁 validation/                                          # ✅ ВАЛИДАЦИЯ (кастомная)
│   │   │   ├── 🗃️ VideoUrl.java                                    # ✅ Аннотация валидации URL видео
│   │   │   └── 🗃️ VideoUrlValidator.java                           # ✅ Валидатор для URL видео
│   │   │
│   │   └── 🌐 web/                                                 # 🌐 ВЕБ-СЛОЙ
│   │       └── 📁 mvc/                                             # Thymeleaf MVC (текущая реализация)
│   │           ├── 📁 controller/                                  # 🚀 КОНТРОЛЛЕРЫ
│   │           │   ├── 📁 admin/                                   # ✅ АДМИНКА (полная)
│   │           │   │   ├── 📁 content/                             # ✅ Управление контентом
│   │           │   │   │   ├── 🗃️ AboutAdminController.java        # ✅ /admin/about
│   │           │   │   │   ├── 🗃️ PhotoGalleryAdminController.java # ✅ /admin/photo-gallery
│   │           │   │   │   ├── 🗃️ ProjectAdminController.java      # ✅ /admin/projects
│   │           │   │   │   └── 🗃️ VideoGalleryAdminController.java # ✅ /admin/video-gallery
│   │           │   │   │
│   │           │   │   ├── 📁 page/                                # ✅ Управление кастомными страницами
│   │           │   │   │   └── 🗃️ AdminPageCustomController.java   # ✅ /admin/pages (create/edit/delete)
│   │           │   │   │
│   │           │   │   ├── 📁 people/                              # ✅ Управление людьми
│   │           │   │   │   ├── 🗃️ PartnerAdminController.java      # ✅ /admin/partners
│   │           │   │   │   ├── 🗃️ TeamMemberAdminController.java   # ✅ /admin/team
│   │           │   │   │   └── 🗃️ UserAdminController.java         # ✅ /admin/users
│   │           │   │   │
│   │           │   │   ├── 🗃️ AdminController.java                 # ✅ /admin (дашборд)
│   │           │   │   └── 🗃️ LoginController.java                 # ✅ /login (авторизация)
│   │           │   │
│   │           │   └── 📁 public_page/                             # ⚠️ ПУБЛИЧНАЯ ЧАСТЬ (неполная)
│   │           │       ├── 🗃️ HomeController.java                  # ✅ / (главная страница)
│   │           │       └── 🗃️ TeamMemberController.java            # ✅ /team (публичная команда)
│   │           │       ❌ НЕТ: ProjectController, GalleryController, CustomPageController
│   │           │
│   │           ├── 📁 dto/                                        # 📋 DTO объекты (частично)
│   │           │   ├── 📁 content/                                # 🚧 В разработке
│   │           │   │   ├── 🗃️ GalleryDTO.java                     # 🚧 Создан
│   │           │   │   └── 🗃️ PhotoDTO.java                       # 🚧 Создан
│   │           │   │
│   │           │   ├── 📁 page/                                   # ⬜ ПУСТО (нужно создать)
│   │           │   │
│   │           │   ├── 🗃️ PageStatistics.java                     # ✅ Готово (существует)
│   │           │   │
│   │           │   └── 📁 people/                                 # ✅ Готово
│   │           │       └── 🗃️ TeamMemberDTO.java                  # ✅ Готово (существует)
│   │           │
│   │           ├── 📁 form/                                       # 🚧 В разработке
│   │           │   ├── 📁 content/                                # 🚧 В разработке
│   │           │   │   ├── 🗃️ AboutForm.java                      # ✅ Перемещён
│   │           │   │   ├── 🗃️ ProjectForm.java                    # ✅ Перемещён
│   │           │   │   ├── 🚧 PhotoGalleryForm.java               # 🚧 Создаём сейчас
│   │           │   │   └── 🗃️ VideoGalleryForm.java               # ✅ Перемещён
│   │           │   │
│   │           │   ├── 📁 page/                                   # ⬜ ПУСТО (нужно создать CustomPageForm)
│   │           │   │
│   │           │   └── 📁 people/                                 # 🚧 В разработке
│   │           │       ├── 🗃️ UserForm.java                       # ⬜ Нужно создать
│   │           │       ├── 🗃️ TeamMemberForm.java                 # ✅ Перемещён
│   │           │       └── 🗃️ PartnerForm.java                    # ✅ Перемещён
│   │           │
│   │           ├── 📁 mapper/                                     # ⬜ ПУСТО (📋 запланировано)
│   │           │
│   │           └── 📁 validation/                                 # 🚧 В разработке
│   │               ├── 🗃️ CustomErrorController.java              # 🚧 Создан
│   │               └── 📁 validators/                             # 📋 Запланировано
│   │                   └── 📋 ProjectValidator.java                # 📋 Запланировано
│   │
│   └── 📁 resources/                                              # 📦 РЕСУРСЫ
│       ├── 📄 application.properties                              # ✅ Основные настройки
│       ├── 📄 application.properties.example                      # 📋 Пример настроек
│       ├── 📄 messages.properties                                 # 🌐 Локализация (основная)
│       ├── 📄 messages_ru_RU.properties                           # 🌐 Локализация (русский)
│       ├── 📄 messages_en_US.properties                           # 🌐 Локализация (английский)
│       │
│       ├── 📁 css/                                                # 🎨 CSS (бэкенд)
│       │   └── 🎨 design-system.css
│       │
│       ├── 📁 static/                                             # 📁 Статические файлы
│       │   ├── 🗃️ 5ffa8a2c-1e4e-4720-8576-1b30171b5c6f.jpg        # 🖼️ Загруженное изображение
│       │   │
│       │   ├── 📁 css/                                            # 🎨 CSS для фронтенда
│       │   │   ├── 🎨 design-system.css                           # ✅ Основные стили
│       │   │   ├── 🎨 photo-gallery.css                           # ✅ Стили галереи
│       │   │   ├── 🎨 photo-gallery-form.css                      # ✅ Стили формы галереи
│       │   │   └── 🎨 photo-gallery-preview.css                   # ✅ Стили превью галереи
│       │   │
│       │   ├── 📁 images/                                         # 🖼️ Изображения
│       │   │   └── 📁 optimized/                                  # 🖼️ Оптимизированные изображения
│       │   │       ├── 🗃️ header-bg.jpg
│       │   │       ├── 🗃️ header-bg.webp
│       │   │       ├── 🗃️ logo@2x.png
│       │   │       └── 🗃️ logo.png
│       │   │
│       │   └── 📁 js/                                             # 📜 JavaScript
│       │       ├── 📜 photo-gallery-form.js                       # ✅ Скрипт формы галереи
│       │       └── 📜 photo-gallery.js                            # ✅ Скрипт галереи
│       │
│       └── 📁 templates/                                          # 📋 Шаблоны Thymeleaf
│           ├── 📄 index.html                                      # ✅ Главная страница
│           ├── 📄 about.html                                      # ✅ Страница "О нас"
│           ├── 📄 contact.html                                    # ✅ Контакты
│           ├── 📄 gallery.html                                    # ✅ Галерея
│           ├── 📄 projects.html                                   # ✅ Проекты
│           ├── 📄 patrons.html                                    # ✅ Партнеры
│           ├── 📄 team.html                                       # ✅ Команда
│           ├── 📄 login.html                                      # ✅ Вход в систему
│           │
│           ├── 📁 admin/                                          # 🏢 Админ панель
│           │   ├── 📄 dashboard.html                              # ✅ Дашборд
│           │   │
│           │   ├── 📁 photo-gallery/                              # 🖼️ Управление фото-галереями
│           │   │   ├── 📄 create.html                             # ✅ Создание
│           │   │   ├── 📄 edit.html                               # ✅ Редактирование
│           │   │   ├── 📄 list.html                               # ✅ Список
│           │   │   └── 📄 preview.html                            # ✅ Превью
│           │   │
│           │   ├── 📁 projects/                                   # 📊 Управление проектами
│           │   │   ├── 📄 create.html                             # ✅ Создание
│           │   │   ├── 📄 edit.html                               # ✅ Редактирование
│           │   │   ├── 📄 list.html                               # ✅ Список
│           │   │   ├── 📄 project-team-management.html            # 👥 Управление командой проекта
│           │   │   └── 📄 project-videos-management.html          # 🎥 Управление видео проекта
│           │   │
│           │   ├── 📁 project-videos/                             # 🎥 Управление видео
│           │   │   ├── 📄 create.html                             # ✅ Создание
│           │   │   ├── 📄 edit.html                               # ✅ Редактирование
│           │   │   └── 📄 list.html                               # ✅ Список
│           │   │
│           │   ├── 📁 site-pages/                                 # 📄 Управление кастомными страницами
│           │   │   ├── 📄 edit.html                               # ✅ Редактирование
│           │   │   └── 📄 list.html                               # ✅ Список
│           │   │
│           │   ├── 📁 team-members/                               # 👥 Управление командой
│           │   │   ├── 📄 create.html                             # ✅ Создание
│           │   │   ├── 📄 edit.html                               # ✅ Редактирование
│           │   │   └── 📄 list.html                               # ✅ Список
│           │   │
│           │   └── 📁 users/                                      # 👤 Управление пользователями
│           │       ├── 📄 create.html                             # ✅ Создание
│           │       ├── 📄 edit.html                               # ✅ Редактирование
│           │       └── 📄 list.html                               # ✅ Список
│           │
│           ├── 📁 error/                                          # 🚨 Страницы ошибок
│           │   ├── 📄 400.html                                    # ✅ Плохой запрос
│           │   ├── 📄 403.html                                    # ✅ Доступ запрещен
│           │   ├── 📄 404.html                                    # ✅ Не найдено
│           │   ├── 📄 500.html                                    # ✅ Внутренняя ошибка
│           │   └── 📄 access-denied.html                          # ✅ Отказано в доступе
│           │
│           ├── 📁 fragments/                                      # 🧩 Фрагменты шаблонов
│           │   ├── 📄 banner.html                                 # ✅ Баннер
│           │   ├── 📄 footer.html                                 # ✅ Футер
│           │   ├── 📄 header.html                                 # ✅ Хедер
│           │   ├── 📄 layout.html                                 # ✅ Основной layout
│           │   ├── 📄 navigation.html                             # ✅ Навигация
│           │   └── 📄 scripts.html                                # ✅ Скрипты
│           │
│           ├── 📁 pages/                                          # 📄 Кастомные страницы
│           │   ├── 📄 create.html                                 # ✅ Создание
│           │   ├── 📄 edit.html                                   # ✅ Редактирование
│           │   ├── 📄 list.html                                   # ✅ Список
│           │   └── 📄 view.html                                   # 🔍 Публичный просмотр
│           │
│           └── 📁 public/                                         # ⬜ ПУСТО (для публичных шаблонов)
│
└── 📁 target/                                                     # 🏗️ Сборка Maven (не в репозитории)                                                        # 🏗️ Сборка Maven (не в репозитории)
```

# 🎨 ЛЕГЕНДА С ИКОНКАМИ

## 📊 Статусы файлов и директорий

| Иконка | Статус | Описание |
|--------|--------|----------|
| ✅ | **Готово** | Работает, протестировано, в продакшене |
| 🔄 | **В процессе** | Активно разрабатывается, частично работает |
| ⚠️ | **Требует внимания** | Есть проблемы, нужно доработать |
| ❌ | **Отсутствует** | Не существует, нужно создать |
| ⬜ | **Пусто/опционально** | Можно добавить, но не обязательно |
| 🚧 | **В разработке** | В активной разработке |
| 🧪 | **Тестируется** | На стадии тестирования |
| 📋 | **Запланировано** | В планах на разработку |
| 🔍 | **На ревью** | На проверке/ревью кода |

## 🔧 Типы задач и действий

| Иконка | Категория | Описание |
|--------|-----------|----------|
| 🔨 | **Рефакторинг** | Улучшение кода без изменения функционала |
| 🧹 | **Очистка кода** | Удаление мусора, упрощение кода |
| 🏗️ | **Перестройка** | Значительные архитектурные изменения |
| 📦 | **Реорганизация** | Перемещение/переименование файлов |
| 🔗 | **Интеграция** | Связывание компонентов, зависимости |
| 🐛 | **Отладка** | Поиск и исправление ошибок |
| ⚡ | **Оптимизация** | Улучшение производительности |
| 🔒 | **Безопасность** | Работа с безопасностью, авторизацией |
| 📚 | **Документация** | Написание/обновление документации |

## 📁 Типы файлов и директорий

| Иконка | Тип | Описание |
|--------|-----|----------|
| 📁 | **Директория** | Папка/каталог |
| 🗃️ | **Файл Java** | Java-класс/интерфейс |
| 📄 | **Файл ресурсов** | HTML, CSS, JS, свойства |
| 🖼️ | **Изображение** | Графические файлы |
| 📜 | **Скрипт** | JavaScript файлы |
| 🎨 | **Стили** | CSS файлы |

## 🏛️ Доменные сущности

| Иконка | Слой | Описание |
|--------|------|----------|
| 🎭 | **Перечисления** | Enum классы |
| 🧩 | **Модели** | Entity сущности |
| 💾 | **Репозитории** | JPA Repository интерфейсы |
| ⚙️ | **Сервисы** | Бизнес-логика |
| 🌐 | **Веб-слой** | Контроллеры, DTO, Form |

## 🌐 Веб-компоненты

| Иконка | Компонент | Описание |
|--------|-----------|----------|
| 🏢 | **Админка** | Административная панель |
| 👤 | **Пользователи** | Управление пользователями |
| 👥 | **Команда** | Члены команды |
| 📊 | **Проекты** | Проекты организации |
| 🖼️ | **Галерея** | Фото/видео галереи |
| 📄 | **Страницы** | Кастомные страницы |

## ⚙️ Инфраструктура

| Иконка | Компонент | Описание |
|--------|-----------|----------|
| 🏗️ | **Инфраструктура** | Конфигурация, хранилище |
| 🔧 | **Конфигурация** | Настройки приложения |
| 💾 | **Хранилище** | Файловое хранилище |
| 🛠️ | **Утилиты** | Вспомогательные классы |
| 📋 | **Валидация** | Кастомная валидация |

## 🎯 Цветовая схема статусов

| Цвет | Статус | Настроение |
|------|--------|------------|
| 🟢 | ✅ Готово | Успех, завершено |
| 🟡 | ⚠️ Внимание | Предупреждение, требует доработки |
| 🔴 | ❌ Отсутствует | Критично, нужно создать |
| 🔵 | 🔄 В процессе | Активная работа |
| 🟣 | 🚧 В разработке | Новый функционал |
| ⚪ | ⬜ Пусто | Нейтрально, опционально |
# <------------------------------------>
## 🔍 АНАЛИЗ ЗАВИСИМОСТЕЙ:

### 1. Кто использует модели (entity):

```shell
# Например: кто использует CustomPage.java?
find src/main/java -name "*.java" -type f -exec grep -l "import.*\.Page;" {} \;

# Для всех моделей:
for entity in $(find src/main/java -path "*/model/*" -name "*.java"); do
    echo "=== $(basename $entity .java) ==="
    find src/main/java -name "*.java" -type f -exec grep -l "import.*$(basename $entity .java);" {} \;
    echo
done
```

### 2. Кто использует репозитории:

```shell
# Например: кто использует CustomPageRepository?
find src/main/java -name "*.java" -type f -exec grep -l "PageRepository" {} \;

# Все репозитории:
for repo in $(find src/main/java -name "*Repository.java"); do
    echo "=== $(basename $repo .java) ==="
    grep -l "$(basename $repo .java)" src/main/java/com/community/cms/domain/service/*/*.java 2>/dev/null
    echo
done
```

### 3. Кто использует сервисы:

```shell
# Например: кто использует CustomPageService?
find src/main/java -name "*.java" -type f -exec grep -l "PageService" {} \;

# Все сервисы:
for service in $(find src/main/java -name "*Service.java"); do
    echo "=== $(basename $service .java) ==="
    grep -l "$(basename $service .java)" src/main/java/com/community/cms/web/mvc/controller/**/*.java 2>/dev/null
    echo
done
```

### 4. Кто использует контроллеры (редко, но может быть):

```shell
# Какие шаблоны используют контроллеры?
find src/main/resources/templates -name "*.html" -type f | xargs grep -l "th:action\|th:href" | sort
```

## 🗺️ КОМПЛЕКСНЫЙ АНАЛИЗ:

### Создаём карту зависимостей для Page (пример):

```shell
echo "=== КАРТА ЗАВИСИМОСТЕЙ Page ==="
echo "Модель: Page.java"
echo "Используется в:"
find src/main/java -name "*.java" -type f -exec grep -l "import.*\.Page;" {} \;
echo ""
echo "Репозиторий: PageRepository.java"
echo "Используется в сервисе:"
grep -l "PageRepository" src/main/java/com/community/cms/domain/service/page/CustomPageService.java
echo ""
echo "Сервис: PageService.java"
echo "Используется в контроллерах:"
grep -l "PageService" src/main/java/com/community/cms/web/mvc/controller/**/*.java 2>/dev/null
```

## Быстрый анализ всех цепочек:

```shell
# Создадим файл с анализом:
cat > /tmp/dependencies.txt << 'EOF'
АНАЛИЗ ЗАВИСИМОСТЕЙ COMMUNITY CMS
=================================

EOF

for model in $(find src/main/java -path "*/model/*" -name "*.java" | xargs basename -s .java); do
    echo "=== $model ===" >> /tmp/dependencies.txt
    
    # Репозиторий
    repo="${model}Repository"
    if [ -f "src/main/java/com/community/cms/domain/repository/*/${repo}.java" ]; then
        echo "📦 Репозиторий: $repo" >> /tmp/dependencies.txt
    fi
    
    # Сервис  
    service="${model}Service"
    if [ -f "src/main/java/com/community/cms/domain/service/*/${service}.java" ]; then
        echo "⚙️  Сервис: $service" >> /tmp/dependencies.txt
        echo "   Используется в контроллерах:" >> /tmp/dependencies.txt
        grep -l "$service" src/main/java/com/community/cms/web/mvc/controller/**/*.java 2>/dev/null | xargs -I{} basename {} .java >> /tmp/dependencies.txt
    fi
    
    # Контроллер
    for controller in $(find src/main/java/com/community/cms/web/mvc/controller -name "*${model}*Controller.java"); do
        echo "🎮 Контроллер: $(basename $controller .java)" >> /tmp/dependencies.txt
    done
    
    echo "" >> /tmp/dependencies.txt
done

cat /tmp/dependencies.txt
```

## 🚀 САМАЯ ВАЖНАЯ КОМАНДА СЕЙЧАС:

```shell
# Какие сервисы НЕ используются в контроллерах?
for service in $(find src/main/java/com/community/cms/domain/service -name "*.java"); do
    service_name=$(basename $service .java)
    if ! grep -r "$service_name" src/main/java/com/community/cms/web/mvc/controller/ > /dev/null; then
        echo "⚠️  НЕ ИСПОЛЬЗУЕТСЯ: $service_name"
    fi
done

# Какие контроллеры НЕ используют сервисы (пустые)?
for controller in $(find src/main/java/com/community/cms/web/mvc/controller -name "*.java"); do
    if ! grep -q "Service" "$controller"; then
        echo "⚠️  НЕТ СЕРВИСА: $(basename $controller .java)"
    fi
done
```

# Валидация

### Найти все контроллеры с валидацией:
```jshelllanguage
grep -r "@Valid\|BindingResult" src/main/java/com/community/cms/web/mvc/controller/ --include="*.java"
```
### Проверим какие Form классы есть:

```shell
find src/main/java/com/community/cms/web/mvc/dto -name "*Form.java"
```

### # Ищем ВСЕ использования
```shell
grep -r "PhotoDTO\|GalleryDTO" --include="*.java" src/main/java/
```

### Посмотрите какие внешние ресурсы используете
```shell
grep -r "https://" src/main/resources/templates/
grep -r "http://" src/main/resources/templates/
```
### Удалить amvera репозиторий

```shell
git remote remove amvera 2>/dev/null || true
```