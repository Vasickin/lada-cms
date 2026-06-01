/**
 * Валидация с модальным окном
 * Поля: название, slug, категория, статус, даты
 */

document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form.needs-validation');
    if (!form) return;

    // Определяем режим (создание или редактирование)
    const projectIdField = document.getElementById('id');
    const isEditMode = projectIdField && projectIdField.value;

    // Добавляем модальное окно, если его нет
    if (!document.getElementById('validationErrorModal')) {
        const modalHTML = `
            <div class="modal fade" id="validationErrorModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content border-0 shadow-lg">
                        <div class="modal-header bg-danger text-white border-0">
                            <h5 class="modal-title">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                Ошибка при сохранении проекта
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p class="mb-3">Пожалуйста, исправьте следующие ошибки:</p>
                            <div id="validationErrorList" class="ps-3"></div>
                        </div>
                        <div class="modal-footer border-0">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Отмена</button>
                            <button type="button" class="btn btn-danger" id="fixErrorsBtn">Исправить</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    // Функция проверки slug (уникальность + формат)
    async function checkSlugUniqueness(slug, excludeId) {
        if (!slug || slug.trim() === '') return { valid: false, message: 'URL-адрес (slug) обязателен для заполнения' };

        // Проверка формата
        const slugPattern = /^[a-z0-9-]+$/;
        if (!slugPattern.test(slug)) {
            return { valid: false, message: 'Slug может содержать только латинские буквы в нижнем регистре, цифры и дефисы' };
        }

        // Проверка уникальности через AJAX
        try {
            let url = `/admin/projects/check-slug?slug=${encodeURIComponent(slug)}`;
            if (excludeId) {
                url += `&excludeId=${excludeId}`;
            }
            const response = await fetch(url);
            const data = await response.json();
            if (data.exists) {
                return { valid: false, message: 'Проект с таким URL уже существует' };
            }
        } catch (error) {
            console.error('Ошибка проверки slug:', error);
        }

        return { valid: true, message: '' };
    }

    // Функция проверки дат
    function validateDates(startDate, eventDate, endDate) {
        const errors = [];

        function parseDate(dateStr) {
            if (!dateStr) return null;
            let parts;
            if (dateStr.includes('-')) {
                parts = dateStr.split('-');
                return new Date(parts[0], parts[1] - 1, parts[2]);
            } else if (dateStr.includes('.')) {
                parts = dateStr.split('.');
                return new Date(parts[2], parts[1] - 1, parts[0]);
            }
            return null;
        }

        const start = parseDate(startDate);
        const event = parseDate(eventDate);
        const end = parseDate(endDate);

        // 1. startDate не может быть позже endDate
        if (start && end && start > end) {
            errors.push({ field: 'startDate', message: 'Дата начала не может быть позже даты окончания' });
        }

        // 2. eventDate не может быть раньше startDate
        if (start && event && event < start) {
            errors.push({ field: 'eventDate', message: 'Дата события не может быть раньше даты начала' });
        }

        // 3. eventDate должна быть в рамках периода (между startDate и endDate)
        if (start && end && event) {
            if (event < start || event > end) {
                errors.push({ field: 'eventDate', message: 'Дата события должна быть в рамках проекта' });
            }
        }

        // 4. endDate не может быть раньше eventDate (одна проверка, без дублей)
        if (event && end && end < event) {
            errors.push({ field: 'endDate', message: 'Дата окончания не может быть раньше даты события' });
        }

        return errors;
    }

    // Функция проверки статуса (полностью соответствует серверной логике)
    function validateStatus(status, startDate, eventDate, endDate) {
        if (!status || status === '') {
            return { valid: false, message: 'Выберите статус проекта' };
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        function parseDate(dateStr) {
            if (!dateStr) return null;
            let parts;
            if (dateStr.includes('-')) {
                parts = dateStr.split('-');
                return new Date(parts[0], parts[1] - 1, parts[2]);
            } else if (dateStr.includes('.')) {
                parts = dateStr.split('.');
                return new Date(parts[2], parts[1] - 1, parts[0]);
            }
            return null;
        }

        const start = parseDate(startDate);
        const event = parseDate(eventDate);
        const end = parseDate(endDate);

        switch (status) {
            case 'UPCOMING':
                // startDate должна быть в будущем (или eventDate, если start нет)
                if (start && start <= today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Ближайшие" для проекта, дата начала которого уже наступила' };
                }
                if (!start && event && event <= today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Ближайшие" для проекта, дата события которого уже наступила' };
                }
                break;

            case 'ACTIVE':
                // endDate не должна быть в прошлом (или eventDate, если end нет)
                if (end && end < today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Активные" для проекта, дата окончания которого уже прошла' };
                }
                if (!end && event && event < today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Активные" для проекта, дата события которого уже прошла' };
                }
                break;

            case 'COMPLETED':
                // startDate должна быть в прошлом (или eventDate, если start нет)
                if (start && start >= today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Завершённые" для проекта, который еще не завершился' };
                }
                if (!start && event && event >= today) {
                    return { valid: false, message: 'Нельзя выбрать статус "Завершённые" для проекта, дата события которого еще не прошла' };
                }
                break;

            case 'ANNUAL':
            case 'ARCHIVED':
                // Для ежегодных и архивных проектов проверок нет
                break;
        }

        return { valid: true, message: '' };
    }

    // Основная функция валидации
    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        // Очищаем старые ошибки
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        document.querySelectorAll('.invalid-feedback').forEach(el => el.remove());

        const errors = [];

        // Получаем значения полей
        const title = document.getElementById('title');
        const slug = document.getElementById('slug');
        const category = document.getElementById('category');
        const status = document.getElementById('status');
        const startDate = document.getElementById('startDate');
        const eventDate = document.getElementById('eventDate');
        const endDate = document.getElementById('endDate');

        // 1. Проверка названия
        if (!title.value || title.value.trim() === '') {
            errors.push({ field: title, message: 'Название проекта обязательно для заполнения' });
        } else if (title.value.trim().length < 3) {
            errors.push({ field: title, message: 'Название должно содержать минимум 3 символа' });
        } else if (title.value.trim().length > 255) {
            errors.push({ field: title, message: 'Название не должно превышать 255 символов' });
        }

        // 2. Проверка slug (уникальность + формат)
        if (slug) {
            const slugCheck = await checkSlugUniqueness(slug.value, isEditMode ? projectIdField.value : null);
            if (!slugCheck.valid) {
                errors.push({ field: slug, message: slugCheck.message });
            }
        }

        // 3. Проверка категории
        if (!category.value || category.value === '') {
            errors.push({ field: category, message: 'Выберите категорию проекта' });
        }

        // 4. Проверка статуса
        const statusCheck = validateStatus(
            status.value,
            startDate ? startDate.value : null,
            eventDate ? eventDate.value : null,
            endDate ? endDate.value : null
        );
        if (!statusCheck.valid) {
            errors.push({ field: status, message: statusCheck.message });
        }

        // 5. Проверка дат
        const dateErrors = validateDates(
            startDate ? startDate.value : null,
            eventDate ? eventDate.value : null,
            endDate ? endDate.value : null
        );
        dateErrors.forEach(err => {
            const field = document.getElementById(err.field);
            if (field) {
                errors.push({ field: field, message: err.message });
            }
        });

        // Если есть ошибки - показываем модальное окно
        if (errors.length > 0) {
            // Показываем ошибки под полями
            errors.forEach(err => {
                err.field.classList.add('is-invalid');
                const div = document.createElement('div');
                div.className = 'invalid-feedback';
                div.textContent = err.message;
                err.field.parentNode.insertBefore(div, err.field.nextSibling);
            });

            // Формируем список для модального окна
            const errorList = document.getElementById('validationErrorList');
            if (errorList) {
                let html = '<ul class="list-unstyled mb-0">';
                errors.forEach(err => {
                    const fieldName = err.field.id === 'title' ? 'Название проекта' :
                        err.field.id === 'slug' ? 'URL-адрес (slug)' :
                            err.field.id === 'category' ? 'Категория' :
                                err.field.id === 'status' ? 'Статус' :
                                    err.field.id === 'startDate' ? 'Дата начала' :
                                        err.field.id === 'eventDate' ? 'Дата события' :
                                            err.field.id === 'endDate' ? 'Дата окончания' : 'Поле';
                    html += `<li class="mb-2 d-flex align-items-start">
                                <i class="bi bi-x-circle-fill text-danger me-2 mt-1"></i>
                                <div><strong>${fieldName}:</strong> <span class="text-muted">${err.message}</span></div>
                            </li>`;
                });
                html += '</ul>';
                errorList.innerHTML = html;
            }

            // Сохраняем ошибки для кнопки "Исправить"
            window.validationErrors = errors;

            // Показываем модальное окно
            const modal = new bootstrap.Modal(document.getElementById('validationErrorModal'));
            modal.show();
            return;
        }

        // Если ошибок нет - отправляем форму
        form.submit();
    });

    // Кнопка "Исправить"
    const fixBtn = document.getElementById('fixErrorsBtn');
    if (fixBtn) {
        fixBtn.addEventListener('click', function() {
            const modalElement = document.getElementById('validationErrorModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }

            setTimeout(() => {
                if (window.validationErrors && window.validationErrors.length > 0) {
                    const firstError = window.validationErrors[0].field;
                    if (firstError) {
                        // Получаем позицию элемента
                        const elementPosition = firstError.getBoundingClientRect().top;
                        // Отступ сверху 120px (было 1000 — это слишком много!)
                        const offsetPosition = elementPosition + window.pageYOffset - 120;

                        // Медленный плавный скролл
                        window.scrollTo({
                            top: offsetPosition,
                            behavior: 'smooth'
                        });

                        setTimeout(() => {
                            firstError.focus();
                            firstError.classList.add('is-invalid');
                        }, 800); // Даём время на завершение скролла
                    }
                    window.validationErrors = null;
                }
            }, 200);
        });
    }

    // Live-валидация при изменении slug
    const slugInput = document.getElementById('slug');
    if (slugInput) {
        let debounceTimer;
        slugInput.addEventListener('input', function() {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(async () => {
                const slugCheck = await checkSlugUniqueness(this.value, isEditMode ? projectIdField.value : null);
                if (!slugCheck.valid) {
                    this.classList.add('is-invalid');
                    const existingError = document.getElementById('slug-error');
                    if (!existingError) {
                        const div = document.createElement('div');
                        div.className = 'invalid-feedback';
                        div.id = 'slug-error';
                        div.textContent = slugCheck.message;
                        this.parentNode.insertBefore(div, this.nextSibling);
                    }
                } else {
                    this.classList.remove('is-invalid');
                    const errorDiv = document.getElementById('slug-error');
                    if (errorDiv) errorDiv.remove();
                }
            }, 500);
        });
    }

    // Убираем ошибку при изменении поля
    const titleInput = document.getElementById('title');
    if (titleInput) {
        titleInput.addEventListener('input', function() {
            this.classList.remove('is-invalid');
            const err = document.getElementById(this.id + '-error');
            if (err) err.remove();
        });
    }

    const categorySelect = document.getElementById('category');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            this.classList.remove('is-invalid');
            const err = document.getElementById(this.id + '-error');
            if (err) err.remove();
        });
    }

    const statusSelect = document.getElementById('status');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            this.classList.remove('is-invalid');
            const err = document.getElementById(this.id + '-error');
            if (err) err.remove();
        });
    }

    const dateFields = ['startDate', 'eventDate', 'endDate'];
    dateFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('change', function() {
                this.classList.remove('is-invalid');
                const err = document.getElementById(this.id + '-error');
                if (err) err.remove();
            });
        }
    });
});