/**
 * ============================================================================
 * МОДУЛЬ: Модальное окно выбора фото (режим создания проекта)
 * ============================================================================
 *
 * Назначение:
 * - Позволяет выбрать фото из галерей для нового проекта
 * - Выбранные фото сохраняются в скрытое поле формы
 * - Ограничение: максимум 10 фото
 * - Показывает превью выбранных фото на странице
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 *
 * Зависимости:
 * - Bootstrap 5 (модальное окно)
 * - common.js (escapeHtml, showAlert)
 *
 * @version 2.0
 * @since 2026-01-14
 */

// === ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ===
let allGalleries = [];          // Все галереи
let currentGalleryId = null;    // ID текущей галереи
let currentGalleryPhotos = [];  // Фото текущей галереи
let selectedPhotos = [];        // Выбранные фото [{id, title, webPath, galleryTitle}]
console.log('=== photo-gallery-modal-create.js загружен ===');

// /**
//  * Универсальный обработчик ошибок загрузки изображений
//  * Использует делегирование событий, не засоряет HTML
//  */
// function setupImageErrorHandler() {
//     document.body.addEventListener('error', function(e) {
//         const img = e.target;
//         if (img.tagName === 'IMG') {
//             // Предотвращаем зацикливание
//             img.onerror = null;
//             // Заменяем на заглушку
//             img.src = '/static/images/placeholder.jpg';
//             // Добавляем класс для стилизации (опционально)
//             img.classList.add('img-load-error');
//         }
//     }, true); // capture фаза для перехвата до всплытия
// }
//
// // Вызываем один раз при загрузке страницы
// document.addEventListener('DOMContentLoaded', setupImageErrorHandler);

// === ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ СТРАНИЦЫ ===
document.addEventListener('DOMContentLoaded', function() {
    console.log('Модалка выбора фото (create): инициализация...');

    // Загружаем выбранные фото из скрытого поля (если есть)
    loadSelectedPhotosFromForm();

    // Настраиваем обработчик поиска галерей
    const searchInput = document.getElementById('gallerySearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            filterGalleries(this.value);
        });
    }

    // Настраиваем обработчики модального окна
    setupModalHandlers();

    // Показываем превью выбранных фото
    showSelectedPhotosPreview();
});

// === ЗАГРУЗКА ДАННЫХ ===

/**
 * Загружает выбранные фото из скрытого поля формы и получает их данные с сервера
 */
function loadSelectedPhotosFromForm() {
    const hiddenField = document.getElementById('selectedPhotoIds');
    if (!hiddenField || !hiddenField.value || hiddenField.value.trim() === '') {
        selectedPhotos = [];
        showSelectedPhotosPreview();
        return;
    }

    const ids = hiddenField.value.split(',')
        .map(id => parseInt(id.trim()))
        .filter(id => !isNaN(id) && id > 0);

    if (ids.length === 0) {
        selectedPhotos = [];
        showSelectedPhotosPreview();
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/admin/projects/photos-info', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify(ids)
    })
        .then(response => response.json())
        .then(photos => {
            selectedPhotos = photos;
            showSelectedPhotosPreview();
            updateHiddenField();
            updateSelectedPhotosCounter();
        })
        .catch(error => {
            console.error('Ошибка загрузки фото:', error);
            selectedPhotos = [];
            showSelectedPhotosPreview();
        });
}

/**
 * Загружает список всех галерей с сервера
 */
function loadGalleries() {
    console.log('Загрузка списка галерей...');

    showLoadingGalleries();

    fetch('/admin/projects/available-galleries')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }
            return response.json();
        })
        .then(galleries => {
            allGalleries = galleries;
            console.log(`Загружено галерей: ${allGalleries.length}`);

            updateGalleriesCount();
            renderGalleriesList();

            if (allGalleries.length > 0) {
                selectGallery(allGalleries[0].id);
            }
        })
        .catch(error => {
            console.error('Ошибка загрузки галерей:', error);
            showGalleriesError();
        });
}

