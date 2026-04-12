package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.WritingPrompt;
import tn.esprit.cours.entity.WritingSubmission;
import tn.esprit.cours.services.IWritingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours/writing")
@RequiredArgsConstructor
public class WritingController {

    private final IWritingService writingService;

    // ── Prompt CRUD ──

    @PostMapping("/prompts/create")
    public ResponseEntity<WritingPrompt> createPrompt(@RequestBody WritingPrompt prompt) {
        return new ResponseEntity<>(writingService.createPrompt(prompt), HttpStatus.CREATED);
    }

    @GetMapping("/prompts/all")
    public ResponseEntity<List<WritingPrompt>> getAllPrompts() {
        return ResponseEntity.ok(writingService.getAllPrompts());
    }

    @GetMapping("/prompts/{id}")
    public ResponseEntity<WritingPrompt> getPromptById(@PathVariable Long id) {
        return ResponseEntity.ok(writingService.getPromptById(id));
    }

    @PutMapping("/prompts/update/{id}")
    public ResponseEntity<WritingPrompt> updatePrompt(@PathVariable Long id, @RequestBody WritingPrompt prompt) {
        return ResponseEntity.ok(writingService.updatePrompt(id, prompt));
    }

    @DeleteMapping("/prompts/delete/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id) {
        writingService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/prompts/archive/{id}")
    public ResponseEntity<WritingPrompt> archivePrompt(@PathVariable Long id) {
        return ResponseEntity.ok(writingService.archivePrompt(id, true));
    }

    @PutMapping("/prompts/unarchive/{id}")
    public ResponseEntity<WritingPrompt> unarchivePrompt(@PathVariable Long id) {
        return ResponseEntity.ok(writingService.archivePrompt(id, false));
    }

    // ── Submissions ──

    @PostMapping("/submissions/start")
    public ResponseEntity<WritingSubmission> startSubmission(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long promptId = body.get("promptId");
        return ResponseEntity.ok(writingService.startOrResumeSubmission(userId, promptId));
    }

    @PutMapping("/submissions/{id}/save")
    public ResponseEntity<WritingSubmission> saveProgress(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String text = body.get("text");
        return ResponseEntity.ok(writingService.saveProgress(id, text));
    }

    @PutMapping("/submissions/{id}/submit")
    public ResponseEntity<WritingSubmission> submitForEvaluation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String text = body.get("text");
        return ResponseEntity.ok(writingService.submitAndEvaluate(id, text));
    }

    @GetMapping("/submissions/user/{userId}")
    public ResponseEntity<List<WritingSubmission>> getUserSubmissions(@PathVariable Long userId) {
        return ResponseEntity.ok(writingService.getUserSubmissions(userId));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<WritingSubmission> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(writingService.getSubmissionById(id));
    }
}
