package it.unisa.resolveIt.model.entity;

import it.unisa.resolveIt.model.enums.Ruolo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
public class Cliente implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\sàèìòùÀÈÌÒ’]{2,30}$")
    private String nome;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\sàèìòùÀÈÌÒ’]{2,30}$")
    private String cognome;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$", message = "Email non valida")
    private String email;

    @NotBlank
    private String password;
    private boolean attivo = true;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Ruolo.CLIENTE.name()));
    }

    public Cliente() {}

    public Cliente(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return email; // restituisce il campo usato per il login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // L'account non scade mai
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // L'account non è mai bloccato
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // La password non scade mai
    }

    @Override
    public boolean isEnabled() {
        return this.attivo;
    }

    public void disable() {
        this.attivo = false;
    }

}
