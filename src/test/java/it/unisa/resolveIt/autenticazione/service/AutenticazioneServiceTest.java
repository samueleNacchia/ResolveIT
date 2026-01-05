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
