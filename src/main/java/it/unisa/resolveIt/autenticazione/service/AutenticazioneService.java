package it.unisa.resolveIt.autenticazione.service;

import org.springframework.security.core.userdetails.UserDetailsService;
/**
 * Servizio per il caricamento degli utenti ai fini dell'autenticazione.
 */
public interface AutenticazioneService extends UserDetailsService {
}
