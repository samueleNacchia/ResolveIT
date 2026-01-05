package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AutenticazioneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    @MockitoBean
    private GestoreRepository gestoreRepository;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    /**
     * Verifica che un tentativo di login con un'email che non rispetta il formato (FE1)
     * venga respinto con un reindirizzamento alla pagina di errore.
     */
    @Test
    @WithAnonymousUser
    public void formatoEmailErrato() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "mario-rossi@errata") // Non rispetta la regex
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login-form?error=true"));
    }

    /**
     * Verifica la gestione del fallimento dell'autenticazione (ME1) nel caso in cui
     * l'email inserita non sia presente in nessuno dei repository (Cliente, Operatore, Gestore).
     */
    @Test
    @WithAnonymousUser
    public void emailNonRegistrata() throws Exception {
        String emailValida = "mario@test.com";

        when(clienteRepository.findByEmail(emailValida)).thenReturn(null);
        when(operatoreRepository.findByEmail(emailValida)).thenReturn(null);
        when(gestoreRepository.findByEmail(emailValida)).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("username", emailValida)
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login-form?error=true"));
    }

    /**
     * Verifica che l'autenticazione fallisca (MP1) quando viene fornita una password
     * errata per un utente esistente, garantendo il redirect alla pagina di login con errore.
     */
    @Test
    @WithAnonymousUser
    public void passwordErrata() throws Exception {
        String email = "cliente@test.com";
        Cliente clienteEsistente = new Cliente();
        clienteEsistente.setEmail(email);
        clienteEsistente.setPassword("$2a$10$hashedPassword...");

        when(clienteRepository.findByEmail(email)).thenReturn(clienteEsistente);

        mockMvc.perform(post("/login")
                        .param("username", email)
                        .param("password", "wrongPassword")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login-form?error=true"));
    }

    /**
     * Testa il successo dell'autenticazione (MP2) per un Cliente.
     * Verifica che il sistema riconosca le credenziali valide e reindirizzi l'utente
     * alla propria home page.
     */
    @Test
    @WithAnonymousUser
    public void loginCliente_Corretto() throws Exception {
        String email = "cliente@test.com";
        String passInChiaro = "cliente123";

        Cliente clienteEsistente = new Cliente();
        clienteEsistente.setEmail(email);
        clienteEsistente.setPassword(new BCryptPasswordEncoder().encode(passInChiaro));

        when(clienteRepository.findByEmail(email)).thenReturn(clienteEsistente);

        mockMvc.perform(post("/login")
                        .param("username", email)
                        .param("password", passInChiaro)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home")); // Successo MP2
    }

    /**
     * Verifica che la pagina di login sia accessibile correttamente
     * per gli utenti non autenticati.
     */
    @Test
    @WithAnonymousUser
    public void loginCorretto_Get() throws Exception {
            mockMvc.perform(get("/login-form"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
    }

    /**
     * Verifica che un Operatore già autenticato, tentando di accedere alla pagina di login,
     * venga reindirizzato automaticamente alla propria area riservata.
     */
    @Test
    @WithMockUser(authorities = "OPERATORE")
    public void loginErrato_OperatoreAutenticato_Get() throws Exception {

        mockMvc.perform(get("/login-form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"));
    }

    /**
     * Verifica che un Gestore già autenticato, tentando di accedere alla pagina di login,
     * venga reindirizzato automaticamente alla propria dashboard di gestione.
     */
    @Test
    @WithMockUser(authorities = "GESTORE")
    public void loginErrato_GestoreAutenticato_Get() throws Exception {

        mockMvc.perform(get("/login-form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore"));
    }

    /**
     * Verifica che un Cliente già autenticato, tentando di accedere alla pagina di login,
     * venga reindirizzato automaticamente alla propria home page dei ticket.
     */
    @Test
    @WithMockUser(authorities = "CLIENTE")
    public void loginErrato_ClienteAutenticato_Get() throws Exception {

        mockMvc.perform(get("/login-form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"));
    }

}
