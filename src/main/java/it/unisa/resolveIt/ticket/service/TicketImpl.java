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
import java.util.stream.Collectors;

@Service
public class TicketImpl implements TicketService{

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private final String titolo_regex = "^[a-zA-Z0-9À-ÿ '‘\".,!?-]{5,100}$";


    private TicketDTO convertToDTO(Ticket t) {
        TicketDTO dto = new TicketDTO();
        dto.setId(t.getID_T());
        dto.setTitolo(t.getTitolo());
        dto.setDescrizione(t.getDescrizione());
        dto.setStato(t.getStato());
        dto.setDataCreazione(t.getDataCreazione());
        dto.setNomeFile(t.getNomeFile());
        if (t.getCategoria() != null) {
            dto.setNomeCategoria(t.getCategoria().getNome());
        }
        return dto;
    }

    @Transactional
    public void addTicket(TicketDTO dto, Cliente autore) throws IOException {
        Ticket ticket = new Ticket();
        ticket.setTitolo(dto.getTitolo());
        ticket.setDescrizione(dto.getDescrizione());

        Categoria cat = categoriaRepository.findById(dto.getIdCategoria()).orElse(null);
        if (cat == null || cat.getStato() == false) {
            throw new RuntimeException("Categoria non valida");
        }
        ticket.setCategoria(cat);

        if (dto.getFileAllegato() != null && !dto.getFileAllegato().isEmpty()) {

            if (dto.getFileAllegato().getSize() > 16 * 1024 * 1024) {
                throw new RuntimeException("Allegato troppo grande");
            }

            String originalName = dto.getFileAllegato().getOriginalFilename();

            if (originalName != null) {
                String lowerName = originalName.toLowerCase();
                if (lowerName.endsWith(".txt") || lowerName.endsWith(".jpg") ||
                        lowerName.endsWith(".jpeg") || lowerName.endsWith(".zip")) {

                    ticket.setAllegato(dto.getFileAllegato().getBytes());
                    ticket.setNomeFile(originalName);
                } else {
                    throw new RuntimeException("Formato allegato non valido");
                }
            }
        }

        if (dto.getTitolo() == null || !dto.getTitolo().matches(titolo_regex)) {
            throw new RuntimeException("Formato titolo non valido");
        }

        if(dto.getDescrizione().length() > 2000){
            throw new RuntimeException("Lunghezza descrizione non valida");
        }

        ticket.setCliente(autore);
        ticket.setStato(Stato.APERTO);
        ticket.setDataCreazione(LocalDateTime.now());

        ticket.setOperatore(null);
        ticket.setDataInCarico(null);
        ticket.setDataAnnullamento(null);
        ticket.setDataResolved(null);

        try {
            ticketRepository.save(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) throw new RuntimeException("Ticket non trovato");

        if (!ticket.getStato().equals(Stato.APERTO)) {
            throw new RuntimeException("Ticket in stato non valido");
        }

        ticket.setDataAnnullamento(LocalDateTime.now());
        ticket.setStato(Stato.ANNULLATO);
        ticketRepository.save(ticket);
    }



    public void assignTicket(Long ticketId, Operatore operatore) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) throw new RuntimeException("Ticket non trovato");

        if (!ticket.getStato().equals(Stato.APERTO)) {
            throw new RuntimeException("Ticket in stato non valido");
        }

        if (operatore == null) {
            throw new RuntimeException("Operatore non valido");
        }

        ticket.setOperatore(operatore);
        ticket.setDataInCarico(LocalDateTime.now());
        ticket.setStato(Stato.IN_CORSO);
        ticketRepository.save(ticket);
    }



    public void resolveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) throw new RuntimeException("Ticket non trovato");

        if (!ticket.getStato().equals(Stato.IN_CORSO)) {
            throw new RuntimeException("Ticket in stato non valido");
        }

        ticket.setDataResolved(LocalDateTime.now());
        ticket.setStato(Stato.RISOLTO);
        ticketRepository.save(ticket);
    }


    public void releaseTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket == null) throw new RuntimeException("Ticket non trovato");

        if (!ticket.getStato().equals(Stato.IN_CORSO)) {
            throw new RuntimeException("Ticket in stato non valido");
        }

        ticket.setOperatore(null);
        ticket.setStato(Stato.APERTO);

        ticketRepository.save(ticket);
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));
    }

    public List<TicketDTO> getTicketUtente(Cliente cliente) {
        return ticketRepository.findByClienteOrderByDataCreazioneDesc(cliente).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<TicketDTO> getTicketDisponibili() {
        return ticketRepository.findByStato(Stato.APERTO).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<TicketDTO> getTicketInCarico(Operatore operatore) {
        return ticketRepository.findByOperatore(operatore).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketUtenteFiltrati(Cliente cliente, Stato stato, String ordine) {
        return ticketRepository.findByClienteAndOptionalStato(cliente, stato, ordine).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}