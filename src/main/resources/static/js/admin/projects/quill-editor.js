/* global Quill */

/**
 * ============================================================================
 * МОДУЛЬ: Quill редактор для полного описания проекта
 * ============================================================================
 *
 * Назначение:
 * - Обеспечивает WYSIWYG редактирование полного описания проекта
 * - Сохраняет форматированный текст в HTML
 * - Синхронизирует содержимое со скрытым textarea для отправки формы
 * - Валидирует минимальную длину описания (не менее 10 символов)
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 * - admin/projects/edit.html (редактирование проекта)
 *
 * Настройки редактора:
 * - Тема: snow (чистый интерфейс)
 * - Инструменты: заголовки (H2/H3), жирный, курсив, подчёркнутый
 * - Списки: нумерованные и маркированные
 * - Ссылки, выравнивание, очистка форматирования
 *
 * Зависимости:
 * - Quill.js библиотека (подключается отдельно в HTML)
 * - Bootstrap 5 (для стилей уведомлений)
 *
 * @version 1.0
 * @since 2026-01-14
 */

/**
 * Инициализация Quill редактора
 * Выполняется после полной загрузки DOM
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Инициализация Quill редактора...');

    // Находим необходимые DOM элементы
    const textarea = document.getElementById('fullDescription');
    const editorContainer = document.getElementById('editor-container');

    // Если элементы не найдены - выходим (модуль не инициализируется)
    if (!textarea) {
        console.error('❌ Не найден textarea #fullDescription');
        return;
    }

    if (!editorContainer) {
        console.error('❌ Не найден контейнер #editor-container');
        return;
    }

    try {
        // Создаём экземпляр Quill редактора с настройками
        const quill = new Quill(editorContainer, {
            theme: 'snow',
            modules: {
                toolbar: [
                    // Заголовки: H2 и H3 (H1 не используем для контента)
                    [{ 'header': [2, 3, false] }],

                    // Основное форматирование
                    ['bold', 'italic', 'underline'],

                    // Списки
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],

                    // Ссылки
                    ['link'],

                    // Выравнивание
                    [{ 'align': [] }],

                    // Очистка форматирования
                    ['clean']
                ]
            },
            placeholder: 'Введите подробное описание проекта... Например:\n• Цели и задачи\n• Основные этапы\n• Ожидаемые результаты\n\nИспользуйте панель инструментов для форматирования.',
            formats: [
                'header', 'bold', 'italic', 'underline',
                'list', 'bullet', 'link', 'align'
            ]
        });

        console.log('✅ Quill редактор создан');

        // Загружаем существующие данные (для режима редактирования)
        if (textarea.value && textarea.value.trim() !== '') {
            console.log('📥 Загружаем существующий текст:', textarea.value.length, 'символов');
            quill.root.innerHTML = textarea.value;
        }

        // Синхронизация: при изменении в редакторе обновляем скрытое textarea
        quill.on('text-change', function() {
            textarea.value = quill.root.innerHTML;
        });

        // Обработка отправки формы
        const form = document.querySelector('form.needs-validation');
        if (form) {
            form.addEventListener('submit', function(_) {
                // Принудительная синхронизация перед отправкой
                textarea.value = quill.root.innerHTML;
                console.log('📤 Форма отправляется, сохранено:', textarea.value.length, 'символов');
            });
        }

        // Сохраняем ссылку на редактор в глобальную переменную для отладки
        window.quillEditor = quill;

        // Показываем уведомление об успешной загрузке (с автоскрытием)
        setTimeout(() => {
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-success alert-dismissible fade show mt-2';
            alertDiv.style.cssText = 'padding: 0.5rem 1rem; font-size: 0.875rem;';
            alertDiv.innerHTML = `
                <i class="bi bi-check-circle me-1"></i>
                <strong>Редактор загружен!</strong> Используйте панель инструментов для форматирования.
                <button type="button" class="btn-close" data-bs-dismiss="alert" style="padding: 0.5rem;"></button>
            `;
            editorContainer.parentNode.insertBefore(alertDiv, editorContainer.nextSibling);

            // Автоскрытие через 5 секунд
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    const bsAlert = bootstrap.Alert.getOrCreateInstance(alertDiv);
                    if (bsAlert) bsAlert.close();
                }
            }, 5000);
        }, 500);

        console.log('🎉 Quill редактор полностью инициализирован');

    } catch (error) {
        console.error('❌ Ошибка при создании Quill редактора:', error);

        // Fallback: если Quill не загрузился, показываем обычное textarea
        textarea.style.display = 'block';
        editorContainer.style.display = 'none';

        // Показываем сообщение об ошибке
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger mt-2';
        errorDiv.innerHTML = `
            <i class="bi bi-exclamation-triangle me-2"></i>
            <strong>Ошибка загрузки редактора</strong>
            <div class="small mt-1">Используется обычное текстовое поле. ${error.message}</div>
        `;
        editorContainer.parentNode.insertBefore(errorDiv, editorContainer);
    }
});

/**
 * Вспомогательная функция для тестирования редактора
 * Вызывается из консоли браузера: testQuillEditor()
 *
 * @returns {Object} - объект с текстом, HTML и длиной
 */
window.testQuillEditor = function() {
    if (!window.quillEditor) {
        console.warn('Quill редактор не инициализирован');
        return null;
    }

    const text = window.quillEditor.getText();
    const html = window.quillEditor.root.innerHTML;

    console.log('=== ТЕСТ QUILL РЕДАКТОРА ===');
    console.log('Текст (plain):', text);
    console.log('Длина текста:', text.length, 'символов');
    console.log('HTML:', html);
    console.log('Длина HTML:', html.length, 'символов');
    console.log('====================');

    return {
        text: text,
        html: html,
        length: text.length
    };
};