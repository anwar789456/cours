package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.services.AiService;
import tn.esprit.cours.services.IQuizService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;
    private final AiService aiService;

    @PostMapping("/quizzes/create-quiz")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        Quiz created = quizService.createQuiz(quiz);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/quizzes/get-quiz-by-id/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping("/quizzes/get-all-quizzes")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @PutMapping("/quizzes/update-quiz/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quiz));
    }

    @DeleteMapping("/quizzes/delete-quiz/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/quizzes/archive/{id}")
    public ResponseEntity<Quiz> archiveQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.archiveQuiz(id, true));
    }

    @PutMapping("/quizzes/unarchive/{id}")
    public ResponseEntity<Quiz> unarchiveQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.archiveQuiz(id, false));
    }

    // ── AI Generation ──

    @PostMapping("/quizzes/generate-description")
    public ResponseEntity<Map<String, String>> generateQuizDescription(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String level = payload.getOrDefault("level", "");
        try {
            String description = aiService.generateQuizDescription(title, level);
            return ResponseEntity.ok(Map.of("description", description));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI generation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/quizzes/generate-single-question")
    public ResponseEntity<Map<String, String>> generateSingleQuestion(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.getOrDefault("title", "");
        String level = (String) payload.getOrDefault("level", "BEGINNER");
        int questionNumber = payload.containsKey("questionNumber") ? ((Number) payload.get("questionNumber")).intValue() : 1;
        try {
            String questionJson = aiService.generateSingleQuizQuestion(title, level, questionNumber);
            return ResponseEntity.ok(Map.of("question", questionJson));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI generation failed: " + e.getMessage()));
        }
    }
}
