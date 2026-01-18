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
import org.springframework.web.multipart.MultipartFile;
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
        model.addAttribute("categorie", categoriaRepository.findAllByStato(true));


        model.addAttribute("statoSelezionato", stato);
        model.addAttribute("ordineSelezionato", ordine);

        return "user-homepage";
    }

    @PreAuthorize("hasAuthority('CLIENTE')")
    @PostMapping("/salva")
    public String saveTicket(@Valid @ModelAttribute("ticketDTO") TicketDTO ticketDTO,
                              BindingResult result,
                              Principal principal,
                              Model model, RedirectAttributes redirectAttributes) throws IOException {

        MultipartFile file = ticketDTO.getFileAllegato();
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            if (fileName != null && !fileName.toLowerCase().matches(".*\\.(txt|jpg|jpeg|zip)$")) {
                result.rejectValue("fileAllegato", "error.file", "Formato non consentito (solo .txt, .jpg, .jpeg, .zip)");
            }
            if (file.getSize() > 16777216) {
                result.rejectValue("fileAllegato", "error.file", "Il file supera il limite di 16MB");
            }
        }

        if (result.hasErrors()) {
            Cliente cliente = clienteRepository.findByEmail(principal.getName());
            model.addAttribute("lista", ticketService.getTicketUtente(cliente));
            model.addAttribute("categorie", categoriaRepository.findAll());
            model.addAttribute("ordineSelezionato", "desc");

            model.addAttribute("openTab", "new-ticket");
            return "user-homepage";
        }

        Cliente cliente = clienteRepository.findByEmail(principal.getName());

        try{
            ticketService.addTicket(ticketDTO, cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket creato con successo!");
            return "redirect:/ticket/home";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: creazione ticket fallita");
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
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try{
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket eliminato con successo!");
            return "redirect:/ticket/home";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: eliminazione ticket fallita");
            return "redirect:/ticket/home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/prendi/{id}")
    public String assign(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        Operatore operatore = operatoreRepository.findByEmail(principal.getName());

        try{
            ticketService.assignTicket(id, operatore);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket assegnato con successo!");
            return "redirect:/ticket/home";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: assegnazione ticket fallita");
            return "redirect:/ticket/home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/risolvi/{id}")
    public String resolve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try{
            ticketService.resolveTicket(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket risolto con successo!");
            return "redirect:/ticket/home";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: risoluzione ticket fallita");
            return "redirect:/ticket/home";
        }
    }

    @PreAuthorize("hasAuthority('OPERATORE')")
    @PostMapping("/rilascia/{id}")
    public String release(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try{
            ticketService.releaseTicket(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket rilasciato con successo!");
            return "redirect:/ticket/home";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: rilascio ticket fallito");
            return "redirect:/ticket/home";
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