/**
 * Загружает фото указанной галереи
 * @param {number} galleryId - ID галереи
 */
function loadGalleryPhotos(galleryId) {
    if (!galleryId) return;

    console.log(`Загрузка фото галереи ID: ${galleryId}`);

    currentGalleryId = galleryId;
    showLoadingPhotos();

    fetch(`/admin/projects/gallery/${galleryId}/photos`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }
            return response.json();
        })
        .then(photos => {
            currentGalleryPhotos = photos;
            console.log(`Загружено фото: ${currentGalleryPhotos.length}`);

            updateCurrentGalleryInfo(galleryId);
            renderGalleryPhotos();
            updatePhotoCheckboxes();
        })
        .catch(error => {
            console.error(`Ошибка загрузки фото:`, error);
            showPhotosError();
        });
}

// === ОТОБРАЖЕНИЕ ДАННЫХ ===

function showLoadingGalleries() {
    const galleriesList = document.getElementById('galleriesList');
    if (galleriesList) {
        galleriesList.innerHTML = `
            <div class="text-center py-5">
                <div class="spinner-border spinner-border-sm text-primary" role="status">
                    <span class="visually-hidden">Загрузка...</span>
                </div>
                <p class="mt-2 text-muted small">Загрузка галерей...</p>
            </div>
        `;
    }
}

function showGalleriesError() {
    const galleriesList = document.getElementById('galleriesList');
    if (galleriesList) {
        galleriesList.innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-1"></i>
                Ошибка загрузки галерей. Проверьте подключение.
            </div>
        `;
    }
}

function renderGalleriesList() {
    const galleriesList = document.getElementById('galleriesList');
    if (!galleriesList) return;

    if (allGalleries.length === 0) {
        galleriesList.innerHTML = `
            <div class="alert alert-info">
                <i class="bi bi-info-circle me-1"></i>
                Нет доступных галерей
            </div>
        `;
        return;
    }

    let html = '';
    allGalleries.forEach(gallery => {
        const isActive = gallery.id === currentGalleryId;
        const badgeClass = gallery.published ? 'bg-success' : 'bg-secondary';
        const activeClass = isActive ? 'active bg-light border-primary' : '';

        html += `
            <a href="#" class="list-group-item list-group-item-action ${activeClass}"
               data-gallery-id="${gallery.id}">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">${escapeHtml(gallery.title)}</h6>
                        <small class="text-muted">${gallery.year} год</small>
                    </div>
                    <div class="text-end">
                        <span class="badge ${badgeClass}">${gallery.photoCount || 0}</span>
                    </div>
                </div>
                ${gallery.description ? `<p class="mb-1 small text-muted">${escapeHtml(gallery.description.substring(0, 50))}${gallery.description.length > 50 ? '...' : ''}</p>` : ''}
            </a>
        `;
    });

    galleriesList.innerHTML = html;

    // Добавляем обработчики для элементов списка галерей
    document.querySelectorAll('#galleriesList .list-group-item').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const galleryId = parseInt(this.getAttribute('data-gallery-id'));
            selectGallery(galleryId);
        });
    });
}

function showLoadingPhotos() {
    const galleryPhotos = document.getElementById('galleryPhotos');
    if (galleryPhotos) {
        galleryPhotos.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="spinner-border spinner-border-sm text-primary" role="status">
                    <span class="visually-hidden">Загрузка...</span>
                </div>
                <p class="mt-2 text-muted small">Загрузка фото...</p>
            </div>
        `;
    }
}

