/**
 * Native Travel Global Client Script
 */

// Global Comparison Manager
window.NT_Compare = {
    STORAGE_KEY: 'nt_compare_tours',

    getIds() {
        try {
            const raw = localStorage.getItem(this.STORAGE_KEY);
            const arr = raw ? JSON.parse(raw) : [];
            return Array.isArray(arr) ? arr.map(Number).filter(n => !isNaN(n) && n > 0) : [];
        } catch (e) {
            console.error('Failed to parse compare list from localStorage:', e);
            return [];
        }
    },

    save(ids) {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(ids));
        this.updateUI();
        window.dispatchEvent(new CustomEvent('compare-updated', { detail: ids }));
    },

    has(id) {
        return this.getIds().includes(Number(id));
    },

    toggle(id) {
        id = Number(id);
        let ids = this.getIds();
        if (ids.includes(id)) {
            ids = ids.filter(x => x !== id);
        } else {
            if (ids.length >= 4) {
                alert('Можно выбрать не более 4 туров для сравнения');
                this.updateUI();
                return false;
            }
            ids.push(id);
        }
        this.save(ids);
        return true;
    },

    add(id) {
        id = Number(id);
        let ids = this.getIds();
        if (!ids.includes(id)) {
            if (ids.length >= 4) {
                alert('Можно выбрать не более 4 туров для сравнения');
                return;
            }
            ids.push(id);
            this.save(ids);
        }
        this.goToCompare();
    },

    remove(id) {
        id = Number(id);
        let ids = this.getIds().filter(x => x !== id);
        this.save(ids);
        if (window.location.pathname === '/compare') {
            if (ids.length > 0) {
                window.location.href = '/compare?ids=' + ids.join(',');
            } else {
                window.location.href = '/compare';
            }
        }
    },

    clear() {
        localStorage.removeItem(this.STORAGE_KEY);
        this.updateUI();
        window.dispatchEvent(new CustomEvent('compare-updated', { detail: [] }));
        if (window.location.pathname === '/compare') {
            window.location.href = '/compare';
        }
    },

    goToCompare(event) {
        if (event && event.preventDefault) {
            event.preventDefault();
        }
        const ids = this.getIds();
        if (ids.length > 0) {
            window.location.href = '/compare?ids=' + ids.join(',');
        } else {
            window.location.href = '/compare';
        }
    },

    updateUI() {
        const ids = this.getIds();
        const count = ids.length;

        // 1. Update all compare badges across the page
        document.querySelectorAll('.global-compare-badge').forEach(badge => {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        });

        // 2. Update floating bar
        const bar = document.getElementById('globalCompareBar');
        const countEl = document.getElementById('globalCompareCount');
        if (bar && countEl) {
            countEl.textContent = count;
            if (window.location.pathname === '/compare') {
                bar.style.display = 'none';
            } else {
                bar.style.display = count > 0 ? 'block' : 'none';
            }
        }

        // 3. Update checkboxes on tour cards
        document.querySelectorAll('.tour-compare-checkbox').forEach(cb => {
            const tourId = Number(cb.dataset.tourId);
            cb.checked = ids.includes(tourId);
        });
    }
};

document.addEventListener('DOMContentLoaded', () => {
    // Initialize Comparison UI
    window.NT_Compare.updateUI();

    // Sync across tabs
    window.addEventListener('storage', (e) => {
        if (e.key === window.NT_Compare.STORAGE_KEY) {
            window.NT_Compare.updateUI();
        }
    });

    // Auto-redirect on /compare if URL has no ids but localStorage does
    if (window.location.pathname === '/compare') {
        const urlParams = new URLSearchParams(window.location.search);
        const idsParam = urlParams.get('ids');
        const storedIds = window.NT_Compare.getIds();
        if (!idsParam && storedIds.length > 0) {
            window.location.replace('/compare?ids=' + storedIds.join(','));
        }
    }

    // Enable Bootstrap tooltips if any
    if (window.bootstrap && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    }

    // Listen to HTMX events for notifications / error handling
    if (document.body) {
        document.body.addEventListener('htmx:responseError', (event) => {
            console.error('HTMX Response Error:', event.detail);
            alert('Произошла ошибка при выполнении запроса. Пожалуйста, попробуйте позже.');
        });
    }
});
