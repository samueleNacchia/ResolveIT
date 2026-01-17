function checkFileSize(input) {
    const errorJs = document.getElementById('file-error-js');
    const fileNameDisplay = document.getElementById('file-name-display');
    const removeBtn = document.getElementById('remove-file-btn');

    if (errorJs) errorJs.innerText = "";

    if (input.files && input.files[0]) {
        const file = input.files[0];
        const fileSizeMB = file.size / (1024 * 1024);
        const allowedExtensions = /(\.txt|\.jpg|\.jpeg|\.zip)$/i;

        if (fileSizeMB > 16 || !allowedExtensions.exec(file.name)) {
            if (errorJs) errorJs.innerText = "File non valido o troppo grande.";
            input.value = "";
            if (fileNameDisplay) fileNameDisplay.innerText = "Nessun file selezionato";
            if (removeBtn) removeBtn.classList.add('hidden');
        } else {
            if (fileNameDisplay) fileNameDisplay.innerText = file.name;
            if (removeBtn) {
                console.log("Mostro la X ora...");
                removeBtn.classList.remove('hidden');
            }
        }
    } else {
        if (removeBtn) removeBtn.classList.add('hidden');
    }
}

function removeAttachment() {
    console.log("Tentativo di rimozione allegato...");

    const input = document.getElementById('file-hidden');
    const fileNameDisplay = document.getElementById('file-name-display');
    const removeBtn = document.getElementById('remove-file-btn');
    const errorJs = document.getElementById('file-error-js');

    if (input) {
        input.value = "";
    }

    if (fileNameDisplay) {
        fileNameDisplay.innerText = "Nessun file selezionato";
    }

    if (removeBtn) {
        removeBtn.classList.add('hidden');
    }

    if (errorJs) {
        errorJs.innerText = "";
    }

    console.log("Allegato rimosso con successo.");
}