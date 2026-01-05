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

import static org.junit.jupiter.api.Assertions.*;
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

    /**
     * Verifica che la registrazione di un cliente fallisca se l'email è già associata a un Operatore.
     * Assicura che venga lanciata un'eccezione e che non avvenga alcun salvataggio nel database.
     */
    @Test
    void emailInUso_operatore() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "pass", "pass");

        when(operatoreRepository.existsByEmail("mario@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrazioneService.registerClient(dto);
        });

        assertEquals("Email già in uso!", exception.getMessage());

        // Verifica che il salvataggio NON sia avvenuto
        verify(clienteRepository, never()).save(any());
    }

    /**
     * Verifica che la registrazione di un cliente fallisca se l'email è già registrata da un altro Cliente.
     * Controlla l'integrità del messaggio di errore e l'assenza di chiamate al metodo save.
     */
    @Test
    void emailInUso_cliente() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "pass", "pass");

        when(clienteRepository.existsByEmail("mario@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrazioneService.registerClient(dto);
        });

        assertEquals("Email già in uso!", exception.getMessage());

        // Verifica che il salvataggio NON sia avvenuto
        verify(clienteRepository, never()).save(any());
    }

    /**
     * Verifica che la registrazione di un cliente fallisca se l'email è già utilizzata da un Gestore.
     * Garantisce che il sistema impedisca la creazione di account duplicati tra diversi ruoli.
     */
    @Test
    void emailInUso_gestore() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "pass", "pass");

        when(gestoreRepository.existsByEmail("mario@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrazioneService.registerClient(dto);
        });

        assertEquals("Email già in uso!", exception.getMessage());

        // Verifica che il salvataggio NON sia avvenuto
        verify(clienteRepository, never()).save(any());
    }

    /**
     * Verifica che il servizio di registrazione validi la corrispondenza tra password e conferma password.
     * Il test si aspetta un'eccezione se le stringhe fornite nel DTO differiscono.
     */
    @Test
    void testRegistrazione_PasswordsDiverse() {
        RegistraUtenteDTO dto = new RegistraUtenteDTO("Mario", "Rossi", "mario@test.com", "pass1", "pass2");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrazioneService.registerClient(dto);
        });

        assertEquals("Le password non coincidono!", exception.getMessage());

        // Verifica che il salvataggio NON sia avvenuto
        verify(clienteRepository, never()).save(any());
    }

    /**
     * Testa il flusso completo di registrazione di un nuovo Cliente con dati validi.
     * Verifica che la password venga criptata, che l'oggetto venga salvato correttamente
     * e che il servizio restituisca le credenziali (UserDetails) del nuovo utente.
     */
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

    /**
     * Testa il corretto salvataggio di un profilo Operatore.
     * Verifica che, dopo i controlli di disponibilità email, venga invocata la persistenza
     * sul repository specifico degli operatori.
     */
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
