package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountImpl implements AccountService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public void removeAccountCliente(long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);

        if (cliente.isPresent()) {
            if (cliente.get().isEnabled()) {
                cliente.get().disable();
                clienteRepository.save(cliente.get());
            } else {
                throw new RuntimeException("Account cliente già disabilitato");
            }
        } else {
            throw new RuntimeException("Account cliente non trovato");
        }
    }


    @Transactional
    public void removeAccountOperatore(long id) {
        Optional<Operatore> operatore = operatoreRepository.findById(id);

        if (operatore.isPresent()) {
            if (operatore.get().isEnabled()) {
                operatore.get().disable();
                operatoreRepository.save(operatore.get());
            } else {
                throw new RuntimeException("Account operatore già disabilitato");
            }
        } else {
            throw new RuntimeException("Account operatore non trovato");
        }
    }


    public MyProfileDTO getUserByEmail(String email) {
        Operatore operatore = operatoreRepository.findByEmail(email);

        if (operatore == null) {
            Cliente cliente = clienteRepository.findByEmail(email);
            if (cliente == null) {
                throw new RuntimeException("Email non registrata!");
            }

            MyProfileDTO dto = new MyProfileDTO();
            dto.setNome(cliente.getNome());
            dto.setCognome(cliente.getCognome());
            dto.setClient(true);

            return dto;
        }

        MyProfileDTO dto = new MyProfileDTO();
        dto.setNome(operatore.getNome());
        dto.setCognome(operatore.getCognome());
        dto.setClient(false);

        return dto;
    }

    @Transactional
    public boolean modifyUser(MyProfileDTO userDto) {
        String email = userDto.getEmail();
        String nome = userDto.getNome();
        String cognome = userDto.getCognome();
        String nuovaPassword = userDto.getPassword();
        String confermaNuovaPassword = userDto.getConfermaPassword();
        boolean passwordChanged = false;

        Operatore operatore = operatoreRepository.findByEmail(email);

        if (operatore != null) {
            operatore.setNome(nome);
            operatore.setCognome(cognome);

            if (nuovaPassword != null && !nuovaPassword.isEmpty()) {
                if (!nuovaPassword.equals(confermaNuovaPassword)) {
                    throw new RuntimeException("Le password non coincidono!");
                }
                operatore.setPassword(passwordEncoder.encode(nuovaPassword));
                passwordChanged = true;
            }
            return passwordChanged;
        }

        Cliente cliente = clienteRepository.findByEmail(email);

        if (cliente != null) {
            cliente.setNome(nome);
            cliente.setCognome(cognome);

            if (nuovaPassword != null && !nuovaPassword.isEmpty()) {
                if (!nuovaPassword.equals(confermaNuovaPassword)) {
                    throw new RuntimeException("Le password non coincidono!");
                }
                cliente.setPassword(passwordEncoder.encode(nuovaPassword));
                passwordChanged = true;
            }
            return passwordChanged;
        }

        throw new RuntimeException("L'utente non è autorizzato alla modifica o non esiste.");
    }
}