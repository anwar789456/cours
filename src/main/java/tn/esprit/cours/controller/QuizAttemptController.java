package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.QuizAttempt;
import tn.esprit.cours.services.IQuizAttemptService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final IQuizAttemptService quizAttemptService;

    @PostMapping("/start")
    public ResponseEntity<QuizAttempt> startAttempt(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long quizId = body.get("quizId");
        return ResponseEntity.ok(quizAttemptService.startOrResumeAttempt(userId, quizId));
    }

    @PutMapping("/{attemptId}/answer")
    public ResponseEntity<QuizAttempt> submitAnswer(@PathVariable Long attemptId,
            @RequestBody Map<String, Object> body) {
        Long questionId = ((Number) body.get("questionId")).longValue();
        String answer = (String) body.get("answer");
        return ResponseEntity.ok(quizAttemptService.submitAnswer(attemptId, questionId, answer));
    }

    @PutMapping("/{attemptId}/complete")
    public ResponseEntity<QuizAttempt> completeAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizAttemptService.completeAttempt(attemptId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizAttempt>> getUserAttempts(@PathVariable Long userId) {
        return ResponseEntity.ok(quizAttemptService.getUserAttempts(userId));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttempt> getAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizAttemptService.getAttempt(attemptId));
    }

    @PostMapping("/{attemptId}/send-results")
    public ResponseEntity<Void> sendResults(@PathVariable Long attemptId,
            @RequestBody Map<String, String> body) {
        String userEmail = body.get("userEmail");
        String userName = body.get("userName");
        quizAttemptService.sendResultsEmail(attemptId, userEmail, userName);
        return ResponseEntity.ok().build();
    }
}
