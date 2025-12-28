package it.unisa.resolveIt.categoria.service;


import it.unisa.resolveIt.model.enums.StatoCategoria;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.unisa.resolveIt.model.entity.Categoria;
import java.util.Optional;

@Service
public class CategoriaService{

    @Autowired
    //Categoria repository
    private CategoriaRepository categoriaRepository ;

    @Transactional
    public Boolean disableCategoria(long id) {
        Optional<Categoria> esistente = categoriaRepository.findById(id);
        if (esistente.isPresent()) {
            if(esistente.get().disable())
            {   categoriaRepository.save(esistente.get());
                return true;
            }
            else return false;
        }
        else
            throw new RuntimeException("Categoria non trovata per l'aggiornamento");
    }
    @Transactional
    public Boolean enableCategoria(long id) {
        Optional<Categoria> esistente = categoriaRepository.findById(id);

        if (esistente.isPresent()) {
            if(esistente.get().enable())
            {   categoriaRepository.save(esistente.get());
                return true;
            }
            else return false;
        }
        else
            throw new RuntimeException("Categoria non trovata per l'aggiornamento");
    }


    @Transactional
    public Boolean addCategoria(Categoria categoria) {
        if (categoriaRepository.existsById(categoria.getID_C()))
            return false;
        else
            categoriaRepository.save(categoria);
        return true;
    }


    @Transactional
    public boolean updateCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Dati categoria non validi");
        }

        Optional<Categoria> esistente = categoriaRepository.findById(categoria.getID_C());

        if (esistente.isPresent()) {
            esistente.get().setNome(categoria.getNome());
            categoriaRepository.save(esistente.get());
            return true;
        } else
            return false;

    }
}