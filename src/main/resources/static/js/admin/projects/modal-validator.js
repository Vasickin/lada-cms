/**
 * Валидация с модальным окном
 * Поля: название, категория, статус
 * Остальное не трогаем
 */

document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form.needs-validation');
    if (!form) return;

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

    form.addEventListener('submit', function(event) {
        // Очищаем старые ошибки
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        document.querySelectorAll('.invalid-feedback').forEach(el => el.remove());

        const errors = [];

        // Проверка названия
        const title = document.getElementById('title');
        if (!title.value || title.value.trim() === '') {
            errors.push({ field: title, message: 'Название проекта обязательно для заполнения' });
        } else if (title.value.trim().length < 3) {
            errors.push({ field: title, message: 'Название должно содержать минимум 3 символа' });
        } else if (title.value.trim().length > 255) {
            errors.push({ field: title, message: 'Название не должно превышать 255 символов' });
        }

        // Проверка категории
        const category = document.getElementById('category');
        if (!category.value || category.value === '') {
            errors.push({ field: category, message: 'Выберите категорию проекта' });
        }

        // Проверка статуса
        const status = document.getElementById('status');
        if (!status.value || status.value === '') {
            errors.push({ field: status, message: 'Выберите статус проекта' });
        }

        // Если есть ошибки - показываем модальное окно
        if (errors.length > 0) {
            event.preventDefault();

            // Показываем ошибки под полями (для контекста)
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
                        err.field.id === 'category' ? 'Категория' : 'Статус';
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
        }
    });

    // Кнопка "Исправить"
    const fixBtn = document.getElementById('fixErrorsBtn');
    if (fixBtn) {
        fixBtn.addEventListener('click', function() {
            const modal = bootstrap.Modal.getInstance(document.getElementById('validationErrorModal'));
            if (modal) modal.hide();

            setTimeout(() => {
                if (window.validationErrors && window.validationErrors.length > 0) {
                    const firstError = window.validationErrors[0].field;
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                    window.validationErrors = null;
                }
            }, 300);
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
});