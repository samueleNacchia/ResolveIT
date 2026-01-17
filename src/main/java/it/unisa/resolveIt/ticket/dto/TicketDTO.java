package it.unisa.resolveIt.ticket.dto;

import it.unisa.resolveIt.model.enums.Stato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;


public class TicketDTO {

    private Long id;

    @NotBlank(message = "Il titolo è obbligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ '‘\".,!?-]{5,100}$", message = "Il titolo deve essere tra 5 e 100 caratteri validi")
    private String titolo;

    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(max = 2000, message = "La descrizione deve avere massimo 2000 caratteri")
    private String descrizione;


    private Long idCategoria;

    private MultipartFile fileAllegato;


    private String nomeFile;
    private Stato stato;
    private LocalDateTime dataCreazione;
    private String nomeCategoria;

    public TicketDTO() {
    }

    public TicketDTO(Long id, String titolo, String descrizione, Long idCategoria, MultipartFile fileAllegato, String nomeFile, Stato stato, LocalDateTime dataCreazione, String nomeCategoria) {
        this.id = id;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idCategoria = idCategoria;
        this.fileAllegato = fileAllegato;
        this.nomeFile = nomeFile;
        this.stato = stato;
        this.dataCreazione = dataCreazione;
        this.nomeCategoria = nomeCategoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public MultipartFile getFileAllegato() {
        return fileAllegato;
    }

    public void setFileAllegato(MultipartFile fileAllegato) {
        this.fileAllegato = fileAllegato;
    }

    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        this.stato = stato;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public String getNomeFile() {
        return nomeFile;
    }

    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }
}