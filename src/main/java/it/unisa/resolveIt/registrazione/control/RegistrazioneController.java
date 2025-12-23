package it.unisa.resolveIt.registrazione.control;

import it.unisa.resolveIt.registrazione.service.RegistrazioneService;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrazioneController {

    @Autowired
    private RegistrazioneService registrazioneService;

    //GET: Mostra il form vuoto quando apri la pagina
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        //Passiamo un DTO vuoto all'HTML per "ospitare" i dati
        model.addAttribute("utenteDTO", new RegistraUtenteDTO());
        return "registrazione"; // Cerca il file registrazione.html
    }

    //POST: Riceve i dati quando premi "Registrati"
    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("utenteDTO") RegistraUtenteDTO dto, BindingResult result, Model model, HttpServletRequest request) {

        //Controllo validazione formale (es. campi vuoti)
        if (result.hasErrors()) {
            return "registrazione"; // Ritorna alla pagina mostrando gli errori
        }

        try {
            //Chiama il tuo Service esistente
            UserDetails user = registrazioneService.registerUser(dto);

            // Autentico il nuovo utente
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return "redirect:/home";
            /*
            //Successo: Pulisci il form e dai feedback positivo
            model.addAttribute("utenteDTO", new RegistraUtenteDTO()); // Resetta i campi
            model.addAttribute("successMessage", "Registrazione completata con successo!");
             */

        } catch (Exception e) {
            //Errore logico (errore del service)
            model.addAttribute("errorMessage", e.getMessage());
            result.rejectValue("confermaPassword", "error.match", e.getMessage());
            return "registrazione";
        }
    }
}
