package it.unisa.resolveIt.registrazione.control;

import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import it.unisa.resolveIt.registrazione.service.RegistrazioneService;
import jakarta.servlet.http.HttpServletRequest;
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
        return "registrazione";
    }

    //POST: Riceve i dati quando premi "Registrati"

    @PostMapping("/register")
    public String registerClientController(@Valid @ModelAttribute("utenteDTO") RegistraUtenteDTO dto, BindingResult result, Model model, HttpServletRequest request) {

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().equals(dto.getConfermaPassword())) {
                // Aggiungiamo l'errore SOLO al campo confermaPassword
                result.rejectValue("confermaPassword", "error.match", "Le password non coincidono!");
            }
        }

        //Controllo validazione formale (es. campi vuoti)
        if (result.hasErrors()) {
            return "registrazione";
        }

        try {
            // Logica per l'utente anonimo che si registra come Cliente
            UserDetails user = registrazioneService.registerClient(dto);

            // Auto-login per il nuovo Cliente
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return "redirect:/ticket/home";

        } catch (Exception e) {
            //Errore logico (errore del service)
            model.addAttribute("errorMessage", e.getMessage());
            return "registrazione";
        }
    }

}
