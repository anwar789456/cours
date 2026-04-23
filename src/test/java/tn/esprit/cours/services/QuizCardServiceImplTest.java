package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.QuizCard;
import tn.esprit.cours.entity.QuizCardStatus;
import tn.esprit.cours.repository.QuizCardRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizCardServiceImplTest {

    @Mock
    private QuizCardRepository quizCardRepository;

    @InjectMocks
    private QuizCardServiceImpl quizCardService;

    private QuizCard card;

    @BeforeEach
    void setUp() {
        card = new QuizCard();
        card.setId(1L);
        card.setTitle("Beginner Vocabulary");
        card.setTotalQuestions(20);
        card.setLevel("BEGINNER");
        card.setProgress(50);
        card.setStatus(QuizCardStatus.CONTINUE);
        card.setIcon("vocab.png");
        card.setXpRequired(0);
    }

    // ── createQuizCard ──

    @Test
    void createQuizCard_savesAndReturns() {
        when(quizCardRepository.save(card)).thenReturn(card);

        QuizCard result = quizCardService.createQuizCard(card);

        assertNotNull(result);
        assertEquals("Beginner Vocabulary", result.getTitle());
        verify(quizCardRepository).save(card);
    }

    // ── getQuizCardById ──

    @Test
    void getQuizCardById_found_returnsCard() {
        when(quizCardRepository.findById(1L)).thenReturn(Optional.of(card));

        QuizCard result = quizCardService.getQuizCardById(1L);

        assertEquals(1L, result.getId());
        assertEquals(QuizCardStatus.CONTINUE, result.getStatus());
    }

    @Test
    void getQuizCardById_notFound_throwsRuntimeException() {
        when(quizCardRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> quizCardService.getQuizCardById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllQuizCards ──

    @Test
    void getAllQuizCards_returnsList() {
        when(quizCardRepository.findAll()).thenReturn(List.of(card));

        assertEquals(1, quizCardService.getAllQuizCards().size());
    }

    @Test
    void getAllQuizCards_empty_returnsEmptyList() {
        when(quizCardRepository.findAll()).thenReturn(List.of());

        assertTrue(quizCardService.getAllQuizCards().isEmpty());
    }

    // ── updateQuizCard ──

    @Test
    void updateQuizCard_found_updatesAllFields() {
        QuizCard update = new QuizCard();
        update.setTitle("Advanced Grammar");
        update.setTotalQuestions(30);
        update.setLevel("ADVANCED");
        update.setProgress(10);
        update.setStatus(QuizCardStatus.START);
        update.setIcon("grammar.png");
        update.setXpRequired(500);

        when(quizCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(quizCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizCard result = quizCardService.updateQuizCard(1L, update);

        assertEquals("Advanced Grammar", result.getTitle());
        assertEquals(30, result.getTotalQuestions());
        assertEquals("ADVANCED", result.getLevel());
        assertEquals(QuizCardStatus.START, result.getStatus());
        assertEquals(500, result.getXpRequired());
        verify(quizCardRepository).save(card);
    }

    @Test
    void updateQuizCard_notFound_throwsRuntimeException() {
        when(quizCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> quizCardService.updateQuizCard(99L, card));
        verify(quizCardRepository, never()).save(any());
    }

    // ── deleteQuizCard ──

    @Test
    void deleteQuizCard_found_deletesSuccessfully() {
        when(quizCardRepository.findById(1L)).thenReturn(Optional.of(card));

        quizCardService.deleteQuizCard(1L);

        verify(quizCardRepository).delete(card);
    }

    @Test
    void deleteQuizCard_notFound_throwsRuntimeException() {
        when(quizCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> quizCardService.deleteQuizCard(99L));
        verify(quizCardRepository, never()).delete(any());
    }
}
