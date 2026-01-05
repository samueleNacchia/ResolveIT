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

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private OperatoreRepository operatoreRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AccountImpl accountService;

    // --- REMOVE CLIENTE ---
    @Test
    void removeAccountCliente_Successo() {
        long id = 1L;
        Cliente c = new Cliente();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));
        accountService.removeAccountCliente(id);
        assertFalse(c.isEnabled());
        verify(clienteRepository).save(c);
    }

    @Test
    void removeAccountCliente_GiaDisabilitato() {
        long id = 1L;
        Cliente c = new Cliente();
        c.disable();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));
        assertThrows(RuntimeException.class, () -> accountService.removeAccountCliente(id));
    }

    @Test
    void removeAccountCliente_NonTrovato() {
        long id = 99L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> accountService.removeAccountCliente(id));
    }

    // --- REMOVE OPERATORE ---
    @Test
    void removeAccountOperatore_Successo() {
        long id = 1L;
        Operatore op = new Operatore();
        when(operatoreRepository.findById(id)).thenReturn(Optional.of(op));
        accountService.removeAccountOperatore(id);
        assertFalse(op.isEnabled());
        verify(operatoreRepository).save(op);
    }

    @Test
    void removeAccountOperatore_GiaDisabilitato() {
        long id = 1L;
        Operatore op = new Operatore();
        op.disable();
        when(operatoreRepository.findById(id)).thenReturn(Optional.of(op));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.removeAccountOperatore(id));
        assertEquals("Account operatore già disabilitato", ex.getMessage());
    }

    @Test
    void removeAccountOperatore_NonTrovato() {
        long id = 99L;
        when(operatoreRepository.findById(id)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.removeAccountOperatore(id));
        assertEquals("Account operatore non trovato", ex.getMessage());
    }

    // --- GET USER ---
    @Test
    void getUserByEmail_TrovaOperatore() {
        String email = "op@test.com";
        Operatore op = new Operatore("Op", "Test", email, "pass");
        when(operatoreRepository.findByEmail(email)).thenReturn(op);
        MyProfileDTO res = accountService.getUserByEmail(email);
        assertFalse(res.isClient());
    }

    @Test
    void getUserByEmail_TrovaCliente() {
        String email = "cli@test.com";
        Cliente cli = new Cliente("Cli", "Test", email, "pass");
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(cli);
        MyProfileDTO res = accountService.getUserByEmail(email);
        assertTrue(res.isClient());
    }

    @Test
    void getUserByEmail_NonTrovato() {
        String email = "null@test.com";
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> accountService.getUserByEmail(email));
    }

    // --- MODIFY USER (Aumentata coverage qui) ---

    @Test
    void modifyUser_Operatore_SoloDati() {
        // Copre il ramo dove password è null/vuota per Operatore
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("op@test.com");
        dto.setNome("NewName");
        dto.setCognome("NewSurname");
        dto.setPassword(""); //

        Operatore op = new Operatore();
        op.setEmail("op@test.com");

        when(operatoreRepository.findByEmail("op@test.com")).thenReturn(op);

        boolean res = accountService.modifyUser(dto);

        assertFalse(res); // Password non cambiata
        assertEquals("NewName", op.getNome());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void modifyUser_Cliente_SoloDati() {
        // Copre il ramo dove password è null/vuota per Cliente
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("cli@test.com");
        dto.setNome("NewName");
        dto.setCognome("NewSurname");
        dto.setPassword(null); //

        Cliente cli = new Cliente();
        cli.setEmail("cli@test.com");

        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail("cli@test.com")).thenReturn(cli);

        boolean res = accountService.modifyUser(dto);

        assertFalse(res);
        assertEquals("NewName", cli.getNome());
    }

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

    @Test
    void modifyUser_NessunUtente() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("ghost@test.com");
        when(operatoreRepository.findByEmail(anyString())).thenReturn(null);
        when(clienteRepository.findByEmail(anyString())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.modifyUser(dto));
        assertEquals("L'utente non è autorizzato alla modifica o non esiste.", ex.getMessage());
    }

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
    @Test
    void modifyUser_Cliente_PasswordMismatch() {
        // Copre il ramo "if (cliente != null)" -> errore password
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

    @Test
    void modifyUser_Cliente_Successo_ConPassword() {
        // Copre il ramo "if (cliente != null)" -> cambio password successo
        // Questo mancava (avevamo solo quello senza password)
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