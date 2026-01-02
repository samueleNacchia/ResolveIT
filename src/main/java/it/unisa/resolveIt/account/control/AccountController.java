package it.unisa.resolveIt.account.control;

import it.unisa.resolveIt.account.service.AccountImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account") //
public class AccountController {

    @Autowired
    private AccountImpl accountImpl;

    @PostMapping("/removeCliente")
    public String removeAccountCliente(@RequestParam("id") long id) {
        try{
        accountImpl.removeAccountCliente(id);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/home"; // Torna alla home dopo l'eliminazione
    }
    @PostMapping("/removeOperatore")
    public String removeAccountOperatore(@RequestParam("id") long id) {
        try {
            accountImpl.removeAccountOperatore(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/home"; // Torna alla home dopo l'eliminazione
    }



}