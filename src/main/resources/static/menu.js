
(function () {
    const LOGO = 'images/logo.png';

    const PAGES = [
        { href: 'departments.html', label: 'Quản lý khoa' },
        { href: 'students.html', label: 'Quản lý sinh viên' },
        { href: 'academic-years.html', label: 'Quản lý niên khóa' }
    ];

    const SVG_OPEN =
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
        'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">';

    const ICONS = {
        edit: SVG_OPEN +
            '<path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/></svg>',

        delete: SVG_OPEN +
            '<polyline points="3 6 5 6 21 6"/>' +
            '<path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>' +
            '<path d="M10 11v6"/><path d="M14 11v6"/>' +
            '<path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>',

        folder: SVG_OPEN +
            '<path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7z"/></svg>',

        folderOpen: SVG_OPEN +
            '<path d="M4 19V6a1 1 0 0 1 1-1h4l2 2h7a1 1 0 0 1 1 1v2"/>' +
            '<path d="M4 19l2.5-8a1 1 0 0 1 .95-.7H21a1 1 0 0 1 .95 1.3L19.6 18.3a1 1 0 0 1-.95.7H4z"/></svg>'
    };

    window.iconButton = function (kind, label) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'icon-btn';
        button.title = label;
        button.setAttribute('aria-label', label);
        button.innerHTML = ICONS[kind];
        return button;
    };

    window.updateSortMarks = function (state) {
        document.querySelectorAll('th.sortable').forEach(th => {
            if (!th.dataset.label) {
                th.dataset.label = th.textContent.trim();
            }

            const active = state.sortBy === th.dataset.sort;
            const mark = active
                ? (state.direction === 'asc' ? ' ▲' : ' ▼')
                : ' ⇅';

            th.textContent = th.dataset.label + mark;
        });
    };

    const style = document.createElement('style');

    style.textContent = `
        .brand {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
        }

        .brand img {
            width: 44px;
            height: 44px;
            object-fit: contain;
            flex-shrink: 0;
        }

        aside nav a {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .nav-ico {
            display: inline-flex;
            color: #6b7280;
        }

        .nav-ico svg {
            width: 20px;
            height: 20px;
        }

        aside nav a.active .nav-ico {
            color: #14397d;
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

        th.act, td.act {
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

    const aside = document.querySelector('aside');
    const title = aside.querySelector('h1');
    const subtitle = aside.querySelector('small');

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

    const nav = document.getElementById('nav');
    const current = location.pathname.split('/').pop();

    PAGES.forEach(page => {
        const isCurrent = page.href === current;

        const link = document.createElement('a');
        link.href = page.href;

        if (isCurrent) {
            link.className = 'active';
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
})();
