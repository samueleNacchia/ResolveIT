package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
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

@ExtendWith(MockitoExtension.class) // JUnit 5 + Mockito
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
    private RegistrazioneService registrazioneService; // La classe da testare

    @Test
    void testRegistrazioneCliente_Successo() {
        // GIVEN: Prepariamo i dati
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "password", "password",  true);

        Cliente clienteSalvato = new Cliente("Mario", "Rossi", "mario@test.com", "passwordCriptata");

        // WHEN: Esaminiamo il comportamento (Mockito dice che l'email non esiste)
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(gestoreRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("passwordCriptata");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvato);

        // THEN: Chiamiamo il metodo
        UserDetails risultato = registrazioneService.registerUser(dto);

        assertNotNull(risultato);
        verify(clienteRepository, times(1)).save(any(Cliente.class)); // Verifica che il save sia stato chiamato
    }
}
