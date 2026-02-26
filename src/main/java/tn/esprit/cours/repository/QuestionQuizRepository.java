package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.QuestionQuiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionQuizRepository extends JpaRepository<QuestionQuiz, Long> {

    /**
     * Find all questions for a given quiz, fetching the quiz reference and options
     * in one query.
     */
    @Query("SELECT q FROM QuestionQuiz q JOIN FETCH q.quiz WHERE q.quiz.id = :quizId")
    List<QuestionQuiz> findByQuizIdWithQuiz(@Param("quizId") Long quizId);

    List<QuestionQuiz> findByQuizId(Long quizId);

    /**
     * Find a question by ID with its quiz eagerly loaded.
     */
    @Query("SELECT q FROM QuestionQuiz q JOIN FETCH q.quiz WHERE q.id = :id")
    Optional<QuestionQuiz> findByIdWithQuiz(@Param("id") Long id);
}
