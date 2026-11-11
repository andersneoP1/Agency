/**
 * Native Travel Global Client Script
 */

// Currency & Internationalization helpers
window.formatMoney = function(amountInRubles) {
    if (amountInRubles === null || amountInRubles === undefined || isNaN(amountInRubles)) {
        return '0';
    }
    const curr = window.NT_CURRENCY || { code: 'RUB', symbol: '₽', rate: 1.0, prefix: false };
    const converted = Math.round(Number(amountInRubles) * curr.rate);
    const formatted = converted.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    if (curr.prefix) {
        return curr.symbol + formatted;
    } else {
        return formatted + ' ' + curr.symbol;
    }
};

window.changeLanguage = function(lang) {
    document.cookie = 'NT_LANG=' + lang + ';path=/;max-age=' + (30 * 24 * 60 * 60);
    const url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    window.location.href = url.toString();
};

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
            if (window.location.pathname === '/compare' || /^\/admin(?:\/|$)/.test(window.location.pathname) || count === 0) {
                bar.style.opacity = '0';
                bar.style.transform = 'translate(-50%, 40px)';
                setTimeout(() => {
                    if (window.NT_Compare.getIds().length === 0 || window.location.pathname === '/compare') {
                        bar.style.display = 'none';
                    }
                }, 300);
            } else {
                bar.style.display = 'block';
                requestAnimationFrame(() => {
                    bar.style.opacity = '1';
                    bar.style.transform = 'translate(-50%, 0)';
                });
            }
        }

        // 3. Update checkboxes on tour cards
        document.querySelectorAll('.tour-compare-checkbox').forEach(cb => {
            const tourId = Number(cb.dataset.tourId);
            cb.checked = ids.includes(tourId);
        });
    }
};

// Global Festive Confetti Burst
window.launchConfetti = function(options) {
    if (typeof confetti === 'function') {
        confetti(Object.assign({
            particleCount: 100,
            spread: 70,
            origin: { y: 0.6 }
        }, options || {}));
    }
};

window.launchDoubleConfetti = function() {
    if (typeof confetti !== 'function') return;
    const end = Date.now() + 2 * 1000;
    const colors = ['#ffc107', '#0d6efd', '#198754', '#dc3545', '#0dcaf0'];

    (function frame() {
        confetti({
            particleCount: 4,
            angle: 60,
            spread: 55,
            origin: { x: 0 },
            colors: colors
        });
        confetti({
            particleCount: 4,
            angle: 120,
            spread: 55,
            origin: { x: 1 },
            colors: colors
        });

        if (Date.now() < end) {
            requestAnimationFrame(frame);
        }
    }());
};

// Animated Number Counter via IntersectionObserver
function initCounters() {
    const counters = document.querySelectorAll('.stat-counter');
    if (!counters.length) return;

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const targetEl = entry.target;
                const targetVal = parseFloat(targetEl.dataset.target) || 0;
                const isFloat = targetEl.dataset.float === 'true';
                const prefix = targetEl.dataset.prefix || '';
                const suffix = targetEl.dataset.suffix || '';
                const duration = 1800;
                const start = performance.now();

                function step(now) {
                    const progress = Math.min((now - start) / duration, 1);
                    const ease = 1 - Math.pow(1 - progress, 4);
                    const current = targetVal * ease;
                    targetEl.textContent = prefix + (isFloat ? current.toFixed(1) : Math.floor(current).toLocaleString('ru-RU')) + suffix;
                    if (progress < 1) {
                        requestAnimationFrame(step);
                    } else {
                        targetEl.textContent = prefix + (isFloat ? targetVal.toFixed(1) : targetVal.toLocaleString('ru-RU')) + suffix;
                    }
                }
                requestAnimationFrame(step);
                obs.unobserve(targetEl);
            }
        });
    }, { threshold: 0.2 });

    counters.forEach(c => observer.observe(c));
}

