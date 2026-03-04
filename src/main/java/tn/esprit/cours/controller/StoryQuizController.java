package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.StoryAttempt;
import tn.esprit.cours.entity.StoryQuiz;
import tn.esprit.cours.entity.StoryWordBank;
import tn.esprit.cours.services.IStoryAttemptService;
import tn.esprit.cours.services.IStoryQuizService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours/story-quizzes")
@RequiredArgsConstructor
public class StoryQuizController {

    private final IStoryQuizService storyQuizService;
    private final IStoryAttemptService storyAttemptService;

    // ── Story Quiz CRUD ──

    @PostMapping("/create")
    public ResponseEntity<StoryQuiz> createStoryQuiz(@RequestBody StoryQuiz storyQuiz) {
        StoryQuiz created = storyQuizService.createStoryQuiz(storyQuiz);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<StoryQuiz>> getAllStoryQuizzes() {
        return ResponseEntity.ok(storyQuizService.getAllStoryQuizzes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryQuiz> getStoryQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(storyQuizService.getStoryQuizById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StoryQuiz> updateStoryQuiz(@PathVariable Long id, @RequestBody StoryQuiz storyQuiz) {
        return ResponseEntity.ok(storyQuizService.updateStoryQuiz(id, storyQuiz));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteStoryQuiz(@PathVariable Long id) {
        storyQuizService.deleteStoryQuiz(id);
        return ResponseEntity.noContent().build();
    }

    // ── Word Bank ──

    @GetMapping("/{id}/word-bank")
    public ResponseEntity<StoryWordBank> getWordBank(@PathVariable Long id) {
        return ResponseEntity.ok(storyQuizService.getWordBank(id));
    }

    @PostMapping("/{id}/word-bank")
    public ResponseEntity<StoryWordBank> saveWordBank(@PathVariable Long id, @RequestBody StoryWordBank wordBank) {
        wordBank.setStoryQuizId(id);
        return ResponseEntity.ok(storyQuizService.saveWordBank(wordBank));
    }

    // ── Validate Answers ──

    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<Integer, Boolean>> validateAnswers(@PathVariable Long id,
            @RequestBody Map<Integer, String> answers) {
        return ResponseEntity.ok(storyQuizService.validateAnswers(id, answers));
    }

    // ── Story Attempts ──

    @PostMapping("/attempts/start")
    public ResponseEntity<StoryAttempt> startAttempt(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long storyQuizId = body.get("storyQuizId");
        return ResponseEntity.ok(storyAttemptService.startOrResumeAttempt(userId, storyQuizId));
    }

    @PutMapping("/attempts/{attemptId}/save")
    public ResponseEntity<StoryAttempt> saveProgress(@PathVariable Long attemptId,
            @RequestBody Map<Integer, String> answers) {
        return ResponseEntity.ok(storyAttemptService.saveProgress(attemptId, answers));
    }

    @PutMapping("/attempts/{attemptId}/complete")
    public ResponseEntity<StoryAttempt> completeAttempt(@PathVariable Long attemptId,
            @RequestBody Map<Integer, String> answers) {
        return ResponseEntity.ok(storyAttemptService.completeAttempt(attemptId, answers));
    }

    @GetMapping("/attempts/user/{userId}")
    public ResponseEntity<List<StoryAttempt>> getUserAttempts(@PathVariable Long userId) {
        return ResponseEntity.ok(storyAttemptService.getUserAttempts(userId));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<StoryAttempt> getAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(storyAttemptService.getAttempt(attemptId));
    }
}
