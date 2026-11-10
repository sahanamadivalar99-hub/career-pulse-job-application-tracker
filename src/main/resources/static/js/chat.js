// AI Career Assistant Interactive Chat
document.addEventListener('DOMContentLoaded', function () {
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const chatMessages = document.getElementById('chatMessages');
    const quickButtons = document.querySelectorAll('.quick-prompt-btn');

    // Auto-scroll to bottom of chat
    function scrollToBottom() {
        if (chatMessages) {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
    }
    scrollToBottom();

    // Quick prompt buttons
    quickButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const prompt = this.dataset.prompt;
            messageInput.value = prompt;
            submitMessage(prompt);
        });
    });

    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const text = messageInput.value.trim();
            if (!text) return;
            submitMessage(text);
        });
    }

    function submitMessage(text) {
        messageInput.value = '';

        // Append user bubble
        appendBubble('user', text, null);

        // Show typing indicator
        const typingIndicator = document.createElement('div');
        typingIndicator.id = 'typingIndicator';
        typingIndicator.className = 'chat-bubble assistant text-muted';
        typingIndicator.innerHTML = '<i class="bi bi-three-dots"></i> Thinking...';
        chatMessages.appendChild(typingIndicator);
        scrollToBottom();

        // Send to backend
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch('/career-assistant/api/ask', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ message: text })
        })
        .then(res => res.json())
        .then(data => {
            const indicator = document.getElementById('typingIndicator');
            if (indicator) indicator.remove();

            appendBubble('assistant', data.message, data.provider);
        })
        .catch(err => {
            const indicator = document.getElementById('typingIndicator');
            if (indicator) indicator.remove();

            appendBubble('assistant', 'Sorry, an error occurred while processing your question. Please try again.', 'ERROR');
        });
    }

    function appendBubble(role, message, provider) {
        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${role}`;

        let html = '';
        if (role === 'assistant') {
            const badgeClass = provider === 'LLM' ? 'bg-success' : 'bg-primary';
            const badgeText = provider === 'LLM' ? 'Gemini AI' : 'Java Knowledge Engine';
            html += `<div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="badge ${badgeClass} text-white" style="font-size:0.68rem;">${badgeText}</span>
                     </div>`;
        }

        // Basic markdown formatting converter
        let formatted = formatMarkdown(message);
        html += `<div>${formatted}</div>`;

        bubble.innerHTML = html;
        chatMessages.appendChild(bubble);
        scrollToBottom();
    }

    function formatMarkdown(text) {
        if (!text) return '';
        // Bold
        let out = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        // Italics
        out = out.replace(/\*(.*?)\*/g, '<em>$1</em>');
        // Headers ###
        out = out.replace(/^### (.*$)/gim, '<h6 class="fw-bold mt-2 mb-1 text-purple">$1</h6>');
        out = out.replace(/^## (.*$)/gim, '<h5 class="fw-bold mt-2 mb-1">$1</h5>');
        // Quotes
        out = out.replace(/^> (.*$)/gim, '<div class="p-2 my-2 bg-light rounded text-muted small border-start border-3 border-purple">$1</div>');
        // Bullet points
        out = out.replace(/^\- (.*$)/gim, '<li class="ms-3">$1</li>');
        // Line breaks
        out = out.replace(/\n/g, '<br/>');
        return out;
    }
});
