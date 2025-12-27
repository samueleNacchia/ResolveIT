package it.unisa.resolveIt.account.control;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.account.service.AccountService;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MyProfileController {

    @Autowired
    private AccountService accountService;

    // GET: Carica la pagina del profilo pre-compilando i campi con i dati attuali dell'utente loggato.
    @GetMapping("/my-profile")
    public String showProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        // Recuperiamo i dati reali dal database usando l'email sicura della sessione
        String email = userDetails.getUsername();
        MyProfileDTO currentData = accountService.getUserByEmail(email);

        // Passiamo il DTO pieno al form
        model.addAttribute("utenteDTO", currentData);

        return "my-profile";
    }

    //POST: Elabora la modifica dei dati. Recupera l'email
    @PostMapping("/my-profile")
    public String modifyUserAccount(@Valid @ModelAttribute("utenteDTO") MyProfileDTO dto, BindingResult result, HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().equals(dto.getConfermaPassword())) {
                // Aggiungiamo l'errore SOLO al campo confermaPassword
                result.rejectValue("confermaPassword", "error.match", "Le password non coincidono");
            }
        }

        // Controllo errori di validazione
        if (result.hasErrors()) {
            return "my-profile";
        }

        try {

            dto.setEmail(userDetails.getUsername());
            boolean passwordChanged = accountService.modifyUser(dto);

            if (userDetails instanceof Operatore op) {
                op.setNome(dto.getNome());
                op.setCognome(dto.getCognome());
            } else if (userDetails instanceof Cliente cl) {
                cl.setNome(dto.getNome());
                cl.setCognome(dto.getCognome());
            }

            if (passwordChanged) {
                request.getSession().invalidate();
                redirectAttributes.addFlashAttribute("passwordSuccess", "Password cambiata correttamente.");
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato con successo!");
            return "redirect:/my-profile";

        } catch (Exception e) {
            // Gestione errori logici (Errori del Service)
            model.addAttribute("errorMessage", e.getMessage());
            return "my-profile";
        }
    }
}