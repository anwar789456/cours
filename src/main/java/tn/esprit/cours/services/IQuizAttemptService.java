package tn.esprit.cours.services;

import tn.esprit.cours.entity.QuizAttempt;

import java.util.List;

public interface IQuizAttemptService {
    QuizAttempt startOrResumeAttempt(Long userId, Long quizId);
    QuizAttempt submitAnswer(Long attemptId, Long questionId, String answer);
    QuizAttempt completeAttempt(Long attemptId);
    List<QuizAttempt> getUserAttempts(Long userId);
    QuizAttempt getAttempt(Long attemptId);
    void sendResultsEmail(Long attemptId, String userEmail, String userName);
}
