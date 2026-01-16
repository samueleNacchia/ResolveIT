package it.unisa.resolveIt.categoria.service;

import it.unisa.resolveIt.model.entity.Categoria;
import it.unisa.resolveIt.model.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test di Unità per la logica di business di {@link CategoriaImpl} e per l'entità {@link Categoria}.
 * <p>
 * Verifica i controlli sui duplicati, la validazione degli input, le transizioni di stato (abilita/disabilita)
 * e la gestione delle eccezioni. Include anche test per garantire la copertura del codice dell'Entità Categoria.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaImpl categoriaService;

    // ===========================================================
    // PARTE 1: TEST DEL SERVICE (CategoriaImpl)

    // --- ADD CATEGORIA ---

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di aggiungere una categoria con un ID già esistente.
     */
    @Test
    void addCategoria_Fallimento_IdEsistente() {
        // Copre il primo ramo dell'OR: if (existsById(...) || ...)
        Categoria cat = new Categoria("Nuova", true);
        cat.setID_C(10);
        when(categoriaRepository.existsById(10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoriaService.addCategoria(cat));
        assertEquals("Categoria già presente nel database", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di aggiungere una categoria con un nome già esistente.
     */
    @Test
    void addCategoria_Fallimento_NomeEsistente() {
        // Copre il secondo ramo dell'OR
        Categoria cat = new Categoria("Nuova", true);
        cat.setID_C(10);
        when(categoriaRepository.existsById(10L)).thenReturn(false);
        when(categoriaRepository.findByNome("Nuova")).thenReturn(new Categoria());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoriaService.addCategoria(cat));
        assertEquals("Categoria già presente nel database", ex.getMessage());
    }

    /**
     * Verifica il corretto salvataggio di una nuova categoria quando ID e nome non sono duplicati.
     */
    @Test
    void addCategoria_Successo() {
        Categoria cat = new Categoria("Nuova", true);
        cat.setID_C(10);
        when(categoriaRepository.existsById(10L)).thenReturn(false);
        when(categoriaRepository.findByNome("Nuova")).thenReturn(null);

        categoriaService.addCategoria(cat);
        verify(categoriaRepository, times(1)).save(cat);
    }

    // --- UPDATE CATEGORIA ---

    /**
     * Verifica che venga lanciata un'eccezione se l'input per l'aggiornamento è null.
     */
    @Test
    void updateCategoria_InputNull() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.updateCategoria(null));
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di aggiornare il nome di una categoria
     * con uno già presente nel database.
     */
    @Test
    void updateCategoria_NomeDuplicato() {
        Categoria input = new Categoria("Esistente", true);
        input.setID_C(5);
        when(categoriaRepository.findByNome("Esistente")).thenReturn(new Categoria());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.updateCategoria(input));
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di aggiornare una categoria non presente per ID.
     */
    @Test
    void updateCategoria_NonTrovataById() {
        Categoria input = new Categoria("Valida", true);
        input.setID_C(99);
        when(categoriaRepository.findByNome("Valida")).thenReturn(null);
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoriaService.updateCategoria(input));
    }

    /**
     * Verifica il corretto aggiornamento di una categoria esistente con dati validi.
     */
    @Test
    void updateCategoria_Successo() {
        Categoria input = new Categoria("NuovoNome", true);
        input.setID_C(1);
        Categoria dbCat = new Categoria("VecchioNome", true);
        dbCat.setID_C(1);

        when(categoriaRepository.findByNome("NuovoNome")).thenReturn(null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(dbCat));

        categoriaService.updateCategoria(input);
        assertEquals("NuovoNome", dbCat.getNome());
        verify(categoriaRepository).save(dbCat);
    }

    // --- DISABLE CATEGORIA ---

    /**
     * Verifica la corretta disabilitazione di una categoria attiva.
     */
    @Test
    void disableCategoria_Successo() {
        long id = 1L;
        Categoria cat = new Categoria("Test", true);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        categoriaService.disableCategoria(id);
        assertFalse(cat.getStato());
        verify(categoriaRepository).save(cat);
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di disabilitare una categoria già disattiva.
     */
    @Test
    void disableCategoria_GiaDisabilitata() {
        long id = 1L;
        Categoria cat = new Categoria("Test", false);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class, () -> categoriaService.disableCategoria(id));
        verify(categoriaRepository, never()).save(any());
    }

    /**
     * Verifica che venga lanciata un'eccezione se la categoria da disabilitare non viene trovata.
     */
    @Test
    void disableCategoria_NonTrovata() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoriaService.disableCategoria(99L));
    }

    // --- ENABLE CATEGORIA ---

    /**
     * Verifica la corretta abilitazione di una categoria disattiva.
     */
    @Test
    void enableCategoria_Successo() {
        long id = 1L;
        Categoria cat = new Categoria("Test", false);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        categoriaService.enableCategoria(id);
        assertTrue(cat.getStato());
        verify(categoriaRepository).save(cat);
    }

    /**
     * Verifica che venga lanciata un'eccezione se si tenta di abilitare una categoria già attiva.
     */
    @Test
    void enableCategoria_GiaAbilitata() {
        long id = 1L;
        Categoria cat = new Categoria("Test", true);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class, () -> categoriaService.enableCategoria(id));
    }

    /**
     * Verifica che venga lanciata un'eccezione se la categoria da abilitare non viene trovata.
     */
    @Test
    void enableCategoria_NonTrovata() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoriaService.enableCategoria(99L));
    }

    // ===========================================================
    // PARTE 2: TEST DELL'ENTITY (Categoria.java)

    /**
     * Test completo sui metodi dell'entità {@link Categoria} per garantire la massima copertura del codice (Branch Coverage).
     * <p>
     * Verifica:
     * <ul>
     * <li>Il metodo <code>equals()</code> e <code>hashCode()</code> con vari scenari (stesso oggetto, null, classe diversa, campi diversi).</li>
     * <li>Il metodo <code>toString()</code>.</li>
     * <li>I metodi getter e setter.</li>
     * <li>La logica interna booleana dei metodi <code>enable()</code> e <code>disable()</code> senza passare dal service.</li>
     * </ul>
     * </p>
     */
    @Test
    void testEntityMethods_FullCoverage() {
        // Setup istanze
        Categoria c1 = new Categoria("Hardware", true);
        c1.setID_C(1);

        Categoria c2 = new Categoria("Hardware", true);
        c2.setID_C(1); // Identico a c1

        Categoria cDiffId = new Categoria("Hardware", true);
        cDiffId.setID_C(2); // ID diverso

        Categoria cDiffNome = new Categoria("Software", true);
        cDiffNome.setID_C(1); // Nome diverso

        Categoria cDiffStato = new Categoria("Hardware", false);
        cDiffStato.setID_C(1); // Stato diverso

        // 1. Test Equals
        assertEquals(c1, c1);       // Stesso oggetto in memoria
        assertEquals(c1, c2);       // Oggetti diversi ma contenuto uguale
        assertNotEquals(c1, null);  // Confronto con null
        assertNotEquals(c1, new Object()); // Confronto con classe diversa
        assertNotEquals(c1, cDiffId);    // ID diverso
        assertNotEquals(c1, cDiffNome);  // Nome diverso
        assertNotEquals(c1, cDiffStato); // Stato diverso

        // 2. Test ToString (Copertura righe)
        String s = c1.toString();
        assertNotNull(s);
        assertTrue(s.contains("Hardware"));

        // 3. Test Getter e Setter
        c1.setNome("Nuovo");
        assertEquals("Nuovo", c1.getNome());
        c1.setStato(false);
        assertFalse(c1.getStato());

        // 5. Test logica interna boolean (Enable/Disable diretti)

        Categoria logica = new Categoria("Logica", true);
        assertTrue(logica.disable()); // Da true a false
        assertFalse(logica.disable()); // Da false resta false (ramo else)

        assertTrue(logica.enable()); // Da false a true
        assertFalse(logica.enable()); // Da true resta true (ramo else)
    }
}