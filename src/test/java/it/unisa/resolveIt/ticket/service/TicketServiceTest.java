package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Categoria;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.TicketRepository;
import it.unisa.resolveIt.ticket.dto.TicketDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TicketImpl ticketService;

    private final String titolo_regex = "^[a-zA-Z0-9À-ÿ '‘\".,!?-]{5,100}";


    @Test
    void quandoSalvoTicket_alloraVieneConvertitoCorrettamente() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setTitolo("Problema Connessione");
        dto.setIdCategoria(1L);
        dto.setDescrizione("Internet non funziona");

        Cliente autore = new Cliente();
        autore.setEmail("test@user.it");

        Categoria cat = new Categoria();
        cat.setStato(true);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

        boolean risultato = ticketService.addTicket(dto, autore);

        assertTrue(risultato);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void addTicket_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setTitolo("Problema Connessione");
        dto.setDescrizione("Internet non funziona");
        dto.setIdCategoria(1L);
        dto.setFileAllegato(new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes()));

        Cliente autore = new Cliente();
        Categoria cat = new Categoria();
        cat.setID_C(1);
        cat.enable();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, autore);

        assertTrue(result);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }


    @Test
    void addTicket_SenzaAllegato_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        dto.setDescrizione("Descrizione valida");
        dto.setFileAllegato(null); // Caso comune: l'utente non carica nulla

        Categoria cat = new Categoria();
        cat.enable();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result, "Il service dovrebbe permettere l'invio senza allegato");
        verify(ticketRepository).save(any(Ticket.class));
    }


    @Test
    void addTicket_FormatoAllegatoNonValido() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        // Formato .exe non permesso
        dto.setFileAllegato(new MockMultipartFile("file", "virus.exe", "application/octet-stream", "content".getBytes()));

        when(categoriaRepository.findById(any())).thenReturn(Optional.of(new Categoria()));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }


    @Test
    void addTicket_FormatoTitoloNonValido() throws IOException {

        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("@_provea$");


        when(categoriaRepository.findById(any())).thenReturn(Optional.of(new Categoria()));

        boolean result = ticketService.addTicket(dto, new Cliente());
        assertFalse(result, "Il service dovrebbe rifiutare titoli con caratteri speciali");
        verify(ticketRepository, never()).save(any());
    }


    @Test
    void addTicket_LunghezzaTestoNonValida() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Problema x");
        // Testiamo il limite: 2001 caratteri (se il limite è 2000)
        String descrizioneTroppoLunga = "a".repeat(2001);
        dto.setDescrizione(descrizioneTroppoLunga);

        when(categoriaRepository.findById(any())).thenReturn(Optional.of(new Categoria()));

        boolean result = ticketService.addTicket(dto, new Cliente());

        // Deve essere FALSE perché abbiamo superato il limite
        assertFalse(result, "Il service dovrebbe rifiutare descrizioni oltre i 2000 caratteri");
        // Verifichiamo che NON abbia salvato nulla
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void addTicket_CategoriaNonValida() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");

        Categoria catInattiva = new Categoria();
        catInattiva.setID_C(1);
        catInattiva.setStato(false); //

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(catInattiva));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result, "Dovrebbe fallire perché la categoria è disattivata (ST_error)"); //
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void addTicket_GrandezzaAllegatoNonValido() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);

        byte[] content = new byte[17 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);
        dto.setFileAllegato(largeFile);

        when(categoriaRepository.findById(any())).thenReturn(Optional.of(new Categoria()));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }


    @Test
    void addTicket_FormatoAllegatoValido_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        dto.setDescrizione("Descrizione valida");
        // EA_OK: Estensione permessa (.jpg)
        dto.setFileAllegato(new MockMultipartFile("file", "immagine.jpg", "image/jpeg", "content".getBytes()));

        Categoria cat = new Categoria();
        cat.setStato(true); // ST_OK: Categoria attiva

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result);
        verify(ticketRepository, times(1)).save(any());
    }

    @Test
    void addTicket_FormatoTitoloValido_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Ticket Supporto 123!");
        dto.setDescrizione("blablabla");

        Categoria cat = new Categoria();
        cat.enable();

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result);
    }

    @Test
    void addTicket_LunghezzaTestoValida_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");

        dto.setDescrizione("a".repeat(2000));

        Categoria cat = new Categoria();
        cat.setStato(true);

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result);
    }

    @Test
    void addTicket_CategoriaAttiva_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        dto.setDescrizione("blablabla");

        Categoria catAttiva = new Categoria();
        catAttiva.setID_C(1);
        catAttiva.setStato(true); // ST_OK: Categoria non disattivata

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(catAttiva));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result);
    }

    @Test
    void addTicket_GrandezzaAllegatoValido_Successo() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        dto.setDescrizione("blablabla");

        // SA_OK: Size <= 16MB (esattamente 16MB)
        byte[] content = new byte[16 * 1024 * 1024];
        MockMultipartFile validFile = new MockMultipartFile("file", "test.zip", "application/zip", content);
        dto.setFileAllegato(validFile);

        Categoria cat = new Categoria();
        cat.setStato(true);

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertTrue(result);
    }

    @Test
    void addTicket_OriginalNameNull_Fallimento() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        // Creiamo un file senza nome originale
        dto.setFileAllegato(new MockMultipartFile("file", null, "text/plain", "content".getBytes()));

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(new Categoria()));

        boolean result = ticketService.addTicket(dto, new Cliente());
        assertFalse(result); // Il ramo "originalName != null" fallisce e prosegue o esce
    }

    @Test
    void addTicket_TitoloNull_Fallimento() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo(null); // Forza il primo pezzo dell'if (dto.getTitolo() == null)
        dto.setDescrizione("Descrizione valida");

        Categoria cat = new Categoria();
        cat.enable();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result); // Copre il ramo rosso del return false sotto il controllo titolo
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void addTicket_OriginalFilenameNull_Fallimento() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        // Creiamo un file che simula un getOriginalFilename() che restituisce null
        MockMultipartFile fileSenzaNome = new MockMultipartFile("file", null, "text/plain", "content".getBytes());
        dto.setFileAllegato(fileSenzaNome);

        Categoria cat = new Categoria();
        cat.enable();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        // Se il tuo codice non ha un 'else' per originalName == null, questo ramo potrebbe rimanere giallo
        // Questo test forza l'uscita o il salto del blocco allegato
        assertFalse(result);
    }

    @Test
    void addTicket_DescrizioneOltreLimite_Fallimento() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");

        // Generiamo 2001 caratteri per attivare il return false
        dto.setDescrizione("a".repeat(2001));

        Categoria cat = new Categoria();
        cat.enable();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result, "Il service dovrebbe fallire con descrizione > 2000 caratteri");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void addTicket_AllegatoTroppoGrande_Fallimento() throws IOException {
        TicketDTO dto = new TicketDTO();
        dto.setIdCategoria(1L);
        dto.setTitolo("Titolo Valido");
        dto.setDescrizione("Descrizione valida");

        // 16MB + 1 byte = 16.777.217 byte
        byte[] content = new byte[(16 * 1024 * 1024) + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", content
        );
        dto.setFileAllegato(largeFile);

        Categoria cat = new Categoria();
        cat.enable();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, new Cliente());

        assertFalse(result, "Il service dovrebbe fallire con file > 16MB");
        verify(ticketRepository, never()).save(any());
    }


    @Test
    void assignTicket_Successo() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.APERTO);
        Operatore op = new Operatore();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.assignTicket(ticketId, op);

        assertTrue(result);
        assertEquals(Stato.IN_CORSO, ticket.getStato());
        assertEquals(op, ticket.getOperatore());
        verify(ticketRepository).save(ticket);
    }



    @Test
    void assignTicket_Fallimento_StatoNonAperto() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.RISOLTO); // Già risolto, non assegnabile

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.assignTicket(ticketId, new Operatore());

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void assignTicket_TicketInesistente_Fallimento() {
        Long ticketId = 999L;
        // Il repository restituisce Empty (Ticket non trovato)
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        boolean result = ticketService.assignTicket(ticketId, new Operatore());

        assertFalse(result); // Copre il ramo 'if (ticket == null)'
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void assignTicket_OperatoreNull_Fallimento() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.APERTO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Passiamo null come operatore
        boolean result = ticketService.assignTicket(ticketId, null);

        assertFalse(result); // Copre il ramo 'if (operatore == null)'
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void resolveTicket_Successo() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.IN_CORSO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.resolveTicket(ticketId);

        assertTrue(result);
        assertEquals(Stato.RISOLTO, ticket.getStato());
        assertNotNull(ticket.getDataResolved());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void resolveTicket_Fallimento_StatoNonAssegnato() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.ANNULLATO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.resolveTicket(ticketId);

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void resolveTicket_TicketInesistente_Fallimento() {
        when(ticketRepository.findById(anyLong())).thenReturn(Optional.empty());
        boolean result = ticketService.resolveTicket(1L);
        assertFalse(result);
    }

    @Test
    void deleteTicket_TicketInesistente_Fallimento() {
        when(ticketRepository.findById(anyLong())).thenReturn(Optional.empty());
        boolean result = ticketService.deleteTicket(1L);
        assertFalse(result);
    }

    @Test
    void deleteTicket_Successo() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.APERTO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.deleteTicket(ticketId);

        assertTrue(result);
        assertEquals(Stato.ANNULLATO, ticket.getStato());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void deleteTicket_Fallimento_StatoNonAssegnato() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.RISOLTO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.deleteTicket(ticketId);

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void releaseTicket_Successo() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.IN_CORSO);
        ticket.setOperatore(new Operatore());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.releaseTicket(ticketId);

        assertTrue(result);
        // POST-CONDIZIONE CORRETTA: deve tornare APERTO e l'operatore deve essere null
        assertEquals(Stato.APERTO, ticket.getStato());
        assertNull(ticket.getOperatore());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void releaseTicket_Fallimento_StatoInCorso() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket();
        ticket.setStato(Stato.RISOLTO);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        boolean result = ticketService.releaseTicket(ticketId);

        assertFalse(result);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void getTicketDisponibili_Successo() {
        ticketService.getTicketDisponibili();
        // Verifica che venga chiamato il repository con lo stato APERTO
        verify(ticketRepository).findByStato(Stato.APERTO);
    }

    @Test
    void getTicketUtente_Successo() {
        Cliente c = new Cliente();
        Ticket ticket = new Ticket();
        ticket.setID_T(1L);
        ticket.setTitolo("Test DTO");
        ticket.setStato(Stato.APERTO);
        when(ticketRepository.findByClienteOrderByDataCreazioneDesc(c)).thenReturn(java.util.List.of(ticket));

        List<TicketDTO> risultato = ticketService.getTicketUtente(c);

        assertNotNull(risultato);
        assertEquals(1, risultato.size());
        assertEquals("Test DTO", risultato.get(0).getTitolo());
        assertEquals(1L, risultato.get(0).getId()); // Verifica mapping ID_T -> id
    }


    @Test
    void getTicketInCarico_Successo() {
        Operatore op = new Operatore();
        Ticket ticket = new Ticket();
        ticket.setID_T(5L);
        ticket.setTitolo("Ticket Operatore");

        when(ticketRepository.findByOperatore(op)).thenReturn(List.of(ticket));

        List<TicketDTO> risultato = ticketService.getTicketInCarico(op);

        assertNotNull(risultato);
        assertEquals(1, risultato.size());
        assertEquals(5L, risultato.get(0).getId());
        verify(ticketRepository).findByOperatore(op);
    }
}