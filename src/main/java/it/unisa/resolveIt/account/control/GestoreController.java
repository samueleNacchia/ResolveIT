package it.unisa.resolveIt.account.control;

import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import it.unisa.resolveIt.registrazione.service.RegistrazioneService;
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
public class GestoreController {

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RegistrazioneService registrazioneService;

    @GetMapping("/gestore")
    public String visualizzaDashboardGestore(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        model.addAttribute("utenteDTO", new RegistraUtenteDTO());

        model.addAttribute("operatori", operatoreRepository.findAll());
        model.addAttribute("clienti", clienteRepository.findAll());
        model.addAttribute("categorie", categoriaRepository.findAll());

        return "gestore";
    }

    @PostMapping("/registerOperator")
    public String registerOperatorController(@ModelAttribute("utenteDTO") RegistraUtenteDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {

        try {
            // Logica per il Gestore che crea un Operatore
            registrazioneService.registerOperator(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Operatore creato con successo!");
            return "redirect:/gestore?section=accounts&success=operatorCreated";

        } catch (Exception e) {
            //Errore logico (errore del service)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/gestore?section=accounts&success=false";
        }
    }
}