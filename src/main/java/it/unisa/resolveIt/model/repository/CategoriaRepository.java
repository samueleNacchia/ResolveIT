package it.unisa.resolveIt.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.unisa.resolveIt.model.entity.Categoria;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @Override
    List<Categoria> findAll();
    Categoria findByNome(String nome);
    List<Categoria> findAllByStato(boolean stato);
    @Override
    Optional<Categoria> findById(Long id);
}
