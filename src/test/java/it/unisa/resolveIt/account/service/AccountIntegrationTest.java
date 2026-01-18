package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe di test di integrazione per i controller relativi alla gestione Account.
 * <p>
 * Verifica il flusso completo delle richieste HTTP:
 * <ul>
 * <li>Chiamate POST per rimuovere account (Gestore).</li>
 * <li>Chiamate GET/POST per visualizzare e modificare il proprio profilo (Utente).</li>
 * <li>Verifica dei redirect e dei parametri URL restituiti.</li>
 * </ul>
 * Il service {@link AccountImpl} è mockato per simulare successi ed eccezioni.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountImpl accountImpl;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // --- TEST ACCOUNT CONTROLLER (Lato Gestore) ---

    /**
     * Verifica il comportamento del controller quando un Gestore rimuove un Cliente con successo.
     * Atteso: Redirect a /gestore con parametro success.
     */
    @Test
    @WithMockUser(authorities = "GESTORE")
    public void removeAccountCliente_Successo() throws Exception {
        mockMvc.perform(post("/account/removeCliente")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success"));
    }

    /**
     * Verifica il comportamento del controller quando si verifica un errore durante la rimozione di un Cliente.
     * Atteso: Redirect a /gestore con parametro error.
     */
    @Test
    @WithMockUser(username = "gestore@test.it", authorities = "GESTORE")
    public void removeAccountCliente_Eccezione() throws Exception {
        // Simuliamo un errore nel service
        doThrow(new RuntimeException("Errore")).when(accountImpl).removeAccountCliente(1L);

        mockMvc.perform(post("/account/removeCliente")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&error"));
    }

    /**
     * Verifica il comportamento del controller quando un Gestore rimuove un Operatore con successo.
     */
    @Test
    @WithMockUser(username = "gestore@test.it", authorities = "GESTORE")
    public void removeAccountOperatore_Successo() throws Exception {
        mockMvc.perform(post("/account/removeOperatore")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success"));
    }

    /**
     * Verifica il comportamento del controller quando si verifica un errore durante la rimozione di un Operatore.
     */
    @Test
    @WithMockUser(username = "gestore@test.it", authorities = "GESTORE")
    public void removeAccountOperatore_Eccezione() throws Exception {
        doThrow(new RuntimeException("Errore")).when(accountImpl).removeAccountOperatore(99L);

        mockMvc.perform(post("/account/removeOperatore")
                        .param("id", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&error"));
    }

    // --- TEST MY PROFILE CONTROLLER ---

    /**
     * Verifica che la pagina "Il mio profilo" venga caricata correttamente con il DTO popolato.
     */
    @Test
    @WithMockUser(username = "cliente@test.com", authorities = "CLIENTE") // .com qui
    public void showProfileForm_Successo() throws Exception {
        String email = "cliente@test.com";
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail(email);
        dto.setNome("Mario");
        dto.setNome("Rossi");

        when(accountImpl.getUserByEmail(email)).thenReturn(dto);

        mockMvc.perform(get("/my-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeExists("utenteDTO"));
    }

    /**
     * Verifica la modifica del profilo senza cambio password (password vuota).
     * Atteso: Redirect alla pagina profilo con messaggio di successo.
     */
    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void modifyUserAccount_PasswordVuota_Successo() throws Exception {
        // Mockiamo che il service ritorni false (password non cambiata)
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword("");
        dto.setConfermaPassword("");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    /**
     * Verifica che il controller rilevi la non corrispondenza delle password prima di chiamare il service.
     * Atteso: Ritorna alla vista con errori di validazione nel Model.
     */
    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void modifyUserAccount_MismatchController() throws Exception {
        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword("PasswordA123");
        dto.setConfermaPassword("PasswordB123");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "confermaPassword"));
    }

    /**
     * Verifica la validazione automatica del DTO (es. campi obbligatori o pattern regex).
     */
    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void modifyUserAccount_ValidazioneDTO_Fallita() throws Exception {
        MyProfileDTO dtoErrato = new MyProfileDTO();
        dtoErrato.setNome("Luigi123"); // Nome non valido (contiene numeri)
        dtoErrato.setCognome("Verdi");
        dtoErrato.setEmail("mail@test.com");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dtoErrato)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "nome"));
    }

    /**
     * Verifica la modifica del profilo con cambio password avvenuto con successo.
     * Atteso: Redirect al login per forzare la ri-autenticazione.
     */
    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void modifyUserAccount_Successo_PasswordChange() throws Exception {
        // Mockiamo che il service ritorni true (password cambiata)
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(true);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword("NewPass123");
        dto.setConfermaPassword("NewPass123");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("passwordSuccess"));
    }

    /**
     * Verifica la gestione delle eccezioni lanciate dal service durante la modifica.
     * Atteso: Ritorna alla vista con messaggio di errore.
     */
    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void modifyUserAccount_EccezioneService() throws Exception {
        doThrow(new RuntimeException("Errore Critico")).when(accountImpl).modifyUser(any());

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attribute("errorMessage", "Errore Critico"));
    }

    // --- TEST DI AGGIORNAMENTO SESSIONE (INSTANCEOF CHECK) ---

    /**
     * Verifica che, se l'utente loggato è un Operatore e modifica il nome,
     * l'oggetto UserDetails in sessione venga aggiornato senza logout.
     */
    @Test
    @WithMockUser(authorities = "OPERATORE")
    public void modifyUserAccount_AggiornaSessione_Operatore() throws Exception {
        Operatore opReale = new Operatore("VecchioNome", "Rossi", "op@test.com", "pass");
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("NuovoNome");
        dto.setCognome("Rossi");
        dto.setEmail("op@test.com");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(user(opReale))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));

        // Asserzione fondamentale: verifica che l'oggetto in memoria sia cambiato
        assertEquals("NuovoNome", opReale.getNome());
    }

    /**
     * Verifica che, se l'utente loggato è un Cliente e modifica il nome,
     * l'oggetto UserDetails in sessione venga aggiornato senza logout.
     */
    @Test
    @WithMockUser(authorities = "CLIENTE")
    public void modifyUserAccount_AggiornaSessione_Cliente() throws Exception {
        Cliente cliReale = new Cliente("VecchioNome", "Verdi", "cli@test.com", "pass");
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("NuovoNomeCli");
        dto.setCognome("Verdi");
        dto.setEmail("cli@test.com");

        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(user(cliReale))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));

        assertEquals("NuovoNomeCli", cliReale.getNome());
    }

}