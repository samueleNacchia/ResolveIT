package it.unisa.resolveIt.account.control;

import it.unisa.resolveIt.account.service.AccountService;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account") //
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/removeCliente")
    public String removeAccountCliente(@RequestParam("id") long id) {
        accountService.removeAccountCliente(id);
        return "redirect:/dashboard"; // Torna alla home dopo l'eliminazione
    }
    @PostMapping("/removeOperatore")
    public String removeAccountOperatore(@RequestParam("id") long id) {
        accountService.removeAccountOperatore(id);

        return "redirect:/dashboard"; // Torna alla home dopo l'eliminazione
    }
    // Aggiornamento dati Cliente (User)
    @PostMapping("/updateCliente")
    public String updateCliente(@ModelAttribute Cliente cliente) {
        try {
            accountService.updateCliente(cliente);
            return "redirect:/profilo?success"; // Ricarica la pagina profilo con messaggio successo
        } catch (Exception e) {
            return "redirect:/profilo?error";
        }
    }

    // Aggiornamento dati Operatore
    @PostMapping("/updateOperatore")
    public String updateOperatore(@ModelAttribute Operatore operatore) {
        try {
            accountService.updateOperatore(operatore);
            return "redirect:/profiloOperatore?success";
        } catch (Exception e) {
            return "redirect:/profiloOperatore?error";
        }
    }
}