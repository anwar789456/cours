package tn.esprit.cours.services;

import tn.esprit.cours.entity.QuizCategory;

import java.util.List;

public interface IQuizCategoryService {
    QuizCategory createQuizCategory(QuizCategory quizCategory);

    QuizCategory getQuizCategoryById(Long id);

    List<QuizCategory> getAllQuizCategories();

    QuizCategory updateQuizCategory(Long id, QuizCategory quizCategory);
    void deleteQuizCategory(Long id);
}
