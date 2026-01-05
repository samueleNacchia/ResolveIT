package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
    private AccountImpl accountImpl;

    // --- TEST ACCOUNT CONTROLLER (Lato Gestore) ---

    @Test
    public void removeAccountCliente_Successo() throws Exception {
        mockMvc.perform(post("/account/removeCliente")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success"));
    }

    @Test
    public void removeAccountCliente_Eccezione() throws Exception {
        doThrow(new RuntimeException("Errore")).when(accountImpl).removeAccountCliente(1L);

        mockMvc.perform(post("/account/removeCliente")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&error"));
    }

    @Test
    public void removeAccountOperatore_Successo() throws Exception {
        mockMvc.perform(post("/account/removeOperatore")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&success"));
    }

    @Test
    public void removeAccountOperatore_Eccezione() throws Exception {
        doThrow(new RuntimeException("Errore")).when(accountImpl).removeAccountOperatore(99L);

        mockMvc.perform(post("/account/removeOperatore")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=accounts&error"));
    }

    // --- TEST MY PROFILE CONTROLLER ---

    @Test
    public void showProfileForm_Successo() throws Exception {
        String email = "cliente@test.com";
        MyProfileDTO dto = new MyProfileDTO();
        dto.setEmail(email);

        when(accountImpl.getUserByEmail(email)).thenReturn(dto);

        mockMvc.perform(get("/my-profile")
                        .with(user(email).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeExists("utenteDTO"));
    }

    // TEST DI BRANCH COVERAGE AGGIUNTI QUI SOTTO

    @Test
    public void modifyUserAccount_PasswordVuota_Successo() throws Exception {
        // Copre il ramo: if (dto.getPassword() != null && !dto.getPassword().isEmpty()) -> FALSE
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword(""); // Vuota
        dto.setConfermaPassword("");

        mockMvc.perform(post("/my-profile")
                        .with(user("mail@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    public void modifyUserAccount_MismatchController() throws Exception {
        // Copre il ramo: if (!dto.getPassword().equals(dto.getConfermaPassword())) -> TRUE
        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword("PasswordA123");
        dto.setConfermaPassword("PasswordB123"); // Diverse

        mockMvc.perform(post("/my-profile")
                        .with(user("mail@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().isOk()) // Rimane nella pagina
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "confermaPassword"));
    }

    @Test
    public void modifyUserAccount_ValidazioneDTO_Fallita() throws Exception {
        // Copre result.hasErrors() -> TRUE
        MyProfileDTO dtoErrato = new MyProfileDTO();
        dtoErrato.setNome("Luigi123"); //
        dtoErrato.setCognome("Verdi");
        dtoErrato.setEmail("mail@test.com");

        mockMvc.perform(post("/my-profile")
                        .with(user("mail@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dtoErrato)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeHasFieldErrors("utenteDTO", "nome"));
    }

    @Test
    public void modifyUserAccount_Successo_PasswordChange() throws Exception {
        // Copre if (passwordChanged) -> TRUE
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(true);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");
        dto.setPassword("NewPass123"); //
        dto.setConfermaPassword("NewPass123");

        mockMvc.perform(post("/my-profile")
                        .with(user("mail@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("passwordSuccess"));
    }

    @Test
    public void modifyUserAccount_EccezioneService() throws Exception {
        // Copre catch (Exception e)
        doThrow(new RuntimeException("Errore Critico")).when(accountImpl).modifyUser(any());

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Luigi");
        dto.setCognome("Verdi");
        dto.setEmail("mail@test.com");

        mockMvc.perform(post("/my-profile")
                        .with(user("mail@test.com").roles("CLIENTE"))
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attribute("errorMessage", "Errore Critico"));
    }

    // --- TEST BRANCH COVERAGE CONTROLLER (INSTANCEOF) ---

    @Test
    public void modifyUserAccount_AggiornaSessione_Operatore() throws Exception {
        // Branch: if (userDetails instanceof Operatore)

        // 1. Creiamo un vero oggetto Operatore (che implementa UserDetails)
        Operatore opReale = new Operatore("VecchioNome", "Rossi", "op@test.com", "pass");

        // 2. Mockiamo il service
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("NuovoNome"); // Questo nome deve finire nell'oggetto opReale
        dto.setCognome("Rossi");
        dto.setEmail("op@test.com");

        // 3. Eseguiamo la richiesta passando l'oggetto opReale direttamente a .with(user(...))
        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(user(opReale)) // <--- MODIFICA FONDAMENTALE: Passiamo l'oggetto reale qui
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile")); // Ci aspettiamo torni al profilo, NON al login

        // 4. Verifica che il controller abbia aggiornato l'oggetto in memoria
        assertEquals("NuovoNome", opReale.getNome());
    }

    @Test
    public void modifyUserAccount_AggiornaSessione_Cliente() throws Exception {
        // Branch: else if (userDetails instanceof Cliente)

        // 1. Creiamo un vero oggetto Cliente
        Cliente cliReale = new Cliente("VecchioNome", "Verdi", "cli@test.com", "pass");

        // 2. Mockiamo il service
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("NuovoNomeCli");
        dto.setCognome("Verdi");
        dto.setEmail("cli@test.com");

        // 3. Eseguiamo la richiesta passando l'oggetto cliReale
        mockMvc.perform(post("/my-profile")
                        .flashAttr("utenteDTO", dto)
                        .with(user(cliReale)) //
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));

        // 4. Verifica che sia entrato nell'else if del Cliente
        assertEquals("NuovoNomeCli", cliReale.getNome());
    }
    @Test
    public void modifyUserAccount_AggiornaSessione_GenericUser() throws Exception {
        // Branch: else finale (Né Operatore né Cliente)
        when(accountImpl.modifyUser(any(MyProfileDTO.class))).thenReturn(false);

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome("Test");
        dto.setCognome("Test");
        dto.setEmail("generic@test.com");

        mockMvc.perform(post("/my-profile")
                        .with(user("generic@test.com")) // Utente generico Spring
                        .flashAttr("utenteDTO", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

    }
}