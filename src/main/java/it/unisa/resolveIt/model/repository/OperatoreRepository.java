package it.unisa.resolveIt.model.repository;

import it.unisa.resolveIt.model.entity.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatoreRepository extends JpaRepository<Operatore, Long> {
    //save(), findAll(), findById(), delete(), count(), ect.. già implementati
    boolean existsByEmail(String email);
    Operatore findByEmail(String email);
}
