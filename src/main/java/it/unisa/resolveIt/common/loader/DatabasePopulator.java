package it.unisa.resolveIt.common.loader;

import it.unisa.resolveIt.model.entity.Categoria;
import it.unisa.resolveIt.model.entity.Cliente;
import it.unisa.resolveIt.model.entity.Gestore;
import it.unisa.resolveIt.model.entity.Operatore;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import it.unisa.resolveIt.model.repository.ClienteRepository;
import it.unisa.resolveIt.model.repository.GestoreRepository;
import it.unisa.resolveIt.model.repository.OperatoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component //dice a Spring di caricare questa classe
public class DatabasePopulator implements CommandLineRunner {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OperatoreRepository operatoreRepository;

    @Autowired
    private GestoreRepository gestoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) throws Exception {

        if(!isRegistered("cliente@test.com")){
            Cliente cliente = new Cliente();
            cliente.setNome("Samuele");
            cliente.setCognome("Nacchia");
            cliente.setEmail("cliente@test.com");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            clienteRepository.save(cliente);
            System.out.println("Cliente registrato!");
        } else {
            System.out.println("Cliente già registrato. Salto il caricamento.");
        }

        if(!isRegistered("operatore@test.com")){
            Operatore operatore = new Operatore();
            operatore.setNome("Andrea");
            operatore.setCognome("Generale");
            operatore.setEmail("operatore@test.com");
            operatore.setPassword(passwordEncoder.encode("operatore123"));
            operatoreRepository.save(operatore);
            System.out.println("Operatore registrato!");
        } else {
            System.out.println("Operatore già registrato. Salto il caricamento.");
        }

        if(!isRegistered("gestore@test.com")){
            Gestore gestore = new Gestore();
            gestore.setEmail("gestore@test.com");
            gestore.setPassword(passwordEncoder.encode("gestore123"));
            gestoreRepository.save(gestore);
            System.out.println("Gestore registrato!");
        } else {
            System.out.println("Gestore già registrato. Salto il caricamento.");
        }

        if (categoriaRepository.count() == 0) {
            Categoria cat1 = new Categoria();
            cat1.setNome("Hardware");
            cat1.enable();
            categoriaRepository.save(cat1);

            Categoria cat2 = new Categoria();
            cat2.setNome("Software");
            cat2.enable();
            categoriaRepository.save(cat2);

            System.out.println("Categorie di base inserite!");
        }

    }

    private boolean isRegistered(String email){
        return (clienteRepository.existsByEmail(email) || operatoreRepository.existsByEmail(email) || gestoreRepository.existsByEmail(email));
    }

}
