package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.StoryAttempt;
import tn.esprit.cours.entity.StoryBlank;
import tn.esprit.cours.entity.StoryQuiz;
import tn.esprit.cours.repository.StoryAttemptRepository;
import tn.esprit.cours.repository.StoryQuizRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoryAttemptServiceImpl implements IStoryAttemptService {

    private final StoryAttemptRepository storyAttemptRepository;
    private final StoryQuizRepository storyQuizRepository;

    @Override
    public StoryAttempt startOrResumeAttempt(Long userId, Long storyQuizId) {
        // Return existing in-progress attempt if one exists
        return storyAttemptRepository.findByUserIdAndStoryQuizIdAndCompletedFalse(userId, storyQuizId)
                .orElseGet(() -> {
                    StoryAttempt attempt = StoryAttempt.builder()
                            .userId(userId)
                            .storyQuizId(storyQuizId)
                            .completed(false)
                            .score(0)
                            .startedAt(LocalDateTime.now())
                            .build();
                    return storyAttemptRepository.save(attempt);
                });
    }

    @Override
    public StoryAttempt saveProgress(Long attemptId, Map<Integer, String> answers) {
        StoryAttempt attempt = storyAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("StoryAttempt not found with id: " + attemptId));
        attempt.getAnswers().putAll(answers);
        return storyAttemptRepository.save(attempt);
    }

    @Override
    public StoryAttempt completeAttempt(Long attemptId, Map<Integer, String> finalAnswers) {
        StoryAttempt attempt = storyAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("StoryAttempt not found with id: " + attemptId));

        attempt.getAnswers().putAll(finalAnswers);

        // Calculate score
        StoryQuiz quiz = storyQuizRepository.findByIdWithBlanks(attempt.getStoryQuizId())
                .orElseThrow(() -> new RuntimeException("StoryQuiz not found with id: " + attempt.getStoryQuizId()));

        int correctCount = 0;
        for (StoryBlank blank : quiz.getBlanks()) {
            String submitted = attempt.getAnswers().get(blank.getBlankIndex());
            if (blank.getCorrectWord() != null
                    && blank.getCorrectWord().equalsIgnoreCase(submitted != null ? submitted.trim() : "")) {
                correctCount++;
            }
        }

        int totalBlanks = quiz.getBlanks().size();
        int earnedXp = totalBlanks > 0
                ? (int) Math.round((double) correctCount / totalBlanks * quiz.getXpReward())
                : 0;

        attempt.setScore(earnedXp);
        attempt.setCompleted(true);
        attempt.setCompletedAt(LocalDateTime.now());

        return storyAttemptRepository.save(attempt);
    }

    @Override
    public List<StoryAttempt> getUserAttempts(Long userId) {
        return storyAttemptRepository.findByUserId(userId);
    }

    @Override
    public StoryAttempt getAttempt(Long attemptId) {
        return storyAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("StoryAttempt not found with id: " + attemptId));
    }
}
