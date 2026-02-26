package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.QuestionQuiz;
import tn.esprit.cours.services.IQuestionQuizService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class QuestionQuizController {

    private final IQuestionQuizService questionService;

    @PostMapping("/questions/create-question")
    public ResponseEntity<QuestionQuiz> createQuestion(@RequestBody QuestionQuiz question) {
        QuestionQuiz created = questionService.createQuestion(question);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/questions/get-question-by-id/{id}")
    public ResponseEntity<QuestionQuiz> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @GetMapping("/questions/get-all-questions")
    public ResponseEntity<List<QuestionQuiz>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/questions/get-questions-by-quiz-id/{quizId}")
    public ResponseEntity<List<QuestionQuiz>> getQuestionsByQuizId(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.getQuestionsByQuizId(quizId));
    }

    @PutMapping("/questions/update-question/{id}")
    public ResponseEntity<QuestionQuiz> updateQuestion(@PathVariable Long id,
            @RequestBody QuestionQuiz question) {
        return ResponseEntity.ok(questionService.updateQuestion(id, question));
    }

    @DeleteMapping("/questions/delete-question/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
