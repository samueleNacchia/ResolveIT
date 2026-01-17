package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.ticket.dto.TicketDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class TicketIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private OperatoreRepository operatoreRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    public void setup() {
        // Inizializzazione manuale come nel test del collega
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Setup utenti per aggirare i filtri di Security che cercano l'utente nel DB
        Cliente mockCliente = new Cliente();
        mockCliente.setEmail("cliente@test.it");

        Operatore mockOp = new Operatore();
        mockOp.setEmail("op@test.it");

        lenient().when(clienteRepository.findByEmail("cliente@test.it")).thenReturn(mockCliente);
        lenient().when(operatoreRepository.findByEmail("op@test.it")).thenReturn(mockOp);
        lenient().when(categoriaRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testSalvaTicket_Successo() throws Exception {
        mockMvc.perform(multipart("/ticket/salva")
                        .param("titolo", "Problema Connessione")
                        .param("descrizione", "Internet non funziona")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"))
                .andExpect(flash().attribute("successMessage", "Ticket creato con successo!"));
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testSalvaTicket_ErroreService() throws Exception {
        // Simula il fallimento del service che porta al redirect con messaggio di errore
        doThrow(new RuntimeException("Errore")).when(ticketService).addTicket(any(TicketDTO.class), any(Cliente.class));

        mockMvc.perform(post("/ticket/salva")
                        .param("titolo", "Valido")
                        .param("descrizione", "Valida")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Errore: creazione ticket fallita"));
    }

    @Test
    @WithMockUser(username = "op@test.it", authorities = "OPERATORE")
    public void testAssegnaTicket_Successo() throws Exception {
        mockMvc.perform(post("/ticket/prendi/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Ticket assegnato con successo!"));
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testDownloadFile_Successo() throws Exception {
        Ticket t = new Ticket();
        t.setAllegato("content".getBytes());
        t.setNomeFile("test.jpg");
        when(ticketService.getTicketById(1L)).thenReturn(t);

        mockMvc.perform(get("/ticket/download/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testDownloadFile_NotFound() throws Exception {
        Ticket t = new Ticket();
        t.setAllegato(null);
        when(ticketService.getTicketById(1L)).thenReturn(t);

        mockMvc.perform(get("/ticket/download/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testEliminaTicket_Successo() throws Exception {
        mockMvc.perform(post("/ticket/elimina/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Ticket eliminato con successo!"));
    }

    @Test
    @WithMockUser(username = "op@test.it", authorities = "OPERATORE")
    public void testRilasciaTicket_Successo() throws Exception {
        mockMvc.perform(post("/ticket/rilascia/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Ticket rilasciato con successo!"));
    }



    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testUserHome_ConFiltri() throws Exception {
        mockMvc.perform(get("/ticket/home")
                        .param("stato", "APERTO")
                        .param("ordine", "asc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statoSelezionato", Stato.APERTO))
                .andExpect(model().attribute("ordineSelezionato", "asc"));

        verify(ticketService).getTicketUtenteFiltrati(any(Cliente.class), eq(Stato.APERTO), eq("asc"));
    }


    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testSalvaTicket_FormatoFileErrato() throws Exception {
        MockMultipartFile filePdf = new MockMultipartFile(
                "fileAllegato", "documento.pdf", "application/pdf", "contenuto".getBytes());

        mockMvc.perform(multipart("/ticket/salva")
                        .file(filePdf)
                        .param("titolo", "Titolo Valido")
                        .param("descrizione", "Descrizione valida")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-homepage"))
                .andExpect(model().attributeHasFieldErrors("ticketDTO", "fileAllegato"))
                .andExpect(model().attributeExists("openTab"));

        verify(ticketService, never()).addTicket(any(), any());
    }


    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testSalvaTicket_FileTroppoGrande() throws Exception {
        byte[] grandeContenuto = new byte[17 * 1024 * 1024]; // 17MB
        MockMultipartFile filePesante = new MockMultipartFile(
                "fileAllegato", "test.jpg", "image/jpeg", grandeContenuto);

        mockMvc.perform(multipart("/ticket/salva")
                        .file(filePesante)
                        .param("titolo", "Titolo")
                        .param("descrizione", "Descrizione")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("ticketDTO", "fileAllegato"));
    }


    @Test
    @WithMockUser(username = "cliente@test.it", authorities = "CLIENTE")
    public void testSalvaTicket_TitoloMancante() throws Exception {
        when(categoriaRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/ticket/salva")
                        .param("titolo", "")
                        .param("descrizione", "Descrizione")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-homepage"))
                .andExpect(model().attributeHasFieldErrors("ticketDTO", "titolo"))
                .andExpect(model().attributeExists("categorie", "lista", "openTab"));
    }

}