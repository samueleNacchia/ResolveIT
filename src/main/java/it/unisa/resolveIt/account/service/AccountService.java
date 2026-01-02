package it.unisa.resolveIt.account.service;

import it.unisa.resolveIt.account.dto.MyProfileDTO;

public interface AccountService {
     /**
     * Rimuove un account (Cliente).
     * Corrisponde a: removeAccountCliente(Cliente account): Boolean
     * @param id intero lungo contenente un campo univico per trovare uno specifico utente nel database.
     */
    public void removeAccountCliente(long id) ;
     /**
      * Rimuove un account (Operatore).
      * Corrisponde a: removeAccountOperatore(Operatore account): Boolean
      *
      * @param id intero lungo contenente un campo univico per trovare uno specifico utente nel database.
      */
    public void removeAccountOperatore(long id) ;
     /**
     * Aggiorna i dati di un Cliente.
     * Corrisponde a: updateUser(User account) : void
     * @param account oggetto che contiene i dati dell'utente da modificare

    Public void updateCliente(Cliente account) ;
     /**
     * Aggiorna i dati di un Operatore.
     * Corrisponde a: updateOperator(Operator account) : void

    public void updateOperatore(Operatore account) ;
    */
      /**
     * Restituisce i dati di un utente
     * @param email Stringa contenente un campo univico per ottenere i dati di uno specifico utente.
     */
    public MyProfileDTO getUserByEmail(String email) ;

    /**
     * Modifica i dati (nome,cognome e password) di un utente (cliente od operatore)
     * Corrisponde a updateUser e updateOpertore
     * @param userDto oggetto che contiene i dati dell'utente da modificare
     * @return Un boolean {@link Boolean} per segnalare se la password è stata modificata.
     */
    public boolean modifyUser(MyProfileDTO userDto) ;

    }
