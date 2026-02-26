package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.QuestionQuiz;
import tn.esprit.cours.repository.QuestionQuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionQuizServiceImpl implements IQuestionQuizService {

    private final QuestionQuizRepository questionRepository;

    @Override
    public QuestionQuiz createQuestion(QuestionQuiz question) {
        return questionRepository.save(question);
    }

    @Override
    public QuestionQuiz getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuestionQuiz not found with id: " + id));
    }

    @Override
    public List<QuestionQuiz> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public List<QuestionQuiz> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Override
    public QuestionQuiz updateQuestion(Long id, QuestionQuiz question) {
        QuestionQuiz existing = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuestionQuiz not found with id: " + id));
        existing.setQuestion(question.getQuestion());
        existing.setOptions(question.getOptions());
        existing.setCorrectAnswer(question.getCorrectAnswer());
        existing.setExplanation(question.getExplanation());
        existing.setType(question.getType());
        existing.setQuiz(question.getQuiz());
        return questionRepository.save(existing);
    }

    @Override
    public void deleteQuestion(Long id) {
        QuestionQuiz existing = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuestionQuiz not found with id: " + id));
        questionRepository.delete(existing);
    }
}
