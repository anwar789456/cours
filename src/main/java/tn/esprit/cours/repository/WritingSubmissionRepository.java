package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.cours.entity.WritingSubmission;

import java.util.List;
import java.util.Optional;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, Long> {

    Optional<WritingSubmission> findByUserIdAndWritingPromptIdAndCompletedFalse(Long userId, Long writingPromptId);

    List<WritingSubmission> findByUserId(Long userId);

    List<WritingSubmission> findByUserIdAndCompleted(Long userId, boolean completed);
}
