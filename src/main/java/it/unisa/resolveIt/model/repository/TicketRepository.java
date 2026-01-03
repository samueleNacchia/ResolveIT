package it.unisa.resolveIt.model.repository;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByClienteOrderByDataCreazioneDesc(Cliente cliente);
    List<Ticket> findByOperatore(Operatore operatore);
    List<Ticket> findByStato(Stato stato);

    @Query("SELECT t FROM Ticket t WHERE t.cliente = :cliente " +
            "AND (:stato IS NULL OR t.stato = :stato) " +
            "ORDER BY " +
            "CASE WHEN :ordine = 'asc' THEN t.dataCreazione END ASC, " +
            "CASE WHEN :ordine = 'desc' THEN t.dataCreazione END DESC")
    List<Ticket> findByClienteAndOptionalStato(@Param("cliente") Cliente cliente,
                                               @Param("stato") Stato stato,
                                               @Param("ordine") String ordine);
}
