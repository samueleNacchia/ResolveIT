package it.unisa.resolveIt.model.repository;

import it.unisa.resolveIt.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    //save(), findAll(), findById(), delete(), count(), ect.. già implementati
    boolean existsByEmail(String email);
    Cliente findByEmail(String email);
}
