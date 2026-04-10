package tn.esprit.cours.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.QuestionQuiz;
import tn.esprit.cours.entity.QuestionType;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.entity.QuizLevel;
import tn.esprit.cours.entity.QuizStatus;
import tn.esprit.cours.services.AiService;
import tn.esprit.cours.services.IQuestionQuizService;
import tn.esprit.cours.services.IQuizService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;
    private final IQuestionQuizService questionService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

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

    @PostMapping("/quizzes/generate-full-quiz")
    public ResponseEntity<?> generateFullQuiz(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.getOrDefault("title", "");
        String description = (String) payload.getOrDefault("description", "");
        String level = (String) payload.getOrDefault("level", "BEGINNER");
        String status = (String) payload.getOrDefault("status", "DRAFT");
        int xpReward = payload.containsKey("xpReward") ? ((Number) payload.get("xpReward")).intValue() : 0;

        try {
            // Create quiz entity
            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setDescription(description);
            quiz.setLevel(QuizLevel.valueOf(level.toUpperCase()));
            quiz.setStatus(QuizStatus.valueOf(status.toUpperCase()));
            quiz.setXpReward(xpReward);
            Quiz savedQuiz = quizService.createQuiz(quiz);

            // Generate questions via AI
            String questionsJson = aiService.generateFullQuiz(title, description, level);
            List<Map<String, Object>> questionsList = new ArrayList<>();
            try {
                questionsList = objectMapper.readValue(questionsJson,
                        new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception parseEx) {
                // AI returned malformed JSON — return quiz without questions
                return ResponseEntity.ok(savedQuiz);
            }

            // Save each question
            for (Map<String, Object> q : questionsList) {
                QuestionQuiz question = new QuestionQuiz();
                question.setQuestion((String) q.getOrDefault("question", ""));
                Object optionsObj = q.get("options");
                if (optionsObj instanceof List<?> opts) {
                    question.setOptions(opts.stream().map(Object::toString).toList());
                }
                question.setCorrectAnswer((String) q.getOrDefault("correctAnswer", ""));
                question.setExplanation((String) q.getOrDefault("explanation", ""));
                question.setType(QuestionType.MCQ);
                question.setQuiz(savedQuiz);
                questionService.createQuestion(question);
            }

            // Return the full quiz with questions
            return ResponseEntity.ok(quizService.getQuizById(savedQuiz.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "AI quiz generation failed: " + e.getMessage()));
        }
    }
}
