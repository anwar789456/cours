package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements IQuizService {

    private final QuizRepository quizRepository;

    @Override
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    public Quiz getQuizById(Long id) {
        return quizRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
    }

    @Override
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAllWithQuestions();
    }

    @Override
    public Quiz updateQuiz(Long id, Quiz quiz) {
        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        existing.setTitle(quiz.getTitle());
        existing.setDescription(quiz.getDescription());
        existing.setLevel(quiz.getLevel());
        existing.setDateStart(quiz.getDateStart());
        existing.setDateEnd(quiz.getDateEnd());
        existing.setStatus(quiz.getStatus());
        existing.setCourseId(quiz.getCourseId());
        existing.setXpReward(quiz.getXpReward());
        return quizRepository.save(existing);
    }

    @Override
    public void deleteQuiz(Long id) {
        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        quizRepository.delete(existing);
    }
}
