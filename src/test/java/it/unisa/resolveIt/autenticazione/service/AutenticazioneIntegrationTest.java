package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AutenticazioneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    // Regex fornita dal TC_1.1
    private final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$";

    // --- TC_1.1_1: Formato email errato ---
    @Test
    public void formatoEmailErrato() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "mario-rossi@errata") // Non rispetta la regex
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // --- TC_1.1_2: Email non registrata (FE1) ---
    @Test
    public void emailNonRegistrata() throws Exception {
        String emailValida = "mario@test.com";

        // Simuliamo che non esista né come cliente né come operatore
        when(clienteRepository.findByEmail(emailValida)).thenReturn(null);
        when(operatoreRepository.findByEmail(emailValida)).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("username", emailValida)
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // --- TC_1.1_3: Password non corretta (MP1) ---
    @Test //
    public void passwordErrata() throws Exception {
        String email = "mario@test.com";
        Cliente clienteEsistente = new Cliente();
        clienteEsistente.setEmail(email);
        clienteEsistente.setPassword("$2a$10$hashedPassword...");

        when(clienteRepository.findByEmail(email)).thenReturn(clienteEsistente);

        mockMvc.perform(post("/login")
                        .param("username", email)
                        .param("password", "wrongPassword")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // --- TC_1.1_4: Autenticazione Corretta ---
    @Test
    public void loginCorretto() throws Exception {
        String email = "ADMIN@TEST.COM";
        String passInChiaro = "admin123";

        Cliente admin = new Cliente();
        admin.setEmail(email);
        admin.setPassword(new BCryptPasswordEncoder().encode(passInChiaro));

        when(clienteRepository.findByEmail(email)).thenReturn(admin);

        mockMvc.perform(post("/login")
                        .param("username", email)
                        .param("password", passInChiaro)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home")); // Successo MP2
    }
}
