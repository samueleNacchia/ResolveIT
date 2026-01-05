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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private Cliente mockCliente;
    private Operatore mockOp;

    @BeforeEach
    public void setup() {
        // Forza il caricamento di Spring Security nel MockMvc
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();

        mockCliente = new Cliente();
        mockCliente.setEmail("cliente@test.it");
        when(clienteRepository.findByEmail("cliente@test.it")).thenReturn(mockCliente);

        mockOp = new Operatore();
        mockOp.setEmail("op@test.it");
        when(operatoreRepository.findByEmail("op@test.it")).thenReturn(mockOp);

        when(categoriaRepository.findAll()).thenReturn(new java.util.ArrayList<>());
    }



    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testUserHome_ConFiltri() throws Exception {
        mockMvc.perform(get("/ticket/home")
                        .param("stato", "APERTO")
                        .param("ordine", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-homepage"));
    }


    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testSalvaTicket_ServiceFailure() throws Exception {
        when(ticketService.addTicket(any(), any())).thenReturn(false);

        mockMvc.perform(post("/ticket/salva")
                        .param("titolo", "Titolo Valido")
                        .param("descrizione", "Descrizione valida")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"));
    }


    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testPrendiInCarico_Successo() throws Exception {
        when(ticketService.assignTicket(eq(1L), any())).thenReturn(true);

        mockMvc.perform(post("/ticket/prendi/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"));
    }

    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testPrendiInCarico_Exception() throws Exception {
        when(ticketService.assignTicket(anyLong(), any())).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(post("/ticket/prendi/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Errore interno: DB Error"));
    }

    // --- TEST DOWNLOAD (Prefisso /ticket aggiunto) ---

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testDownloadFile_NotFound() throws Exception {
        Ticket t = new Ticket();
        t.setAllegato(null);
        when(ticketService.getTicketById(1L)).thenReturn(t);

        // L'URL deve includere /ticket perché è definito a livello di classe nel Controller
        mockMvc.perform(get("/ticket/download/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testDownloadFile_Successo() throws Exception {
        Ticket t = new Ticket();
        t.setAllegato("content".getBytes());
        t.setNomeFile("test.jpg");
        when(ticketService.getTicketById(1L)).thenReturn(t);

        mockMvc.perform(get("/ticket/download/1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testEliminaTicket_Fallimento() throws Exception {
        when(ticketService.deleteTicket(anyLong())).thenReturn(false);

        mockMvc.perform(post("/ticket/elimina/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Operazione fallita"));
    }

    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testRisolvi_Fallimento() throws Exception {
        when(ticketService.resolveTicket(anyLong())).thenReturn(false);

        mockMvc.perform(post("/ticket/risolvi/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Operazione fallita"));
    }

    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testPrendiInCarico_Eccezione() throws Exception {
        when(ticketService.assignTicket(anyLong(), any())).thenThrow(new RuntimeException("Errore Database"));

        mockMvc.perform(post("/ticket/prendi/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Errore interno: Errore Database"));
    }

    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testDownloadFile_NomeDefault() throws Exception {
        Ticket t = new Ticket();
        t.setAllegato("dati".getBytes());
        t.setNomeFile(null);
        when(ticketService.getTicketById(1L)).thenReturn(t);

        mockMvc.perform(get("/ticket/download/1"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"allegato.dat\""));
    }


    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testOperatoreHome_Successo() throws Exception {
        // 1. Preparazione dei Mock
        when(operatoreRepository.findByEmail("op@test.it")).thenReturn(mockOp);
        when(ticketService.getTicketInCarico(mockOp)).thenReturn(new java.util.ArrayList<>());
        when(ticketService.getTicketDisponibili()).thenReturn(new java.util.ArrayList<>());

        // 2. Esecuzione e Verifica
        mockMvc.perform(get("/ticket/operatore-home"))
                .andExpect(status().isOk())
                .andExpect(view().name("operatore-homepage"))
                .andExpect(model().attributeExists("listaLavoro", "listaAttesa"));

        // 3. Verifica che i metodi siano stati effettivamente chiamati (ottimo per il coverage)
        verify(operatoreRepository).findByEmail("op@test.it");
        verify(ticketService).getTicketInCarico(mockOp);
        verify(ticketService).getTicketDisponibili();
    }


    // RAMO 1: Successo (success = true)
    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testRilascia_Successo() throws Exception {
        // Mock del service che restituisce true
        when(ticketService.releaseTicket(1L)).thenReturn(true);

        mockMvc.perform(post("/ticket/rilascia/1")
                        .with(csrf())) // Obbligatorio per le POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"))
                .andExpect(flash().attribute("successMessage", "Ticket rilasciato con successo!"));

        // Verifica che il metodo del service sia stato chiamato esattamente una volta
        verify(ticketService, times(1)).releaseTicket(1L);
    }

    // RAMO 2: Fallimento (success = false)
    @Test
    @WithMockUser(username = "op@test.it", authorities = {"OPERATORE"})
    public void testRilascia_Fallimento() throws Exception {
        // Mock del service che restituisce false
        when(ticketService.releaseTicket(1L)).thenReturn(false);

        mockMvc.perform(post("/ticket/rilascia/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/operatore-home"))
                .andExpect(flash().attribute("errorMessage", "Operazione fallita"));

        verify(ticketService, times(1)).releaseTicket(1L);
    }


    @Test
    @WithMockUser(username = "cliente@test.it", authorities = {"CLIENTE"})
    public void testSalvaTicket_Success() throws Exception {
        when(clienteRepository.findByEmail("cliente@test.it")).thenReturn(mockCliente);
        // Istruiamo il service a restituire true
        when(ticketService.addTicket(any(TicketDTO.class), eq(mockCliente))).thenReturn(true);

        mockMvc.perform(post("/ticket/salva")
                        .param("titolo", "Titolo Valido")
                        .param("descrizione", "Descrizione valida")
                        .param("idCategoria", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket/home"))
                .andExpect(flash().attribute("successMessage", "Ticket creato con successo!"));
    }

}
