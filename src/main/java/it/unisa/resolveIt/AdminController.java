package it.unisa.resolveIt;

import it.unisa.resolveIt.model.entity.Gestore;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gestore")
public class AdminController {

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

   @GetMapping("")
    public String visualizzaDashboardGestore(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        model.addAttribute("operatori", operatoreRepository.findAll());
        model.addAttribute("clienti", clienteRepository.findAll());
        model.addAttribute("categorie", categoriaRepository.findAll());

        return "gestore";
    }
}