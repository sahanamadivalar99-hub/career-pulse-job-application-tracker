// HTML5 Drag and Drop for Kanban Board
document.addEventListener('DOMContentLoaded', function () {
    const cards = document.querySelectorAll('.kanban-card');
    const columns = document.querySelectorAll('.kanban-cards-container');

    cards.forEach(card => {
        card.addEventListener('dragstart', handleDragStart);
        card.addEventListener('dragend', handleDragEnd);
    });

    columns.forEach(col => {
        col.addEventListener('dragover', handleDragOver);
        col.addEventListener('dragenter', handleDragEnter);
        col.addEventListener('dragleave', handleDragLeave);
        col.addEventListener('drop', handleDrop);
    });

    let draggedCard = null;

    function handleDragStart(e) {
        draggedCard = this;
        this.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', this.dataset.id);
    }

    function handleDragEnd() {
        this.classList.remove('dragging');
        columns.forEach(col => col.classList.remove('drag-hover'));
    }

    function handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    }

    function handleDragEnter(e) {
        e.preventDefault();
        this.classList.add('drag-hover');
    }

    function handleDragLeave() {
        this.classList.remove('drag-hover');
    }

    function handleDrop(e) {
        e.preventDefault();
        this.classList.remove('drag-hover');

        if (!draggedCard) return;

        const targetColumn = this.closest('.kanban-column');
        const newStatus = targetColumn.dataset.status;
        const applicationId = draggedCard.dataset.id;

        // Move DOM element immediately for snappy responsiveness
        this.appendChild(draggedCard);

        // Update column count badges
        updateColumnCounts();

        // Send AJAX update to backend
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const formData = new URLSearchParams();
        formData.append('id', applicationId);
        formData.append('status', newStatus);

        fetch('/applications/api/status', {
            method: 'POST',
            headers: headers,
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (!data.success) {
                console.error('Failed to update application status:', data.error);
                alert('Could not update status: ' + data.error);
            }
        })
        .catch(err => {
            console.error('Network error updating application status:', err);
        });
    }

    function updateColumnCounts() {
        document.querySelectorAll('.kanban-column').forEach(col => {
            const countBadge = col.querySelector('.column-count');
            const cardCount = col.querySelectorAll('.kanban-card').length;
            if (countBadge) {
                countBadge.textContent = cardCount;
            }
        });
    }
});
