/**
 * Native Travel Main Client Script
 */
document.addEventListener('DOMContentLoaded', () => {
    // Enable Bootstrap tooltips if any
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    // Listen to HTMX events for notifications / error handling
    document.body.addEventListener('htmx:responseError', (event) => {
        console.error('HTMX Response Error:', event.detail);
        alert('Произошла ошибка при выполнении запроса. Пожалуйста, попробуйте позже.');
    });
});
