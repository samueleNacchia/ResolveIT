package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
    private GestoreRepository gestoreRepository;

    @MockitoBean
    private RegistrazioneService registrazioneService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    /**
     * Verifica il comportamento del sistema in caso di inserimento di un'email con formato non valido (FE1).
     * Il test si aspetta il ritorno alla vista di registrazione con errori di validazione sul campo "email".
     */
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

    /**
     * Verifica la gestione dell'errore quando si tenta di registrare un utente con un'email già presente nel sistema.
     * Simula il lancio di un'eccezione dal service e verifica che il messaggio d'errore sia visualizzato correttamente nel modello.
     */
    @Test
    public void emailGiaRegistrata() throws Exception {

        when(registrazioneService.registerClient(any(RegistraUtenteDTO.class)))
                .thenThrow(new RuntimeException("Email già in uso!"));

        mockMvc.perform(post("/register")
                        .param("email", "cliente@test.com")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registrazione"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Email già in uso!"));
    }

    /**
     * Verifica il vincolo di lunghezza minima della password (LP1).
     * Il test si aspetta un errore di validazione sul campo "password" se questa è inferiore a 8 caratteri.
     */
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

    /**
     * Verifica che il sistema segnali un errore quando la password di conferma non coincide con la password inserita (MCP1).
     * Controlla che l'oggetto DTO contenga errori di validazione globali o di corrispondenza.
     */
    @Test
    public void matchPasswordErrato() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Diversa123") // MCP1: No match
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registrazione"))
                .andExpect(content().string(containsString("Le password non coincidono!")));
    }

    /**
     * Verifica la validazione del campo nome (FNO1).
     * Il test fallisce se il nome inserito non è valido.
     */
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

    /**
     * Verifica la validazione del campo cognome (FCO1).
     *  Il test fallisce se il cognome inserito non è valido.
     */
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

    /**
     * Testa lo scenario di successo per la registrazione di un nuovo Cliente.
     * Verifica che, dopo l'inserimento di dati validi, l'utente venga reindirizzato alla homepage
     * e che avvenga l'autenticazione automatica (Auto-Login).
     */
    @Test
    @WithAnonymousUser
    public void registrazioneCliente_Successo() throws Exception {
        Cliente clienteFinto = new Cliente("Mario", "Rossi", "mario@email.it", "hashedPass");

        when(registrazioneService.registerClient(any(RegistraUtenteDTO.class))).thenReturn(clienteFinto);

        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"))
                .andExpect(authenticated().withUsername("mario@email.it")); // Verifica Auto-Login
    }

    /**
     * Verifica che un Gestore autenticato possa registrare correttamente un nuovo Operatore nel sistema.
     * Controlla il reindirizzamento alla dashboard del gestore e la presenza di un messaggio di conferma (flash attribute).
     */
    @Test
    @WithMockUser(username = "gestore@test.com", authorities = "GESTORE")
    public void registrazioneOperatore_Successo() throws Exception {

        mockMvc.perform(post("/registerOperator")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success=operatorCreated"))
                .andExpect(flash().attribute("successMessage", "Operatore creato con successo!"))
                .andExpect(authenticated().withUsername("gestore@test.com")); // IL GESTORE RESTA LOGGATO;

        verify(registrazioneService, times(1)).registerOperator(any(RegistraUtenteDTO.class));
    }

    /**
     * Verifica i vincoli di sicurezza per la registrazione degli operatori.
     * Controlla che un utente con ruolo CLIENTE non abbia i permessi per accedere alla funzione di creazione operatore.
     */
    @Test
    @WithMockUser(authorities = "CLIENTE")
    void registerOperator_NonPermesso() throws Exception {
        mockMvc.perform(post("/registerOperator")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Verifica che un utente già autenticato non possa rieseguire
     * una procedura di registrazione cliente standard.
     */
    @Test
    @WithMockUser(authorities = "CLIENTE")
    void registerClient_NonPermesso() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "mario@email.it")
                        .param("password", "Password123")
                        .param("confermaPassword", "Password123")
                        .param("nome", "Mario")
                        .param("cognome", "Rossi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"));
    }

}