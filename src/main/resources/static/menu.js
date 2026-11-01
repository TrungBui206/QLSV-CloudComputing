
(function () {
    "use strict";

    // ==========================================
    // 1. CẤU HÌNH
    // ==========================================

    const LOGO = 'images/logo.png';

    const PAGES = [
        {
            href: 'departments.html',
            label: 'Quản lý khoa'
        },
        {
            href: 'classes.html',
            label: 'Quản lý lớp học'
        },
        {
            href: 'students.html',
            label: 'Quản lý sinh viên'
        },
        {
            href: 'academic-years.html',
            label: 'Quản lý niên khóa'
        },
        {
            href: 'documents.html',
            label: 'Quản lý tài liệu'
        }
    ];

    const AUTH_KEYS = {
        token: 'qlsv_access_token',
        type: 'qlsv_token_type',
        expiresAt: 'qlsv_expires_at'
    };

    // ==========================================
    // 2. BIỂU TƯỢNG
    // ==========================================

    const SVG_OPEN =
        '<svg viewBox="0 0 24 24" fill="none" ' +
        'stroke="currentColor" ' +
        'stroke-width="2" ' +
        'stroke-linecap="round" ' +
        'stroke-linejoin="round">';

    const ICONS = {
        edit:
            SVG_OPEN +
            '<path d="M17 3a2.828 2.828 0 1 1 4 4' +
            'L7.5 20.5 2 22l1.5-5.5L17 3z"/>' +
            '</svg>',

        delete:
            SVG_OPEN +
            '<polyline points="3 6 5 6 21 6"/>' +
            '<path d="M19 6l-1 14a2 2 0 0 1-2 2H8' +
            'a2 2 0 0 1-2-2L5 6"/>' +
            '<path d="M10 11v6"/>' +
            '<path d="M14 11v6"/>' +
            '<path d="M9 6V4a1 1 0 0 1 1-1h4' +
            'a1 1 0 0 1 1 1v2"/>' +
            '</svg>',

        folder:
            SVG_OPEN +
            '<path d="M3 7a2 2 0 0 1 2-2h4l2 2h8' +
            'a2 2 0 0 1 2 2v8' +
            'a2 2 0 0 1-2 2H5' +
            'a2 2 0 0 1-2-2V7z"/>' +
            '</svg>',

        folderOpen:
            SVG_OPEN +
            '<path d="M4 19V6a1 1 0 0 1 1-1h4' +
            'l2 2h7a1 1 0 0 1 1 1v2"/>' +
            '<path d="M4 19l2.5-8' +
            'a1 1 0 0 1 .95-.7H21' +
            'a1 1 0 0 1 .95 1.3' +
            'L19.6 18.3' +
            'a1 1 0 0 1-.95.7H4z"/>' +
            '</svg>',

        logout:
            SVG_OPEN +
            '<path d="M9 21H5a2 2 0 0 1-2-2V5' +
            'a2 2 0 0 1 2-2h4"/>' +
            '<polyline points="16 17 21 12 16 7"/>' +
            '<line x1="21" y1="12" x2="9" y2="12"/>' +
            '</svg>'
    };

    // ==========================================
    // 3. NÚT BIỂU TƯỢNG CHO CRUD
    // ==========================================

    window.iconButton = function (kind, label) {
        const button = document.createElement('button');

        button.type = 'button';
        button.className = 'icon-btn';
        button.title = label;
        button.setAttribute('aria-label', label);

        if (ICONS[kind]) {
            button.innerHTML = ICONS[kind];
        } else {
            button.textContent = label || '';
        }

        return button;
    };

    // ==========================================
    // 4. BIỂU TƯỢNG SẮP XẾP
    // ==========================================

    window.updateSortMarks = function (state) {
        document.querySelectorAll(
            'th.sortable'
        ).forEach(th => {

            if (!th.dataset.label) {
                th.dataset.label = th.textContent
                    .replace(/[▲▼⇅]/g, '')
                    .trim();
            }

            const active =
                state.sortBy === th.dataset.sort;

            const mark = active
                ? (
                    state.direction === 'asc'
                        ? ' ▲'
                        : ' ▼'
                )
                : '';

            th.textContent =
                th.dataset.label + mark;
        });
    };

    // ==========================================
    // 5. CSS CHUNG CHO MENU
    // ==========================================

    const style = document.createElement('style');

    style.textContent = `
        .brand {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 16px;
        }

        .brand img {
            width: 44px;
            height: 44px;
            object-fit: contain;
            flex-shrink: 0;
        }

        .brand > div {
            min-width: 0;
        }

        .brand h1 {
            margin: 0;
            line-height: 1.4;
        }

        .brand small {
            display: block;
            margin-top: 4px;
        }

        aside nav a {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        aside nav a.active {
            background: #e8eefb;
            color: #14397d;
            font-weight: 600;
        }

        .nav-ico {
            display: inline-flex;
            flex-shrink: 0;
            color: #6b7280;
        }

        .nav-ico svg {
            width: 20px;
            height: 20px;
        }

        aside nav a.active .nav-ico {
            color: #14397d;
        }

        .menu-logout {
            display: flex;
            align-items: center;
            gap: 10px;
            width: 100%;
            margin-top: 18px;
            padding: 10px 12px;
            border: 1px solid #fecaca;
            border-radius: 8px;
            background: #fff;
            color: #b91c1c;
            font-family: inherit;
            font-size: 14px;
            text-align: left;
            cursor: pointer;
        }

        .menu-logout:hover {
            background: #fef2f2;
        }

        .menu-logout svg {
            width: 20px;
            height: 20px;
        }

        .menu-divider {
            height: 1px;
            margin-top: 18px;
            background: #e5e7eb;
        }

        main {
            display: flex;
            flex-direction: column;
        }

        main > .card:last-of-type {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        main > .card:last-of-type .foot {
            margin-top: auto;
            padding-top: 12px;
        }

        th.act,
        td.act {
            width: 110px;
        }

        .act .icon-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 34px;
            height: 34px;
            padding: 0;
            margin-right: 6px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            background: #fff;
            color: #6b7280;
            cursor: pointer;
        }

        .act .icon-btn:hover {
            background: #f3f4f6;
            color: #374151;
        }

        .act .icon-btn svg {
            width: 18px;
            height: 18px;
        }
    `;

    document.head.append(style);

    // ==========================================
    // 6. TẠO LOGO VÀ TÊN HỆ THỐNG
    // ==========================================

    const aside = document.querySelector('aside');

    if (!aside) {
        return;
    }

    const title = aside.querySelector('h1');
    const subtitle = aside.querySelector('small');

    if (title && subtitle &&
        !aside.querySelector('.brand')) {

        const text = document.createElement('div');

        text.append(title, subtitle);

        const img = document.createElement('img');

        img.src = LOGO;
        img.alt = 'Logo trường';

        img.onerror = () => img.remove();

        const brand = document.createElement('div');

        brand.className = 'brand';
        brand.append(img, text);

        aside.prepend(brand);
    }

    // ==========================================
    // 7. TẠO THANH ĐIỀU HƯỚNG
    // ==========================================

    const nav = document.getElementById('nav');

    const current = window.location.pathname
        .split('/')
        .pop();

    if (nav) {
        nav.replaceChildren();

        PAGES.forEach(page => {
            const isCurrent =
                page.href === current;

            const link = document.createElement('a');

            link.href = page.href;

            if (isCurrent) {
                link.className = 'active';

                link.setAttribute(
                    'aria-current',
                    'page'
                );
            }

            const icon = document.createElement('span');

            icon.className = 'nav-ico';

            icon.innerHTML = isCurrent
                ? ICONS.folderOpen
                : ICONS.folder;

            const label = document.createElement('span');

            label.textContent = page.label;

            link.append(icon, label);

            nav.append(link);
        });
    }

    // ==========================================
    // 8. ĐĂNG XUẤT
    // ==========================================

    function clearAuth() {
        sessionStorage.removeItem(
            AUTH_KEYS.token
        );

        sessionStorage.removeItem(
            AUTH_KEYS.type
        );

        sessionStorage.removeItem(
            AUTH_KEYS.expiresAt
        );
    }

    function logout() {
        const confirmed = confirm(
            'Bạn có chắc chắn muốn đăng xuất khỏi hệ thống không?'
        );

        if (!confirmed) {
            return;
        }

        clearAuth();

        window.location.replace('login.html');
    }

    // Trang classes.html đã có nút Đăng xuất
    // nên không tạo thêm nút thứ hai trên trang đó.

    const existingLogout =
        document.querySelector('#logoutBtn, .menu-logout');


    if (!existingLogout && nav) {
        const divider = document.createElement('div');

        divider.className = 'menu-divider';

        const logoutButton =
            document.createElement('button');

        logoutButton.type = 'button';
        logoutButton.id = 'logoutBtn';

        logoutButton.className = 'menu-logout';

        logoutButton.title = 'Đăng xuất';

        logoutButton.innerHTML =
            ICONS.logout +
            '<span>Đăng xuất</span>';

        logoutButton.addEventListener(
            'click',
            logout
        );

        nav.after(divider, logoutButton);
    }
})();
