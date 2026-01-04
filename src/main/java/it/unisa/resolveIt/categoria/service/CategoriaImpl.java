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
            // CORREZIONE: Se la disabilitazione ha successo, salvo.
            if (esistente.get().disable()) {
                categoriaRepository.save(esistente.get());
            } else {
                // ALTRIMENTI (else), se era già disabilitata, lancio l'errore.
                // Senza questo 'else', l'errore veniva lanciato sempre, annullando il salvataggio!
                throw new RuntimeException("Categoria già disabilitata");
            }
        } else {
            throw new RuntimeException("Categoria non trovata per la disattivazione");
        }
    }

    @Transactional
    public void enableCategoria(long id) {
        Optional<Categoria> esistente = categoriaRepository.findById(id);

        if (esistente.isPresent()) {
            // CORREZIONE: Aggiunto else anche qui
            if (esistente.get().enable()) {
                categoriaRepository.save(esistente.get());
            } else {
                throw new RuntimeException("Categoria già abilitata");
            }
        } else {
            throw new RuntimeException("Categoria non trovata per l'aggiornamento");
        }
    }


    @Transactional
    public void addCategoria(Categoria categoria) {
        // Nota: uso getID_C() perché hai confermato che l'entità usa ID_C
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