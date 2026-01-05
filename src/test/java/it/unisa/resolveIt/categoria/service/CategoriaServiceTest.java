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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaImpl categoriaService;

    // ===========================================================
    // PARTE 1: TEST DEL SERVICE (CategoriaImpl)

    // --- ADD CATEGORIA ---
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
    @Test
    void updateCategoria_InputNull() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.updateCategoria(null));
    }

    @Test
    void updateCategoria_NomeDuplicato() {
        Categoria input = new Categoria("Esistente", true);
        input.setID_C(5);
        when(categoriaRepository.findByNome("Esistente")).thenReturn(new Categoria());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.updateCategoria(input));
    }

    @Test
    void updateCategoria_NonTrovataById() {
        Categoria input = new Categoria("Valida", true);
        input.setID_C(99);
        when(categoriaRepository.findByNome("Valida")).thenReturn(null);
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoriaService.updateCategoria(input));
    }

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
    @Test
    void disableCategoria_Successo() {
        long id = 1L;
        Categoria cat = new Categoria("Test", true);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        categoriaService.disableCategoria(id);
        assertFalse(cat.getStato());
        verify(categoriaRepository).save(cat);
    }

    @Test
    void disableCategoria_GiaDisabilitata() {
        long id = 1L;
        Categoria cat = new Categoria("Test", false);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class, () -> categoriaService.disableCategoria(id));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void disableCategoria_NonTrovata() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoriaService.disableCategoria(99L));
    }

    // --- ENABLE CATEGORIA ---
    @Test
    void enableCategoria_Successo() {
        long id = 1L;
        Categoria cat = new Categoria("Test", false);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        categoriaService.enableCategoria(id);
        assertTrue(cat.getStato());
        verify(categoriaRepository).save(cat);
    }

    @Test
    void enableCategoria_GiaAbilitata() {
        long id = 1L;
        Categoria cat = new Categoria("Test", true);
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class, () -> categoriaService.enableCategoria(id));
    }

    @Test
    void enableCategoria_NonTrovata() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoriaService.enableCategoria(99L));
    }

    // ===========================================================
    // PARTE 2: TEST DELL'ENTITY (Categoria.java)


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