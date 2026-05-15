/**
 * ============================================================================
 * МОДУЛЬ: Drag & Drop для управления партнёрами проекта
 * ============================================================================
 *
 * Назначение:
 * - Обеспечивает перетаскивание партнёров между колонками
 * - Поддерживает как create.html (все партнёры в одной колонке), так и edit.html (разделение на сервере)
 * - Автоматически определяет структуру DOM при инициализации
 * - Обновляет скрытое поле с ID выбранных партнёров
 * - Синхронизирует счётчики и превью
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 * - admin/projects/edit.html (редактирование проекта)
 *
 * Особенности:
 * - Универсальный код для обеих страниц
 * - Сохраняет URL логотипа при перемещении (data-logo-url)
 * - Автоматически обновляет превью выбранных партнёров
 *
 * @version 1.0
 * @since 2026-01-14
 */

/**
 * Инициализация модуля Drag & Drop для партнёров
 */
function initPartnersDragAndDrop() {
    console.log('🖱️ Инициализация DnD для партнёров...');

    // Находим контейнеры
    const availableContainer = document.getElementById('availablePartnersContainer');
    const projectContainer = document.getElementById('projectPartnersContainer');

    if (!availableContainer || !projectContainer) {
        console.log('DnD для партнёров: контейнеры не найдены');
        return;
    }

    // Настраиваем перетаскивание для всех элементов
    setupPartnerDraggableElements();

    // Настраиваем зоны Drop
    setupPartnerDropZones(availableContainer, projectContainer);

    // Синхронизируем состояние из hidden поля (восстановление после ошибок)
    syncPartnersFromHiddenField();

    // Обновляем счётчики
    updatePartnerCounters();

    // Обновляем скрытое поле
    updatePartnerHiddenInput();

    // Обновляем превью
    updatePartnersPreview();

    console.log('✅ DnD для партнёров инициализирован');
}

/**
 * Синхронизирует DnD компоненты с hidden полем selectedPartnerIds
 * Вызывается при инициализации для восстановления состояния после ошибки валидации
 */
function syncPartnersFromHiddenField() {
    const hiddenField = document.getElementById('selectedPartnerIds');
    if (!hiddenField || !hiddenField.value) {
        console.log('Нет сохранённых ID партнёров, используем начальное состояние');
        return;
    }

    const selectedIds = hiddenField.value.split(',').filter(id => id.trim() !== '');
    if (selectedIds.length === 0) return;

    console.log('🔄 Синхронизация партнёров из hidden поля:', selectedIds);

    // Получаем все элементы партнёров
    const allPartners = document.querySelectorAll('.partner-draggable');

    allPartners.forEach(partner => {
        const partnerId = partner.getAttribute('data-partner-id');
        const isSelected = selectedIds.includes(partnerId);
        const isInProject = partner.closest('#projectPartnersContainer') !== null;

        // Если должен быть в проекте, но находится в доступных - перемещаем
        if (isSelected && !isInProject) {
            movePartner(partnerId, 'projectPartnersContainer');
        }
        // Если не должен быть в проекте, но находится в проекте - перемещаем обратно
        else if (!isSelected && isInProject) {
            movePartner(partnerId, 'availablePartnersContainer');
        }
    });

    updatePartnerCounters();
    updatePartnersPreview();
    console.log('✅ Синхронизация партнёров завершена');
}

/**
 * Настраивает все элементы с классом .partner-draggable для перетаскивания
 */
function setupPartnerDraggableElements() {
    const draggableElements = document.querySelectorAll('.partner-draggable');

    draggableElements.forEach(element => {
        // Устанавливаем атрибут draggable
        element.setAttribute('draggable', 'true');

        // Обработчик начала перетаскивания
        element.addEventListener('dragstart', function(e) {
            e.dataTransfer.setData('text/plain', this.getAttribute('data-partner-id'));
            this.classList.add('dragging');
        });

        // Обработчик окончания перетаскивания
        element.addEventListener('dragend', function() {
            this.classList.remove('dragging');
        });

        // Обработчик клика для выделения (Ctrl/Cmd для множественного)
        element.addEventListener('click', function(e) {
            if (!e.ctrlKey && !e.metaKey) {
                // Снимаем выделение со всех
                document.querySelectorAll('.partner-draggable.selected').forEach(el => {
                    el.classList.remove('selected');
                });
            }
            this.classList.toggle('selected');
        });
    });
}

/**
 * Настраивает зоны, куда можно перетаскивать партнёров
 * @param {HTMLElement} availableContainer - контейнер доступных партнёров
 * @param {HTMLElement} projectContainer - контейнер партнёров проекта
 */
