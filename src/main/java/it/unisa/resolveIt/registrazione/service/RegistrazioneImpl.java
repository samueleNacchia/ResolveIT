package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrazioneImpl implements RegistrazioneService{

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private GestoreRepository gestoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails registerClient(RegistraUtenteDTO dto) {
        validateRegistration(dto);
        String passwordHash = passwordEncoder.encode(dto.getPassword());

        Cliente nuovoCliente = new Cliente(dto.getNome(), dto.getCognome(), dto.getEmail(), passwordHash);
        return clienteRepository.save(nuovoCliente);
    }


    @Override
    @PreAuthorize("hasAuthority('GESTORE')")
    public UserDetails registerOperator(RegistraUtenteDTO dto) {
        validateRegistration(dto);
        String passwordHash = passwordEncoder.encode(dto.getPassword());

        Operatore nuovoOperatore = new Operatore(dto.getNome(), dto.getCognome(), dto.getEmail(), passwordHash);
        return operatoreRepository.save(nuovoOperatore);
    }


    private void validateRegistration(RegistraUtenteDTO dto) {
        if (clienteRepository.existsByEmail(dto.getEmail()) ||
                operatoreRepository.existsByEmail(dto.getEmail()) ||
                gestoreRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email già in uso!");
        }
        if (!dto.getPassword().equals(dto.getConfermaPassword())) {
            throw new RuntimeException("Le password non coincidono!");
        }
    }
    
}
