// === КАСТОМНОЕ ПОДТВЕРЖДЕНИЕ ===
let currentForm = null;

function showCustomConfirm(message, form) {
    document.getElementById('customConfirmMessage').textContent = message;
    currentForm = form;
    const modal = new bootstrap.Modal(document.getElementById('customConfirmModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', function() {
    const confirmBtn = document.getElementById('customConfirmButton');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            if (currentForm) {
                const modal = bootstrap.Modal.getInstance(document.getElementById('customConfirmModal'));
                if (modal) modal.hide();
                currentForm.submit();
            }
        });
    }
});

// === ФИЛЬТРЫ И ПОИСК ===
document.addEventListener('DOMContentLoaded', function() {
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        // Автоотправка при изменении select
        const filterSelects = filterForm.querySelectorAll('select');
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                setTimeout(() => filterForm.submit(), 100);
            });
        });

        // Debounce для поиска
        const searchInput = filterForm.querySelector('input[name="search"]');
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => filterForm.submit(), 1500);
            });
            searchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    filterForm.submit();
                }
            });
        }
    }
});

// === СМЕНА РАЗМЕРА СТРАНИЦЫ ===
function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}