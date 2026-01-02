package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // --- TEST AREA CLIENTE ---

    @Test
    @WithMockUser(username = "cliente@test.it", roles = {"CLIENTE"})
    public void testUserHome() throws Exception {
        Cliente mockCliente = new Cliente();
        mockCliente.setEmail("user@test.it");

        when(clienteRepository.findByEmail(anyString())).thenReturn(mockCliente);
        when(ticketService.getTicketUtente(any())).thenReturn(new ArrayList<>());
        when(categoriaRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/ticket/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-homepage"))
                .andExpect(model().attributeExists("lista", "ticketDTO", "categorie"));
    }

    @Test
    @WithMockUser(authorities = "CLIENTE")
    public void testSalvaTicket_Successo() throws Exception {
        when(clienteRepository.findByEmail(anyString())).thenReturn(new Cliente());
        // Simuliamo il successo del service
        when(ticketService.addTicket(any(), any())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("fileAllegato", "test.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/ticket/salva")
                        .file(file)
                        .param("titolo", "Problema WiFi")
                        .param("descrizione", "Non si connette più")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"))
                .andExpect(flash().attribute("successMessage", "Ticket creato con successo!"));
    }

    @Test
    @WithMockUser(authorities = "CLIENTE")
    public void testEliminaTicket_Successo() throws Exception {
        when(ticketService.deleteTicket(1L)).thenReturn(true);

        mockMvc.perform(post("/ticket/elimina/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"))
                .andExpect(flash().attribute("successMessage", "Ticket eliminato con successo!"));
    }

    // --- TEST AREA OPERATORE ---

    @Test
    @WithMockUser(roles = {"OPERATORE"})
    public void testOperatoreHome() throws Exception {
        Operatore mockOp = new Operatore();
        mockOp.setEmail("op@test.it");

        when(operatoreRepository.findByEmail(anyString())).thenReturn(mockOp);

        mockMvc.perform(get("/ticket/operatore-home"))
                .andExpect(status().isOk())
                .andExpect(view().name("operatore-homepage"))
                .andExpect(model().attributeExists("listaLavoro", "listaAttesa"));
    }

    @Test
    @WithMockUser(roles = {"OPERATORE"})
    public void testPrendiInCarico_Successo() throws Exception {
        when(operatoreRepository.findByEmail(anyString())).thenReturn(new Operatore());
        when(ticketService.assignTicket(anyLong(), any())).thenReturn(true);

        mockMvc.perform(post("/ticket/prendi/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"))
                .andExpect(flash().attribute("successMessage", "Ticket assegnato con successo!"));
    }

    @Test
    @WithMockUser(roles = {"OPERATORE"})
    public void testRisolviTicket_Successo() throws Exception {
        when(ticketService.resolveTicket(1L)).thenReturn(true);

        mockMvc.perform(post("/ticket/risolvi/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"))
                .andExpect(flash().attribute("successMessage", "Ticket risolto con successo!"));
    }

    @Test
    @WithMockUser(roles = {"OPERATORE"})
    public void testRilasciaTicket_Successo() throws Exception {
        when(ticketService.releaseTicket(1L)).thenReturn(true);

        mockMvc.perform(post("/ticket/rilascia/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"))
                .andExpect(flash().attribute("successMessage", "Ticket rilasciato con successo!"));
    }

    // --- TEST CASI DI ERRORE (BRANCH COVERAGE) ---

    @Test
    @WithMockUser(username = "cliente@test.it", roles = {"CLIENTE"})
    public void testSalvaTicket_FallimentoService() throws Exception {
        when(clienteRepository.findByEmail(anyString())).thenReturn(new Cliente());
        // Simuliamo fallimento (es. regex non rispettata o file troppo grande)
        when(ticketService.addTicket(any(), any())).thenReturn(false);

        mockMvc.perform(multipart("/ticket/salva")
                        .param("titolo", "Err")
                        .param("descrizione", "Corto")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Errore: formato allegato non supportato o dati mancanti."));
    }
}