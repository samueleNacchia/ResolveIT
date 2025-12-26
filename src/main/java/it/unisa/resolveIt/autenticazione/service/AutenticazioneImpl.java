package it.unisa.resolveIt.autenticazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Gestore;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticazioneImpl implements AutenticazioneService, UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private GestoreRepository gestoreRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Cerca gestore
        Gestore gestore = gestoreRepository.findByEmail(email);
        if (gestore != null) {
            return gestore;
        }

        // Cerca operatore
        Operatore operatore = operatoreRepository.findByEmail(email);
        if (operatore != null) {
            return operatore;
        }

        // Cerca cliente
        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente != null) {
            return cliente;
        }

        throw new UsernameNotFoundException("Utente non trovato con email: " + email);
    }

}
