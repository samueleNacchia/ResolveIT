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
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;


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


    @Transactional
    public void creaTicketDaDTO(TicketDTO dto, Cliente autore) throws IOException {
        // 1. Inizializzazione
        Ticket ticket = new Ticket();

        // 2. Mapping dati utente (dal DTO)
        ticket.setTitolo(dto.getTitolo());
        ticket.setTesto(dto.getDescrizione());

        // Recupero categoria (necessario perché il Ticket vuole l'oggetto, non l'ID)
        Categoria cat = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        ticket.setCategoria(cat);

        // 3. Gestione Allegato (Conversione da MultipartFile a byte[])
        if (dto.getFileAllegato() != null && !dto.getFileAllegato().isEmpty()) {
            String originalName = dto.getFileAllegato().getOriginalFilename();

            if (originalName != null) {
                String lowerName = originalName.toLowerCase();
                // Controllo robusto sulle estensioni permesse
                if (lowerName.endsWith(".txt") || lowerName.endsWith(".jpg") ||
                        lowerName.endsWith(".jpeg") || lowerName.endsWith(".zip")) {

                    ticket.setAllegato(dto.getFileAllegato().getBytes());
                } else {
                    // Se l'estensione non è tra quelle permesse, lanciamo un'eccezione
                    throw new IllegalArgumentException("Estensione file non valida. Ammessi solo .txt, .jpg, .zip");
                }
            }
        }

        // 4. Mapping dati di sistema (Automatici)
        ticket.setCliente(autore);           // Identificato nel Controller
        ticket.setStato(Stato.APERTO);       // Stato iniziale
        ticket.setDataCreazione(LocalDateTime.now()); // Timestamp preciso

        // 5. Inizializzazione campi futuri (esplicitamente null)
        ticket.setOperatore(null);
        ticket.setDataInCarico(null);
        ticket.setDataAnnullamento(null);
        ticket.setDataResolved(null);

        // 6. Salvataggio
        ticketRepository.save(ticket);
    }
}