package it.unisa.resolveIt.ticket.control;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.entity.Ticket;
import it.unisa.resolveIt.model.enums.Stato;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.ticket.dto.TicketDTO;
import it.unisa.resolveIt.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

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

    @PreAuthorize("hasAuthority('CLIENTE')")
    @GetMapping("/home")
    public String userHome(@RequestParam(required = false) Stato stato,
                           @RequestParam(required = false, defaultValue = "desc") String ordine,
                           Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());

        List<TicketDTO> tickets = ticketService.getTicketUtenteFiltrati(cliente, stato, ordine);

        model.addAttribute("lista", tickets);
        model.addAttribute("ticketDTO", new TicketDTO());
        model.addAttribute("categorie", categoriaRepository.findAll());

        // AGGIUNTO PER L'HTML
        model.addAttribute("statoSelezionato", stato);
        model.addAttribute("ordineSelezionato", ordine);

        return "user-homepage";
    }

    @PreAuthorize("hasAuthority('CLIENTE')")
    @PostMapping("/salva")
    public String salvaTicket(@Valid @ModelAttribute("ticketDTO") TicketDTO ticketDTO,
                              BindingResult result,
                              Principal principal,
                              Model model, RedirectAttributes redirectAttributes) throws IOException {

        if (result.hasErrors()) {
            Cliente cliente = clienteRepository.findByEmail(principal.getName());
            model.addAttribute("lista", ticketService.getTicketUtente(cliente));
            model.addAttribute("categorie", categoriaRepository.findAll());
            model.addAttribute("ordineSelezionato", "desc");
            return "user-homepage";
        }

        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        boolean success = ticketService.addTicket(ticketDTO, cliente);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Ticket creato con successo!");
            return "redirect:/ticket/home";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: formato o dimensione file non validi.");
            return "redirect:/ticket/home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @GetMapping("/operatore-home")
    public String operatoreHome(Model model, Principal principal) {
        Operatore operatore = operatoreRepository.findByEmail(principal.getName());

        model.addAttribute("listaLavoro", ticketService.getTicketInCarico(operatore));
        model.addAttribute("listaAttesa", ticketService.getTicketDisponibili());

        return "operatore-homepage";
    }


    @PreAuthorize("hasAuthority('CLIENTE')")
    @PostMapping("/elimina/{id}")
    public String eliminaTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = ticketService.deleteTicket(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Ticket eliminato con successo!");
            return "redirect:/ticket/home";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Operazione fallita");
            return "redirect:/ticket/home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/prendi/{id}")
    public String prendiInCarico(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        Operatore operatore = operatoreRepository.findByEmail(principal.getName());

        try {
            boolean success = ticketService.assignTicket(id, operatore);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Ticket assegnato correttamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Impossibile assegnare il ticket (ID non trovato o già in carico)");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Errore interno: " + e.getMessage());
        }

        return "redirect:/ticket/operatore-home";
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/risolvi/{id}")
    public String risolvi(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = ticketService.resolveTicket(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Ticket risolto con successo!");
            return "redirect:/ticket/operatore-home";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Operazione fallita");
            return "redirect:/ticket/operatore-home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/rilascia/{id}")
    public String rilascia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = ticketService.releaseTicket(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Ticket rilasciato con successo!");
            return "redirect:/ticket/operatore-home";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Operazione fallita");
            return "redirect:/ticket/operatore-home";
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {

        Ticket t = ticketService.getTicketById(id);

        if (t.getAllegato() == null) {
            return ResponseEntity.notFound().build();
        }

        String nome = (t.getNomeFile() != null) ? t.getNomeFile() : "allegato.dat";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(t.getAllegato());
    }


}