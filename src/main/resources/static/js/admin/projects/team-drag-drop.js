/**
 * ============================================================================
 * МОДУЛЬ: Drag & Drop для управления командой проекта
 * ============================================================================
 *
 * Назначение:
 * - Обеспечивает перетаскивание членов команды между колонками
 * - Поддерживает как create.html (все члены в одной колонке), так и edit.html (разделение на сервере)
 * - Автоматически определяет структуру DOM при инициализации
 * - Обновляет скрытое поле с ID выбранных участников
 * - Синхронизирует счётчики в статистике
 * - ВОССТАНАВЛИВАЕТ состояние после ошибок валидации из hidden поля
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 * - admin/projects/edit.html (редактирование проекта)
 *
 * Особенности:
 * - Универсальный код для обеих страниц
 * - Не зависит от имён переменных Thymeleaf
 * - Работает с DOM напрямую через data-атрибуты
 * - Сохраняет состояние в hidden поле и восстанавливает из него
 *
 * @version 2.0
 * @since 2026-01-15
 */

/**
 * Инициализация модуля Drag & Drop
 */
function initTeamDragAndDrop() {
    console.log('🖱️ Инициализация DnD для команды проекта...');

    // Находим контейнеры
    const availableContainer = document.getElementById('availableMembersContainer');
    const projectContainer = document.getElementById('projectTeamContainer');

    if (!availableContainer || !projectContainer) {
        console.log('DnD для команды: контейнеры не найдены');
        return;
    }

    // Настраиваем перетаскивание для всех элементов
    setupDraggableElements();

    // Настраиваем зоны Drop
    setupDropZones(availableContainer, projectContainer);

    // ВАЖНО: Синхронизируем состояние из hidden поля (восстановление после ошибок)
    syncTeamFromHiddenField();

    // Обновляем счётчики
    updateTeamCounters();

    updateTeamPreview();

    // Обновляем скрытое поле
    updateTeamHiddenInput();

    console.log('✅ DnD для команды инициализирован');
}

/**
 * Синхронизирует DnD компоненты с hidden полем selectedTeamMemberIds
 * Вызывается при инициализации для восстановления состояния после ошибки валидации
 */
function syncTeamFromHiddenField() {
    const hiddenField = document.getElementById('selectedTeamMemberIds');
    if (!hiddenField || !hiddenField.value) {
        console.log('Нет сохранённых ID команды, используем начальное состояние');
        return;
    }

    const selectedIds = hiddenField.value.split(',').filter(id => id.trim() !== '');
    if (selectedIds.length === 0) return;

    console.log('🔄 Синхронизация команды из hidden поля:', selectedIds);

    // Получаем все элементы команды
    const allMembers = document.querySelectorAll('.member-draggable');

    allMembers.forEach(member => {
        const memberId = member.getAttribute('data-member-id');
        const isSelected = selectedIds.includes(memberId);
        const isInProject = member.closest('#projectTeamContainer') !== null;

        // Если должен быть в проекте, но находится в доступных - перемещаем
        if (isSelected && !isInProject) {
            moveTeamMember(memberId, 'projectTeamContainer');
        }
        // Если не должен быть в проекте, но находится в проекте - перемещаем обратно
        else if (!isSelected && isInProject) {
            moveTeamMember(memberId, 'availableMembersContainer');
        }
    });

    // Обновляем счётчики после синхронизации
    updateTeamCounters();
    updateTeamPreview();
    console.log('✅ Синхронизация команды завершена');
}

/**
 * Настраивает все элементы с классом .member-draggable для перетаскивания
 */
function setupDraggableElements() {
    const draggableElements = document.querySelectorAll('.member-draggable');

    draggableElements.forEach(element => {
        // Устанавливаем атрибут draggable
        element.setAttribute('draggable', 'true');

        // Обработчик начала перетаскивания
        element.addEventListener('dragstart', function(e) {
            e.dataTransfer.setData('text/plain', this.getAttribute('data-member-id'));
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
                document.querySelectorAll('.member-draggable.selected').forEach(el => {
                    el.classList.remove('selected');
                });
            }
            this.classList.toggle('selected');
        });
    });
}

/**
 * Настраивает зоны, куда можно перетаскивать элементы
 * @param {HTMLElement} availableContainer - контейнер доступных членов
 * @param {HTMLElement} projectContainer - контейнер команды проекта
 */
