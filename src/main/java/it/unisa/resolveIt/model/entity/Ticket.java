package it.unisa.resolveIt.model.entity;

import it.unisa.resolveIt.model.enums.Stato;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
public class Ticket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_T;

    @NotBlank(message = "Il titolo è obbligatorio e non può essere vuoto")
    @Column(nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ '‘\".,!?-]{5,100}$", message = "Titolo non valido")
    private String titolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaStub categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operatore_id")
    private Operatore operatore;


    @Column(updatable = false, nullable = false)
    private LocalDateTime dataCreazione;

    private LocalDateTime dataAnnullamento;
    private LocalDateTime dataInCarico;
    private LocalDateTime dataResolved;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] allegato;

    @NotBlank
    @Column(length = 2000)
    private String testo;

    @Enumerated(EnumType.STRING)
    private Stato stato;

    public Ticket() {}

    public Ticket(String titolo, Cliente cliente, LocalDateTime dataCreazione, LocalDateTime dataAnnullamento, LocalDateTime dataInCarico, LocalDateTime dataResolved, byte[] allegato, String testo) {
        this.titolo = titolo;
        this.cliente = cliente;
        this.dataCreazione = dataCreazione;
        this.dataAnnullamento = dataAnnullamento;
        this.dataInCarico = dataInCarico;
        this.dataResolved = dataResolved;
        this.allegato = allegato;
        this.testo = testo;
    }

    public Long getID_T() {
        return ID_T;
    }

    public void setID_T(Long ID_T) {
        this.ID_T = ID_T;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public LocalDateTime getDataAnnullamento() {
        return dataAnnullamento;
    }

    public void setDataAnnullamento(LocalDateTime dataAnnullamento) {
        this.dataAnnullamento = dataAnnullamento;
    }

    public LocalDateTime getDataInCarico() {
        return dataInCarico;
    }

    public void setDataInCarico(LocalDateTime dataInCarico) {
        this.dataInCarico = dataInCarico;
    }

    public LocalDateTime getDataResolved() {
        return dataResolved;
    }

    public void setDataResolved(LocalDateTime dataResolved) {
        this.dataResolved = dataResolved;
    }

    public byte[] getAllegato() {
        return allegato;
    }

    public void setAllegato(byte[] allegato) {
        this.allegato = allegato;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public CategoriaStub getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaStub categoria) {
        this.categoria = categoria;
    }

    public Operatore getOperatore() {
        return operatore;
    }

    public void setOperatore(Operatore operatore) {
        this.operatore = operatore;
    }

    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        this.stato = stato;
    }
}
