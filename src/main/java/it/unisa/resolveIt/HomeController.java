package it.unisa.resolveIt;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal Object principal, Model model) {

        // Verifichiamo se l'utente è loggato
        if (principal == null) {
            return "redirect:/login";
        }

        // Passiamo l'oggetto così com'è (sarà Cliente o Operatore)
        model.addAttribute("utente", principal);

        return "home";
    }
}