function setupDropZones(availableContainer, projectContainer) {
    const containers = [availableContainer, projectContainer];

    containers.forEach(container => {
        container.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('drop-zone-active');
        });

        container.addEventListener('dragleave', function() {
            this.classList.remove('drop-zone-active');
        });

        container.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('drop-zone-active');

            const memberId = e.dataTransfer.getData('text/plain');
            const draggedElement = document.querySelector(`.member-draggable[data-member-id="${memberId}"]`);

            if (draggedElement && draggedElement.parentElement !== this) {
                moveTeamMember(memberId, this.id);
            }
        });
    });
}

/**
 * Перемещает члена команды между контейнерами
 * @param {string} memberId - ID члена команды
 * @param {string} targetContainerId - ID целевого контейнера
 */
function moveTeamMember(memberId, targetContainerId) {
    const memberElement = document.querySelector(`.member-draggable[data-member-id="${memberId}"]`);
    if (!memberElement) return;

    const targetContainer = document.getElementById(targetContainerId);
    if (!targetContainer) return;

    // Определяем направление перемещения
    const isMovingToProject = targetContainerId === 'projectTeamContainer';

    // Клонируем элемент с новыми стилями
    const clonedElement = memberElement.cloneNode(true);

    // ✅ ДОБАВИТЬ ЭТОТ БЛОК - сохраняем URL аватарки
    const avatarUrl = memberElement.getAttribute('data-avatar-url');
    if (avatarUrl) {
        clonedElement.setAttribute('data-avatar-url', avatarUrl);
    }

    // Меняем стили в зависимости от колонки
    if (isMovingToProject) {
        // Перемещаем в проект - меняем иконку и бейдж
        const icon = clonedElement.querySelector('.bi-person-circle, .bi-person-check');
        if (icon) {
            icon.className = 'bi bi-person-check fs-4 text-success';
        }

        const badge = clonedElement.querySelector('.badge');
        if (badge) {
            badge.className = 'badge bg-success text-white';
            badge.textContent = 'В проекте';
        }
    } else {
        // Перемещаем обратно в доступные - меняем иконку и бейдж
        const icon = clonedElement.querySelector('.bi-person-circle, .bi-person-check');
        if (icon) {
            icon.className = 'bi bi-person-circle fs-4 text-primary';
        }

        const badge = clonedElement.querySelector('.badge');
        if (badge) {
            badge.className = 'badge bg-primary text-white';
            badge.textContent = 'Доступен';
        }
    }

    // Очищаем классы выделения
    clonedElement.classList.remove('selected', 'dragging');

    // Удаляем старый элемент
    memberElement.remove();

    // Добавляем новый в целевую колонку
    targetContainer.appendChild(clonedElement);

    // Настраиваем события для нового элемента
    setupDraggableForSingleElement(clonedElement);

    // Обновляем UI
    updateTeamCounters();
    updateTeamHiddenInput();
    updateEmptyTeamMessage();
    updateTeamPreview();
}

/**
 * Настраивает DnD для одного элемента (после клонирования)
 * @param {HTMLElement} element - элемент для настройки
 */
function setupDraggableForSingleElement(element) {
    element.setAttribute('draggable', 'true');

    element.addEventListener('dragstart', function(e) {
        e.dataTransfer.setData('text/plain', this.getAttribute('data-member-id'));
        this.classList.add('dragging');
    });

    element.addEventListener('dragend', function() {
        this.classList.remove('dragging');
    });

    element.addEventListener('click', function(e) {
        if (!e.ctrlKey && !e.metaKey) {
            document.querySelectorAll('.member-draggable.selected').forEach(el => {
                el.classList.remove('selected');
            });
        }
        this.classList.toggle('selected');
    });
}

/**
 * Обновляет все счётчики на странице
 */
function updateTeamCounters() {
    const availableCount = document.querySelectorAll('#availableMembersContainer .member-draggable').length;
    const projectCount = document.querySelectorAll('#projectTeamContainer .member-draggable').length;

    // Обновляем счётчики в интерфейсе
    const availableCountElement = document.getElementById('availableCount');
    const projectTeamCountElement = document.getElementById('projectTeamCount');
    const projectMembersCountElement = document.getElementById('projectMembersCount');

    if (availableCountElement) availableCountElement.textContent = String(availableCount);
    if (projectTeamCountElement) projectTeamCountElement.textContent = String(projectCount);
    if (projectMembersCountElement) projectMembersCountElement.textContent = String(projectCount);
}

