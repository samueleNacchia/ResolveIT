package it.unisa.resolveIt.categoria.service;

import it.unisa.resolveIt.model.entity.Categoria;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

    // --- TEST AGGIUNTA CATEGORIA ---

    @Test
    public void addCategoria_Successo() throws Exception {
        // Simuliamo che non esista nessuna categoria con quel nome o ID
        when(categoriaRepository.existsById(any())).thenReturn(false);
        when(categoriaRepository.findByNome(any())).thenReturn(null);

        mockMvc.perform(post("/categoria/addCategoria")
                        .with(user("gestore@test.com").roles("GESTORE")) // Autenticazione richiesta
                        .param("nome", "Hardware")
                        .param("stato", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                // Verifica il redirect specifico definito nel tuo Controller
                .andExpect(redirectedUrl("/gestore?section=categories&success"));
    }

    @Test
    public void addCategoria_Errore_NomeEsistente() throws Exception {
        // Simuliamo che il nome esista già -> Il service lancerà RuntimeException
        when(categoriaRepository.existsById(any())).thenReturn(false);
        when(categoriaRepository.findByNome("Hardware")).thenReturn(new Categoria());

        mockMvc.perform(post("/categoria/addCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("nome", "Hardware")
                        .param("stato", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                // Il controller cattura l'eccezione e manda qui:
                .andExpect(redirectedUrl("/gestore?section=categories&error"));
    }

    // --- TEST DISABILITAZIONE ---

    @Test
    public void disableCategoria_Successo() throws Exception {
        long id = 1L;
        // Simuliamo una categoria attiva
        Categoria catAttiva = new Categoria("Test", true);
        catAttiva.setID_C((int)id);

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(catAttiva));

        mockMvc.perform(post("/categoria/disableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", String.valueOf(id))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }

    // --- TEST ABILITAZIONE ---

    @Test
    public void enableCategoria_Successo() throws Exception {
        long id = 2L;
        // Simuliamo una categoria disattiva (stato = false)
        Categoria catDisattiva = new Categoria("Test", false);
        catDisattiva.setID_C((int)id);

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(catDisattiva));

        mockMvc.perform(post("/categoria/enableCategoria")
                        .with(user("gestore@test.com").roles("GESTORE"))
                        .param("id", String.valueOf(id))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestore?section=categories"));
    }
}