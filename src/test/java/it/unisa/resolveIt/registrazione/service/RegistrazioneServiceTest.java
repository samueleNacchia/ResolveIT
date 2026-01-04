package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrazioneServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private OperatoreRepository operatoreRepository;

    @Mock
    private GestoreRepository gestoreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrazioneImpl registrazioneService; // La classe da testare

    @Test
    void testRegistrazioneCliente_Successo() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "password", "password");

        Cliente clienteSalvato = new Cliente("Mario", "Rossi", "mario@test.com", "passwordCriptata");

        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(gestoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("passwordCriptata");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvato);

        UserDetails risultato = registrazioneService.registerClient(dto);

        assertNotNull(risultato);

        // Verifica che il save sia stato chiamato
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testRegistrazioneOperatore_Successo() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "password", "password");

        Operatore operatoreSalvato = new Operatore("Mario", "Rossi", "mario@test.com", "passwordCriptata");

        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(gestoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("passwordCriptata");
        when(operatoreRepository.save(any(Operatore.class))).thenReturn(operatoreSalvato);

        registrazioneService.registerOperator(dto);

        // Verifica che il save sia stato chiamato
        verify(operatoreRepository, times(1)).save(any(Operatore.class));
    }
}
