package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.QuizCategory;

@Repository
public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {
}
