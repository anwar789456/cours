package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.QuizCategory;
import tn.esprit.cours.services.IQuizCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class QuizCategoryController {

    private final IQuizCategoryService quizCategoryService;

    @PostMapping("/quiz-categories/create-quiz-category")
    public ResponseEntity<QuizCategory> createQuizCategory(@RequestBody QuizCategory quizCategory) {
        QuizCategory created = quizCategoryService.createQuizCategory(quizCategory);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/quiz-categories/get-quiz-category-by-id/{id}")
    public ResponseEntity<QuizCategory> getQuizCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(quizCategoryService.getQuizCategoryById(id));
    }

    @GetMapping("/quiz-categories/get-all-quiz-categories")
    public ResponseEntity<List<QuizCategory>> getAllQuizCategories() {
        return ResponseEntity.ok(quizCategoryService.getAllQuizCategories());
    }

    @PutMapping("/quiz-categories/update-quiz-category/{id}")
    public ResponseEntity<QuizCategory> updateQuizCategory(@PathVariable Long id, @RequestBody QuizCategory quizCategory) {
        return ResponseEntity.ok(quizCategoryService.updateQuizCategory(id, quizCategory));
    }

    @DeleteMapping("/quiz-categories/delete-quiz-category/{id}")
    public ResponseEntity<Void> deleteQuizCategory(@PathVariable Long id) {
        quizCategoryService.deleteQuizCategory(id);
        return ResponseEntity.noContent().build();
    }
}
