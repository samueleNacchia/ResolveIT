package it.unisa.resolveIt.model.repository;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;



@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCliente(Cliente cliente);
    List<Ticket> findByOperatore(Operatore operatore);
    List<Ticket> findByStato(Stato stato);
    List<Ticket> findByTitoloContainingIgnoreCase(String parola);
    List<Ticket> findByDataResolvedAfter(LocalDateTime data);
}
