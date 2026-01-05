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

@SpringBootTest
@AutoConfigureMockMvc
public class CategoriaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaRepository categoriaRepository;


    @MockitoBean
    private CategoriaImpl categoriaImpl;

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

    @Test
    public void disableCategoria_Successo() throws Exception {
        mockMvc.perform(post("/categoria/disableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }

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