function showPhotosError() {
    const galleryPhotos = document.getElementById('galleryPhotos');
    if (galleryPhotos) {
        galleryPhotos.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-1"></i>
                    Ошибка загрузки фото галереи
                </div>
            </div>
        `;
    }
}

function renderGalleryPhotos() {
    const galleryPhotos = document.getElementById('galleryPhotos');
    if (!galleryPhotos) return;

    if (currentGalleryPhotos.length === 0) {
        galleryPhotos.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="bi bi-images display-6 text-muted"></i>
                <p class="mt-2 text-muted">В этой галерее нет фото</p>
            </div>
        `;
        return;
    }

    let html = '';
    currentGalleryPhotos.forEach(photo => {
        const isSelected = selectedPhotos.some(sp => sp.id === photo.id);
        const selectedClass = isSelected ? 'selected-photo' : '';

        html += `
            <div class="col">
                <div class="card h-100 photo-card ${selectedClass}"
                     data-photo-id="${photo.id}"
                     data-photo-title="${escapeHtml(photo.fileName)}"
                     data-photo-path="${photo.webPath}"
                     data-gallery-title="${escapeHtml(photo.galleryTitle)}">
                    <div class="position-relative">
                        <img src="${photo.webPath || '/static/images/placeholder.jpg'}"
                            class="card-img-top"
                            style="height: 120px; object-fit: cover;"
                            alt="${escapeHtml(photo.fileName)}"
                            onerror="this.onerror=null; this.src='/static/images/placeholder.jpg'">
                        ${isSelected ? `
                        <div class="position-absolute top-0 end-0 m-1">
                            <span class="badge bg-success">
                                <i class="bi bi-check-circle"></i>
                            </span>
                        </div>
                        ` : ''}
                        ${photo.isPrimary ? `
                        <div class="position-absolute top-0 start-0 m-1">
                            <span class="badge bg-warning">
                                <i class="bi bi-star-fill"></i>
                            </span>
                        </div>
                        ` : ''}
                    </div>
                    <div class="card-body p-2">
                        <div class="form-check mb-0">
                            <input type="checkbox"
                                   class="form-check-input photo-checkbox"
                                   id="photo_${photo.id}"
                                   ${isSelected ? 'checked' : ''}>
                            <label class="form-check-label small" for="photo_${photo.id}">
                                ${escapeHtml(photo.fileName.length > 20 ? photo.fileName.substring(0, 20) + '...' : photo.fileName)}
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    galleryPhotos.innerHTML = html;

    // Добавляем обработчики для карточек фото
    document.querySelectorAll('.photo-card').forEach(card => {
        card.addEventListener('click', function(e) {
            // Не обрабатываем клик по чекбоксу
            if (e.target.classList.contains('photo-checkbox') ||
                e.target.type === 'checkbox' ||
                e.target.closest('.form-check')) {
                return;
            }

            const photoId = parseInt(this.getAttribute('data-photo-id'));
            const title = this.getAttribute('data-photo-title');
            const path = this.getAttribute('data-photo-path');
            const galleryTitle = this.getAttribute('data-gallery-title');

            togglePhotoSelection(photoId, title, path, galleryTitle);
        });
    });

    // Добавляем обработчики для чекбоксов
    document.querySelectorAll('.photo-checkbox').forEach(checkbox => {
        checkbox.addEventListener('click', function(e) {
            e.stopPropagation();
            const card = this.closest('.photo-card');
            const photoId = parseInt(card.getAttribute('data-photo-id'));
            const title = card.getAttribute('data-photo-title');
            const path = card.getAttribute('data-photo-path');
            const galleryTitle = card.getAttribute('data-gallery-title');

            togglePhotoSelection(photoId, title, path, galleryTitle);
        });
    });
}

function showSelectedPhotosPreview() {
    const previewContainer = document.getElementById('selectedPhotosPreview');
    if (!previewContainer) return;

    if (selectedPhotos.length === 0) {
        previewContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-1"></i>
                    Фото не выбраны. Нажмите кнопку "Выбрать фото из галерей".
                </div>
            </div>
        `;
        return;
    }

    let html = '<div class="col-12 mb-2"><strong>Выбранные фото:</strong></div>';

    selectedPhotos.forEach((photo, index) => {
        html += `
            <div class="col-6 col-md-4 col-lg-3 mb-3" data-preview-photo-id="${photo.id}">
                <div class="card h-100">
                    <div class="position-relative">
                        <img src="${photo.webPath || '/static/images/placeholder.jpg'}"
                            class="card-img-top"
                            style="height: 120px; object-fit: cover;"
                            alt="${escapeHtml(photo.title)}"
                            onerror="this.onerror=null; this.src='/static/images/placeholder.jpg'">
                        <span class="position-absolute top-0 start-0 badge bg-primary m-1">${index + 1}</span>
                    </div>
                    <div class="card-body p-2">
                        <p class="small mb-1 text-truncate" title="${escapeHtml(photo.title)}">
                            ${escapeHtml(photo.title.length > 20 ? photo.title.substring(0, 20) + '...' : photo.title)}
                        </p>
                    </div>
                    <div class="card-footer p-2 text-center">
                        <button type="button" class="btn btn-sm btn-danger remove-photo-btn"
                                data-photo-id="${photo.id}">
                            <i class="bi bi-x me-1"></i>Удалить
                        </button>
                    </div>
                </div>
            </div>
        `;
    });

    previewContainer.innerHTML = html;

    // Добавляем обработчики для кнопок удаления в превью
    document.querySelectorAll('.remove-photo-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const photoId = parseInt(this.getAttribute('data-photo-id'));
            removePhotoFromSelection(photoId);
        });
    });
}