/**
 * Обновляет скрытое поле с ID выбранных членов команды
 */
function updateTeamHiddenInput() {
    const selectedElements = document.querySelectorAll('#projectTeamContainer .member-draggable');
    const ids = Array.from(selectedElements).map(el => el.getAttribute('data-member-id'));

    const hiddenField = document.getElementById('selectedTeamMemberIds');
    if (hiddenField) {
        hiddenField.value = ids.join(',');
        console.log('📝 Обновлён список участников проекта:', ids);
    }
}

/**
 * Показывает или скрывает сообщение о пустой команде проекта
 */
function updateEmptyTeamMessage() {
    const projectContainer = document.getElementById('projectTeamContainer');
    const emptyMessage = document.getElementById('emptyTeamMessage');

    if (projectContainer && emptyMessage) {
        const hasMembers = projectContainer.querySelectorAll('.member-draggable').length > 0;
        emptyMessage.style.display = hasMembers ? 'none' : 'block';
    }
}

/**
 * Добавляет выбранных членов в проект
 */
function addSelectedToProject() {
    const selected = document.querySelectorAll('#availableMembersContainer .member-draggable.selected');
    selected.forEach(member => {
        const memberId = member.getAttribute('data-member-id');
        moveTeamMember(memberId, 'projectTeamContainer');
    });
}

/**
 * Удаляет выбранных членов из проекта
 */
function removeSelectedFromProject() {
    const selected = document.querySelectorAll('#projectTeamContainer .member-draggable.selected');
    selected.forEach(member => {
        const memberId = member.getAttribute('data-member-id');
        moveTeamMember(memberId, 'availableMembersContainer');
    });
}

// Делаем функции глобальными для вызова из HTML (onclick атрибуты)
window.addSelectedToProject = addSelectedToProject;
window.removeSelectedFromProject = removeSelectedFromProject;

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initTeamDragAndDrop();
});

/**
 * Обновляет превью выбранных членов команды (аналогично партнёрам)
 */
function updateTeamPreview() {
    const previewContainer = document.getElementById('selectedTeamPreview');
    if (!previewContainer) return;

    const projectContainer = document.getElementById('projectTeamContainer');
    if (!projectContainer) return;

    const selectedElements = projectContainer.querySelectorAll('.member-draggable');

    if (selectedElements.length === 0) {
        previewContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-light text-center py-4">
                    <i class="bi bi-people display-4 text-muted mb-3"></i>
                    <h6 class="text-muted">Члены команды не выбраны</h6>
                    <p class="text-muted small mb-0">
                        Добавьте членов команды в проект для отображения в предпросмотре
                    </p>
                </div>
            </div>
        `;
        return;
    }

    let html = '<div class="col-12 mb-2"><strong>Выбранные члены команды:</strong></div>';

    selectedElements.forEach(element => {
        const memberId = element.getAttribute('data-member-id');
        const name = element.querySelector('strong')?.textContent || `Участник ${memberId}`;
        const position = element.querySelector('small.text-muted, small:not(.text-muted)')?.textContent || 'Должность не указана';

        // Получаем URL аватарки из data-атрибута (как у партнёров)
        const avatarUrl = element.getAttribute('data-avatar-url');

        let avatarHtml = '';
        if (avatarUrl && avatarUrl.trim() !== '') {
            avatarHtml = `<img src="${escapeHtml(avatarUrl)}" 
                               alt="${escapeHtml(name)}" 
                               style="width: 60px; height: 60px; object-fit: cover; border-radius: 50%;" 
                               class="img-fluid">`;
        } else {
            avatarHtml = '<i class="bi bi-person-badge fs-1 text-primary"></i>';
        }

        html += `
            <div class="col-6 col-md-4 col-lg-3 mb-3" data-preview-member-id="${memberId}">
                <div class="card h-100">
                    <div class="card-body text-center">
                        <div class="mb-3" style="height: 60px; display: flex; align-items: center; justify-content: center;">
                            ${avatarHtml}
                        </div>
                        <h6 class="mb-1">${escapeHtml(name)}</h6>
                        <small class="text-muted d-block mb-2">${escapeHtml(position)}</small>
                        <span class="badge bg-success">В проекте</span>
                    </div>
                </div>
            </div>
        `;
    });

    previewContainer.innerHTML = html;
}