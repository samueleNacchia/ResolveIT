package it.unisa.resolveIt.model.entity;


import it.unisa.resolveIt.model.enums.StatoCategoria;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

@Entity
public class Categoria {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    long ID_C;
    @NotBlank @Pattern(regexp = "^[A-ZÀ-ÿa-z\\s]{3,50}$")
    String nome;
    @NotBlank
    StatoCategoria stato;

    public Categoria() {
    }

    public Categoria(String nome, StatoCategoria stato) {

        this.nome = nome;
        this.stato = stato;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return ID_C == categoria.ID_C && Objects.equals(nome, categoria.nome) && stato == categoria.stato;
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "ID_C=" + ID_C +
                ", nome='" + nome + '\'' +
                ", stato=" + stato +
                '}';
    }

    public StatoCategoria getStato() {
        return stato;
    }

    public void setStato(StatoCategoria stato) {
        this.stato = stato;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public long getID_C() {
        return ID_C;
    }

    public void setID_C(int ID_C) {
        this.ID_C = ID_C;
    }
    public boolean disable  (){
        if (stato == StatoCategoria.ATTIVO)
        {   stato = StatoCategoria.DISATTIVATO;
            return true;
        }
        else return false;
    }
    public boolean enable  (){
        if (stato == StatoCategoria.DISATTIVATO)
        {   stato = StatoCategoria.ATTIVO;
            return true;
        }
        else return false;
    }
}
