// photo-gallery-form.js
// JavaScript для формы создания/редактирования фото-галереи

class PhotoGalleryForm {
    constructor() {
        this.form = document.getElementById('photoGalleryForm');
        this.dropzone = document.getElementById('dropzone');
        this.fileInput = document.getElementById('fileInput');
        this.uploadedFiles = document.getElementById('uploadedFiles');
        this.fileList = document.getElementById('fileList');
        this.files = [];
        this.maxFiles = parseInt(this.fileInput?.getAttribute('data-max-files')) || 15;
        this.isEdit = window.isEdit || false;
        this.existingCount = 0;

        this.init();
    }

    init() {
        if (!this.form) return;

        this.existingCount = this.getExistingImageCount();
        this.setupEventListeners();
        this.setupDragAndDrop();
        this.setupDescriptionCounter();
        this.setupCategoryValidation();
        this.setupFormValidation();
        this.setupPreviewButton();
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ ==========

    setupEventListeners() {
        // Обработчик выбора файлов
        if (this.fileInput) {
            this.fileInput.addEventListener('change', (e) => this.handleFiles(e.target.files));
        }

        // Очистка всех файлов
        const clearAllBtn = document.getElementById('clearAllBtn');
        if (clearAllBtn) {
            clearAllBtn.addEventListener('click', () => this.clearAllFiles());
        }

        // Переключение сортировки
        const sortToggleBtn = document.getElementById('sortToggleBtn');
        if (sortToggleBtn) {
            sortToggleBtn.addEventListener('click', () => this.toggleSortMode());
        }

        // Подтверждение удаления
        const deleteBtn = document.getElementById('deleteBtn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', (e) => {
                if (!confirm('Вы уверены, что хотите удалить этот элемент? Это действие нельзя отменить.')) {
                    e.preventDefault();
                }
            });
        }
    }

