package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
}
