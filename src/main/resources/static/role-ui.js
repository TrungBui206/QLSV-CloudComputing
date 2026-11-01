
(function () {
    'use strict';

    const moduleName = window.location.pathname
        .split('/')
        .pop()
        .replace('.html', '');

    const POLICIES = {
        classes: {
            create: ['ADMIN', 'LECTURER'],
            update: ['ADMIN', 'LECTURER'],
            delete: ['ADMIN']
        },
        students: {
            create: ['ADMIN', 'LECTURER'],
            update: ['ADMIN', 'LECTURER'],
            delete: ['ADMIN']
        },
        departments: {
            create: ['ADMIN'],
            update: ['ADMIN'],
            delete: ['ADMIN']
        },
        'academic-years': {
            create: ['ADMIN'],
            update: ['ADMIN'],
            delete: ['ADMIN']
        },
        documents: {
            create: ['ADMIN', 'LECTURER'],
            delete: ['ADMIN'],
            download: ['ADMIN', 'LECTURER', 'STUDENT']
        }
    };

    if (!POLICIES[moduleName]) return;

    let currentRole = null;
    let rolePromise = null;

    const columnCount = {
        classes: 6,
        students: 6,
        departments: 5,
        'academic-years': 7,
        documents: 6
    };

    const style = document.createElement('style');

    style.textContent = `
        [hidden] {
            display: none !important;
        }

        body:not([data-role-ui-ready="yes"]) #addBtn,
        body:not([data-role-ui-ready="yes"]) .act {
            display: none !important;
        }
    `;

    document.head.appendChild(style);

    function can(action, module = moduleName) {
        return !!currentRole &&
            Array.isArray(POLICIES[module]?.[action]) &&
            POLICIES[module][action].includes(currentRole);
    }

    function hasRowActions(module = moduleName) {
        return can('update', module) ||
            can('delete', module) ||
            can('download', module);
    }

    function identifyAction(button) {
        const label = [
            button.title,
            button.getAttribute('aria-label'),
            button.textContent
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();

        if (label.includes('xóa')) return 'delete';

        if (
            label.includes('sửa') ||
            label.includes('chỉnh')
        ) {
            return 'update';
        }

        if (
            label.includes('tải') ||
            label.includes('download')
        ) {
            return 'download';
        }

        return null;
    }

    function apply(module = moduleName) {
        if (module !== moduleName || !currentRole) return;

        const addButton = document.getElementById('addBtn');

        if (addButton) {
            addButton.hidden = !can('create');
        }

        const showActions = hasRowActions();

        document.querySelectorAll('th.act').forEach(th => {
            th.hidden = !showActions;
        });

        document.querySelectorAll('td.act').forEach(td => {
            td.hidden = !showActions;

            td.querySelectorAll('button').forEach(button => {
                const action = identifyAction(button);

                button.hidden = !action || !can(action);
            });
        });

        document.querySelectorAll('#rows tr').forEach(tr => {
            if (
                tr.children.length === 1 &&
                tr.firstElementChild?.hasAttribute('colspan')
            ) {
                tr.firstElementChild.colSpan =
                    columnCount[moduleName] -
                    (showActions ? 0 : 1);
            }
        });
    }

    function clearAuth() {
        [
            'qlsv_access_token',
            'qlsv_token_type',
            'qlsv_expires_at'
        ].forEach(key => {
            sessionStorage.removeItem(key);
        });
    }

    function goToLogin() {
        window.location.replace('login.html');
    }

    async function loadRole() {
        const token =
            sessionStorage.getItem('qlsv_access_token');

        const expiry =
            sessionStorage.getItem('qlsv_expires_at');

        if (
            !token ||
            (expiry && Number(expiry) <= Date.now())
        ) {
            clearAuth();
            goToLogin();

            throw new Error(
                'Chưa đăng nhập hoặc JWT hết hạn'
            );
        }

        const response = await fetch('/api/auth/me', {
            cache: 'no-store',
            headers: {
                Authorization: 'Bearer ' + token
            }
        });

        if (response.status === 401) {
            clearAuth();
            goToLogin();

            throw new Error(
                'Phiên đăng nhập không còn hợp lệ'
            );
        }

        if (!response.ok) {
            throw new Error(
                'Không xác minh được vai trò: HTTP ' +
                response.status
            );
        }

        const result = await response.json();
        const role = result?.data?.role;

        if (
            !['ADMIN', 'LECTURER', 'STUDENT'].includes(role)
        ) {
            throw new Error('Vai trò không hợp lệ');
        }

        currentRole = role;
        document.body.dataset.roleUiReady = 'yes';

        apply();

        return role;
    }

    function ready() {
        if (!rolePromise) {
            rolePromise = loadRole();
        }

        return rolePromise;
    }

    function guard(module, action) {
        if (can(action, module)) {
            return true;
        }

        alert('Bạn không có quyền thực hiện thao tác này.');

        return false;
    }

    document.addEventListener('click', event => {
        const button = event.target.closest('button');

        if (!button) return;

        let action = null;

        if (button.id === 'addBtn') {
            action = 'create';
        } else if (button.closest('td.act')) {
            action = identifyAction(button);
        }

        if (action && !can(action)) {
            event.preventDefault();
            event.stopImmediatePropagation();

            alert('Bạn không có quyền thực hiện thao tác này.');
        }
    }, true);

    document.addEventListener('submit', event => {
        if (event.target?.id !== 'frm') return;

        if (!can('create') && !can('update')) {
            event.preventDefault();
            event.stopImmediatePropagation();

            alert('Bạn không có quyền lưu dữ liệu ở trang này.');
        }
    }, true);

    const observer = new MutationObserver(() => apply());
    const rows = document.getElementById('rows');

    if (rows) {
        observer.observe(rows, {
            childList: true,
            subtree: true
        });
    }

    window.RoleUI = Object.freeze({
        ready,
        can: (module, action) => can(action, module),
        hasRowActions,
        guard,
        apply
    });

    ready().catch(error => {
        console.error('RoleUI:', error.message);
    });
})();