function renderSelectedPhotosInModal() {
    const selectedContainer = document.getElementById('selectedPhotosContainer');
    if (!selectedContainer) return;

    if (selectedPhotos.length === 0) {
        selectedContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-warning py-2 mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    Фото не выбраны. Выберите фото из галерей выше (максимум 10).
                </div>
            </div>
        `;
        return;
    }

    let html = '';
    selectedPhotos.forEach((photo, index) => {
        html += `
            <div class="col-6 col-md-4 col-lg-3" data-modal-photo-id="${photo.id}">
                <div class="card selected-photo-card">
                    <div class="position-relative">
                        <img src="${photo.webPath || '/static/images/placeholder.jpg'}"
                            class="card-img-top"
                            style="height: 100px; object-fit: cover;"
                            alt="${escapeHtml(photo.title)}"
                            onerror="this.onerror=null; this.src='/static/images/placeholder.jpg'">
                        <span class="position-absolute top-0 start-0 badge bg-primary m-1">${index + 1}</span>
                        <span class="position-absolute top-0 end-0 m-1">
                            <button type="button" class="btn btn-sm btn-danger btn-sm remove-modal-photo-btn"
                                    data-photo-id="${photo.id}">
                                <i class="bi bi-x"></i>
                            </button>
                        </span>
                    </div>
                    <div class="card-body p-2">
                        <p class="small mb-1 text-truncate" title="${escapeHtml(photo.title)}">
                            ${escapeHtml(photo.title.length > 15 ? photo.title.substring(0, 15) + '...' : photo.title)}
                        </p>
                    </div>
                </div>
            </div>
        `;
    });

    selectedContainer.innerHTML = html;

    // Добавляем обработчики для кнопок удаления в модалке
    document.querySelectorAll('.remove-modal-photo-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const photoId = parseInt(this.getAttribute('data-photo-id'));
            removePhotoFromSelection(photoId);
        });
    });
}

// === УПРАВЛЕНИЕ ВЫБОРОМ ===

function selectGallery(galleryId) {
    console.log(`Выбрана галерея: ${galleryId}`);

    document.querySelectorAll('#galleriesList .list-group-item').forEach(item => {
        const itemGalleryId = parseInt(item.getAttribute('data-gallery-id'));
        if (itemGalleryId === galleryId) {
            item.classList.add('active', 'bg-light', 'border-primary');
        } else {
            item.classList.remove('active', 'bg-light', 'border-primary');
        }
    });

    loadGalleryPhotos(galleryId);
}

function togglePhotoSelection(photoId, photoTitle, webPath, galleryTitle) {
    const existingIndex = selectedPhotos.findIndex(photo => photo.id === photoId);

    if (existingIndex >= 0) {
        selectedPhotos.splice(existingIndex, 1);
        console.log(`Удалено фото ID: ${photoId}`);
    } else {
        if (selectedPhotos.length >= 10) {
            showAlert('Можно выбрать максимум 10 фото. Удалите одно из выбранных.', 'warning');
            return;
        }

        selectedPhotos.push({
            id: photoId,
            title: photoTitle,
            webPath: webPath,
            galleryTitle: galleryTitle
        });
        console.log(`Добавлено фото ID: ${photoId}`);
    }

    updateSelectedCount();
    renderSelectedPhotosInModal();
    updatePhotoCheckboxes();
    updateSelectedPhotosCounter();
}

function removePhotoFromSelection(photoId) {
    if (!confirm('Удалить это фото из проекта?')) return;

    selectedPhotos = selectedPhotos.filter(photo => photo.id !== photoId);
    console.log(`Удалено фото ID: ${photoId}, осталось: ${selectedPhotos.length}`);

    updateSelectedCount();
    renderSelectedPhotosInModal();
    showSelectedPhotosPreview();
    updatePhotoCheckboxes();
    updateHiddenField();
    updateSelectedPhotosCounter();
}

function clearSelectedPhotos() {
    if (selectedPhotos.length === 0) return;
    if (!confirm(`Удалить все ${selectedPhotos.length} выбранных фото?`)) return;

    selectedPhotos = [];
    console.log('Очищены все выбранные фото');

    updateSelectedCount();
    renderSelectedPhotosInModal();
    showSelectedPhotosPreview();
    updatePhotoCheckboxes();
    updateHiddenField();
    updateSelectedPhotosCounter();
}

function updatePhotoCheckboxes() {
    currentGalleryPhotos.forEach(photo => {
        const checkbox = document.getElementById(`photo_${photo.id}`);
        if (checkbox) {
            const isSelected = selectedPhotos.some(sp => sp.id === photo.id);
            checkbox.checked = isSelected;

            const card = checkbox.closest('.photo-card');
            if (card) {
                if (isSelected) {
                    card.classList.add('selected-photo');
                } else {
                    card.classList.remove('selected-photo');
                }
            }
        }
    });
}

// === ФИЛЬТРАЦИЯ И ПОИСК ===

function filterGalleries(query) {
    if (!query || query.trim() === '') {
        document.querySelectorAll('#galleriesList .list-group-item').forEach(item => {
            item.style.display = 'block';
        });
        return;
    }

    const searchLower = query.toLowerCase().trim();
    document.querySelectorAll('#galleriesList .list-group-item').forEach(item => {
        const title = item.querySelector('h6')?.textContent?.toLowerCase() || '';
        const year = item.querySelector('small')?.textContent?.toLowerCase() || '';
        const description = item.querySelector('p')?.textContent?.toLowerCase() || '';

        const matches = title.includes(searchLower) || year.includes(searchLower) || description.includes(searchLower);
        item.style.display = matches ? 'block' : 'none';
    });
}

function clearGallerySearch() {
    const searchInput = document.getElementById('gallerySearch');
    if (searchInput) {
        searchInput.value = '';
        filterGalleries('');
        searchInput.focus();
    }
}

// === ОБНОВЛЕНИЕ UI ===

function updateGalleriesCount() {
    const countElement = document.getElementById('galleriesCount');
    if (countElement) countElement.textContent = String(allGalleries.length);
}

function updateCurrentGalleryInfo(galleryId) {
    const gallery = allGalleries.find(g => g.id === galleryId);
    if (!gallery) return;

    const titleElement = document.getElementById('currentGalleryTitle');
    const infoElement = document.getElementById('currentGalleryInfo');
    const countElement = document.getElementById('photosInGalleryCount');

    if (titleElement) titleElement.innerHTML = `<i class="bi bi-image me-1"></i>${escapeHtml(gallery.title)}`;
    if (infoElement) infoElement.textContent = `${gallery.year} год${gallery.description ? ` - ${gallery.description.substring(0, 50)}${gallery.description.length > 50 ? '...' : ''}` : ''}`;
    if (countElement) countElement.textContent = `${currentGalleryPhotos.length} фото`;
}

function updateSelectedCount() {
    const countElement = document.getElementById('selectedCount');
    if (countElement) countElement.textContent = String(selectedPhotos.length);
}

function updateSelectedPhotosCounter() {
    const counterElement = document.getElementById('selectedPhotosCount');
    if (counterElement) counterElement.textContent = String(selectedPhotos.length);
}

function updateHiddenField() {
    const hiddenField = document.getElementById('selectedPhotoIds');
    if (hiddenField) {
        hiddenField.value = selectedPhotos.map(photo => photo.id).join(',');
        console.log('Обновлено скрытое поле:', hiddenField.value);
    }
}

// === МОДАЛЬНОЕ ОКНО ===

function setupModalHandlers() {
    const modalElement = document.getElementById('photoGalleryModal');
    if (!modalElement) return;

    modalElement.addEventListener('show.bs.modal', function() {
        console.log('Открытие модального окна галерей');
        loadGalleries();
        renderSelectedPhotosInModal();
        updateSelectedCount();
    });

    modalElement.addEventListener('hidden.bs.modal', function() {
        console.log('Закрытие модального окна галерей');
        clearGallerySearch();
    });

    // Обработчик кнопки "Очистить все"
    const clearAllBtn = document.getElementById('clearAllPhotosBtn');
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', function() {
            clearSelectedPhotos();
        });
    }
}

function openPhotoGalleryModal() {
    console.log('Открываем модальное окно галерей...');
    const modal = new bootstrap.Modal(document.getElementById('photoGalleryModal'));
    modal.show();
}

function saveSelectedPhotos() {
    console.log('Сохранение выбранных фото:', selectedPhotos.length);

    updateHiddenField();
    showSelectedPhotosPreview();
    updateSelectedPhotosCounter();

    const modal = bootstrap.Modal.getInstance(document.getElementById('photoGalleryModal'));
    if (modal) modal.hide();

    showAlert(`Выбрано ${selectedPhotos.length} фото`, 'success');
}

// Делаем функции глобальными (только те, что нужны из HTML)
window.selectGallery = selectGallery;
window.clearSelectedPhotos = clearSelectedPhotos;
window.openPhotoGalleryModal = openPhotoGalleryModal;
window.saveSelectedPhotos = saveSelectedPhotos;
window.clearGallerySearch = clearGallerySearch;

// CSS стили для модального окна
if (!document.querySelector('#photo-gallery-modal-styles')) {
    const style = document.createElement('style');
    style.id = 'photo-gallery-modal-styles';
    style.textContent = `
        .photo-card {
            transition: all 0.2s ease;
            border: 2px solid transparent;
            cursor: pointer;
        }
        .photo-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .photo-card.selected-photo {
            border-color: #198754 !important;
            background-color: rgba(25, 135, 84, 0.05);
        }
        .photo-card.border-warning {
            border-color: #ffc107 !important;
        }
        .selected-photo-card {
            transition: all 0.2s ease;
            border: 2px solid #0d6efd;
        }
        .selected-photo-card:hover {
            transform: scale(1.02);
        }
        #galleriesList .list-group-item.active {
            background-color: #e7f1ff;
            color: #0a58ca;
            border-left: 3px solid #0d6efd;
        }
        #galleriesList .list-group-item:hover:not(.active) {
            background-color: #f8f9fa;
        }
    `;
    document.head.appendChild(style);
}