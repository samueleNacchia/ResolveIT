package it.unisa.resolveIt.ticket.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class TicketDTO {

    private Long id;

    @NotBlank(message = "Il titolo è obbligatorio")
    private String titolo;

    @NotBlank(message = "La descrizione è obbligatoria")
    private String descrizione;

    @NotNull(message = "Seleziona una categoria")
    private Long idCategoria;

    // Campo per il file fisico proveniente dal form
    private MultipartFile fileAllegato;

    public TicketDTO() {
    }

    public TicketDTO(String titolo, String descrizione, Long idCategoria, MultipartFile fileAllegato) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idCategoria = idCategoria;
        this.fileAllegato = fileAllegato;
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
}