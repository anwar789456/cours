package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.cours.entity.WritingPrompt;

public interface WritingPromptRepository extends JpaRepository<WritingPrompt, Long> {
}