function setupPartnerDropZones(availableContainer, projectContainer) {
    const containers = [availableContainer, projectContainer];

    containers.forEach(container => {
        container.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('partner-drop-zone-active');
        });

        container.addEventListener('dragleave', function() {
            this.classList.remove('partner-drop-zone-active');
        });

        container.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('partner-drop-zone-active');

            const partnerId = e.dataTransfer.getData('text/plain');
            const draggedElement = document.querySelector(`.partner-draggable[data-partner-id="${partnerId}"]`);

            if (draggedElement && draggedElement.parentElement !== this) {
                movePartner(partnerId, this.id);
            }
        });
    });
}

/**
 * Перемещает партнёра между контейнерами
 * @param {string} partnerId - ID партнёра
 * @param {string} targetContainerId - ID целевого контейнера
 */
function movePartner(partnerId, targetContainerId) {
    const partnerElement = document.querySelector(`.partner-draggable[data-partner-id="${partnerId}"]`);
    if (!partnerElement) return;

    // Сохраняем URL логотипа из старого элемента
    const logoUrl = partnerElement.getAttribute('data-logo-url') || '';

    const targetContainer = document.getElementById(targetContainerId);
    if (!targetContainer) return;

    // Определяем направление перемещения
    const isMovingToProject = targetContainerId === 'projectPartnersContainer';

    // Клонируем элемент с новыми стилями
    const clonedElement = partnerElement.cloneNode(true);

    // Сохраняем URL логотипа в новом элементе
    clonedElement.setAttribute('data-logo-url', logoUrl);

    // Очищаем классы выделения
    clonedElement.classList.remove('selected', 'dragging');

    // Меняем стили в зависимости от колонки
    if (isMovingToProject) {
        // Перемещаем в проект - зелёные стили
        const icon = clonedElement.querySelector('.bi-building, .bi-handshake');
        if (icon) {
            icon.className = 'bi bi-handshake fs-4 text-success';
        }

        const badge = clonedElement.querySelector('.badge');
        if (badge) {
            badge.className = 'badge bg-success text-white';
            badge.textContent = 'В проекте';
        }
    } else {
        // Перемещаем в доступные - синие стили
        const icon = clonedElement.querySelector('.bi-building, .bi-handshake');
        if (icon) {
            icon.className = 'bi bi-building fs-4 text-info';
        }

        const badge = clonedElement.querySelector('.badge');
        if (badge) {
            badge.className = 'badge bg-info text-white';
            badge.textContent = 'Доступен';
        }
    }

    // Удаляем старый элемент
    partnerElement.remove();

    // Добавляем новый в целевую колонку
    targetContainer.appendChild(clonedElement);

    // Настраиваем события для нового элемента
    setupPartnerDraggableForSingleElement(clonedElement);

    // Обновляем UI
    updatePartnerCounters();
    updatePartnerHiddenInput();
    updateEmptyPartnerMessage();
    updatePartnersPreview();
}

/**
 * Настраивает DnD для одного элемента (после клонирования)
 * @param {HTMLElement} element - элемент для настройки
 */
function setupPartnerDraggableForSingleElement(element) {
    element.setAttribute('draggable', 'true');

    element.addEventListener('dragstart', function(e) {
        e.dataTransfer.setData('text/plain', this.getAttribute('data-partner-id'));
        this.classList.add('dragging');
    });

    element.addEventListener('dragend', function() {
        this.classList.remove('dragging');
    });

    element.addEventListener('click', function(e) {
        if (!e.ctrlKey && !e.metaKey) {
            document.querySelectorAll('.partner-draggable.selected').forEach(el => {
                el.classList.remove('selected');
            });
        }
        this.classList.toggle('selected');
    });
}

/**
 * Обновляет все счётчики партнёров на странице
 */
function updatePartnerCounters() {
    const availableCount = document.querySelectorAll('#availablePartnersContainer .partner-draggable').length;
    const projectCount = document.querySelectorAll('#projectPartnersContainer .partner-draggable').length;

    // Обновляем счётчики в интерфейсе
    const availableCountElement = document.getElementById('availablePartnersCount');
    const projectDragCountElement = document.getElementById('projectPartnersDragCount');
    const selectedCountElement = document.getElementById('selectedPartnersCount');
    const projectCountElement = document.getElementById('projectPartnersCount');

    if (availableCountElement) availableCountElement.textContent = String(availableCount);
    if (projectDragCountElement) projectDragCountElement.textContent = String(projectCount);
    if (selectedCountElement) selectedCountElement.textContent = String(projectCount);
    if (projectCountElement) projectCountElement.textContent = String(projectCount);
}

