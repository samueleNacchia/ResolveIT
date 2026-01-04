package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Gestore;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticazioneImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private GestoreRepository gestoreRepository;

    /**
     * Carica un utente dal sistema dato il suo indirizzo email.
     *
     * <p>Il metodo cerca l'utente in questo ordine:
     * <ol>
     *     <li>Gestore</li>
     *     <li>Operatore</li>
     *     <li>Cliente</li>
     * </ol>
     * Se l'utente viene trovato in uno di questi repository, viene restituito
     * come {@link UserDetails} per l'autenticazione tramite Spring Security.
     *
     * <p>Se nessun utente corrisponde all'email fornita, viene lanciata un'eccezione
     * {@link UsernameNotFoundException}.
     *
     * @param email l'indirizzo email dell'utente da caricare; deve essere non null
     * @return un oggetto {@link UserDetails} rappresentante l'utente trovato,
     *         utilizzabile per l'autenticazione in Spring Security
     * @throws UsernameNotFoundException se non esiste alcun utente con l'email fornita
     */

    @Override
    @PreAuthorize("isAnonymous()")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Cerca gestore
        Gestore gestore = gestoreRepository.findByEmail(email);
        if (gestore != null) {
            return gestore;
        }

        // Cerca operatore
        Operatore operatore = operatoreRepository.findByEmail(email);
        if (operatore != null) {
            return operatore;
        }

        // Cerca cliente
        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente != null) {
            return cliente;
        }

        throw new UsernameNotFoundException("Utente non trovato con email: " + email);
    }

}
