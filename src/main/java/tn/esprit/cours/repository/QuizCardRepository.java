package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.QuizCard;

@Repository
public interface QuizCardRepository extends JpaRepository<QuizCard, Long> {
}
