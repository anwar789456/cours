package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.StoryWordBank;

import java.util.Optional;

@Repository
public interface StoryWordBankRepository extends JpaRepository<StoryWordBank, Long> {
    Optional<StoryWordBank> findByStoryQuizId(Long storyQuizId);
}
