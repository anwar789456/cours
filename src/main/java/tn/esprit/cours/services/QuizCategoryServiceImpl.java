package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.QuizCategory;
import tn.esprit.cours.repository.QuizCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCategoryServiceImpl implements IQuizCategoryService {

    private final QuizCategoryRepository quizCategoryRepository;

    @Override
    public QuizCategory createQuizCategory(QuizCategory quizCategory) {
        return quizCategoryRepository.save(quizCategory);
    }

    @Override
    public QuizCategory getQuizCategoryById(Long id) {
        return quizCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCategory not found with id: " + id));
    }

    @Override
    public List<QuizCategory> getAllQuizCategories() {
        return quizCategoryRepository.findAll();
    }

    @Override
    public QuizCategory updateQuizCategory(Long id, QuizCategory quizCategory) {
        QuizCategory existing = quizCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCategory not found with id: " + id));
        existing.setTitle(quizCategory.getTitle());
        existing.setDescription(quizCategory.getDescription());
        existing.setTotalSets(quizCategory.getTotalSets());
        existing.setIcon(quizCategory.getIcon());
        return quizCategoryRepository.save(existing);
    }

    @Override
    public void deleteQuizCategory(Long id) {
        QuizCategory existing = quizCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCategory not found with id: " + id));
        quizCategoryRepository.delete(existing);
    }
}
