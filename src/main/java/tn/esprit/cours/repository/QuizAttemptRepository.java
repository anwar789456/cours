package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.QuizAttempt;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByUserIdAndQuizIdAndCompletedFalse(Long userId, Long quizId);
    List<QuizAttempt> findByUserIdAndCompleted(Long userId, boolean completed);
    Optional<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    List<QuizAttempt> findByUserId(Long userId);
}
