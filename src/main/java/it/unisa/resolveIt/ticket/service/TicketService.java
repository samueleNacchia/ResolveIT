package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.model.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;


    public Ticket creaTicket(Ticket ticket) {
        ticket.setDataCreazione(LocalDateTime.now());
        ticket.setStato(Stato.APERTO);
        return ticketRepository.save(ticket);
    }

    public Ticket annullaTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setDataAnnullamento(LocalDateTime.now());
        ticket.setStato(Stato.ANNULLATO);
        return ticketRepository.save(ticket);
    }


    public Ticket prendiInCarico(Long ticketId, Operatore operatore) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setOperatore(operatore);
        ticket.setDataInCarico(LocalDateTime.now());
        ticket.setStato(Stato.IN_CORSO);
        return ticketRepository.save(ticket);
    }


    public Ticket risolviTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setDataResolved(LocalDateTime.now());
        ticket.setStato(Stato.RISOLTO);
        return ticketRepository.save(ticket);
    }


    public List<Ticket> getTicketUtente(Cliente cliente) {
        return ticketRepository.findByCliente(cliente);
    }


    public List<Ticket> getTicketDisponibili() {
        return ticketRepository.findByStato(Stato.APERTO);
    }

    public List<Ticket> getTicketInCarico(Operatore operatore) {
        return ticketRepository.findByOperatore(operatore);
    }
}