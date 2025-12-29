package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.springframework.security.core.userdetails.UserDetails;

public interface RegistrazioneService {
    /**
     * Registra un nuovo cliente nel sistema.
     * Valida i dati forniti, effettua l'hashing della password e salva l'entitá nel database.
     *
     * @param userDto oggetto che contiene i dati del cliente da registrare
     * @return un oggetto {@link UserDetails} di Spring Security che rappresenta l'utente autenticabile,
     *         con username (email), password hashata e ruoli (Authorities). Utilizzato per l'auto-login.
     */
    public UserDetails registerClient(RegistraUtenteDTO userDto);

    /**
     * Registra un nuovo operatore nel sistema.
     * Valida i dati forniti e salva l'entitá Operatore nel database.
     *
     * @param userDto oggetto che contiene i dati dell'operatore da registrare
     */
    public void registerOperator(RegistraUtenteDTO userDto);
}
