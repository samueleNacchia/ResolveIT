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

    /**
     * Permette ad un Cliente di inviare un nuovo ticket di supporto tecnico.
     * Il metodo convalida i dati in ingresso, inclusa l'esistenza della categoria
     * e il formato dell'allegato (.txt, .jpg, .jpeg, .zip).
     *
     * @param dto l'oggetto {@link TicketDTO} con i dati del ticket.
     * Pre-condizione: ticket non nullo e non già esistente.
     * @param autore il {@link Cliente} proprietario del ticket.
     * @return {@code true} se creato con successo; {@code false} se la categoria
     * non esiste o l'allegato ha un formato non valido.
     * Post-condizione: se {@code true}, il ticket è persistito nel database.
     */
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


    /**
     * Permette ad un Cliente di annullare un ticket di supporto da lui inviato.
     * Il metodo cambia lo stato del ticket in {@code Stato.ANNULLATO} e registra
     * il timestamp dell'operazione. Affinché l'operazione vada a buon fine,
     * il ticket deve essere nello stato "APERTO".
     *
     * @param ticketId l'identificativo univoco del ticket da annullare.
     * Pre-condizione: l'ID deve riferirsi a un ticket esistente nel sistema
     * ({@code TicketDAO.exists}) e il suo stato deve essere "APERTO".
     * @return {@code true} se l'annullamento è avvenuto con successo.
     * @throws java.util.NoSuchElementException se non viene trovato alcun ticket con l'ID fornito.
     * Post-condizione: {@code result = true} implica che il ticket è stato rimosso
     * dalla coda dei ticket attivi o annullato logicamente.
     * @see it.unisa.resolveIt.model.entity.Ticket
     */
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


    /**
     * Permette ad un Operatore di prendere in carico un ticket di supporto per risolverlo.
     * Il metodo associa l'operatore al ticket, aggiorna lo stato e registra
     * il timestamp della presa in carico.
     *
     * @param ticketId l'identificativo del ticket da assegnare.
     * Pre-condizione: il ticket deve esistere e deve essere in stato "APERTO".
     * @param operatore l'oggetto {@link Operatore} che prende in carico il ticket.
     * Pre-condizione: l'operatore non deve essere nullo e deve essere registrato nel sistema.
     * @return {@code true} se l'assegnazione è avvenuta con successo;
     * {@code false} se il ticket non è in stato "APERTO".
     * @throws java.util.NoSuchElementException se il ticketId non corrisponde ad alcun ticket.
     * Post-condizione: se {@code true}, il ticket è associato all'operatore e lo stato
     * è impostato su "IN_CORSO" (assegnato).
     */
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


    /**
     * Permette ad un Operatore di contrassegnare un ticket come risolto.
     * Il metodo aggiorna lo stato del ticket a "RISOLTO" e registra il timestamp
     * dell'avvenuta risoluzione. L'operazione è consentita solo se il ticket è
     * attualmente in fase di lavorazione.
     *
     * @param ticketId l'identificativo del ticket da chiudere.
     * Pre-condizione: il ticket deve esistere e il suo stato deve essere "IN_CORSO".
     * @return {@code true} se il ticket è stato risolto con successo;
     * {@code false} se il ticket non era in uno stato idoneo alla risoluzione.
     * @throws java.util.NoSuchElementException se l'ID fornito non corrisponde a nessun ticket.
     * Post-condizione: se {@code true}, lo stato del ticket nel sistema è "RISOLTO".
     */
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

    /**
     * Permette ad un Operatore di rilasciare un ticket da lui preso in carico e riportarlo come aperto.
     * Il metodo rimuove l'associazione con l'operatore corrente e reimposta lo stato
     * del ticket affinché torni disponibile per altri membri dello staff.
     *
     * @param ticketId l'identificativo del ticket da rilasciare.
     * Pre-condizione: il ticket deve esistere e il suo stato corrente deve essere "IN_CORSO".
     * @return {@code true} se il rilascio è avvenuto con successo;
     * {@code false} se il ticket non era in stato "IN_CORSO".
     * @throws java.util.NoSuchElementException se l'ID fornito non corrisponde a nessun ticket esistente.
     * Post-condizione: se {@code true}, l'operatore assegnato è nullo e lo stato è "APERTO".
     */
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

    /**
     * Recupera la lista di tutti i ticket inviati da un determinato cliente.
     * * @param cliente l'oggetto {@link Cliente} di cui si vogliono recuperare i ticket.
     * Pre-condizione: il cliente non deve essere nullo e deve essere registrato nel sistema.
     * @return una {@link List} di {@link Ticket} appartenenti al cliente; lista vuota se non ci sono ticket.
     * Post-condizione: ogni ticket nella lista restituita deve avere l'attributo cliente uguale al parametro fornito.
     */
    public List<Ticket> getTicketUtente(Cliente cliente) {
        return ticketRepository.findByCliente(cliente);
    }

    /**
     * Recupera tutti i ticket attualmente presenti nel sistema con stato "APERTO".
     * Questi sono i ticket che non sono ancora stati presi in carico da alcun operatore.
     * * @return una {@link List} di {@link Ticket} il cui stato è {@code Stato.APERTO}.
     * Post-condizione: tutti i ticket restituiti devono avere {@code stato = "aperto"}.
     */
    public List<Ticket> getTicketDisponibili() {
        return ticketRepository.findByStato(Stato.APERTO);
    }

    /**
     * Recupera la lista dei ticket attualmente in fase di lavorazione da un operatore specifico.
     * * @param operatore l'oggetto {@link Operatore} di cui si vogliono visualizzare i ticket in carico.
     * Pre-condizione: l'operatore non deve essere nullo e deve esistere nel sistema.
     * @return una {@link List} di {@link Ticket} assegnati all'operatore.
     * Post-condizione: ogni ticket nella lista deve avere l'operatore assegnato uguale al parametro e stato "IN_CORSO".
     */
    public List<Ticket> getTicketInCarico(Operatore operatore) {
        return ticketRepository.findByOperatore(operatore);
    }

}