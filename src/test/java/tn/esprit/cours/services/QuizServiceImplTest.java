package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.entity.QuizLevel;
import tn.esprit.cours.entity.QuizStatus;
import tn.esprit.cours.repository.QuizRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Java Quiz");
        quiz.setDescription("Test your Java knowledge");
        quiz.setLevel(QuizLevel.BEGINNER);
        quiz.setStatus(QuizStatus.OPEN);
        quiz.setCourseId(10L);
        quiz.setXpReward(50);
        quiz.setArchived(false);
    }

    // ── createQuiz ──

    @Test
    void createQuiz_savesAndReturnsQuiz() {
        when(quizRepository.save(quiz)).thenReturn(quiz);

        Quiz result = quizService.createQuiz(quiz);

        assertNotNull(result);
        assertEquals("Java Quiz", result.getTitle());
        verify(quizRepository).save(quiz);
    }

    // ── getQuizById ──

    @Test
    void getQuizById_existingId_returnsQuiz() {
        when(quizRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(quiz));

        Quiz result = quizService.getQuizById(1L);

        assertEquals(1L, result.getId());
        assertEquals(QuizLevel.BEGINNER, result.getLevel());
        verify(quizRepository).findByIdWithQuestions(1L);
    }

    @Test
    void getQuizById_notFound_throwsRuntimeException() {
        when(quizRepository.findByIdWithQuestions(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> quizService.getQuizById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllQuizzes ──

    @Test
    void getAllQuizzes_returnsList() {
        when(quizRepository.findAllWithQuestions()).thenReturn(List.of(quiz));

        List<Quiz> result = quizService.getAllQuizzes();

        assertEquals(1, result.size());
        assertEquals("Java Quiz", result.get(0).getTitle());
        verify(quizRepository).findAllWithQuestions();
    }

    @Test
    void getAllQuizzes_emptyRepository_returnsEmptyList() {
        when(quizRepository.findAllWithQuestions()).thenReturn(List.of());

        List<Quiz> result = quizService.getAllQuizzes();

        assertTrue(result.isEmpty());
    }

    // ── updateQuiz ──

    @Test
    void updateQuiz_existingId_updatesAllFields() {
        Quiz update = new Quiz();
        update.setTitle("Advanced Quiz");
        update.setDescription("Hard questions");
        update.setLevel(QuizLevel.ADVANCED);
        update.setStatus(QuizStatus.CLOSED);
        update.setCourseId(20L);
        update.setXpReward(200);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        Quiz result = quizService.updateQuiz(1L, update);

        assertEquals("Advanced Quiz", result.getTitle());
        assertEquals(QuizLevel.ADVANCED, result.getLevel());
        assertEquals(QuizStatus.CLOSED, result.getStatus());
        assertEquals(200, result.getXpReward());
        verify(quizRepository).save(quiz);
    }

    @Test
    void updateQuiz_notFound_throwsRuntimeException() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> quizService.updateQuiz(99L, quiz));
        verify(quizRepository, never()).save(any());
    }

    // ── deleteQuiz ──

    @Test
    void deleteQuiz_existingId_deletesSuccessfully() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(1L);

        verify(quizRepository).delete(quiz);
    }

    @Test
    void deleteQuiz_notFound_throwsRuntimeException() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> quizService.deleteQuiz(99L));
        verify(quizRepository, never()).delete(any());
    }

    // ── archiveQuiz ──

    @Test
    void archiveQuiz_setsArchivedTrue() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(quiz)).thenReturn(quiz);

        Quiz result = quizService.archiveQuiz(1L, true);

        assertTrue(result.isArchived());
        verify(quizRepository).save(quiz);
    }

    @Test
    void archiveQuiz_setsArchivedFalse() {
        quiz.setArchived(true);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(quiz)).thenReturn(quiz);

        Quiz result = quizService.archiveQuiz(1L, false);

        assertFalse(result.isArchived());
    }

    @Test
    void archiveQuiz_notFound_throwsRuntimeException() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> quizService.archiveQuiz(99L, true));
        verify(quizRepository, never()).save(any());
    }
}
