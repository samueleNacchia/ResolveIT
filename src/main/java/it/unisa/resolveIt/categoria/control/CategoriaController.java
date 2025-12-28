package it.unisa.resolveIt.categoria.control;

import it.unisa.resolveIt.categoria.service.CategoriaService;
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
    private CategoriaService categoriaService;

    @PostMapping("/disableCategoria")
    public String removeAccountCliente(@RequestParam("id") long id) {
        categoriaService.disableCategoria(id);
        return "redirect:/dashboard"; // Torna alla home dopo l'eliminazione
    }
    @PostMapping("/enableCategoria")
    public String removeAccountOperatore(@RequestParam("id") long id) {
        categoriaService.enableCategoria(id);

        return "redirect:/dashboard"; // Torna alla home dopo l'eliminazione
    }
    // Aggiornamento dati Cliente (User)
    @PostMapping("/updateCategoria")
    public String updateCliente(@ModelAttribute Categoria categoria) {
        try {
            categoriaService.updateCategoria(categoria);
            return "redirect:/home?success"; // Ricarica la pagina home del gest
        } catch (Exception e) {
            return "redirect:/home?error";
        }
    }

}