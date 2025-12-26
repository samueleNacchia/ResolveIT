document.addEventListener('DOMContentLoaded', function() {
    // Seleziona tutte le icone che si trovano dentro un wrapper per password
    const toggleIcons = document.querySelectorAll('.password-wrapper .eye-icon');

    toggleIcons.forEach(icon => {
        icon.addEventListener('click', function() {
            // Trova il campo input corrispondente all'icona cliccata
            const inputField = this.parentElement.querySelector('input');

            if (inputField) {
                // Verifica il tipo attuale e lo inverte
                const type = inputField.getAttribute('type') === 'password' ? 'text' : 'password';
                inputField.setAttribute('type', type);

                // Cambia l'icona da occhio a occhio sbarrato
                this.classList.toggle('fa-eye');
                this.classList.toggle('fa-eye-slash');
            }
        });
    });
});