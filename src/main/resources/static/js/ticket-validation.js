function validaESalva() {
    console.log("Funzione validaESalva chiamata"); // Verifica in console F12
    const form = document.getElementById('ticketForm');

    // Usiamo gli ID generati da th:field (solitamente coincidono col nome del campo)
    const titoloField = document.getElementById('titolo');
    const descrizioneField = document.getElementById('descrizione');
    const fileInput = document.getElementById('file-hidden');

    if (!titoloField || !descrizioneField) {
        console.error("Errore: Campi non trovati nel DOM");
        return;
    }

    const titolo = titoloField.value.trim();
    const descrizione = descrizioneField.value.trim();

    // 1. VALIDAZIONE TITOLO
    const titoloRegex = /^[a-zA-Z0-9À-ÿ '‘".,!?-]{5,100}$/;
    if (!titoloRegex.test(titolo)) {
        Swal.fire({
            icon: 'error',
            title: 'Titolo non valido',
            text: 'Il titolo deve avere tra i 5 e i 100 caratteri.',
            confirmButtonColor: '#4f46e5'
        });
        return;
    }

    // 2. VALIDAZIONE DESCRIZIONE
    if (descrizione.length === 0 || descrizione.length > 2000) {
        Swal.fire({
            icon: 'error',
            title: 'Descrizione non valida',
            text: 'La descrizione è obbligatoria (max 2000 caratteri).',
            confirmButtonColor: '#4f46e5'
        });
        return;
    }

    // 3. VALIDAZIONE DIMENSIONE FILE (Anti-crash)
    if (fileInput.files && fileInput.files.length > 0) {
        const fileSize = fileInput.files[0].size / 1024 / 1024;
        if (fileSize > 16) {
            Swal.fire({
                icon: 'error',
                title: 'File troppo grande',
                text: 'L\'allegato supera il limite di 16MB.',
                confirmButtonColor: '#4f46e5'
            });
            fileInput.value = "";
            return;
        }
    }

    if (fileInput.files && fileInput.files.length > 0) {
        const fileName = fileInput.files[0].name;
        const allowedExtensions = /(\.txt|\.jpg|\.jpeg|\.zip)$/i;
        if (!allowedExtensions.exec(fileName)) {
            Swal.fire({
                icon: 'error',
                title: 'Errore Allegato',
                text: 'Il formato del file non è consentito.',
                confirmButtonColor: '#4f46e5'
            });
            return; // Blocca l'invio
        }
    }
    form.submit(); // Questo DEVE far partire la richiesta al controller
}

// Keep the rest of your file name display logic below...