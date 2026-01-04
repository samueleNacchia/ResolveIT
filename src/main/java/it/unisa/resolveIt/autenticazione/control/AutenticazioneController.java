package it.unisa.resolveIt.autenticazione.control;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class AutenticazioneController {

    // Mostra solo il form HTML
    @GetMapping("/login")
    public String showLoginForm(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

            if (roles.contains("OPERATORE")) {
                return "redirect:/ticket/operatore-home";
            }
            if (roles.contains("GESTORE")) {
                return "redirect:/gestore";
            }
            if (roles.contains("CLIENTE")) {
                return "redirect:/ticket/home";
            }
        }
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
