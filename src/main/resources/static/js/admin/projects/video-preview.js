/**
 * ============================================================================
 * МОДУЛЬ: Превью видео для проекта
 * ============================================================================
 *
 * Назначение:
 * - Позволяет вставить ссылку на видео с YouTube, Vimeo или Rutube
 * - Автоматически извлекает ID видео и формирует embed-код
 * - Показывает предпросмотр видео перед сохранением
 * - Поддерживает клавишу Enter для быстрого просмотра
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 * - admin/projects/edit.html (редактирование проекта)
 *
 * Поддерживаемые платформы:
 * - YouTube (youtube.com, youtu.be)
 * - Vimeo (vimeo.com)
 * - Rutube (rutube.ru)
 *
 * Зависимости:
 * - Bootstrap 5 (для стилей и анимаций)
 *
 * @version 1.0
 * @since 2026-01-14
 */

// Глобальная переменная для хранения текущего URL видео
let currentVideoUrl = '';

/**
 * Основная функция для показа превью видео
 * Вызывается при нажатии кнопки "Превью" или Enter в поле ввода
 *
 * Алгоритм работы:
 * 1. Получает URL из поля ввода
 * 2. Проверяет валидность URL
 * 3. Определяет платформу (YouTube/Vimeo/Rutube)
 * 4. Извлекает ID видео
 * 5. Формирует embed-код
 * 6. Отображает превью
 */
function showVideoPreview() {
    // Получаем элементы DOM
    const urlInput = document.getElementById('videoUrlInput');
    const previewContainer = document.getElementById('videoPreviewContainer');
    const previewContent = document.getElementById('videoPreviewContent');
    const emptyState = document.getElementById('videoEmptyState');
    const previewState = document.getElementById('videoPreviewState');

    // Получаем и очищаем URL от лишних пробелов
    const url = urlInput.value.trim();

    // Валидация: URL не должен быть пустым
    if (!url) {
        alert('Введите ссылку на видео');
        urlInput.focus();
        return;
    }

    // Валидация: протокол должен быть HTTPS
    if (!url.startsWith('https://')) {
        alert('Ссылка должна начинаться с https://');
        return;
    }

    // Извлекаем ID видео в зависимости от сервиса
    let videoId = null;
    let embedUrl = null;
    let platform = '';

    // === ОПРЕДЕЛЕНИЕ ПЛАТФОРМЫ И ИЗВЛЕЧЕНИЕ ID ===

    if (url.includes('youtube.com') || url.includes('youtu.be')) {
        platform = 'YouTube';
        videoId = extractYouTubeId(url);
        if (videoId) {
            embedUrl = `https://www.youtube.com/embed/${videoId}`;
        }
    } else if (url.includes('vimeo.com')) {
        platform = 'Vimeo';
        videoId = extractVimeoId(url);
        if (videoId) {
            embedUrl = `https://player.vimeo.com/video/${videoId}`;
        }
    } else if (url.includes('rutube.ru')) {
        platform = 'Rutube';
        videoId = extractRutubeId(url);
        if (videoId) {
            embedUrl = `https://rutube.ru/play/embed/${videoId}`;
        }
    }

    // Если ID успешно извлечён - показываем превью
    if (embedUrl && videoId) {
        // Формируем HTML embed-кода с адаптивным соотношением сторон 16:9
        previewContent.innerHTML = `
    <div class="ratio ratio-16x9">
        <iframe src="${embedUrl}"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen
                style="border: 0; border-radius: 8px;">
        </iframe>
    </div>
`;

        // Показываем контейнер превью
        previewContainer.style.display = 'block';

        // Скрываем пустое состояние, показываем состояние с превью
        emptyState.style.display = 'none';
        previewState.style.display = 'block';

        // Сохраняем текущий URL для возможного использования при отправке формы
        currentVideoUrl = url;

        console.log(`✅ Превью создано для ${platform}: ${url}`);
    } else {
        // Не удалось распознать платформу или извлечь ID
        alert('Некорректная ссылка на видео. Поддерживаются: YouTube, Vimeo, Rutube');
    }
}

