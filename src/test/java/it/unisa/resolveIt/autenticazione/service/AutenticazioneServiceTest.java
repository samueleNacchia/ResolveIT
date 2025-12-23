package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.repository.ClienteRepository;
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
    @InjectMocks
    private AutenticazioneService autenticazioneService;

    @Test
    void loadUserByUsername_TrovaCliente_Successo() {
        String email = "cliente@test.com";
        Cliente c = new Cliente();
        c.setEmail(email);

        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(c);

        UserDetails user = autenticazioneService.loadUserByUsername(email);

        assertNotNull(user);
        assertEquals(email, user.getUsername());
    }

    @Test
    void loadUserByUsername_UtenteInesistente_LanciaException() {
        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            autenticazioneService.loadUserByUsername("non-esiste@test.com");
        });
    }
}
