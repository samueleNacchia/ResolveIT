package it.unisa.resolveIt.autenticazione.control;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticazioneController {

    // Mostra solo il form HTML
    @GetMapping("/login-form")
    public String showLoginForm(Authentication auth) {
        return "login";
    }
}
