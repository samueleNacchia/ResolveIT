// ticket-actions.js

function switchTab(tabId) {
    // 1. Nascondi tutti i contenuti dei tab e mostra quello selezionato
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    const targetContent = document.getElementById('tab-' + tabId);
    if (targetContent) targetContent.classList.add('active');

    // 2. Gestisci lo stato visuale di TUTTI i bottoni nella sidebar
    document.querySelectorAll('.tab-btn').forEach(btn => {
        // Rimuoviamo lo stato attivo e mettiamo quello inattivo a tutti
        btn.classList.remove('tab-btn-active');
        btn.classList.add('tab-btn-inactive');
    });

    // 3. Attiva solo il bottone cliccato
    const activeBtn = document.getElementById('btn-' + tabId);
    if (activeBtn) {
        activeBtn.classList.remove('tab-btn-inactive');
        activeBtn.classList.add('tab-btn-active');
    }

    // 4. Aggiorna il titolo della pagina (opzionale)
    const title = document.getElementById('tab-title');
    if (title) {
        title.innerText = (tabId === 'working') ? 'Ticket in Carico' : 'Ticket in Attesa';
    }
}

function toggleRow(id) {
    const detailRow = document.getElementById('detail-' + id);
    const btn = document.getElementById('btn-' + id);

    if (detailRow && btn) {
        if (detailRow.classList.contains('hidden')) {
            detailRow.classList.remove('hidden');
            btn.innerText = '-';
            btn.classList.replace('toggle-plus', 'toggle-minus');
        } else {
            detailRow.classList.add('hidden');
            btn.innerText = '+';
            btn.classList.replace('toggle-minus', 'toggle-plus');
        }
    }
}

function confirmAction(id, azione) {
    const config = {
        rilascia: { title: 'Rilasciare il ticket?', text: 'Tornerà disponibile per altri operatori.', icon: 'warning', color: '#ef4444', url: '/ticket/rilascia/' },
        risolvi: { title: 'Risolvere il ticket?', text: 'Il ticket verrà chiuso definitivamente.', icon: 'success', color: '#10b981', url: '/ticket/risolvi/' },
        assegna: { title: 'Prendere in carico?', text: 'Il ticket verrà assegnato a te.', icon: 'info', color: '#4f46e5', url: '/ticket/assegna/' },
        elimina: { title: 'Eliminare il ticket?', text: 'L\'azione è irreversibile.', icon: 'warning', color: '#ef4444', url: '/ticket/elimina/' }
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
            form.action = c.url + id;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            if (csrfToken) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = '_csrf';
                input.value = csrfToken;
                form.appendChild(input);
            }

            document.body.appendChild(form);
            form.submit();
        }
    });
}