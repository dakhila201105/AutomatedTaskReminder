// Define switchView globally
window.switchView = function(viewId) {
    const map={
    table:'table-view',
    card: 'card-view',
    calender: 'calendar-view'
    };
    // Validate viewId
    if (!viewId || !['table-view', 'card-view', 'calendar-view'].includes(viewId)) {
        viewId = 'table-view';
    }

    console.log('Switching to view:', viewId);

    // Update buttons
    const buttons = document.querySelectorAll('.btn-view-toggle');
    buttons.forEach(btn => {
        if (btn.getAttribute('data-view') === viewId) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    // Update views
    const views = document.querySelectorAll('.view');
    views.forEach(view => {
        if (view.id === viewId) {
            view.classList.add('active');
        } else {
            view.classList.remove('active');
        }
    });

    // Persist choice
    try {
        localStorage.setItem('taskAppView', viewId);
    } catch (e) {
        console.log('LocalStorage not available');
    }
};

document.addEventListener('DOMContentLoaded', function() {
    // Initialize from localStorage or default
    try {
        const savedView = localStorage.getItem('taskAppView');
        if (savedView) {
            switchView(savedView);
        }
    } catch (e) {
        console.log('LocalStorage not available');
    }
});
