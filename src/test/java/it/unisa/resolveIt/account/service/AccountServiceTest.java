package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Classe di test unitari per {@link AccountService}.
 * <p>
 * Utilizza Mockito per isolare la logica di business dai repository e verificare
 * il comportamento di metodi come rimozione account (soft delete), recupero profilo
 * e modifica dati utente.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private OperatoreRepository operatoreRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AccountImpl accountService;

    // --- REMOVE CLIENTE ---

    /**
     * Verifica che un account Cliente venga disabilitato correttamente (soft delete).
     * <p>Precondizione: Il cliente esiste ed è attivo.</p>
     * <p>Postcondizione: Lo stato 'enabled' è false e il repository esegue il save.</p>
     */
    @Test
    void removeAccountCliente_Successo() {
        long id = 1L;
        Cliente c = new Cliente();
        // Default enabled is true, ma per sicurezza nel test lo esplicitiamo o ci affidiamo al costruttore
        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));

        accountService.removeAccountCliente(id);

        assertFalse(c.isEnabled());
        verify(clienteRepository).save(c);
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di disabilitare un Cliente già disabilitato.
     */
    @Test
    void removeAccountCliente_GiaDisabilitato() {
        long id = 1L;
        Cliente c = new Cliente();
        c.disable();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));

        assertThrows(RuntimeException.class, () -> accountService.removeAccountCliente(id));
    }

    /**
     * Verifica che venga lanciata un'eccezione se il Cliente non esiste nel database.
     */
    @Test
    void removeAccountCliente_NonTrovato() {
        long id = 99L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.removeAccountCliente(id));
    }

    // --- REMOVE OPERATORE ---

    /**
     * Verifica che un account Operatore venga disabilitato correttamente (soft delete).
     */
    @Test
    void removeAccountOperatore_Successo() {
        long id = 1L;
        Operatore op = new Operatore();
        when(operatoreRepository.findById(id)).thenReturn(Optional.of(op));

        accountService.removeAccountOperatore(id);

        assertFalse(op.isEnabled());
        verify(operatoreRepository).save(op);
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di disabilitare un Operatore già disabilitato.
     */
    @Test
    void removeAccountOperatore_GiaDisabilitato() {
        long id = 1L;
        Operatore op = new Operatore();
        op.disable();
        when(operatoreRepository.findById(id)).thenReturn(Optional.of(op));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.removeAccountOperatore(id));
        assertEquals("Account operatore già disabilitato", ex.getMessage());
    }

    /**
     * Verifica che venga lanciata un'eccezione se l'Operatore non esiste nel database.
     */
    @Test
    void removeAccountOperatore_NonTrovato() {
        long id = 99L;
        when(operatoreRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.removeAccountOperatore(id));
        assertEquals("Account operatore non trovato", ex.getMessage());
    }

    // --- GET USER ---

    /**
     * Verifica il recupero di un profilo Operatore tramite email.
     * Il flag 'client' nel DTO deve essere false.
     */
    @Test
    void getUserByEmail_TrovaOperatore() {
        String email = "op@test.com";
        Operatore op = new Operatore("Op", "Test", email, "pass");
        when(operatoreRepository.findByEmail(email)).thenReturn(op);

        MyProfileDTO res = accountService.getUserByEmail(email);

        assertFalse(res.isClient());
        assertEquals("Op", res.getNome());
    }

    /**
     * Verifica il recupero di un profilo Cliente tramite email.
     * Il flag 'client' nel DTO deve essere true.
     */
    @Test
    void getUserByEmail_TrovaCliente() {
        String email = "cli@test.com";
        Cliente cli = new Cliente("Cli", "Test", email, "pass");
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(cli);

        MyProfileDTO res = accountService.getUserByEmail(email);

        assertTrue(res.isClient());
        assertEquals("Cli", res.getNome());
    }

    /**
     * Verifica che venga lanciata un'eccezione se l'email non corrisponde a nessun utente.
     */
    @Test
    void getUserByEmail_NonTrovato() {
        String email = "null@test.com";
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> accountService.getUserByEmail(email));
    }

    // --- MODIFY USER ---

    /**
     * Verifica la modifica dei soli dati anagrafici (no password) per un Operatore.
     * <p>Password nel DTO è stringa vuota.</p>
     */
    @Test
    void modifyUser_Operatore_SoloDati() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("op@test.com");
        dto.setNome("NewName");
        dto.setCognome("NewSurname");
        dto.setPassword("");

        Operatore op = new Operatore();
        op.setEmail("op@test.com");
        op.setNome("OldName");

        when(operatoreRepository.findByEmail("op@test.com")).thenReturn(op);

        boolean res = accountService.modifyUser(dto);

        assertFalse(res); // Password non cambiata
        assertEquals("NewName", op.getNome());
        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * Verifica la modifica dei soli dati anagrafici (no password) per un Cliente.
     * <p>Password nel DTO è null.</p>
     */
    @Test
    void modifyUser_Cliente_SoloDati() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("cli@test.com");
        dto.setNome("NewName");
        dto.setCognome("NewSurname");
        dto.setPassword(null);

        Cliente cli = new Cliente();
        cli.setEmail("cli@test.com");

        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail("cli@test.com")).thenReturn(cli);

        boolean res = accountService.modifyUser(dto);

        assertFalse(res);
        assertEquals("NewName", cli.getNome());
    }

    /**
     * Verifica che venga lanciata un'eccezione se le password (nuova e conferma) non coincidono per un Operatore.
     */
    @Test
    void modifyUser_Operatore_PasswordMismatch() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("op@test.com");
        dto.setPassword("A");
        dto.setConfermaPassword("B");
        Operatore op = new Operatore();
        op.setEmail("op@test.com");

        when(operatoreRepository.findByEmail("op@test.com")).thenReturn(op);

        assertThrows(RuntimeException.class, () -> accountService.modifyUser(dto));
    }

    /**
     * Verifica che venga lanciata un'eccezione se l'email nel DTO non trova nessun riscontro nel DB.
     */
    @Test
    void modifyUser_NessunUtente() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("ghost@test.com");
        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail(anyString())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.modifyUser(dto));
        assertEquals("L'utente non è autorizzato alla modifica o non esiste.", ex.getMessage());
    }

    /**
     * Verifica la modifica completa (dati + password) per un Operatore.
     * Atteso: ritorna true (password cambiata).
     */
    @Test
    void modifyUser_Successo_ConPassword() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("op@test.com");
        dto.setPassword("NewPass");
        dto.setConfermaPassword("NewPass");
        Operatore op = new Operatore();
        op.setEmail("op@test.com");

        when(operatoreRepository.findByEmail("op@test.com")).thenReturn(op);
        when(passwordEncoder.encode("NewPass")).thenReturn("Hashed");

        boolean res = accountService.modifyUser(dto);
        assertTrue(res);
        assertEquals("Hashed", op.getPassword());
    }

    /**
     * Verifica che venga lanciata un'eccezione se le password non coincidono per un Cliente.
     */
    @Test
    void modifyUser_Cliente_PasswordMismatch() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("cli@test.com");
        dto.setPassword("PassA");
        dto.setConfermaPassword("PassB");

        Cliente cli = new Cliente();
        cli.setEmail("cli@test.com");

        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail("cli@test.com")).thenReturn(cli);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                accountService.modifyUser(dto)
        );
        assertEquals("Le password non coincidono!", ex.getMessage());
    }

    /**
     * Verifica la modifica completa (dati + password) per un Cliente.
     */
    @Test
    void modifyUser_Cliente_Successo_ConPassword() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("cli@test.com");
        dto.setPassword("NewPass");
        dto.setConfermaPassword("NewPass");

        Cliente cli = new Cliente();
        cli.setEmail("cli@test.com");

        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail("cli@test.com")).thenReturn(cli);
        when(passwordEncoder.encode("NewPass")).thenReturn("HashedPass");

        boolean res = accountService.modifyUser(dto);

        assertTrue(res);
        assertEquals("HashedPass", cli.getPassword());
    }
}