package it.unisa.resolveIt;

import it.unisa.resolveIt.model.entity.Gestore;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/gestore")
    public String adminDashboard(@AuthenticationPrincipal Gestore gestore, Model model) {

        // Verifichiamo se l'utente è loggato
        if (gestore == null) {
            return "redirect:/login";
        }

        model.addAttribute("gestore", gestore);
        return "home-gestore";
    }
}
