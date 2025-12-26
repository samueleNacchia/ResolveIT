package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.springframework.security.core.userdetails.UserDetails;

public interface RegistrazioneService {
    public UserDetails registerUser(RegistraUtenteDTO userDto);
}
