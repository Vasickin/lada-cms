// static/js/admin/projects/common.js

// ===== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ =====
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showAlert(message, type = 'success') {
    const oldAlerts = document.querySelectorAll('.custom-alert');
    oldAlerts.forEach(alert => alert.remove());

    const alertDiv = document.createElement('div');
    alertDiv.className = `custom-alert alert alert-${type} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = `
        top: 20px;
        right: 20px;
        z-index: 9999;
        max-width: 300px;
        animation: slideInRight 0.3s ease;
    `;

    alertDiv.innerHTML = `
        <i class="bi ${type === 'success' ? 'bi-check-circle' : 'bi-exclamation-triangle'} me-1"></i>
        ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(alertDiv);

    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getOrCreateInstance(alertDiv);
        if (bsAlert) bsAlert.close();
    }, 3000);
}

// ===== BOOTSTRAP ВАЛИДАЦИЯ =====
(function() {
    'use strict'
    const forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }
            form.classList.add('was-validated')
        }, false)
    })
})();

// ===== СОЗДАНИЕ НОВОЙ КАТЕГОРИИ =====
document.addEventListener('DOMContentLoaded', function() {
    const createBtn = document.getElementById('createCategoryBtn');
    const categorySelect = document.getElementById('category');

    if (createBtn && categorySelect) {
        createBtn.addEventListener('click', function() {
            const newCategory = prompt('Введите название новой категории:');

            if (newCategory && newCategory.trim()) {
                const trimmedName = newCategory.trim();

                // Проверяем, нет ли уже такой категории
                let exists = false;
                for (let i = 0; i < categorySelect.options.length; i++) {
                    if (categorySelect.options[i].value === trimmedName) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    alert('Категория "' + trimmedName + '" уже существует!');
                    categorySelect.value = trimmedName;
                } else {
                    const newOption = document.createElement('option');
                    newOption.value = trimmedName;
                    newOption.textContent = trimmedName;
                    categorySelect.appendChild(newOption);
                    categorySelect.value = trimmedName;

                    showAlert(`Категория "${trimmedName}" создана и выбрана!`, 'success');
                }
            }
        });
    }
});

// Добавляем CSS для анимации
if (!document.querySelector('#common-alert-styles')) {
    const style = document.createElement('style');
    style.id = 'common-alert-styles';
    style.textContent = `
        .custom-alert {
            animation: slideInRight 0.3s ease;
        }
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
    `;
    document.head.appendChild(style);
}

console.log('✅ common.js загружен');