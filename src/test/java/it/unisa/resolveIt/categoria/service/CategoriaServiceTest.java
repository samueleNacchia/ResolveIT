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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaImpl categoriaService;

    // --- TEST DISABLE (Logica "già disabilitata") ---

    @Test
    void disableCategoria_Successo() {
        long id = 1L;
        Categoria cat = new Categoria("Test", true); // È attiva (true)

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        // Esecuzione
        categoriaService.disableCategoria(id);

        // Verifica: deve diventare false e deve essere salvata
        assertFalse(cat.getStato());
        verify(categoriaRepository, times(1)).save(cat);
    }

    @Test
    void disableCategoria_GiaDisabilitata_LanciaEccezione() {
        long id = 1L;
        Categoria cat = new Categoria("Test", false); // È GIÀ disattiva (false)

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        // Esecuzione: mi aspetto l'eccezione definita nel ramo 'else' del tuo codice
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            categoriaService.disableCategoria(id);
        });

        assertEquals("Categoria già disabilitata", ex.getMessage());
        // Importante: save NON deve essere chiamato
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void disableCategoria_NonTrovata() {
        long id = 99L;
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            categoriaService.disableCategoria(id);
        });

        assertEquals("Categoria non trovata per la disattivazione", ex.getMessage());
    }

    // --- TEST ENABLE (Logica "già abilitata") ---

    @Test
    void enableCategoria_Successo() {
        long id = 2L;
        Categoria cat = new Categoria("Test", false); // È disattiva (false)

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        categoriaService.enableCategoria(id);

        // Verifica: diventa true e viene salvata
        assertTrue(cat.getStato());
        verify(categoriaRepository, times(1)).save(cat);
    }

    @Test
    void enableCategoria_GiaAbilitata_LanciaEccezione() {
        long id = 2L;
        Categoria cat = new Categoria("Test", true); // È GIÀ attiva (true)

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            categoriaService.enableCategoria(id);
        });

        assertEquals("Categoria già abilitata", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    // --- TEST UPDATE ---

    @Test
    void updateCategoria_NomeDuplicato() {
        Categoria input = new Categoria("Esistente", true);
        input.setID_C(10);

        // Il codice fornito lancia eccezione se findByNome != null
        when(categoriaRepository.findByNome("Esistente")).thenReturn(new Categoria());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.updateCategoria(input);
        });

        assertEquals("Categoria già esistente", ex.getMessage());
    }
}