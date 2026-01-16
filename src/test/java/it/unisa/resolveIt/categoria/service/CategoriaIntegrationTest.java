package it.unisa.resolveIt.categoria.service;

import it.unisa.resolveIt.model.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di Integrazione per il sottosistema Categoria.
 * <p>
 * Questa classe verifica il comportamento del {@link it.unisa.resolveIt.categoria.control.CategoriaController},
 * simulando richieste HTTP tramite {@link MockMvc}.
 * Vengono testati i flussi di aggiunta, abilitazione e disabilitazione delle categorie,
 * inclusa la gestione delle eccezioni e i reindirizzamenti (Redirect).
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
public class CategoriaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private CategoriaImpl categoriaImpl;

    /**
     * Verifica il successo dell'operazione di aggiunta di una nuova categoria.
     * Controlla che il controller reindirizzi alla pagina corretta con il parametro di successo.
     */
    @Test
    public void addCategoria_Successo() throws Exception {
        mockMvc.perform(post("/categoria/addCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("nome", "Hardware")
                        .param("stato", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories&success"));
    }

    /**
     * Verifica la gestione di un'eccezione generica durante l'aggiunta di una categoria.
     * Simula un errore nel service e controlla che il controller reindirizzi alla pagina con il parametro di errore.
     */
    @Test
    public void addCategoria_Errore_ExceptionGenerica() throws Exception {
        // Simula l'eccezione che fa scattare il catch del controller
        doThrow(new RuntimeException("Errore DB")).when(categoriaImpl).addCategoria(any());

        mockMvc.perform(post("/categoria/addCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("nome", "Hardware")
                        .param("stato", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories&error"));
    }

    /**
     * Verifica il successo dell'operazione di disabilitazione di una categoria.
     * Controlla il reindirizzamento alla sezione categorie del gestore.
     */
    @Test
    public void disableCategoria_Successo() throws Exception {
        mockMvc.perform(post("/categoria/disableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }

    /**
     * Verifica la gestione delle eccezioni durante la disabilitazione di una categoria.
     * Anche in caso di errore (simulato nel service), il controller deve garantire un reindirizzamento sicuro.
     */
    @Test
    public void disableCategoria_EccezioneGenerica() throws Exception {
        doThrow(new RuntimeException("Errore")).when(categoriaImpl).disableCategoria(1L);

        mockMvc.perform(post("/categoria/disableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }

    /**
     * Verifica il successo dell'operazione di abilitazione di una categoria.
     * Controlla il reindirizzamento alla sezione categorie del gestore.
     */
    @Test
    public void enableCategoria_Successo() throws Exception {
        mockMvc.perform(post("/categoria/enableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }
}