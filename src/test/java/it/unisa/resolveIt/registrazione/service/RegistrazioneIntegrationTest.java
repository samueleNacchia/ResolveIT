package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrazioneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    @MockitoBean
    private RegistrazioneService registrazioneService;

    // --- TC_1.2_1: Formato Email Errato (FE1) ---
    @Test
    public void emailErrata() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@outlog")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(view().name("registrazione"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "email"));
    }

    // --- TC_1.2_2: Password troppo corta (LP1) ---
    @Test
    public void passwordCorta() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "1234567") // LP1: < 8 caratteri
                        .param("confermaPassword", "1234567")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(view().name("registrazione"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "password"));
    }

    // --- TC_1.2_3: Conferma Password non corrispondente (MCP1) ---
    @Test
    public void matchPasswordErrato() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Diversa123") // MCP1: No match
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(view().name("registrazione"))
                // Qui verifichi l'errore globale o sul campo confermaPassword
                .andExpect(model().attributeHasErrors("utenteDTO"));
    }

    // --- TC_1.2_4: Nome non corretto (FNO1) ---
    @Test
    public void nomeErrato() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "M") // FNO1: troppo corto (min 2)
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(view().name("registrazione"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "nome"));
    }

    // --- TC_1.2_5: Cognome non corretto (FCO1) ---
    @Test
    public void cognomeErrato() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "R123") // FCO1: numeri non ammessi
                        .with(csrf()))
                .andExpect(view().name("registrazione"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "cognome"));
    }

    // --- TC_1.2_6: Registrazione Corretta ---
    @Test
    public void registrazioneCorretta() throws Exception {
        registrazioneCliente_Successo();
        registrazioneOperatore_Successo();
    }

    private void registrazioneCliente_Successo() throws Exception {
        Cliente clienteFinto = new Cliente("Mario", "Rossi", "mario@email.it", "hashedPass");

        when(registrazioneService.registerClient(any(RegistraUtenteDTO.class))).thenReturn(clienteFinto);
        when(clienteRepository.save((Cliente) any(Cliente.class))).thenReturn(clienteFinto);
        when(clienteRepository.existsByEmail("mario@email.it")).thenReturn(false);
        when(operatoreRepository.existsByEmail("mario@email.it")).thenReturn(false);


        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(authenticated().withUsername("mario@email.it")); // Verifica Auto-Login
    }

    private void registrazioneOperatore_Successo() throws Exception {
        Operatore operatoreFinto = new Operatore("Mario", "Rossi", "mario@email.it", "hashedPass");

        when(registrazioneService.registerOperator(any(RegistraUtenteDTO.class))).thenReturn(operatoreFinto);
        when(operatoreRepository.save((Operatore) any(Operatore.class))).thenReturn(operatoreFinto);
        when(clienteRepository.existsByEmail("mario@email.it")).thenReturn(false);
        when(operatoreRepository.existsByEmail("mario@email.it")).thenReturn(false);


        mockMvc.perform(post("/register")
                        .with(user("gestore@test.com").authorities(new SimpleGrantedAuthority("GESTORE")))
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername("gestore@test.com")); // IL GESTORE RESTA LOGGATO;
    }
}