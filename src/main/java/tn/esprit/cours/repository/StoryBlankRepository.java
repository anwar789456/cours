package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.StoryBlank;

import java.util.List;

@Repository
public interface StoryBlankRepository extends JpaRepository<StoryBlank, Long> {
    List<StoryBlank> findByStoryQuizId(Long storyQuizId);
}
