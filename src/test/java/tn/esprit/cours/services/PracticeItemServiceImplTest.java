package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.PracticeItem;
import tn.esprit.cours.repository.PracticeItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeItemServiceImplTest {

    @Mock
    private PracticeItemRepository practiceItemRepository;

    @InjectMocks
    private PracticeItemServiceImpl practiceItemService;

    private PracticeItem item;

    @BeforeEach
    void setUp() {
        item = new PracticeItem();
        item.setId(1L);
        item.setTitle("Vocabulary");
        item.setDescription("Practice vocabulary");
        item.setColor("#FF5733");
    }

    // ── createPracticeItem ──

    @Test
    void createPracticeItem_savesAndReturns() {
        when(practiceItemRepository.save(item)).thenReturn(item);

        PracticeItem result = practiceItemService.createPracticeItem(item);

        assertNotNull(result);
        assertEquals("Vocabulary", result.getTitle());
        verify(practiceItemRepository).save(item);
    }

    // ── getPracticeItemById ──

    @Test
    void getPracticeItemById_found_returnsItem() {
        when(practiceItemRepository.findById(1L)).thenReturn(Optional.of(item));

        PracticeItem result = practiceItemService.getPracticeItemById(1L);

        assertEquals(1L, result.getId());
        assertEquals("#FF5733", result.getColor());
    }

    @Test
    void getPracticeItemById_notFound_throwsRuntimeException() {
        when(practiceItemRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> practiceItemService.getPracticeItemById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllPracticeItems ──

    @Test
    void getAllPracticeItems_returnsList() {
        when(practiceItemRepository.findAll()).thenReturn(List.of(item));

        assertEquals(1, practiceItemService.getAllPracticeItems().size());
    }

    @Test
    void getAllPracticeItems_empty_returnsEmptyList() {
        when(practiceItemRepository.findAll()).thenReturn(List.of());

        assertTrue(practiceItemService.getAllPracticeItems().isEmpty());
    }

    // ── updatePracticeItem ──

    @Test
    void updatePracticeItem_found_updatesAllFields() {
        PracticeItem update = new PracticeItem();
        update.setTitle("Grammar");
        update.setDescription("Practice grammar");
        update.setColor("#00FF00");

        when(practiceItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(practiceItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PracticeItem result = practiceItemService.updatePracticeItem(1L, update);

        assertEquals("Grammar", result.getTitle());
        assertEquals("Practice grammar", result.getDescription());
        assertEquals("#00FF00", result.getColor());
        verify(practiceItemRepository).save(item);
    }

    @Test
    void updatePracticeItem_notFound_throwsRuntimeException() {
        when(practiceItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> practiceItemService.updatePracticeItem(99L, item));
        verify(practiceItemRepository, never()).save(any());
    }

    // ── deletePracticeItem ──

    @Test
    void deletePracticeItem_found_deletesSuccessfully() {
        when(practiceItemRepository.findById(1L)).thenReturn(Optional.of(item));

        practiceItemService.deletePracticeItem(1L);

        verify(practiceItemRepository).delete(item);
    }

    @Test
    void deletePracticeItem_notFound_throwsRuntimeException() {
        when(practiceItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> practiceItemService.deletePracticeItem(99L));
        verify(practiceItemRepository, never()).delete(any());
    }
}
