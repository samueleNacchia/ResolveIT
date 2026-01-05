function validateTicketForm(event) {
    const form = event.target;
    const titolo = form.querySelector('input[name="titolo"]').value.trim();
    const descrizione = form.querySelector('textarea[name="descrizione"]').value.trim();

    const titoloRegex = /^[a-zA-Z0-9À-ÿ '‘".,!?-]{5,100}$/;

    if (!titoloRegex.test(titolo)) {
        event.preventDefault();
        Swal.fire({
            icon: 'error',
            title: 'Titolo non valido',
            text: 'Il titolo deve avere tra i 5 e i 100 caratteri e non contenere simboli speciali non autorizzati.',
            confirmButtonColor: '#4f46e5'
        });
        return false;
    }

    if (descrizione.length === 0 || descrizione.length > 2000) {
        event.preventDefault();
        Swal.fire({
            icon: 'error',
            title: 'Descrizione non valida',
            text: 'La descrizione è obbligatoria e non può superare i 2000 caratteri.',
            confirmButtonColor: '#4f46e5'
        });
        return false;
    }

    return true;
}

document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('file-hidden');
    const fileNameDisplay = document.getElementById('file-name-display');

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                const fileName = this.files[0].name;
                fileNameDisplay.textContent = fileName;
                fileNameDisplay.classList.remove('text-gray-400');
                fileNameDisplay.classList.add('text-indigo-600', 'font-bold');
            } else {
                fileNameDisplay.textContent = 'Nessun file selezionato';
                fileNameDisplay.classList.add('text-gray-400');
            }
        });
    }
});