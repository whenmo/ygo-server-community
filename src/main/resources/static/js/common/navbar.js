document.addEventListener('DOMContentLoaded', function () {
    const adminBtn = document.getElementById('adminMenuBtn');
    const adminPanel = document.getElementById('adminDropdownPanel');

    // 若非管理員（畫面上無此元素）則直接結束
    if (!adminBtn || !adminPanel) return;

    // 點擊「管理員後台」切換面板顯示狀態
    adminBtn.addEventListener('click', function (event) {
        event.stopPropagation();
        const isHidden = adminPanel.style.display === 'none' || adminPanel.style.display === '';
        adminPanel.style.display = isHidden ? 'block' : 'none';
    });

    // 點擊面板內部時不關閉（避免操作時誤關）
    adminPanel.addEventListener('click', function (event) {
        event.stopPropagation();
    });

    // 點擊頁面其他空白處時自動關閉面板
    document.addEventListener('click', function () {
        adminPanel.style.display = 'none';
    });
});