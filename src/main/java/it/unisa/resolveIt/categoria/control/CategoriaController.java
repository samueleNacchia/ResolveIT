package it.unisa.resolveIt.categoria.control;

import it.unisa.resolveIt.categoria.service.CategoriaImpl;
import it.unisa.resolveIt.model.entity.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/categoria") // Prefisso per tutte le rotte di questo controller
public class CategoriaController {

    @Autowired
    private CategoriaImpl categoriaImpl;

    @PostMapping("/disableCategoria")
    public String disableCategoria(@RequestParam("id") long id) {
        try {
            categoriaImpl.disableCategoria(id);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "redirect:/dashboard"; // Torna alla home dopo l'eliminazione
    }
    @PostMapping("/enableCategoria")
    public String enableCategoria(@RequestParam("id") long id) {
        try {
            categoriaImpl.enableCategoria(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/home"; // Torna alla home dopo l'eliminazione
    }
    // Aggiornamento dati Cliente (User)
    @PostMapping("/updateCategoria")
    public String updateCategoria(@ModelAttribute Categoria categoria) {
        try {
            categoriaImpl.updateCategoria(categoria);
            return "redirect:/home?success"; // Ricarica la pagina home del gest
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/home?error";
        }
    }
    @PostMapping("/addCategoria")
    public String addCategoria(@ModelAttribute Categoria categoria) {
        try {
            categoriaImpl.addCategoria(categoria);
            return "redirect:/home?success"; // Ricarica la pagina home del gestore
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/home?error";
        }
    }

}