document.addEventListener('DOMContentLoaded', () => {
    // Initialize AOS animations
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 650,
            once: true,
            offset: 40,
            easing: 'ease-out-cubic'
        });
    }

    // Initialize Animated Counters
    initCounters();

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

    // Initialize Sliding Indicators for Navbars & Tabs
    function initSlidingIndicator(options) {
        const { container, itemSelector, indicatorSelector, activeClass = 'active', followHover = true } = options;
        if (!container) return;

        let indicator = container.querySelector(indicatorSelector);
        if (!indicator) {
            indicator = document.createElement('span');
            indicator.className = indicatorSelector.replace(/^\./, '');
            container.appendChild(indicator);
        }

        function getBox(el) {
            if (!el || el.offsetParent === null) return null;
            const cRect = container.getBoundingClientRect();
            const elRect = el.getBoundingClientRect();
            return {
                left: elRect.left - cRect.left + container.scrollLeft,
                width: elRect.width
            };
        }

        function move(target, immediate = false) {
            const box = getBox(target);
            if (!box || box.width === 0) {
                indicator.style.opacity = '0';
                return;
            }

            if (immediate) {
                indicator.style.transition = 'none';
            }

            indicator.style.opacity = '1';
            indicator.style.transform = `translateX(${box.left}px)`;
            indicator.style.width = `${box.width}px`;

            if (immediate) {
                indicator.offsetHeight; // reflow
                indicator.style.transition = '';
            }
        }

        function getActive() {
            return container.querySelector(`${itemSelector}.${activeClass}`);
        }

        function reset(immediate = false) {
            const active = getActive();
            if (active) {
                move(active, immediate);
            } else {
                indicator.style.opacity = '0';
            }
        }

        const items = container.querySelectorAll(itemSelector);
        items.forEach(item => {
            if (followHover) {
                item.addEventListener('mouseenter', () => move(item));
            }
            item.addEventListener('click', () => {
                setTimeout(() => {
                    const active = getActive() || item;
                    move(active);
                }, 30);
            });
        });

        if (followHover) {
            container.addEventListener('mouseleave', () => reset());
        }

        // Handle Bootstrap tab shown event
        container.addEventListener('shown.bs.tab', (e) => {
            if (e.target) {
                move(e.target);
            }
        });

        window.addEventListener('resize', () => reset(true));

        // Initial positioning
        requestAnimationFrame(() => reset(true));
        setTimeout(() => reset(false), 100);
        setTimeout(() => reset(false), 350);
    }

    // Attach to navbar and tab containers
    document.querySelectorAll('.sliding-nav-container').forEach(nav => {
        initSlidingIndicator({
            container: nav,
            itemSelector: '.nav-link',
            indicatorSelector: '.nav-sliding-indicator',
            activeClass: 'active',
            followHover: true
        });
    });

    document.querySelectorAll('.sliding-tab-container').forEach(tabContainer => {
        initSlidingIndicator({
            container: tabContainer,
            itemSelector: '.nav-link',
            indicatorSelector: '.tab-sliding-indicator',
            activeClass: 'active',
            followHover: true
        });
    });

    // Listen to HTMX events for notifications / error handling
    if (document.body) {
        document.body.addEventListener('htmx:responseError', (event) => {
            console.error('HTMX Response Error:', event.detail);
            alert('Произошла ошибка при выполнении запроса. Пожалуйста, попробуйте позже.');
        });
    }
});

// Country-aware telephone fields, including dynamically rendered booking forms.
(() => {
 const countries = [
 ['Россия','7','### ###-##-##'],['Kazakhstan','7','### ###-##-##'],
 ['Belarus','375','## ###-##-##'],['Armenia','374','## ###-###'],
 ['Azerbaijan','994','## ###-##-##'],['Kyrgyzstan','996','### ###-###'],
 ['Moldova','373','## ###-###'],['Tajikistan','992','## ###-##-##'],
 ['Turkmenistan','993','## ##-##-##'],['Uzbekistan','998','## ###-##-##'],
 ['Ukraine','380','## ###-##-##'],['Georgia','995','### ##-##-##']];
 function init() { document.querySelectorAll('input[type="tel"]').forEach(input => {
  if(input.dataset.countryPhone) return; input.dataset.countryPhone='true';
  const select=document.createElement('select'); select.className='form-select mb-2';select.setAttribute('aria-label','?????? ??????????? ??????');
  countries.forEach(([name,code],i)=>select.add(new Option(name+' +'+code,i)));
  input.before(select);
  const full=document.createElement('input');full.type='hidden';
  const model=input.getAttribute('x-model'); if(model) {full.setAttribute('x-model',model);input.removeAttribute('x-model');}
  if(input.name){full.name=input.name;input.removeAttribute('name');}input.after(full);
  const initial=input.value; let detected=countries.findIndex(c=>initial.replace(/\D/g,'').startsWith(c[1]));if(detected>=0)select.value=detected;
  const format=(digits,mask)=>{let i=0;return [...mask].map(c=>c==='#'?(digits[i++]||''):(i<digits.length?c:'')).join('');};
  function update() {const [,code,mask]=countries[select.value];const digits=input.value.replace(/\D/g,'').slice(0,(mask.match(/#/g)||[]).length);input.value=format(digits,mask); input.placeholder=mask.replace(/#/g,'0');input.maxLength=mask.length;input.setCustomValidity(digits && digits.length!==mask.split('#').length-1?'??????? ?????? ????? ????????':'');full.value=digits?'+'+code+' '+input.value:'';full.dispatchEvent(new Event('input',{bubbles:true}));}
  if(initial) input.value=initial.replace(/\D/g,'').slice(countries[select.value][1].length);
  input.addEventListener('input',update);select.addEventListener('change',()=>{input.value='';update();});update();
 }); }
 document.addEventListener('DOMContentLoaded',()=>{init();new MutationObserver(init).observe(document.body,{childList:true,subtree:true});});
})();
