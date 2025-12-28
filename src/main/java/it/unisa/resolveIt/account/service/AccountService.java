package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountService{

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    /**
     * Rimuove un account (Cliente o Operatore).
     * Corrisponde a: removeAccount(Account account): Boolean
     */
    @Transactional
    public Boolean removeAccountCliente(long id) {

        if (clienteRepository.existsById(id)) {
            Optional<Cliente> cliente =clienteRepository.findById(id) ;
            if (cliente.isPresent() && cliente.get().isEnabled()) {
                cliente.get().disable();
                clienteRepository.save(cliente.get());
                return true;
            }
        }
        return false;
    }


    @Transactional
    public Boolean removeAccountOperatore(long id) {
        if (operatoreRepository.existsById(id)) {
            Optional<Operatore> operatore = operatoreRepository.findById(id);
            if (operatore.isPresent() && operatore.get().isEnabled()) {
                operatore.get().disable();
                operatoreRepository.save(operatore.get());
                return true;
            }
        }
        return false;
    }
    /**
     * Aggiorna i dati di un Cliente.
     * Corrisponde a: updateUser(User account) : void
     */
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

    /**
     * Aggiorna i dati di un Operatore.
     * Corrisponde a: updateOperator(Operator account) : void
     */
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
}