package it.unisa.resolveIt.registrazione.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistraUtenteDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    @Pattern(regexp = "^[a-zA-Z\\sàèìòùÀÈÌÒ’]{2,30}$", message = "Il nome non è valido")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Pattern(regexp = "^[a-zA-Z\\sàèìòùÀÈÌÒ’]{2,30}$", message = "Il cognome non è valido")
    private String cognome;

    @NotBlank(message = "L''email è obbligatoria")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$", message = "Email non valida")
    @Size(max = 100,message = "Email è troppo lunga")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8,message = "La password deve essere di almeno 8 caratteri")
    @Size(max = 64,message = "Email è troppo lunga")
    private String password;

    private String confermaPassword;
    private boolean isClient = true;

    public RegistraUtenteDTO() {
    }

    public RegistraUtenteDTO(String nome, String cognome, String email, String password, String confermaPassword, boolean isClient) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.confermaPassword = confermaPassword;
        this.isClient = isClient;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
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

    public String getConfermaPassword() {
        return confermaPassword;
    }

    public void setConfermaPassword(String confermaPassword) {
        this.confermaPassword = confermaPassword;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean client) {
        isClient = client;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
