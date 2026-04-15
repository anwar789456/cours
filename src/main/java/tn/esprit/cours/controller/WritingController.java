package tn.esprit.cours.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.WritingPrompt;
import tn.esprit.cours.entity.WritingSubmission;
import tn.esprit.cours.services.AiService;
import tn.esprit.cours.services.IWritingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours/writing")
@RequiredArgsConstructor
public class WritingController {

    private final IWritingService writingService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

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

    // ── AI Generation ──

    @PostMapping("/prompts/generate-ai")
    public ResponseEntity<?> generateWritingPrompt(@RequestBody Map<String, Object> payload) {
        String title      = (String) payload.getOrDefault("title", "");
        String difficulty = (String) payload.getOrDefault("difficulty", "BEGINNER");
        String type       = (String) payload.getOrDefault("type", "");
        String hints      = (String) payload.getOrDefault("hints", "");
        int    xpReward   = payload.containsKey("xpReward") ? ((Number) payload.get("xpReward")).intValue() : 50;

        try {
            String json = aiService.generateWritingPrompt(title, difficulty, type, hints);

            @SuppressWarnings("unchecked")
            Map<String, Object> aiResult = objectMapper.readValue(json, Map.class);

            WritingPrompt prompt = new WritingPrompt();
            prompt.setTitle(title);
            prompt.setDescription((String) aiResult.getOrDefault("description", "Write about " + title + "."));
            prompt.setDifficulty(difficulty.toUpperCase());
            prompt.setXpReward(xpReward);
            prompt.setMinWords(aiResult.containsKey("minWords") ? ((Number) aiResult.get("minWords")).intValue() : 40);
            prompt.setMaxWords(aiResult.containsKey("maxWords") ? ((Number) aiResult.get("maxWords")).intValue() : 150);
            prompt.setArchived(false);

            WritingPrompt saved = writingService.createPrompt(prompt);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "AI generation failed: " + e.getMessage()));
        }
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
