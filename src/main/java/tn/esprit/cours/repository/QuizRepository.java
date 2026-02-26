package tn.esprit.cours.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.Quiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * Fetch a single Quiz with all its QuestionQuiz children in ONE query.
     * Solves the N+1 problem for getQuizById.
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Fetch all Quizzes with their QuestionQuiz children in ONE query.
     * Uses DISTINCT to avoid cartesian product duplicates.
     */
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions")
    List<Quiz> findAllWithQuestions();

    /**
     * Paginated Quiz listing (without fetching children — lightweight).
     */
    @Query("SELECT q FROM Quiz q")
    Page<Quiz> findAllPaged(Pageable pageable);
}
