package it.unisa.resolveIt.ticket.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.ticket.dto.TicketDTO;

import java.io.IOException;
import java.util.List;

public interface TicketService {

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
    boolean addTicket(TicketDTO dto, Cliente autore) throws IOException;


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
    boolean deleteTicket(Long ticketId);



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
    boolean assignTicket(Long ticketId, Operatore operatore);




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
    boolean resolveTicket(Long ticketId);



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
    boolean releaseTicket(Long ticketId);


    Ticket getTicketById(Long id);


    /**
     * Recupera la lista di tutti i ticket inviati da un determinato cliente.
     * * @param cliente l'oggetto {@link Cliente} di cui si vogliono recuperare i ticket.
     * Pre-condizione: il cliente non deve essere nullo e deve essere registrato nel sistema.
     * @return una {@link List} di {@link Ticket} appartenenti al cliente; lista vuota se non ci sono ticket.
     * Post-condizione: ogni ticket nella lista restituita deve avere l'attributo cliente uguale al parametro fornito.
     */
    List<TicketDTO> getTicketUtente(Cliente cliente);



    /**
     * Recupera tutti i ticket attualmente presenti nel sistema con stato "APERTO".
     * Questi sono i ticket che non sono ancora stati presi in carico da alcun operatore.
     * * @return una {@link List} di {@link Ticket} il cui stato è {@code Stato.APERTO}.
     * Post-condizione: tutti i ticket restituiti devono avere {@code stato = "aperto"}.
     */
    List<TicketDTO> getTicketDisponibili();



    /**
     * Recupera la lista dei ticket attualmente in fase di lavorazione da un operatore specifico.
     * * @param operatore l'oggetto {@link Operatore} di cui si vogliono visualizzare i ticket in carico.
     * Pre-condizione: l'operatore non deve essere nullo e deve esistere nel sistema.
     * @return una {@link List} di {@link Ticket} assegnati all'operatore.
     * Post-condizione: ogni ticket nella lista deve avere l'operatore assegnato uguale al parametro e stato "IN_CORSO".
     */
    List<TicketDTO> getTicketInCarico(Operatore operatore);


    List<TicketDTO> getTicketUtenteFiltrati(Cliente cliente, Stato stato, String ordine);
}
