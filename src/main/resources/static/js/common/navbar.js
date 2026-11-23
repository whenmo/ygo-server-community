document.addEventListener('DOMContentLoaded', function () {
    const adminBtn = document.getElementById('adminMenuBtn');
    const adminPanel = document.getElementById('adminDropdownPanel');
    const personalBtn = document.getElementById('personalMenuBtn');
    const personalPanel = document.getElementById('personalDropdownPanel');

    if (adminBtn && adminPanel) {
        adminBtn.addEventListener('click', function (event) {
            event.stopPropagation();
            const isHidden = adminPanel.style.display === 'none' || adminPanel.style.display === '';
            adminPanel.style.display = isHidden ? 'block' : 'none';
        });

        adminPanel.addEventListener('click', function (event) {
            event.stopPropagation();
        });

        document.addEventListener('click', function () {
            adminPanel.style.display = 'none';
        });
    };

    if (personalBtn && personalPanel) {
        personalBtn.addEventListener('click', function (event) {
            event.stopPropagation();
            const isHidden = personalPanel.style.display === 'none' || personalPanel.style.display === '';
            personalPanel.style.display = isHidden ? 'block' : 'none';
        });

        personalPanel.addEventListener('click', function (event) {
            event.stopPropagation();
        });

        document.addEventListener('click', function () {
            personalPanel.style.display = 'none';
        });
    };
});