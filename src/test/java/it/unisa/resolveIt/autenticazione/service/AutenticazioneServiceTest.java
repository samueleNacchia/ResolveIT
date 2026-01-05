package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Gestore;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticazioneServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock private OperatoreRepository operatoreRepository;

    @Mock private GestoreRepository gestoreRepository;

    @InjectMocks
    private AutenticazioneImpl autenticazioneService;

    /**
     * Verifica che il metodo carichi correttamente un {@link Gestore} cercandolo per email.
     * Assicura che, se l'utente è presente nel repository dei gestori, venga restituito
     * un oggetto {@link UserDetails} valido con l'username corretto.
     */
    @Test
    void loadUserByUsername_TrovaGestore_Successo() {
        String email = "gestore@test.com";
        Gestore ges = new Gestore();
        ges.setEmail(email);

        when(gestoreRepository.findByEmail(email)).thenReturn(ges);

        UserDetails user = autenticazioneService.loadUserByUsername(email);

        assertNotNull(user);
        assertEquals(email, user.getUsername());
    }

    /**
     * Verifica il caricamento di un {@link Operatore} quando la ricerca nei gestori fallisce.
     * Il test simula l'assenza dell'email nel repository dei gestori e la sua presenza
     * in quello degli operatori, validando il risultato restituito.
     */
    @Test
    void loadUserByUsername_TrovaOperatore_Successo() {
        String email = "operatore@test.com";
        Operatore op = new Operatore();
        op.setEmail(email);

        when(gestoreRepository.findByEmail(email)).thenReturn(null);
        when(operatoreRepository.findByEmail(email)).thenReturn(op);

        UserDetails user = autenticazioneService.loadUserByUsername(email);

        assertNotNull(user);
        assertEquals(email, user.getUsername());
    }

    /**
     * Verifica il caricamento di un {@link Cliente} quando l'email non appartiene né
     * a un gestore né a un operatore. Assicura che il servizio completi la catena di
     * ricerca arrivando correttamente al repository dei clienti.
     */
    @Test
    void loadUserByUsername_TrovaCliente_Successo() {
        String email = "cliente@test.com";
        Cliente cl = new Cliente();
        cl.setEmail(email);

        when(gestoreRepository.findByEmail(email)).thenReturn(null);
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(cl);

        UserDetails user = autenticazioneService.loadUserByUsername(email);

        assertNotNull(user);
        assertEquals(email, user.getUsername());
    }

    /**
     * Verifica che venga lanciata un'eccezione {@link UsernameNotFoundException}
     * qualora l'email fornita non sia presente in nessuno dei repository del sistema.
     */
    @Test
    void loadUserByUsername_UtenteInesistente() {
        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail(anyString())).thenReturn(null);
        when(gestoreRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            autenticazioneService.loadUserByUsername("non-esiste@test.com");
        });
    }
}