/**
 * Извлекает ID видео из URL YouTube
 * Поддерживаемые форматы:
 * - https://youtube.com/watch?v=XXXXXXXXXXX
 * - https://youtu.be/XXXXXXXXXXX
 * - https://youtube.com/embed/XXXXXXXXXXX
 * - https://youtube.com/v/XXXXXXXXXXX
 *
 * @param {string} url - URL видео на YouTube
 * @returns {string|null} - ID видео или null, если не найдено
 */
function extractYouTubeId(url) {
    const patterns = [
        /youtube\.com\/watch\?v=([a-zA-Z0-9_-]+)/,
        /youtu\.be\/([a-zA-Z0-9_-]+)/,
        /youtube\.com\/embed\/([a-zA-Z0-9_-]+)/,
        /youtube\.com\/v\/([a-zA-Z0-9_-]+)/
    ];

    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match && match[1]) {
            return match[1];
        }
    }
    return null;
}

/**
 * Извлекает ID видео из URL Vimeo
 * Поддерживаемые форматы:
 * - https://vimeo.com/123456789
 *
 * @param {string} url - URL видео на Vimeo
 * @returns {string|null} - ID видео или null, если не найдено
 */
function extractVimeoId(url) {
    const pattern = /vimeo\.com\/(\d+)/;
    const match = url.match(pattern);
    return match ? match[1] : null;
}

/**
 * Извлекает ID видео из URL Rutube
 * Поддерживаемые форматы:
 * - https://rutube.ru/video/XXXXXXXXXXX/
 * - https://rutube.ru/play/embed/XXXXXXXXXXX
 *
 * @param {string} url - URL видео на Rutube
 * @returns {string|null} - ID видео или null, если не найдено
 */
function extractRutubeId(url) {
    const patterns = [
        /rutube\.ru\/video\/([a-zA-Z0-9._-]+)/,
        /rutube\.ru\/play\/embed\/([a-zA-Z0-9._-]+)/,
        /rutube\.ru\/shorts\/([a-zA-Z0-9._-]+)/
    ];

    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match && match[1]) {
            console.log('Найден Rutube ID:', match[1]);
            return match[1];
        }
    }
    console.log('Rutube ID не найден в URL:', url);
    return null;
}

/**
 * Очищает поле ввода видео и скрывает превью
 * Вызывается при нажатии кнопки "Очистить"
 */
function clearVideoField() {
    const urlInput = document.getElementById('videoUrlInput');
    if (urlInput) {
        urlInput.value = '';
        urlInput.focus();
    }
    hideVideoPreview();
    console.log('🧹 Поле видео очищено');
}

/**
 * Скрывает блок с превью видео и показывает пустое состояние
 */
function hideVideoPreview() {
    const previewContainer = document.getElementById('videoPreviewContainer');
    const previewState = document.getElementById('videoPreviewState');
    const emptyState = document.getElementById('videoEmptyState');

    if (previewContainer) previewContainer.style.display = 'none';
    if (previewState) previewState.style.display = 'none';
    if (emptyState) emptyState.style.display = 'block';

    currentVideoUrl = '';
}

/**
 * Автоматически показывает превью при загрузке страницы (для режима редактирования)
 * Если в поле уже есть URL - показываем превью с небольшой задержкой
 */
function autoPreviewIfUrlExists() {
    const urlInput = document.getElementById('videoUrlInput');
    if (urlInput && urlInput.value && urlInput.value.trim() !== '') {
        // Небольшая задержка для полной загрузки DOM
        setTimeout(showVideoPreview, 500);
    }
}

/**
 * Инициализация модуля при загрузке страницы
 * Настраивает обработчик клавиши Enter в поле ввода
 */
document.addEventListener('DOMContentLoaded', function() {
    const urlInput = document.getElementById('videoUrlInput');

    if (urlInput) {
        // Обработчик нажатия Enter в поле ввода
        urlInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                showVideoPreview();
            }
        });

        console.log('✅ Модуль превью видео инициализирован');
    } else {
        console.log('Модуль превью видео: поле #videoUrlInput не найдено');
    }

    // Автоматическое превью для режима редактирования
    autoPreviewIfUrlExists();
});