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

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

        boolean result = ticketService.addTicket(dto, autore);

        assertTrue(result);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
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
    void releaseTicket_Fallimento_TicketInesistente() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = ticketService.releaseTicket(99L);

        assertFalse(result);
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
        ticketService.getTicketUtente(c);
        verify(ticketRepository).findByCliente(c);
    }


    @Test
    void getTicketInCarico_Successo() {
        Operatore op = new Operatore();
        ticketService.getTicketInCarico(op);
        verify(ticketRepository).findByOperatore(op);
    }
}