package tn.esprit.cours.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.WritingPrompt;
import tn.esprit.cours.entity.WritingSubmission;
import tn.esprit.cours.repository.WritingPromptRepository;
import tn.esprit.cours.repository.WritingSubmissionRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WritingServiceImpl implements IWritingService {

    private final WritingPromptRepository promptRepository;
    private final WritingSubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    @Value("${python.script.path:/opt/minolingo/scripts/writing_assess.py}")
    private String pythonScriptPath;

    @Value("${python.executable:python3}")
    private String pythonExecutable;

    // ── Prompt CRUD ──

    @Override
    public WritingPrompt createPrompt(WritingPrompt prompt) {
        return promptRepository.save(prompt);
    }

    @Override
    public WritingPrompt getPromptById(Long id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Writing prompt not found: " + id));
    }

    @Override
    public List<WritingPrompt> getAllPrompts() {
        return promptRepository.findAll();
    }

    @Override
    public WritingPrompt updatePrompt(Long id, WritingPrompt prompt) {
        WritingPrompt existing = getPromptById(id);
        existing.setTitle(prompt.getTitle());
        existing.setDescription(prompt.getDescription());
        existing.setDifficulty(prompt.getDifficulty());
        existing.setXpReward(prompt.getXpReward());
        existing.setMinWords(prompt.getMinWords());
        existing.setMaxWords(prompt.getMaxWords());
        return promptRepository.save(existing);
    }

    @Override
    public void deletePrompt(Long id) {
        promptRepository.deleteById(id);
    }

    @Override
    public WritingPrompt archivePrompt(Long id, boolean archived) {
        WritingPrompt prompt = getPromptById(id);
        prompt.setArchived(archived);
        return promptRepository.save(prompt);
    }

    // ── Submission Flow ──

    @Override
    public WritingSubmission startOrResumeSubmission(Long userId, Long promptId) {
        return submissionRepository.findByUserIdAndWritingPromptIdAndCompletedFalse(userId, promptId)
                .orElseGet(() -> {
                    WritingSubmission submission = WritingSubmission.builder()
                            .userId(userId)
                            .writingPromptId(promptId)
                            .completed(false)
                            .startedAt(LocalDateTime.now())
                            .build();
                    return submissionRepository.save(submission);
                });
    }

    @Override
    public WritingSubmission saveProgress(Long submissionId, String text) {
        WritingSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));
        submission.setSubmittedText(text);
        return submissionRepository.save(submission);
    }

    @Override
    public WritingSubmission submitAndEvaluate(Long submissionId, String text) {
        WritingSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        WritingPrompt prompt = getPromptById(submission.getWritingPromptId());
        submission.setSubmittedText(text);

        // Call Python script via ProcessBuilder
        try {
            String level = prompt.getDifficulty() != null ? prompt.getDifficulty() : "BEGINNER";

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    pythonScriptPath,
                    "--text", text,
                    "--prompt", prompt.getTitle(),
                    "--level", level
            );
            pb.redirectErrorStream(false);

            Process process = pb.start();

            // Read stdout (the JSON result)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            // Log stderr for debugging without blocking
            try (BufferedReader errReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errReader.readLine()) != null) {
                    System.err.println("[python-assess] " + line);
                }
            }

            process.waitFor();

            String json = output.toString().trim();
            if (!json.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(json, Map.class);
                submission.setOverallScore(((Number) result.getOrDefault("overallScore", 0)).intValue());
                submission.setGrammarScore(((Number) result.getOrDefault("grammarScore", 0)).intValue());
                submission.setSpellingScore(((Number) result.getOrDefault("spellingScore", 0)).intValue());
                submission.setContentScore(((Number) result.getOrDefault("contentScore", 0)).intValue());
                submission.setOverallFeedback((String) result.getOrDefault("overallFeedback", ""));
                submission.setFeedbackJson(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
            submission.setOverallFeedback("Assessment is temporarily unavailable. Your writing has been saved.");
        }

        submission.setCompleted(true);
        submission.setCompletedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    @Override
    public List<WritingSubmission> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserId(userId);
    }

    @Override
    public WritingSubmission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));
    }
}
