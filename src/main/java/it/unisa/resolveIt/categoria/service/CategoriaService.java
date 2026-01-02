package it.unisa.resolveIt.categoria.service;

import it.unisa.resolveIt.model.entity.Categoria;


public interface CategoriaService {
    /**
    * Disabilita una categoria facendo in modo che non possano essere creati nuovi ticket che fanno riferimento a essa
    * @param id è un intero lungo che contiene la chiave primaria della categoria per trovarla nel database
    */
    public void disableCategoria(long id) ;
    /**
     * Riabilita una categoria facendo in modo che possano essere creati nuovi ticket che fanno riferimento a essa
     * @param id è un intero lungo che contiene la chiave primaria della categoria per trovarla nel database
     */
    public void enableCategoria(long id) ;

    /**
     * Crea una nuova categoria a cui i ticket possono fare riferimento
     * @param categoria è un oggetto da memorizzare in modo persistente se non già presente nel database
     * */
    public void addCategoria(Categoria categoria) ;

    /**
     * Aggiorna una categoria già esistente
     * @param categoria è un oggetto contenente l'identificativo della categoria da modificare, e i parametri di
     * come andrà modificata
     */
    public void updateCategoria(Categoria categoria);


    }
