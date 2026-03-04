package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.StoryAttempt;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryAttemptRepository extends JpaRepository<StoryAttempt, Long> {
    Optional<StoryAttempt> findByUserIdAndStoryQuizIdAndCompletedFalse(Long userId, Long storyQuizId);
    List<StoryAttempt> findByUserId(Long userId);
    List<StoryAttempt> findByUserIdAndCompleted(Long userId, boolean completed);
}
