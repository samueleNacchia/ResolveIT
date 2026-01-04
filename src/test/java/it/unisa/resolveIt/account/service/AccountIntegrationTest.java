package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // --- TEST ACCOUNT CONTROLLER (Lato Gestore) ---

    @Test
    public void removeAccountCliente_Successo() throws Exception {
        long id = 1L;
        // Simuliamo un cliente attivo
        Cliente cliente = new Cliente("Mario", "Rossi", "mario@test.com", "pass");
        // Nota: attivo è true di default nel costruttore/campo

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        mockMvc.perform(post("/account/removeCliente")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", String.valueOf(id))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success"));
    }

    @Test
    public void removeAccountOperatore_Errore_NonTrovato() throws Exception {
        long id = 99L;
        // Simuliamo che l'operatore non esista -> Il service lancerà RuntimeException
        when(operatoreRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(post("/account/removeOperatore")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", String.valueOf(id))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                // Il controller cattura l'eccezione e reindirizza con parametro error
                .andExpect(redirectedUrl("/gestore?section=accounts&error"));
    }

    // --- TEST MY PROFILE CONTROLLER (Lato Utente) ---

    @Test
    public void showProfileForm_Successo() throws Exception {
        String email = "cliente@test.com";
        Cliente cliente = new Cliente("Luigi", "Verdi", email, "pass");

        // Simuliamo che l'utente loggato venga trovato nel repository
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(cliente);

        mockMvc.perform(get("/my-profile")
                        .with(user(email).roles("CLIENTE"))) // Utente loggato
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeExists("utenteDTO"));
    }

    @Test
    public void modifyUserAccount_ErroreValidazione_Nome() throws Exception {
        // Testiamo il caso TC_4.2_1 (Formato nome non corretto)
        MyProfileDTO dtoErrato = new MyProfileDTO();
        dtoErrato.setNome("Luigi123"); // Numeri non permessi dalla Regex
        dtoErrato.setCognome("Verdi");
        dtoErrato.setEmail("cliente@test.com");

        mockMvc.perform(post("/my-profile")
                        .with(user("cliente@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dtoErrato) // Passiamo il DTO
                        .with(csrf()))
                .andExpect(status().isOk()) // Non fa redirect, ricarica la pagina
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "nome"));
    }

    @Test
    public void modifyUserAccount_Successo_SenzaPassword() throws Exception {
        // Testiamo il caso TC_4.2_5 (Modifica corretta)
        String email = "cliente@test.com";
        Cliente cliente = new Cliente("Luigi", "Verdi", email, "hashedPass");

        MyProfileDTO dtoValido = new MyProfileDTO();
        dtoValido.setNome("LuigiNuovo");
        dtoValido.setCognome("VerdiNuovo");
        dtoValido.setEmail(email);
        dtoValido.setPassword(""); // Nessun cambio password
        dtoValido.setConfermaPassword("");

        // Setup mock per trovare l'utente
        when(operatoreRepository.findByEmail(email)).thenReturn(null);
        when(clienteRepository.findByEmail(email)).thenReturn(cliente);

        mockMvc.perform(post("/my-profile")
                        .with(user(email).roles("CLIENTE"))
                        .flashAttr("utenteDTO", dtoValido)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile")) // Redirect standard
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    public void modifyUserAccount_Successo_ConPassword() throws Exception {
        // Caso cambio password -> Logout e redirect a login
        String email = "op@test.com";
        Operatore op = new Operatore("Mario", "Rossi", email, "oldPass");

        MyProfileDTO dtoPass = new MyProfileDTO();
        dtoPass.setNome("Mario");
        dtoPass.setCognome("Rossi");
        dtoPass.setEmail(email);
        dtoPass.setPassword("NewPass123"); // Password valida
        dtoPass.setConfermaPassword("NewPass123");

        when(operatoreRepository.findByEmail(email)).thenReturn(op);
        when(passwordEncoder.encode("NewPass123")).thenReturn("hashedNewPass");

        mockMvc.perform(post("/my-profile")
                        .with(user(email).roles("OPERATORE"))
                        .flashAttr("utenteDTO", dtoPass)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login")) // Redirect specifico per cambio psw
                .andExpect(flash().attributeExists("passwordSuccess"));
    }
}