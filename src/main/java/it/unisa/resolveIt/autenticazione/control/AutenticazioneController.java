package it.unisa.resolveIt.autenticazione.control;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticazioneController {

    // Mostra solo il form HTML
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }


    @GetMapping("/dashboard")
    public String dashboardRedirect(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("OPERATORE"))) {
            return "redirect:/ticket/operatore-home";
        }
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("GESTORE"))) {
            return "redirect:/gestore";
        }
        return "redirect:/ticket/home";
    }
}
