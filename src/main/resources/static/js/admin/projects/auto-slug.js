// Функция генерации slug из названия
function generateSlugFromTitle() {
    const titleInput = document.getElementById('title');
    const slugInput = document.getElementById('slug');
    const slugPreview = document.getElementById('slugPreview');

    if (!titleInput.value.trim()) return;

    // Генерация slug (простая версия)
    let slug = titleInput.value
        .toLowerCase()
        .replace(/[а-яё]/g, function(match) {
            const map = {
                'а':'a','б':'b','в':'v','г':'g','д':'d','е':'e','ё':'yo',
                'ж':'zh','з':'z','и':'i','й':'y','к':'k','л':'l','м':'m',
                'н':'n','о':'o','п':'p','р':'r','с':'s','т':'t','у':'u',
                'ф':'f','х':'h','ц':'ts','ч':'ch','ш':'sh','щ':'shch',
                'ъ':'','ы':'y','ь':'','э':'e','ю':'yu','я':'ya',
                ' ':'-','_':'-','.':'-',',':'-','!':'','?':'',':':'',';':'',
                '(':'',')':'','[':'',']':'','{':'','}':'','\'':'','"':''
            };
            return map[match] || match;
        })
        .replace(/[^a-z0-9-]/g, '-')  // Все остальное в дефисы
        .replace(/-+/g, '-')          // Множественные дефисы в один
        .replace(/^-|-$/g, '');       // Убираем дефисы с краев

    // Сохраняем в скрытое поле
    slugInput.value = slug;

    // Показываем превью
    if (slugPreview) {
        slugPreview.textContent = slug;
        document.getElementById('slugStatus').style.display = 'block';
    }

    // Проверяем доступность (если нужно)
    checkSlugAvailability(slug);
}

// Проверка доступности slug
function checkSlugAvailability(slug) {
    if (!slug) return;

    // Можно добавить AJAX запрос к /admin/projects/check-slug
    // Пока просто логируем
    console.log('Generated slug:', slug);
}

// Показать/скрыть расширенное поле
function toggleSlugField() {
    const advancedField = document.getElementById('slugAdvancedField');
    const manualInput = document.getElementById('slugManual');
    const slugInput = document.getElementById('slug');

    if (advancedField.style.display === 'none') {
        advancedField.style.display = 'block';
        document.getElementById('slugStatus').style.display = 'none';
        manualInput.value = slugInput.value;
        manualInput.focus();
    } else {
        advancedField.style.display = 'none';
        document.getElementById('slugStatus').style.display = 'block';
    }
}

function hideSlugField() {
    document.getElementById('slugAdvancedField').style.display = 'none';
    document.getElementById('slugStatus').style.display = 'block';
}

// Обновить slug из ручного ввода
function updateSlugFromManual() {
    const manualInput = document.getElementById('slugManual');
    const slugInput = document.getElementById('slug');
    const slugPreview = document.getElementById('slugPreview');

    let manualSlug = manualInput.value
        .toLowerCase()
        .replace(/[^a-z0-9-]/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-|-$/g, '');

    if (manualSlug) {
        slugInput.value = manualSlug;
        slugPreview.textContent = manualSlug;
        hideSlugField();
        checkSlugAvailability(manualSlug);
    }
}

// Инициализация при загрузке
document.addEventListener('DOMContentLoaded', function() {
    // Генерируем slug если название уже есть
    const titleInput = document.getElementById('title');
    if (titleInput && titleInput.value) {
        setTimeout(generateSlugFromTitle, 100);
    }

    // Автогенерация при вводе
    if (titleInput) {
        titleInput.addEventListener('input', generateSlugFromTitle);
    }

    // Enter в ручном поле
    const manualInput = document.getElementById('slugManual');
    if (manualInput) {
        manualInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                updateSlugFromManual();
            }
        });
    }
});