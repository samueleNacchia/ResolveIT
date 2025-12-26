package it.unisa.resolveIt.ticket.control;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.ticket.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // 1. CREAZIONE TICKET (Visualizza Form)
    @GetMapping("/nuovo")
    public String formCreazione(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "creaTicket"; // Pagina HTML con il form
    }

    // 2. CREAZIONE TICKET (Salvataggio dati)
    @PostMapping("/salva")
    public String salvaTicket(@ModelAttribute Ticket ticket) {
        ticketService.creaTicket(ticket);
        return "redirect:/ticket/miei-ticket";
    }

    // 3. VISUALIZZAZIONE TICKET UTENTE
    @GetMapping("/miei-ticket")
    public String visualizzaMieiTicket(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("utenteLoggato");
        model.addAttribute("lista", ticketService.getTicketUtente(cliente));
        return "listaTicketUtente";
    }

    // 4. VISUALIZZAZIONE TICKET DISPONIBILI (Per Operatore)
    @GetMapping("/disponibili")
    public String visualizzaDisponibili(Model model) {
        model.addAttribute("lista", ticketService.getTicketDisponibili());
        return "listaTicketDisponibili";
    }

    // 5. PRESA IN CARICO TICKET
    @PostMapping("/prendi-in-carico/{id}")
    public String prendiInCarico(@PathVariable Long id, HttpSession session) {
        Operatore operatore = (Operatore) session.getAttribute("operatoreLoggato");
        ticketService.prendiInCarico(id, operatore);
        return "redirect:/ticket/in-carico";
    }

    // 6. ANNULLAMENTO TICKET
    @PostMapping("/annulla/{id}")
    public String annulla(@PathVariable Long id) {
        ticketService.annullaTicket(id);
        return "redirect:/ticket/miei-ticket";
    }

    // 7. RISOLUZIONE TICKET
    @PostMapping("/risolvi/{id}")
    public String risolvi(@PathVariable Long id) {
        ticketService.risolviTicket(id);
        return "redirect:/ticket/in-carico";
    }
}
