package tn.esprit.cours.services;

import tn.esprit.cours.entity.QuestionQuiz;

import java.util.List;

public interface IQuestionQuizService {
    QuestionQuiz createQuestion(QuestionQuiz question);

    QuestionQuiz getQuestionById(Long id);

    List<QuestionQuiz> getAllQuestions();

    List<QuestionQuiz> getQuestionsByQuizId(Long quizId);

    QuestionQuiz updateQuestion(Long id, QuestionQuiz question);
    void deleteQuestion(Long id);
}
