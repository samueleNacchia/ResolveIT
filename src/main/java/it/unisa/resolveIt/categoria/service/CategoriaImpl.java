package it.unisa.resolveIt.categoria.service;

import it.unisa.resolveIt.model.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.unisa.resolveIt.model.entity.Categoria;
import java.util.Optional;

@Service
public class CategoriaImpl implements  CategoriaService{

    @Autowired
    //Categoria repository
    private CategoriaRepository categoriaRepository ;

    @Transactional
    public void disableCategoria(long id) {
        Optional<Categoria> esistente = categoriaRepository.findById(id);
        if (esistente.isPresent()) {
            if(esistente.get().disable())
            {   categoriaRepository.save(esistente.get()); }
            throw new RuntimeException("Categoria già disabilitata");
        }
        else
            throw new RuntimeException("Categoria non trovata per la disattivazione");
    }

    @Transactional
    public void enableCategoria(long id) {
        Optional<Categoria> esistente = categoriaRepository.findById(id);

        if (esistente.isPresent()) {
            if(esistente.get().enable())
            {   categoriaRepository.save(esistente.get()); }
            throw new RuntimeException("Categoria già abilitata");
        }
        else
            throw new RuntimeException("Categoria non trovata per l'aggiornamento");
    }


    @Transactional
    public void addCategoria(Categoria categoria) {
        if (categoriaRepository.existsById(categoria.getID_C()) || (categoriaRepository.findByNome(categoria.getNome()) != null))
            throw new RuntimeException("Categoria già presente nel database");
        else
            categoriaRepository.save(categoria);
    }


    @Transactional
    public void updateCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Dati categoria non validi");
        }

        Optional<Categoria> esistente = categoriaRepository.findById(categoria.getID_C());

        if (esistente.isPresent()) {
            esistente.get().setNome(categoria.getNome());
            categoriaRepository.save(esistente.get());

        } else
            throw new RuntimeException("Categoria non trovata per l'aggiornamento");

    }
}