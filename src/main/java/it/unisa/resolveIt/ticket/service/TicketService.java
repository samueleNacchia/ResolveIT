package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.ticket.dto.TicketDTO;

import java.io.IOException;
import java.util.List;

public interface TicketService {
    boolean addTicket(TicketDTO dto, Cliente autore) throws IOException;
    boolean deleteTicket(Long ticketId);
    boolean assignTicket(Long ticketId, Operatore operatore);
    boolean resolveTicket(Long ticketId);
    boolean releaseTicket(Long ticketId);
    List<Ticket> getTicketUtente(Cliente cliente);
    List<Ticket> getTicketDisponibili();
    List<Ticket> getTicketInCarico(Operatore operatore);
}
