// Funzioni globali (devono stare fuori per essere viste dai th:onclick)
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    const targetContent = document.getElementById('tab-' + tabId);
    if (targetContent) targetContent.classList.add('active');

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('tab-btn-active');
        btn.classList.add('tab-btn-inactive');
    });

    const activeBtn = document.getElementById('btn-' + tabId);
    if (activeBtn) {
        activeBtn.classList.remove('tab-btn-inactive');
        activeBtn.classList.add('tab-btn-active');
    }

    const title = document.getElementById('tab-title');
    if (title) {
        title.innerText = (tabId === 'working') ? 'Ticket in Carico' : 'Ticket in Attesa';
    }
}

function toggleRow(id) {
    const detailRow = document.getElementById('detail-' + id);
    const btn = document.getElementById('btn-' + id);
    if (detailRow && btn) {
        const isHidden = detailRow.classList.contains('hidden');
        detailRow.classList.toggle('hidden');
        btn.innerText = isHidden ? '-' : '+';
        btn.classList.toggle('toggle-plus', !isHidden);
        btn.classList.toggle('toggle-minus', isHidden);
    }
}

function toggleAttachment(id) {
    const row = document.getElementById('attachment-row-' + id);
    if (row) {
        row.classList.toggle('hidden');
    }
}

function confirmAction(id, azione) {
    const config = {
        rilascia: { title: 'Rilasciare il ticket?', text: 'Tornerà disponibile per altri operatori.', icon: 'warning', color: '#ef4444', url: '/ticket/rilascia/' },
        risolvi: { title: 'Risolvere il ticket?', text: 'Il ticket verrà chiuso definitivamente.', icon: 'success', color: '#10b981', url: '/ticket/risolvi/' },
        prendi: { title: 'Prendere in carico?', text: 'Il ticket verrà assegnato a te.', icon: 'info', color: '#4f46e5', url: '/ticket/prendi/' },
        elimina: { title: 'Annullare il ticket?', text: 'L\'azione cambierà lo stato in ANNULLATO.', icon: 'warning', color: '#ef4444', url: '/ticket/elimina/' }
    };

    const c = config[azione];

    Swal.fire({
        title: c.title,
        text: c.text,
        icon: c.icon,
        showCancelButton: true,
        confirmButtonColor: c.color,
        cancelButtonColor: '#9ca3af',
        confirmButtonText: 'Sì, procedi',
        cancelButtonText: 'Annulla'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = window.location.origin + c.url + id;

            const token = document.querySelector('meta[name="_csrf"]')?.content;
            if (token) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = '_csrf';
                input.value = token;
                form.appendChild(input);
            }
            document.body.appendChild(form);
            form.submit();
        }
    });
}

// Logiche da eseguire al caricamento della pagina
document.addEventListener('DOMContentLoaded', function() {

    // 1. Gestione nome file allegato
    const fileInput = document.getElementById('file-hidden');
    const fileNameDisplay = document.getElementById('file-name-display');
    if (fileInput && fileNameDisplay) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                fileNameDisplay.textContent = this.files[0].name;
                fileNameDisplay.classList.remove('text-gray-400', 'italic');
                fileNameDisplay.classList.add('text-indigo-600', 'font-bold');
            } else {
                fileNameDisplay.textContent = "Nessun file selezionato";
                fileNameDisplay.classList.add('text-gray-400', 'italic');
            }
        });
    }

    // 2. Sparizione automatica banner (5 secondi)
    const alerts = document.querySelectorAll('.flash-message');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
});