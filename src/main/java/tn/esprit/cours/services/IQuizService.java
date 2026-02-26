package tn.esprit.cours.services;

import tn.esprit.cours.entity.Quiz;

import java.util.List;

public interface IQuizService {
    Quiz createQuiz(Quiz quiz);

    Quiz getQuizById(Long id);

    List<Quiz> getAllQuizzes();

    Quiz updateQuiz(Long id, Quiz quiz);
    void deleteQuiz(Long id);
}
