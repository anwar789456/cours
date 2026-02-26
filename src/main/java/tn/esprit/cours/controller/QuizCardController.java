package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.QuizCard;
import tn.esprit.cours.services.IQuizCardService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class QuizCardController {

    private final IQuizCardService quizCardService;

    @PostMapping("/quiz-cards/create-quiz-card")
    public ResponseEntity<QuizCard> createQuizCard(@RequestBody QuizCard quizCard) {
        QuizCard created = quizCardService.createQuizCard(quizCard);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/quiz-cards/get-quiz-card-by-id/{id}")
    public ResponseEntity<QuizCard> getQuizCardById(@PathVariable Long id) {
        return ResponseEntity.ok(quizCardService.getQuizCardById(id));
    }

    @GetMapping("/quiz-cards/get-all-quiz-cards")
    public ResponseEntity<List<QuizCard>> getAllQuizCards() {
        return ResponseEntity.ok(quizCardService.getAllQuizCards());
    }

    @PutMapping("/quiz-cards/update-quiz-card/{id}")
    public ResponseEntity<QuizCard> updateQuizCard(@PathVariable Long id, @RequestBody QuizCard quizCard) {
        return ResponseEntity.ok(quizCardService.updateQuizCard(id, quizCard));
    }

    @DeleteMapping("/quiz-cards/delete-quiz-card/{id}")
    public ResponseEntity<Void> deleteQuizCard(@PathVariable Long id) {
        quizCardService.deleteQuizCard(id);
        return ResponseEntity.noContent().build();
    }
}
