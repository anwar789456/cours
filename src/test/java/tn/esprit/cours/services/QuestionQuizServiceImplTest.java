package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.QuestionQuiz;
import tn.esprit.cours.entity.QuestionType;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.repository.QuestionQuizRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionQuizServiceImplTest {

    @Mock
    private QuestionQuizRepository questionRepository;

    @InjectMocks
    private QuestionQuizServiceImpl questionService;

    private QuestionQuiz question;

    @BeforeEach
    void setUp() {
        question = new QuestionQuiz();
        question.setId(1L);
        question.setQuestion("What is Java?");
        question.setOptions(List.of("A language", "A framework", "An OS", "A DB"));
        question.setCorrectAnswer("A language");
        question.setExplanation("Java is a programming language.");
        question.setType(QuestionType.MCQ);
    }

    // ── createQuestion ──

    @Test
    void createQuestion_savesAndReturns() {
        when(questionRepository.save(question)).thenReturn(question);

        QuestionQuiz result = questionService.createQuestion(question);

        assertNotNull(result);
        assertEquals("What is Java?", result.getQuestion());
        verify(questionRepository).save(question);
    }

    // ── getQuestionById ──

    @Test
    void getQuestionById_found_returnsQuestion() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        QuestionQuiz result = questionService.getQuestionById(1L);

        assertEquals(1L, result.getId());
        assertEquals(QuestionType.MCQ, result.getType());
    }

    @Test
    void getQuestionById_notFound_throwsRuntimeException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.getQuestionById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllQuestions ──

    @Test
    void getAllQuestions_returnsList() {
        when(questionRepository.findAll()).thenReturn(List.of(question));

        assertEquals(1, questionService.getAllQuestions().size());
    }

    @Test
    void getAllQuestions_empty_returnsEmptyList() {
        when(questionRepository.findAll()).thenReturn(List.of());

        assertTrue(questionService.getAllQuestions().isEmpty());
    }

    // ── getQuestionsByQuizId ──

    @Test
    void getQuestionsByQuizId_returnsList() {
        when(questionRepository.findByQuizId(5L)).thenReturn(List.of(question));

        List<QuestionQuiz> result = questionService.getQuestionsByQuizId(5L);

        assertEquals(1, result.size());
        verify(questionRepository).findByQuizId(5L);
    }

    @Test
    void getQuestionsByQuizId_noResults_returnsEmptyList() {
        when(questionRepository.findByQuizId(99L)).thenReturn(List.of());

        assertTrue(questionService.getQuestionsByQuizId(99L).isEmpty());
    }

    // ── updateQuestion ──

    @Test
    void updateQuestion_found_updatesAllFields() {
        Quiz quiz = new Quiz();
        quiz.setId(2L);

        QuestionQuiz update = new QuestionQuiz();
        update.setQuestion("What is Spring?");
        update.setOptions(List.of("Framework", "Language"));
        update.setCorrectAnswer("Framework");
        update.setExplanation("Spring is a framework.");
        update.setType(QuestionType.TRUE_FALSE);
        update.setQuiz(quiz);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestionQuiz result = questionService.updateQuestion(1L, update);

        assertEquals("What is Spring?", result.getQuestion());
        assertEquals("Framework", result.getCorrectAnswer());
        assertEquals(QuestionType.TRUE_FALSE, result.getType());
        assertSame(quiz, result.getQuiz());
        verify(questionRepository).save(question);
    }

    @Test
    void updateQuestion_notFound_throwsRuntimeException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> questionService.updateQuestion(99L, question));
        verify(questionRepository, never()).save(any());
    }

    // ── deleteQuestion ──

    @Test
    void deleteQuestion_found_deletesSuccessfully() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        questionService.deleteQuestion(1L);

        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestion_notFound_throwsRuntimeException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> questionService.deleteQuestion(99L));
        verify(questionRepository, never()).delete(any());
    }
}
