package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.QuestionQuiz;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.entity.QuizAttempt;
import tn.esprit.cours.repository.QuestionQuizRepository;
import tn.esprit.cours.repository.QuizAttemptRepository;
import tn.esprit.cours.repository.QuizRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements IQuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionQuizRepository questionQuizRepository;

    @Override
    public QuizAttempt startOrResumeAttempt(Long userId, Long quizId) {
        // Return existing in-progress attempt if one exists
        return quizAttemptRepository.findByUserIdAndQuizIdAndCompletedFalse(userId, quizId)
                .orElseGet(() -> {
                    Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                            .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

                    QuizAttempt attempt = QuizAttempt.builder()
                            .userId(userId)
                            .quizId(quizId)
                            .score(0)
                            .totalQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
                            .answeredQuestions(0)
                            .completed(false)
                            .startedAt(LocalDateTime.now())
                            .build();
                    return quizAttemptRepository.save(attempt);
                });
    }

    @Override
    public QuizAttempt submitAnswer(Long attemptId, Long questionId, String answer) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("QuizAttempt not found with id: " + attemptId));

        if (attempt.isCompleted()) {
            throw new RuntimeException("This attempt is already completed.");
        }

        // Check if this question was already answered
        boolean alreadyAnswered = attempt.getAnswers().containsKey(questionId);

        // Save the answer
        attempt.getAnswers().put(questionId, answer);

        // Check correctness
        QuestionQuiz question = questionQuizRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        boolean isCorrect = question.getCorrectAnswer() != null
                && question.getCorrectAnswer().equalsIgnoreCase(answer != null ? answer.trim() : "");

        // Update score — if re-answering, we need to recalculate
        if (!alreadyAnswered) {
            attempt.setAnsweredQuestions(attempt.getAnsweredQuestions() + 1);
        }

        // Recalculate total score based on all answers
        int totalScore = 0;
        Quiz quiz = quizRepository.findByIdWithQuestions(attempt.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (quiz.getQuestions() != null) {
            for (QuestionQuiz q : quiz.getQuestions()) {
                String submittedAnswer = attempt.getAnswers().get(q.getId());
                if (submittedAnswer != null && q.getCorrectAnswer() != null
                        && q.getCorrectAnswer().equalsIgnoreCase(submittedAnswer.trim())) {
                    totalScore += (quiz.getXpReward() != null ? quiz.getXpReward() : 0);
                }
            }
            // Distribute XP evenly across questions
            int questionCount = quiz.getQuestions().size();
            if (questionCount > 0 && quiz.getXpReward() != null) {
                totalScore = 0;
                int xpPerQuestion = quiz.getXpReward() / questionCount;
                for (QuestionQuiz q : quiz.getQuestions()) {
                    String submittedAnswer = attempt.getAnswers().get(q.getId());
                    if (submittedAnswer != null && q.getCorrectAnswer() != null
                            && q.getCorrectAnswer().equalsIgnoreCase(submittedAnswer.trim())) {
                        totalScore += xpPerQuestion;
                    }
                }
            }
        }

        attempt.setScore(totalScore);
        return quizAttemptRepository.save(attempt);
    }

    @Override
    public QuizAttempt completeAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("QuizAttempt not found with id: " + attemptId));

        attempt.setCompleted(true);
        attempt.setCompletedAt(LocalDateTime.now());

        return quizAttemptRepository.save(attempt);
    }

    @Override
    public List<QuizAttempt> getUserAttempts(Long userId) {
        return quizAttemptRepository.findByUserId(userId);
    }

    @Override
    public QuizAttempt getAttempt(Long attemptId) {
        return quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("QuizAttempt not found with id: " + attemptId));
    }
}
