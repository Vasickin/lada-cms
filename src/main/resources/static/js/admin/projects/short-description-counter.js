const MAX_SHORT_DESCRIPTION_LENGTH = 2000;
let shortDescriptionTextarea, countElement, errorElement, errorCountElement;

function updateShortDescriptionCounter() {
    if (!shortDescriptionTextarea || !countElement) return;

    const length = shortDescriptionTextarea.value.length;
    countElement.textContent = length;

    if (length > MAX_SHORT_DESCRIPTION_LENGTH) {
        countElement.style.color = '#dc3545';
        countElement.style.fontWeight = 'bold';
        shortDescriptionTextarea.classList.add('is-invalid');
        if (errorElement) {
            errorElement.style.display = 'block';
            if (errorCountElement) errorCountElement.textContent = length;
        }
    } else if (length > MAX_SHORT_DESCRIPTION_LENGTH * 0.9) {
        countElement.style.color = '#fd7e14';
        countElement.style.fontWeight = 'bold';
        shortDescriptionTextarea.classList.remove('is-invalid');
        shortDescriptionTextarea.classList.add('is-valid');
        if (errorElement) errorElement.style.display = 'none';
    } else if (length > MAX_SHORT_DESCRIPTION_LENGTH * 0.7) {
        countElement.style.color = '#0d6efd';
        shortDescriptionTextarea.classList.remove('is-invalid');
        shortDescriptionTextarea.classList.add('is-valid');
        if (errorElement) errorElement.style.display = 'none';
    } else {
        countElement.style.color = '';
        countElement.style.fontWeight = '';
        shortDescriptionTextarea.classList.remove('is-invalid', 'is-valid');
        if (errorElement) errorElement.style.display = 'none';
    }
}

function validateShortDescription(event) {
    if (!shortDescriptionTextarea) return true;
    const length = shortDescriptionTextarea.value.length;

    if (length > MAX_SHORT_DESCRIPTION_LENGTH) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        shortDescriptionTextarea.classList.add('is-invalid');
        shortDescriptionTextarea.scrollIntoView({ behavior: 'smooth', block: 'center' });
        shortDescriptionTextarea.focus();
        return false;
    }
    return true;
}

document.addEventListener('DOMContentLoaded', function() {
    shortDescriptionTextarea = document.getElementById('shortDescription');
    countElement = document.getElementById('shortDescriptionCount');
    errorElement = document.getElementById('shortDescriptionError');
    errorCountElement = document.getElementById('shortDescriptionErrorCount');

    if (!shortDescriptionTextarea) return;

    updateShortDescriptionCounter();
    shortDescriptionTextarea.addEventListener('input', updateShortDescriptionCounter);

    const form = document.querySelector('form.needs-validation');
    if (form) {
        form.addEventListener('submit', validateShortDescription);
        shortDescriptionTextarea.addEventListener('blur', () => validateShortDescription(null));
    }
});