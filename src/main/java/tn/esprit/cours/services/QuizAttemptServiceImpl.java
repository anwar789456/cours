package tn.esprit.cours.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final JavaMailSender mailSender;

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

    @Override
    public void sendResultsEmail(Long attemptId, String userEmail, String userName) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("QuizAttempt not found with id: " + attemptId));

        Quiz quiz = quizRepository.findByIdWithQuestions(attempt.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuestionQuiz> questions = quiz.getQuestions() != null ? quiz.getQuestions() : List.of();

        // Calculate correct count
        int correctCount = 0;
        for (QuestionQuiz q : questions) {
            String submitted = attempt.getAnswers().get(q.getId());
            if (submitted != null && q.getCorrectAnswer() != null
                    && q.getCorrectAnswer().equalsIgnoreCase(submitted.trim())) {
                correctCount++;
            }
        }

        int totalCount = questions.size();
        int percentage = totalCount > 0 ? Math.round((float) correctCount / totalCount * 100) : 0;
        int xpEarned = attempt.getScore();

        String displayName = (userName != null && !userName.isBlank()) ? userName : "Student";

        // Build score color
        String scoreColor = percentage >= 80 ? "#22c55e" : (percentage >= 50 ? "#f59e0b" : "#ef4444");
        String encouragement;
        if (percentage == 100) encouragement = "Perfect score! You're amazing! 🏆";
        else if (percentage >= 80) encouragement = "Great job! Almost perfect! 🌟";
        else if (percentage >= 60) encouragement = "Good work! Keep practicing! 💪";
        else if (percentage >= 40) encouragement = "Nice try! You're getting there! 📚";
        else encouragement = "Keep learning! Practice makes perfect! 🌱";

        // Build question rows
        StringBuilder rowsHtml = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            QuestionQuiz q = questions.get(i);
            String submitted = attempt.getAnswers().getOrDefault(q.getId(), "—");
            boolean correct = q.getCorrectAnswer() != null
                    && q.getCorrectAnswer().equalsIgnoreCase(submitted.trim());
            String icon = correct ? "✅" : "❌";
            String rowBg = (i % 2 == 0) ? "#f9fafb" : "#ffffff";
            rowsHtml.append(String.format("""
                <tr style="background:%s">
                  <td style="padding:10px 14px;font-size:13px;color:#374151;border-bottom:1px solid #e5e7eb">%s</td>
                  <td style="padding:10px 14px;font-size:13px;color:#374151;border-bottom:1px solid #e5e7eb">%s %s</td>
                  <td style="padding:10px 14px;font-size:13px;color:#16a34a;border-bottom:1px solid #e5e7eb">%s</td>
                  <td style="padding:10px 14px;font-size:13px;text-align:center;border-bottom:1px solid #e5e7eb">%s</td>
                </tr>
                """,
                rowBg,
                escapeHtml(q.getQuestion() != null ? q.getQuestion() : ""),
                escapeHtml(submitted),
                correct ? "" : "<span style='color:#dc2626'>(incorrect)</span>",
                escapeHtml(q.getCorrectAnswer() != null ? q.getCorrectAnswer() : ""),
                icon
            ));
        }

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Segoe UI',Arial,sans-serif">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f3f4f6;padding:32px 0">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08)">
                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:36px 40px;text-align:center">
                        <div style="font-size:36px;margin-bottom:8px">🦉</div>
                        <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700">Quiz Results</h1>
                        <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px">%s</p>
                      </td>
                    </tr>
                    <!-- Greeting -->
                    <tr>
                      <td style="padding:28px 40px 0">
                        <p style="margin:0;font-size:16px;color:#374151">Hi <strong>%s</strong>! 👋</p>
                        <p style="margin:8px 0 0;font-size:15px;color:#6b7280">Here are your results for <strong>%s</strong></p>
                      </td>
                    </tr>
                    <!-- Score Card -->
                    <tr>
                      <td style="padding:24px 40px">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f9fafb;border-radius:12px;overflow:hidden">
                          <tr>
                            <td style="padding:24px;text-align:center;border-right:1px solid #e5e7eb">
                              <div style="font-size:40px;font-weight:800;color:%s">%d%%</div>
                              <div style="font-size:12px;color:#9ca3af;margin-top:4px;text-transform:uppercase;letter-spacing:0.05em">Score</div>
                            </td>
                            <td style="padding:24px;text-align:center;border-right:1px solid #e5e7eb">
                              <div style="font-size:40px;font-weight:800;color:#374151">%d<span style="font-size:20px;color:#9ca3af">/%d</span></div>
                              <div style="font-size:12px;color:#9ca3af;margin-top:4px;text-transform:uppercase;letter-spacing:0.05em">Correct</div>
                            </td>
                            <td style="padding:24px;text-align:center">
                              <div style="font-size:40px;font-weight:800;color:#f59e0b">+%d</div>
                              <div style="font-size:12px;color:#9ca3af;margin-top:4px;text-transform:uppercase;letter-spacing:0.05em">XP Earned</div>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:16px 0 0;font-size:15px;color:#374151;text-align:center">%s</p>
                      </td>
                    </tr>
                    <!-- Question Breakdown -->
                    <tr>
                      <td style="padding:0 40px 28px">
                        <h3 style="margin:0 0 12px;font-size:15px;font-weight:600;color:#374151">Question Breakdown</h3>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="border:1px solid #e5e7eb;border-radius:8px;overflow:hidden">
                          <tr style="background:#f3f4f6">
                            <th style="padding:10px 14px;font-size:12px;text-align:left;color:#6b7280;font-weight:600;border-bottom:1px solid #e5e7eb">Question</th>
                            <th style="padding:10px 14px;font-size:12px;text-align:left;color:#6b7280;font-weight:600;border-bottom:1px solid #e5e7eb">Your Answer</th>
                            <th style="padding:10px 14px;font-size:12px;text-align:left;color:#6b7280;font-weight:600;border-bottom:1px solid #e5e7eb">Correct Answer</th>
                            <th style="padding:10px 14px;font-size:12px;text-align:center;color:#6b7280;font-weight:600;border-bottom:1px solid #e5e7eb">Result</th>
                          </tr>
                          %s
                        </table>
                      </td>
                    </tr>
                    <!-- CTA -->
                    <tr>
                      <td style="padding:0 40px 32px;text-align:center">
                        <a href="https://minolingo.online" style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);color:#ffffff;text-decoration:none;padding:12px 32px;border-radius:50px;font-size:14px;font-weight:600">
                          Continue Learning on MinoLingo
                        </a>
                      </td>
                    </tr>
                    <!-- Footer -->
                    <tr>
                      <td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #e5e7eb">
                        <p style="margin:0;font-size:12px;color:#9ca3af">MinoLingo — Your English Learning Companion 🌍</p>
                        <p style="margin:4px 0 0;font-size:12px;color:#d1d5db">This email was sent to %s</p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """,
            escapeHtml(quiz.getTitle() != null ? quiz.getTitle() : "Quiz"),
            escapeHtml(displayName),
            escapeHtml(quiz.getTitle() != null ? quiz.getTitle() : "the quiz"),
            scoreColor, percentage,
            correctCount, totalCount,
            xpEarned,
            encouragement,
            rowsHtml,
            escapeHtml(userEmail)
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("mino@minolingo.online", "MinoLingo");
            helper.setTo(userEmail);
            helper.setSubject("Your Quiz Results: " + (quiz.getTitle() != null ? quiz.getTitle() : "Quiz") + " — " + percentage + "%");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send quiz result email: " + e.getMessage(), e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
