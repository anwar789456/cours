package tn.esprit.cours.services;

import tn.esprit.cours.entity.QuizCard;

import java.util.List;

public interface IQuizCardService {
    QuizCard createQuizCard(QuizCard quizCard);

    QuizCard getQuizCardById(Long id);

    List<QuizCard> getAllQuizCards();

    QuizCard updateQuizCard(Long id, QuizCard quizCard);
    void deleteQuizCard(Long id);
}
