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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private OperatoreRepository operatoreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountImpl accountService;

    // --- TEST REMOVE CLIENTE ---

    @Test
    void removeAccountCliente_Successo() {
        long id = 1L;
        Cliente c = new Cliente();
        // Nota: nel codice fornito, 'attivo' è true di default

        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));

        accountService.removeAccountCliente(id);

        assertFalse(c.isEnabled()); // Verifica cambio stato
        verify(clienteRepository, times(1)).save(c);
    }

    @Test
    void removeAccountCliente_GiaDisabilitato() {
        long id = 1L;
        Cliente c = new Cliente();
        c.disable(); // Imposta attivo = false

        when(clienteRepository.findById(id)).thenReturn(Optional.of(c));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.removeAccountCliente(id);
        });

        assertEquals("Account cliente già disabilitato", ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void removeAccountCliente_NonTrovato() {
        long id = 99L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.removeAccountCliente(id);
        });

        assertEquals("Account cliente non trovato", ex.getMessage());
    }

    // --- TEST GET USER BY EMAIL ---

    @Test
    void getUserByEmail_TrovaOperatore() {
        String email = "op@test.com";
        Operatore op = new Operatore("Op", "Test", email, "pass");

        when(operatoreRepository.findByEmail(email)).thenReturn(op);

        MyProfileDTO result = accountService.getUserByEmail(email);

        assertNotNull(result);
        assertFalse(result.isClient()); // Deve essere false per operatore
        assertEquals("Op", result.getNome());
    }

    @Test
    void getUserByEmail_TrovaCliente() {
        String email = "cli@test.com";
        Cliente cli = new Cliente("Cli", "Test", email, "pass");

        when(operatoreRepository.findByEmail(email)).thenReturn(null); // Non è operatore
        when(clienteRepository.findByEmail(email)).thenReturn(cli);    // È cliente

        MyProfileDTO result = accountService.getUserByEmail(email);

        assertNotNull(result);
        assertTrue(result.isClient()); // Deve essere true per cliente
    }

    // --- TEST MODIFY USER ---

    @Test
    void modifyUser_PasswordMismatch() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        dto.setConfermaPassword("passwordDIVERSA");

        Operatore op = new Operatore();
        op.setEmail("test@test.com");

        when(operatoreRepository.findByEmail("test@test.com")).thenReturn(op);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.modifyUser(dto);
        });

        assertEquals("Le password non coincidono!", ex.getMessage());
    }

    @Test
    void modifyUser_Successo_CambioDatiEPassword() {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail("test@test.com");
        dto.setNome("NuovoNome");
        dto.setCognome("NuovoCognome");
        dto.setPassword("NewPass123");
        dto.setConfermaPassword("NewPass123");

        Operatore op = new Operatore();
        op.setEmail("test@test.com");
        op.setPassword("OldPass");

        when(operatoreRepository.findByEmail("test@test.com")).thenReturn(op);
        when(passwordEncoder.encode("NewPass123")).thenReturn("HashedNewPass");

        boolean passwordChanged = accountService.modifyUser(dto);

        assertTrue(passwordChanged);
        assertEquals("NuovoNome", op.getNome());
        assertEquals("HashedNewPass", op.getPassword());
    }
}