package it.unisa.resolveIt.registrazione.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import it.unisa.resolveIt.registrazione.dto.RegistraUtenteDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
    public UserDetails registerUser(RegistraUtenteDTO userDto){

        String email = userDto.getEmail();

        if (clienteRepository.existsByEmail(email) || operatoreRepository.existsByEmail(email) || gestoreRepository.existsByEmail(email)) {
            throw new RuntimeException("Email già in uso!");
        }

        if (!userDto.getPassword().equals(userDto.getConfermaPassword())) {
            throw new RuntimeException("Le password non coincidono!");
        }

        String nome = userDto.getNome();
        String cognome = userDto.getCognome();
        String passwordHash = passwordEncoder.encode(userDto.getPassword());

        UserDetails savedUser;

        if(userDto.isClient()){
            Cliente newClient = new Cliente(nome, cognome, email, passwordHash);
            savedUser = clienteRepository.save(newClient);
        } else {
            Operatore newOperator = new Operatore(nome, cognome, email, passwordHash);
            savedUser = operatoreRepository.save(newOperator);
        }

        return savedUser;
    }
    
}
