package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Categoria;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.TicketRepository;
import it.unisa.resolveIt.ticket.dto.TicketDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketImpl implements TicketService{

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;


    @Transactional
    public boolean addTicket(TicketDTO dto, Cliente autore) throws IOException {
        Ticket ticket = new Ticket();

        ticket.setTitolo(dto.getTitolo());
        ticket.setTesto(dto.getDescrizione());

        Categoria cat = categoriaRepository.findById(dto.getIdCategoria()).orElse(null);
        if (cat == null) {
            return false;
        }
        ticket.setCategoria(cat);

        if (dto.getFileAllegato() != null && !dto.getFileAllegato().isEmpty()) {
            String originalName = dto.getFileAllegato().getOriginalFilename();

            if (originalName != null) {
                String lowerName = originalName.toLowerCase();
                if (lowerName.endsWith(".txt") || lowerName.endsWith(".jpg") ||
                        lowerName.endsWith(".jpeg") || lowerName.endsWith(".zip")) {

                    ticket.setAllegato(dto.getFileAllegato().getBytes());
                } else {
                    return false;
                }
            }
        }

        ticket.setCliente(autore);
        ticket.setStato(Stato.APERTO);
        ticket.setDataCreazione(LocalDateTime.now());

        ticket.setOperatore(null);
        ticket.setDataInCarico(null);
        ticket.setDataAnnullamento(null);
        ticket.setDataResolved(null);

        ticketRepository.save(ticket);
        return true;
    }



    public boolean deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) return false;

        if (!ticket.getStato().equals(Stato.APERTO)) {
            return false;
        }

        ticket.setDataAnnullamento(LocalDateTime.now());
        ticket.setStato(Stato.ANNULLATO);
        ticketRepository.save(ticket);
        return true;
    }



    public boolean assignTicket(Long ticketId, Operatore operatore) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) return false;

        if (!ticket.getStato().equals(Stato.APERTO)) {
            return false;
        }

        if (operatore == null) {
            return false;
        }

        ticket.setOperatore(operatore);
        ticket.setDataInCarico(LocalDateTime.now());
        ticket.setStato(Stato.IN_CORSO);
        ticketRepository.save(ticket);
        return true;
    }



    public boolean resolveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) return false;

        if (!ticket.getStato().equals(Stato.IN_CORSO)) {
            return false;
        }

        ticket.setDataResolved(LocalDateTime.now());
        ticket.setStato(Stato.RISOLTO);
        ticketRepository.save(ticket);
        return true;
    }


    public boolean releaseTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) return false;

        if (!ticket.getStato().equals(Stato.IN_CORSO)) {
            return false;
        }

        ticket.setOperatore(null);
        ticket.setStato(Stato.APERTO);

        ticketRepository.save(ticket);
        return true;
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