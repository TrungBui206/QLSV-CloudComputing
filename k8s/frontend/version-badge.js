// Nhãn nhỏ ở góc màn hình: phiên bản frontend và node đang phục vụ. Bấm để mở trang /cluster.html.
(function () {
  fetch('/frontend-info', { cache: 'no-store' })
    .then(function (r) { return r.json(); })
    .then(function (i) {
      var a = document.createElement('a');
      a.href = '/cluster.html';
      a.textContent = 'frontend ' + i.version + ' · ' + i.node;
      a.title = 'Pod: ' + i.pod;
      a.style.cssText = 'position:fixed;right:12px;bottom:12px;z-index:9999;padding:6px 12px;' +
        'border-radius:999px;font:600 12px/1.2 system-ui,Arial,sans-serif;color:#fff;text-decoration:none;' +
        'box-shadow:0 2px 6px rgba(0,0,0,.25);background:' + (i.version === 'v1' ? '#1D4ED8' : '#15803D');
      document.body.appendChild(a);
    })
    .catch(function () {});
})();
