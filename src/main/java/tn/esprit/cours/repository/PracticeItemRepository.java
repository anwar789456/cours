package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.PracticeItem;

@Repository
public interface PracticeItemRepository extends JpaRepository<PracticeItem, Long> {
}