    setupDragAndDrop() {
        if (!this.dropzone) return;

        // Предотвращаем стандартное поведение
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            this.dropzone.addEventListener(eventName, this.preventDefaults, false);
        });

        // Визуальные эффекты
        ['dragenter', 'dragover'].forEach(eventName => {
            this.dropzone.addEventListener(eventName, () => this.highlightDropzone(), false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            this.dropzone.addEventListener(eventName, () => this.unhighlightDropzone(), false);
        });

        // Обработка сброса файлов
        this.dropzone.addEventListener('drop', (e) => {
            const files = Array.from(e.dataTransfer.files);
            this.handleFiles(files);
        });

        // Клик по dropzone
        this.dropzone.addEventListener('click', () => {
            if (this.fileInput) {
                this.fileInput.click();
            }
        });
    }

    setupDescriptionCounter() {
        const description = document.getElementById('description');
        const counter = document.getElementById('descriptionCounter');

        if (description && counter) {
            const updateCounter = () => {
                const length = description.value.length;
                counter.textContent = `${length}/2000`;

                if (length > 2000) {
                    counter.classList.add('text-danger', 'fw-bold');
                } else if (length > 1900) {
                    counter.classList.add('text-warning');
                    counter.classList.remove('text-danger', 'fw-bold');
                } else {
                    counter.classList.remove('text-danger', 'text-warning', 'fw-bold');
                }
            };

            // Устанавливаем начальное значение
            updateCounter();

            // Обновляем при изменении
            description.addEventListener('input', updateCounter);
        }
    }

    setupCategoryValidation() {
        const categoryCheckboxes = document.querySelectorAll('.category-checkbox');
        const categoryContainer = document.querySelector('.categories-container');

        if (categoryCheckboxes.length > 0 && categoryContainer) {
            categoryCheckboxes.forEach(checkbox => {
                checkbox.addEventListener('change', () => {
                    const checkedCount = document.querySelectorAll('.category-checkbox:checked').length;

                    if (checkedCount > 0) {
                        categoryContainer.classList.remove('border-danger');
                    }
                });
            });
        }
    }

    setupFormValidation() {
        if (!this.form) return;

        const validationAlert = document.getElementById('validationAlert');
        const submitBtn = document.getElementById('submitBtn');

        this.form.addEventListener('submit', (e) => {
            if (!this.validateForm()) {
                e.preventDefault();
                e.stopPropagation();

                if (validationAlert) {
                    validationAlert.classList.remove('d-none');
                    validationAlert.scrollIntoView({ behavior: 'smooth' });
                }

                this.form.classList.add('was-validated');
            } else if (submitBtn) {
                // Блокируем кнопку и меняем текст
                submitBtn.disabled = true;
                const originalHtml = submitBtn.innerHTML;
                submitBtn.innerHTML = `
                    <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                    Сохранение...
                `;

                // Восстанавливаем кнопку через 5 секунд (на случай ошибки)
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalHtml;
                }, 5000);
            }
        });

        // Валидация при изменении полей
        const inputs = this.form.querySelectorAll('input[required], textarea[required]');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                input.classList.remove('is-invalid');
                if (validationAlert) {
                    validationAlert.classList.add('d-none');
                }
            });
        });
    }

    setupPreviewButton() {
        const previewBtn = document.getElementById('previewBtn');
        if (previewBtn && this.isEdit) {
            previewBtn.addEventListener('click', () => {
                const itemId = window.itemId || null;
                if (itemId) {
                    window.open(`/admin/photo-gallery/preview/${itemId}`, '_blank');
                }
            });
        }
    }

    // ========== РАБОТА С ФАЙЛАМИ ==========

    handleFiles(fileList) {
        const files = Array.from(fileList);

        // Проверяем лимит файлов
        const totalFiles = this.files.length + files.length + this.existingCount;
        if (totalFiles > this.maxFiles) {
            this.showAlert(`Максимальное количество файлов: ${this.maxFiles}. Вы пытаетесь загрузить ${files.length} файлов, но уже есть ${this.files.length + this.existingCount}.`, 'danger');
            return;
        }

        // Фильтруем и валидируем файлы
        const validFiles = files.filter(file => this.validateFile(file));

        // Добавляем файлы
        validFiles.forEach(file => {
            this.files.push(file);
            this.addFileToList(file);
        });

        // Обновляем интерфейс
        this.updateFileInterface();
    }

    validateFile(file) {
        // Проверяем тип файла
        if (!file.type.startsWith('image/')) {
            this.showAlert(`Файл "${file.name}" не является изображением и будет пропущен.`, 'warning');
            return false;
        }

        // Проверяем размер файла (5MB)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            this.showAlert(`Файл "${file.name}" слишком большой (${this.formatFileSize(file.size)}). Максимальный размер: 5MB.`, 'warning');
            return false;
        }

        // Проверяем расширение
        const validExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp'];
        const extension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));

        if (!validExtensions.includes(extension)) {
            this.showAlert(`Файл "${file.name}" имеет неподдерживаемое расширение. Поддерживаются: JPG, PNG, GIF, WebP, BMP.`, 'warning');
            return false;
        }

        return true;
    }

    addFileToList(file) {
        if (!this.fileList) return;

        const fileId = `file-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        const fileElement = document.createElement('div');
        fileElement.className = 'list-group-item file-list-item fade-in';
        fileElement.id = fileId;
        fileElement.dataset.fileName = file.name;
        fileElement.dataset.fileSize = file.size;

        fileElement.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center" style="flex: 1;">
                    <div class="file-preview-placeholder me-3">
                        <div class="spinner-border spinner-border-sm text-primary" role="status">
                            <span class="visually-hidden">Загрузка...</span>
                        </div>
                    </div>
                    <div class="file-info">
                        <div class="file-name text-truncate" title="${file.name}">
                            ${file.name}
                        </div>
                        <div class="file-size text-muted small">
                            ${this.formatFileSize(file.size)}
                        </div>
                    </div>
                </div>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="photoGalleryForm.setAsPrimary('${fileId}')" title="Сделать основным">
                        <i class="bi bi-star"></i>
                    </button>
                    <button type="button" class="btn btn-outline-danger" onclick="photoGalleryForm.removeFile('${fileId}')" title="Удалить">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </div>
        `;

        this.fileList.appendChild(fileElement);

        // Создаем предпросмотр изображения
        this.createFilePreview(file, fileElement);
    }

    createFilePreview(file, fileElement) {
        const reader = new FileReader();
        const previewPlaceholder = fileElement.querySelector('.file-preview-placeholder');

        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'file-preview rounded';
            img.alt = file.name;
            img.title = file.name;

            previewPlaceholder.innerHTML = '';
            previewPlaceholder.appendChild(img);
        };

        reader.readAsDataURL(file);
    }

    removeFile(fileId) {
        const fileElement = document.getElementById(fileId);
        if (!fileElement) return;

        // Удаляем из массива
        const fileName = fileElement.dataset.fileName;
        this.files = this.files.filter(f => f.name !== fileName);

        // Удаляем из DOM с анимацией
        fileElement.classList.add('opacity-0');
        setTimeout(() => {
            fileElement.remove();
            this.updateFileInterface();
        }, 300);
    }

    setAsPrimary(fileId) {
        // Снимаем выделение с других файлов
        document.querySelectorAll('.file-list-item.primary').forEach(item => {
            item.classList.remove('primary');
            const btn = item.querySelector('.btn-outline-primary');
            if (btn) {
                btn.classList.replace('btn-primary', 'btn-outline-primary');
                const icon = btn.querySelector('.bi');
                if (icon) {
                    icon.classList.replace('bi-star-fill', 'bi-star');
                }
            }
        });

        // Выделяем текущий файл
        const fileElement = document.getElementById(fileId);
        if (fileElement) {
            fileElement.classList.add('primary');
            const btn = fileElement.querySelector('.btn-outline-primary');
            if (btn) {
                btn.classList.replace('btn-outline-primary', 'btn-primary');
                const icon = btn.querySelector('.bi');
                if (icon) {
                    icon.classList.replace('bi-star', 'bi-star-fill');
                }
            }

            // Перемещаем в начало списка
            if (fileElement.parentNode.firstChild !== fileElement) {
                this.fileList.insertBefore(fileElement, this.fileList.firstChild);

                // Также перемещаем файл в массиве
                const fileName = fileElement.dataset.fileName;
                const fileIndex = this.files.findIndex(f => f.name === fileName);
                if (fileIndex > 0) {
                    const [file] = this.files.splice(fileIndex, 1);
                    this.files.unshift(file);
                    this.updateFormData();
                }
            }
        }
    }

    clearAllFiles() {
        if (this.files.length === 0) return;

        if (confirm(`Удалить все ${this.files.length} загруженных файлов?`)) {
            // Удаляем все файлы из DOM с анимацией
            const fileItems = this.fileList.querySelectorAll('.file-list-item');
            fileItems.forEach((item, index) => {
                setTimeout(() => {
                    item.classList.add('opacity-0');
                    setTimeout(() => item.remove(), 300);
                }, index * 50);
            });

            // Очищаем массив
            this.files = [];

            // Обновляем интерфейс
            setTimeout(() => this.updateFileInterface(), fileItems.length * 50 + 300);
        }
    }

    toggleSortMode() {
        const isManualSort = this.fileList.classList.toggle('manual-sort');
        const sortToggleBtn = document.getElementById('sortToggleBtn');

        if (sortToggleBtn) {
            if (isManualSort) {
                sortToggleBtn.innerHTML = '<i class="bi bi-check2-square me-1"></i>Сортировка включена';
                sortToggleBtn.classList.replace('btn-outline-secondary', 'btn-success');
                this.enableManualSort();
            } else {
                sortToggleBtn.innerHTML = '<i class="bi bi-arrow-down-up me-1"></i>Сортировка';
                sortToggleBtn.classList.replace('btn-success', 'btn-outline-secondary');
                this.disableManualSort();
            }
        }
    }

    enableManualSort() {
        // Включаем возможность перетаскивания для сортировки
        if (this.fileList && typeof Sortable !== 'undefined') {
            new Sortable(this.fileList, {
                animation: 150,
                ghostClass: 'sortable-ghost',
                onEnd: () => {
                    // Обновляем порядок файлов в массиве
                    this.updateFilesOrder();
                }
            });
        }
    }

    disableManualSort() {
        // Отключаем сортировку
        // (Sortable.js автоматически уничтожается при создании нового экземпляра)
    }

    updateFilesOrder() {
        const fileItems = Array.from(this.fileList.querySelectorAll('.file-list-item'));
        const newFiles = [];

        fileItems.forEach(item => {
            const fileName = item.dataset.fileName;
            const file = this.files.find(f => f.name === fileName);
            if (file) {
                newFiles.push(file);
            }
        });

        this.files = newFiles;
        this.updateFormData();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    updateFileInterface() {
        // Обновляем счетчики
        this.updateFileCounters();

        // Показываем/скрываем блок с файлами
        if (this.uploadedFiles) {
            if (this.files.length > 0) {
                this.uploadedFiles.classList.remove('d-none');
            } else {
                this.uploadedFiles.classList.add('d-none');
            }
        }

        // Обновляем предупреждение о лимите
        this.updateLimitWarning();

        // Обновляем FormData
        this.updateFormData();
    }

    updateFileCounters() {
        const fileCount = document.getElementById('fileCount');
        const currentFileCount = document.getElementById('currentFileCount');

        if (fileCount) {
            fileCount.textContent = `(${this.files.length})`;
        }

        if (currentFileCount) {
            const totalCount = this.files.length + this.existingCount;
            currentFileCount.textContent = totalCount;

            // Подсвечиваем если接近 лимита
            if (totalCount >= this.maxFiles) {
                currentFileCount.classList.add('text-danger', 'fw-bold');
            } else if (totalCount >= this.maxFiles - 3) {
                currentFileCount.classList.add('text-warning');
                currentFileCount.classList.remove('text-danger', 'fw-bold');
            } else {
                currentFileCount.classList.remove('text-danger', 'text-warning', 'fw-bold');
            }
        }
    }

    updateLimitWarning() {
        const warningElement = document.querySelector('.limit-warning');
        if (warningElement) {
            const remaining = this.maxFiles - (this.files.length + this.existingCount);

            if (remaining <= 0) {
                warningElement.textContent = 'Достигнут лимит файлов!';
                warningElement.classList.remove('d-none', 'text-warning');
                warningElement.classList.add('text-danger', 'fw-bold');
            } else if (remaining <= 3) {
                warningElement.textContent = `Можно добавить еще ${remaining} файлов`;
                warningElement.classList.remove('d-none', 'text-danger', 'fw-bold');
                warningElement.classList.add('text-warning');
            } else {
                warningElement.classList.add('d-none');
            }
        }
    }

    updateFormData() {
        if (!this.fileInput) return;

        // Создаем новый DataTransfer
        const dt = new DataTransfer();
        this.files.forEach(file => {
            dt.items.add(file);
        });

        // Обновляем input файлов
        this.fileInput.files = dt.files;
    }

    getExistingImageCount() {
        const currentFileCount = document.getElementById('currentFileCount');
        if (currentFileCount && this.isEdit) {
            return parseInt(currentFileCount.textContent) || 0;
        }
        return 0;
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    showAlert(message, type = 'info') {
        // Создаем временное уведомление
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alert.style.top = '20px';
        alert.style.right = '20px';
        alert.style.zIndex = '9999';
        alert.style.minWidth = '300px';
        alert.innerHTML = `
            <i class="bi ${type === 'danger' ? 'bi-exclamation-triangle' :
                           type === 'warning' ? 'bi-exclamation-circle' :
                           'bi-info-circle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(alert);

        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            if (alert.parentNode) {
                alert.classList.remove('show');
                setTimeout(() => alert.remove(), 300);
            }
        }, 5000);
    }

    // ========== ВАЛИДАЦИЯ ФОРМЫ ==========

    validateForm() {
        let isValid = true;

        // Проверка обязательных полей
        isValid = this.validateRequiredFields() && isValid;

        // Проверка категорий
        isValid = this.validateCategories() && isValid;

        // Проверка файлов
        isValid = this.validateFiles() && isValid;

        return isValid;
    }

    validateRequiredFields() {
        let isValid = true;
        const requiredFields = this.form.querySelectorAll('[required]');

        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                field.classList.add('is-invalid');
                isValid = false;
            } else {
                field.classList.remove('is-invalid');
            }
        });

        return isValid;
    }

    validateCategories() {
        const categoryCheckboxes = this.form.querySelectorAll('.category-checkbox:checked');
        const categoryContainer = document.querySelector('.categories-container');
        let isValid = categoryCheckboxes.length > 0;

        if (categoryContainer) {
            if (isValid) {
                categoryContainer.classList.remove('border-danger');
            } else {
                categoryContainer.classList.add('border-danger');
            }
        }

        return isValid;
    }

    validateFiles() {
        const totalFiles = this.files.length + this.existingCount;
        let isValid = true;

        // При создании нужно хотя бы одно изображение
        if (!this.isEdit && totalFiles === 0) {
            if (this.dropzone) {
                this.dropzone.classList.add('border-danger');
            }
            isValid = false;
        } else if (this.dropzone) {
            this.dropzone.classList.remove('border-danger');
        }

        // Проверка лимита файлов
        if (totalFiles > this.maxFiles) {
            this.showAlert(`Превышен лимит файлов. Максимум: ${this.maxFiles}`, 'danger');
            isValid = false;
        }

        return isValid;
    }

    // ========== DRAG & DROP ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    highlightDropzone() {
        if (this.dropzone) {
            this.dropzone.classList.add('dragover');
        }
    }

    unhighlightDropzone() {
        if (this.dropzone) {
            this.dropzone.classList.remove('dragover');
        }
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    window.photoGalleryForm = new PhotoGalleryForm();

    // Инициализация Bootstrap компонентов
    initBootstrapComponents();
});

function initBootstrapComponents() {
    // Tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Модальные окна
    const modalElements = document.querySelectorAll('.modal');
    modalElements.forEach(modalEl => {
        new bootstrap.Modal(modalEl);
    });
}

// Глобальные функции для вызова из HTML
function removeFile(fileId) {
    if (window.photoGalleryForm) {
        window.photoGalleryForm.removeFile(fileId);
    }
}

function setAsPrimary(fileId) {
    if (window.photoGalleryForm) {
        window.photoGalleryForm.setAsPrimary(fileId);
    }
}