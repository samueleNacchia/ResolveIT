package it.unisa.resolveIt.ticket.control;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.ticket.dto.TicketDTO;
import it.unisa.resolveIt.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // --- DASHBOARD UNIFICATA CLIENTE ---
    @PreAuthorize("hasAuthority('CLIENTE')")
    @GetMapping("/home")
    public String userHome(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        model.addAttribute("lista", ticketService.getTicketUtente(cliente));
        model.addAttribute("ticketDTO", new TicketDTO());
        model.addAttribute("categorie", categoriaRepository.findAll());
        return "user-homepage";
    }

    // --- DASHBOARD UNIFICATA OPERATORE (Per i tuoi Mockup) ---
    @PreAuthorize("hasAuthority('OPERATORE')")
    @GetMapping("/operatore-home")
    public String operatoreHome(Model model, Principal principal) {
        Operatore operatore = operatoreRepository.findByEmail(principal.getName());

        // Alimenta il tab "My Working Ticket"
        model.addAttribute("listaLavoro", ticketService.getTicketInCarico(operatore));
        // Alimenta il tab "Assign New Ticket"
        model.addAttribute("listaAttesa", ticketService.getTicketDisponibili());

        return "operatore-homepage";
    }

    // --- AZIONI CLIENTE ---
    @PreAuthorize("hasAuthority('CLIENTE')")
    @PostMapping("/salva")
    public String salvaTicket(@Valid @ModelAttribute("ticketDTO") TicketDTO ticketDTO,
                              BindingResult result,
                              Principal principal,
                              Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categorie", categoriaRepository.findAll());
            return "user-homepage"; // Torna alla home se ci sono errori
        }
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        ticketService.creaTicketDaDTO(ticketDTO, cliente);
        return "redirect:/ticket/home";
    }

    @PreAuthorize("hasAuthority('CLIENTE')")
    @PostMapping("/elimina/{id}")
    public String eliminaTicket(@PathVariable Long id) {
        ticketService.annullaTicket(id);
        return "redirect:/ticket/home";
    }

    // --- AZIONI OPERATORE ---
    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/prendi/{id}")
    public String prendiInCarico(@PathVariable Long id, Principal principal) {
        Operatore operatore = operatoreRepository.findByEmail(principal.getName());
        ticketService.prendiInCarico(id, operatore);
        return "redirect:/ticket/operatore-home";
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/risolvi/{id}")
    public String risolvi(@PathVariable Long id) {
        ticketService.risolviTicket(id);
        return "redirect:/ticket/operatore-home";
    }
}