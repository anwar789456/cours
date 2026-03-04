package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.StoryQuiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryQuizRepository extends JpaRepository<StoryQuiz, Long> {

    @Query("SELECT sq FROM StoryQuiz sq LEFT JOIN FETCH sq.blanks WHERE sq.id = :id")
    Optional<StoryQuiz> findByIdWithBlanks(@Param("id") Long id);

    @Query("SELECT DISTINCT sq FROM StoryQuiz sq LEFT JOIN FETCH sq.blanks")
    List<StoryQuiz> findAllWithBlanks();
}
