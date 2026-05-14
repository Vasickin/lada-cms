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
 *
 * Используется в:
 * - admin/projects/create.html (создание нового проекта)
 * - admin/projects/edit.html (редактирование проекта)
 *
 * Особенности:
 * - Универсальный код для обеих страниц
 * - Не зависит от имён переменных Thymeleaf
 * - Работает с DOM напрямую через data-атрибуты
 *
 * @version 1.0
 * @since 2026-01-14
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

    // Обновляем счётчики
    updateTeamCounters();

    // Обновляем скрытое поле
    updateTeamHiddenInput();

    console.log('✅ DnD для команды инициализирован');
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