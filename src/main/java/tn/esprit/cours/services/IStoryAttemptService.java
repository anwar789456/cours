package tn.esprit.cours.services;

import tn.esprit.cours.entity.StoryAttempt;

import java.util.List;
import java.util.Map;

public interface IStoryAttemptService {
    StoryAttempt startOrResumeAttempt(Long userId, Long storyQuizId);
    StoryAttempt saveProgress(Long attemptId, Map<Integer, String> answers);
    StoryAttempt completeAttempt(Long attemptId, Map<Integer, String> finalAnswers);
    List<StoryAttempt> getUserAttempts(Long userId);
    StoryAttempt getAttempt(Long attemptId);
}
