package it.unisa.resolveIt.model.repository;

import it.unisa.resolveIt.model.entity.Gestore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GestoreRepository extends JpaRepository<Gestore, Long> {
    //save(), findAll(), findById(), delete(), count(), ect.. già implementati
    boolean existsByEmail(String email);
    Gestore findByEmail(String email);
}