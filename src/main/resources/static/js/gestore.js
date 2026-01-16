
    const regexNameSurname = /^[a-zA-Z\sàèìòùÀÈÌÒ’]{2,30}$/;
    const regexEmail = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,10}$/;
    const regexCategory = /^[a-zA-Z\sàèìòùÀÈÌÒ’]{2,30}$/;

    function showSection(sectionId) {
    const homeView = document.getElementById('home-view');
    const accountsView = document.getElementById('accounts-view');
    const categoriesView = document.getElementById('categories-view');

    homeView.style.display = 'none';
    accountsView.style.display = 'none';
    categoriesView.style.display = 'none';

    if (sectionId === 'accounts') {
    accountsView.style.display = 'block';
    document.getElementById('accounts-list').style.display = 'block';
    document.getElementById('add-operator-form').style.display = 'none';
} else if (sectionId === 'categories') {
    categoriesView.style.display = 'block';
} else {
    homeView.style.display = 'flex';
}
}

    function toggleOperatorForm(show) {
    const list = document.getElementById('accounts-list');
    const form = document.getElementById('add-operator-form');
    if (show) {
    list.style.display = 'none';
    form.style.display = 'flex';
} else {
    list.style.display = 'block';
    form.style.display = 'none';
    resetErrors();
}
}

    function toggleCategoryModal(show) {
    const modal = document.getElementById('category-modal');
    const form = document.getElementById('formCategoria');
    const title = document.getElementById('modalTitle');
    const inputId = document.getElementById('catId');
    const inputNome = document.getElementById('catNome');

    if (show) {
    form.action = '/categoria/addCategoria';
    title.innerText = "Inserisci nome della categoria";
    inputId.value = "0";
    inputNome.value = "";
    modal.style.display = 'flex';
} else {
    modal.style.display = 'none';
    resetErrors();
}
}

    function openEditCategoryModal(id, nomeAttuale) {
    const modal = document.getElementById('category-modal');
    const form = document.getElementById('formCategoria');
    const title = document.getElementById('modalTitle');
    const inputId = document.getElementById('catId');
    const inputNome = document.getElementById('catNome');

    form.action = '/categoria/updateCategoria';
    title.innerText = "Modifica categoria";
    inputId.value = id;
    inputNome.value = nomeAttuale;
    modal.style.display = 'flex';
}

    function resetErrors() {
    document.querySelectorAll('.error-msg').forEach(el => el.style.display = 'none');
}

    function validateOperatorForm() {
    let isValid = true;
    const nome = document.getElementById('opNome').value;
    const cognome = document.getElementById('opCognome').value;
    const email = document.getElementById('opEmail').value;

    if (!regexNameSurname.test(nome)) { document.getElementById('errorOpNome').style.display = 'block'; isValid = false; }
    else document.getElementById('errorOpNome').style.display = 'none';

    if (!regexNameSurname.test(cognome)) { document.getElementById('errorOpCognome').style.display = 'block'; isValid = false; }
    else document.getElementById('errorOpCognome').style.display = 'none';

    if (!regexEmail.test(email)) { document.getElementById('errorOpEmail').style.display = 'block'; isValid = false; }
    else document.getElementById('errorOpEmail').style.display = 'none';

    return isValid;
}

    function validateCategoryForm() {
    const nomeCat = document.getElementById('catNome').value;
    if (!regexCategory.test(nomeCat)) { document.getElementById('errorCatNome').style.display = 'block'; return false; }
    else { document.getElementById('errorCatNome').style.display = 'none'; return true; }
}

    /* --- GESTIONE POPUP AL CARICAMENTO --- */
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        const sectionParam = urlParams.get('section');

        // Parametri URL
        const hasError = urlParams.has('error');
        const successValue = urlParams.get('success');

        // Recuperiamo il messaggio specifico dal server (se presente)
        const serverMsgInput = document.getElementById('serverErrorMessage');
        const serverMessage = serverMsgInput ? serverMsgInput.value : null;

        /* --- 1. Logica Categorie --- */
        if (sectionParam === 'categories') {
            showSection('categories');

            if (hasError) {
                Swal.fire({
                    icon: 'error', title: 'Errore', text: 'Impossibile salvare la categoria.', confirmButtonColor: '#d33'
                });
            } else if (successValue !== null) {
                Swal.fire({
                    icon: 'success', title: 'Successo', text: 'Categorie aggiornate!', timer: 2000, showConfirmButton: false
                });
            }

            /* --- 2. Logica Accounts (AGGIORNATA) --- */
        } else if (sectionParam === 'accounts') {
            showSection('accounts');

            // CASO ERRORE (success=false)
            if (successValue === 'false') {
                // Usiamo il messaggio del server se c'è, altrimenti uno generico
                const msgText = serverMessage && serverMessage.trim() !== ""
                    ? serverMessage
                    : 'Impossibile registrare l\'operatore. Controlla i dati o se l\'email esiste già.';

                Swal.fire({
                    icon: 'error',
                    title: 'Operazione Fallita',
                    text: msgText, // Qui apparirà "Email già presente" o "Password non coincidono"
                    confirmButtonColor: '#d33'
                });
            }
            // CASO SUCCESSO
            else if (successValue === 'operatorCreated') {
                Swal.fire({
                    icon: 'success',
                    title: 'Operatore Creato',
                    text: 'Il nuovo account operatore è attivo.',
                    timer: 2500,
                    showConfirmButton: false
                });
            }
            // Altri successi (delete/disable)
            else if (urlParams.has('success')) {
                Swal.fire({
                    icon: 'success', title: 'Successo', text: 'Operazione completata!', timer: 2000, showConfirmButton: false
                });
            }

        } else {
            showSection('home');
        }
    });
    /* --- FUNZIONE DI CONFERMA DISABILITAZIONE --- */
    function confirmDisable(id, tipoUtente) {
    const config = {
    'operatore': {
    title: 'Disabilitare operatore?',
    text: 'L\'operatore non potrà più accedere al sistema.',
    url: '/account/removeOperatore'
},
    'cliente': {
    title: 'Disabilitare cliente?',
    text: 'Il cliente non potrà più effettuare login.',
    url: '/account/removeCliente'
}
};

    const c = config[tipoUtente];

    Swal.fire({
    title: c.title,
    text: c.text,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: 'Sì, disabilita',
    cancelButtonText: 'Annulla'
}).then((result) => {
    if (result.isConfirmed) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = c.url;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    if (csrfToken) {
    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = '_csrf';
    csrfInput.value = csrfToken;
    form.appendChild(csrfInput);
}

    const idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.name = 'id';
    idInput.value = id;
    form.appendChild(idInput);

    document.body.appendChild(form);
    form.submit();
}
});
}

