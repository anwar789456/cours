package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.QuizCategory;
import tn.esprit.cours.repository.QuizCategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizCategoryServiceImplTest {

    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @InjectMocks
    private QuizCategoryServiceImpl quizCategoryService;

    private QuizCategory category;

    @BeforeEach
    void setUp() {
        category = new QuizCategory();
        category.setId(1L);
        category.setTitle("Grammar");
        category.setDescription("Grammar exercises");
        category.setTotalSets(10);
        category.setIcon("grammar.png");
    }

    // ── createQuizCategory ──

    @Test
    void createQuizCategory_savesAndReturns() {
        when(quizCategoryRepository.save(category)).thenReturn(category);

        QuizCategory result = quizCategoryService.createQuizCategory(category);

        assertNotNull(result);
        assertEquals("Grammar", result.getTitle());
        verify(quizCategoryRepository).save(category);
    }

    // ── getQuizCategoryById ──

    @Test
    void getQuizCategoryById_found_returnsCategory() {
        when(quizCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        QuizCategory result = quizCategoryService.getQuizCategoryById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Grammar", result.getTitle());
    }

    @Test
    void getQuizCategoryById_notFound_throwsRuntimeException() {
        when(quizCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> quizCategoryService.getQuizCategoryById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllQuizCategories ──

    @Test
    void getAllQuizCategories_returnsList() {
        when(quizCategoryRepository.findAll()).thenReturn(List.of(category));

        assertEquals(1, quizCategoryService.getAllQuizCategories().size());
    }

    @Test
    void getAllQuizCategories_empty_returnsEmptyList() {
        when(quizCategoryRepository.findAll()).thenReturn(List.of());

        assertTrue(quizCategoryService.getAllQuizCategories().isEmpty());
    }

    // ── updateQuizCategory ──

    @Test
    void updateQuizCategory_found_updatesAllFields() {
        QuizCategory update = new QuizCategory();
        update.setTitle("Vocabulary");
        update.setDescription("Vocabulary exercises");
        update.setTotalSets(20);
        update.setIcon("vocab.png");

        when(quizCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(quizCategoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizCategory result = quizCategoryService.updateQuizCategory(1L, update);

        assertEquals("Vocabulary", result.getTitle());
        assertEquals("Vocabulary exercises", result.getDescription());
        assertEquals(20, result.getTotalSets());
        assertEquals("vocab.png", result.getIcon());
        verify(quizCategoryRepository).save(category);
    }

    @Test
    void updateQuizCategory_notFound_throwsRuntimeException() {
        when(quizCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> quizCategoryService.updateQuizCategory(99L, category));
        verify(quizCategoryRepository, never()).save(any());
    }

    // ── deleteQuizCategory ──

    @Test
    void deleteQuizCategory_found_deletesSuccessfully() {
        when(quizCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        quizCategoryService.deleteQuizCategory(1L);

        verify(quizCategoryRepository).delete(category);
    }

    @Test
    void deleteQuizCategory_notFound_throwsRuntimeException() {
        when(quizCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> quizCategoryService.deleteQuizCategory(99L));
        verify(quizCategoryRepository, never()).delete(any());
    }
}