/**
 * Обновляет скрытое поле с ID выбранных партнёров
 */
function updatePartnerHiddenInput() {
    const selectedElements = document.querySelectorAll('#projectPartnersContainer .partner-draggable');
    const ids = Array.from(selectedElements).map(el => el.getAttribute('data-partner-id'));

    let hiddenField = document.getElementById('selectedPartnerIds');

    // Если поля нет - создаём его
    if (!hiddenField) {
        hiddenField = document.createElement('input');
        hiddenField.type = 'hidden';
        hiddenField.id = 'selectedPartnerIds';
        hiddenField.name = 'selectedPartnerIds';

        const form = document.querySelector('form.needs-validation');
        if (form) {
            form.appendChild(hiddenField);
        }
    }

    if (hiddenField) {
        hiddenField.value = ids.join(',');
        console.log('📝 Обновлён список партнёров проекта:', ids);
    }
}

/**
 * Показывает или скрывает сообщение о пустых колонках
 */
function updateEmptyPartnerMessage() {
    const projectContainer = document.getElementById('projectPartnersContainer');
    const emptyMessage = document.getElementById('emptyPartnersMessage');

    if (projectContainer && emptyMessage) {
        const hasPartners = projectContainer.querySelectorAll('.partner-draggable').length > 0;
        emptyMessage.style.display = hasPartners ? 'none' : 'block';
    }
}

/**
 * Обновляет превью выбранных партнёров
 */
function updatePartnersPreview() {
    const previewContainer = document.getElementById('selectedPartnersPreview');
    if (!previewContainer) return;

    const projectContainer = document.getElementById('projectPartnersContainer');
    if (!projectContainer) return;

    const selectedElements = projectContainer.querySelectorAll('.partner-draggable');

    if (selectedElements.length === 0) {
        previewContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-light text-center py-4">
                    <i class="bi bi-handshake display-4 text-muted mb-3"></i>
                    <h6 class="text-muted">Партнёры не выбраны</h6>
                    <p class="text-muted small mb-0">
                        Добавьте партнёров в проект для отображения логотипов на странице
                    </p>
                </div>
            </div>
        `;
        return;
    }

    let html = '<div class="col-12 mb-2"><strong>Выбранные партнёры:</strong></div>';

    selectedElements.forEach(element => {
        const partnerId = element.getAttribute('data-partner-id');
        const logoUrl = element.getAttribute('data-logo-url') || '';
        const name = element.querySelector('strong')?.textContent || `Партнёр ${partnerId}`;
        const typeElement = element.querySelector('small.text-muted');
        const type = typeElement ? typeElement.textContent : 'Тип не указан';

        // Отображение логотипа или иконки
        let logoHtml;
        if (logoUrl && logoUrl.trim() !== '') {
            logoHtml = `<img src="${escapeHtml(logoUrl)}"
                             alt="${escapeHtml(name)}"
                             style="max-height: 60px; max-width: 100%; object-fit: contain;"
                             class="img-fluid">`;
        } else {
            logoHtml = '<i class="bi bi-handshake fs-1 text-success"></i>';
        }

        html += `
            <div class="col-6 col-md-4 col-lg-3 mb-3">
                <div class="card h-100">
                    <div class="card-body text-center">
                        <div class="mb-3" style="height: 60px; display: flex; align-items: center; justify-content: center;">
                            ${logoHtml}
                        </div>
                        <h6 class="mb-1">${escapeHtml(name)}</h6>
                        <small class="text-muted d-block mb-2">${escapeHtml(type)}</small>
                        <span class="badge bg-success">В проекте</span>
                    </div>
                </div>
            </div>
        `;
    });

    previewContainer.innerHTML = html;
}

/**
 * Добавляет выбранных партнёров в проект
 */
function addSelectedPartnersToProject() {
    const selected = document.querySelectorAll('#availablePartnersContainer .partner-draggable.selected');
    selected.forEach(partner => {
        const partnerId = partner.getAttribute('data-partner-id');
        movePartner(partnerId, 'projectPartnersContainer');
    });
}

/**
 * Удаляет выбранных партнёров из проекта
 */
function removeSelectedPartnersFromProject() {
    const selected = document.querySelectorAll('#projectPartnersContainer .partner-draggable.selected');
    selected.forEach(partner => {
        const partnerId = partner.getAttribute('data-partner-id');
        movePartner(partnerId, 'availablePartnersContainer');
    });
}

// Делаем функции глобальными для вызова из HTML (onclick атрибуты)
window.addSelectedPartnersToProject = addSelectedPartnersToProject;
window.removeSelectedPartnersFromProject = removeSelectedPartnersFromProject;

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initPartnersDragAndDrop();
});