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

        if (clienteRepository.existsById(id)) {
            Optional<Cliente> cliente =clienteRepository.findById(id) ;
            if (cliente.isPresent() && cliente.get().isEnabled()) {
                cliente.get().disable();
                clienteRepository.save(cliente.get());
            }
        }
        throw new RuntimeException("Account cliente non registrato");
    }


    @Transactional
    public void removeAccountOperatore(long id) {
        if (operatoreRepository.existsById(id)) {
            Optional<Operatore> operatore = operatoreRepository.findById(id);
            if (operatore.isPresent() && operatore.get().isEnabled()) {
                operatore.get().disable();
                operatoreRepository.save(operatore.get());
            }
        }
        throw new RuntimeException("Account operatore non registrato");
    }
/*
    @Transactional
    public void updateCliente(Cliente account) {
        // Pre-condizione: account <> null e deve esistere nel DB
        if (account == null) {
            throw new IllegalArgumentException("Dati cliente non validi");
        }

        Optional<Cliente> esistente = clienteRepository.findById(account.getId());

        if (esistente.isPresent()) {
            esistente.get().setNome(account.getNome());
            esistente.get().setCognome(account.getCognome());
            esistente.get().setEmail(account.getEmail());
            esistente.get().setPassword(account.getPassword());
            clienteRepository.save(esistente.get());
        } else {
            throw new RuntimeException("Cliente non trovato per l'aggiornamento");
        }
    }


    @Transactional
    public void updateOperatore(Operatore account) {
        if (account == null) {
            throw new IllegalArgumentException("Dati operatore non validi");
        }

        Optional<Operatore> esistente = operatoreRepository.findById(account.getId());

        if (esistente.isPresent()) {
            esistente.get().setNome(account.getNome());
            esistente.get().setCognome(account.getCognome());
            esistente.get().setEmail(account.getEmail());
            esistente.get().setPassword(account.getPassword());
            operatoreRepository.save(esistente.get());
        } else {
            throw new RuntimeException("Operatore non trovato per l'aggiornamento");
        }
    }
*/
    public MyProfileDTO getUserByEmail(String email) {

        // Cerchiamo l'utente nel DB. Se non esiste, lanciamo un'eccezione.
        Operatore operatore = operatoreRepository.findByEmail(email);

        if (operatore == null) {
            Cliente cliente = clienteRepository.findByEmail(email);
            if (cliente == null) {
                throw new RuntimeException("Email non registrata!");
            }

            // DTO popolato con i dati del cliente
            MyProfileDTO dto = new MyProfileDTO();
            dto.setNome(cliente.getNome());
            dto.setCognome(cliente.getCognome());
            dto.setClient(true);

            return dto;
        }

        // DTO popolato con i dati dell'operatore
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

        // Se non è un Operatore, cerchiamo tra i Clienti
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