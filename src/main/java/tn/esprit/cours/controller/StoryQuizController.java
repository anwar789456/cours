package tn.esprit.cours.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.StoryAttempt;
import tn.esprit.cours.entity.StoryBlank;
import tn.esprit.cours.entity.StoryQuiz;
import tn.esprit.cours.entity.StoryWordBank;
import tn.esprit.cours.services.AiService;
import tn.esprit.cours.services.IStoryAttemptService;
import tn.esprit.cours.services.IStoryQuizService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours/story-quizzes")
@RequiredArgsConstructor
public class StoryQuizController {

    private final IStoryQuizService storyQuizService;
    private final IStoryAttemptService storyAttemptService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

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

    @PutMapping("/archive/{id}")
    public ResponseEntity<StoryQuiz> archiveStoryQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(storyQuizService.archiveStoryQuiz(id, true));
    }

    @PutMapping("/unarchive/{id}")
    public ResponseEntity<StoryQuiz> unarchiveStoryQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(storyQuizService.archiveStoryQuiz(id, false));
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

    // ── AI Generation ──

    @PostMapping("/generate-full-story")
    public ResponseEntity<?> generateFullStoryQuiz(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.getOrDefault("title", "");
        String difficulty = (String) payload.getOrDefault("difficulty", "BEGINNER");
        int xpReward = payload.containsKey("xpReward") ? ((Number) payload.get("xpReward")).intValue() : 50;

        try {
            String json = aiService.generateFullStoryQuiz(title, difficulty);

            Map<String, Object> data;
            try {
                data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            } catch (Exception parseEx) {
                return ResponseEntity.status(500).body(Map.of("error", "AI returned invalid JSON"));
            }

            String storyTemplate = (String) data.getOrDefault("storyTemplate", "");
            Object blanksObj = data.get("blanks");
            Object wordBankObj = data.get("wordBank");

            // Build StoryQuiz entity
            StoryQuiz storyQuiz = new StoryQuiz();
            storyQuiz.setTitle(title);
            storyQuiz.setStoryTemplate(storyTemplate);
            storyQuiz.setDifficulty(difficulty);
            storyQuiz.setXpReward(xpReward);

            // Parse blanks
            List<StoryBlank> blankList = new ArrayList<>();
            if (blanksObj instanceof List<?> blanksRaw) {
                for (Object b : blanksRaw) {
                    if (b instanceof Map<?, ?> bMap) {
                        StoryBlank blank = new StoryBlank();
                        blank.setBlankIndex(((Number) bMap.getOrDefault("blankIndex", 0)).intValue());
                        blank.setCorrectWord((String) bMap.getOrDefault("correctWord", ""));
                        blank.setHint((String) bMap.getOrDefault("hint", ""));
                        blank.setStoryQuiz(storyQuiz);
                        blankList.add(blank);
                    }
                }
            }
            storyQuiz.setBlanks(blankList);

            StoryQuiz saved = storyQuizService.createStoryQuiz(storyQuiz);

            // Save word bank
            if (wordBankObj instanceof List<?> wordsRaw) {
                List<String> words = wordsRaw.stream().map(Object::toString).toList();
                StoryWordBank wordBank = new StoryWordBank();
                wordBank.setStoryQuizId(saved.getId());
                wordBank.setWords(new ArrayList<>(words));
                storyQuizService.saveWordBank(wordBank);
            }

            return ResponseEntity.ok(storyQuizService.getStoryQuizById(saved.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "AI story generation failed: " + e.getMessage()));
        }
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
