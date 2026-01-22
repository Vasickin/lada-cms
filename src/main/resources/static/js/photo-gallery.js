// photo-gallery.js
// JavaScript для админ-панели фото-галереи

document.addEventListener('DOMContentLoaded', function() {
    console.log('Photo Gallery Admin loaded');

    // Инициализация всех компонентов
    initFilters();
    initTableActions();
    initStatusToggles();
    initSearch();
});

/**
 * Инициализация фильтров
 */
function initFilters() {
    // Фильтр по году
    const yearFilter = document.getElementById('yearFilter');
    if (yearFilter) {
        yearFilter.addEventListener('change', function() {
            const year = this.value;
            if (year) {
                window.location.href = `/admin/photo-gallery/year/${year}`;
            } else {
                window.location.href = `/admin/photo-gallery`;
            }
        });
    }

    // Фильтр по категории
    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', function() {
            const categoryId = this.value;
            if (categoryId) {
                window.location.href = `/admin/photo-gallery/category/${categoryId}`;
            } else {
                window.location.href = `/admin/photo-gallery`;
            }
        });
    }
}

/**
 * Инициализация действий в таблице
 */
function initTableActions() {
    // Подтверждение удаления
    const deleteForms = document.querySelectorAll('form[onsubmit*="Удалить элемент"]');
    deleteForms.forEach(form => {
        form.onsubmit = function(e) {
            return confirm('Вы уверены, что хотите удалить этот элемент? Это действие нельзя отменить.');
        };
    });

    // Подтверждение публикации/снятия с публикации
    const publishForms = document.querySelectorAll('form[onsubmit*="Опубликовать элемент"]');
    publishForms.forEach(form => {
        form.onsubmit = function(e) {
            return confirm('Опубликовать элемент? Он станет видимым на сайте.');
        };
    });

    const unpublishForms = document.querySelectorAll('form[onsubmit*="Снять элемент с публикации"]');
    unpublishForms.forEach(form => {
        form.onsubmit = function(e) {
            return confirm('Снять элемент с публикации? Он перестанет быть видимым на сайте.');
        };
    });
}

/**
 * Инициализация переключения статусов
 */
function initStatusToggles() {
    // Можно добавить AJAX переключение статусов без перезагрузки страницы
    // (опционально для улучшения UX)
}

/**
 * Инициализация поиска
 */
function initSearch() {
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        const searchInput = searchForm.querySelector('input[name="search"]');
        let searchTimeout;

        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                if (this.value.trim().length >= 2 || this.value.trim().length === 0) {
                    searchForm.submit();
                }
            }, 500);
        });
    }
}

/**
 * Показать/скрыть детали элемента
 */
function toggleItemDetails(itemId) {
    const detailsRow = document.getElementById(`details-${itemId}`);
    if (detailsRow) {
        detailsRow.classList.toggle('d-none');
    }
}

/**
 * Копировать ID элемента в буфер обмена
 */
function copyItemId(itemId) {
    navigator.clipboard.writeText(itemId)
        .then(() => {
            // Показать уведомление
            alert('ID скопирован в буфер обмена: ' + itemId);
        })
        .catch(err => {
            console.error('Ошибка при копировании: ', err);
        });
}

// Экспорт функций для использования в других скриптах
window.PhotoGalleryAdmin = {
    initFilters,
    initTableActions,
    initStatusToggles,
    initSearch,
    toggleItemDetails,
    copyItemId
};