package tn.esprit.cours.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.Cours;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {

    /**
     * Fetch a single Cours with all its ContenuPedagogique children in ONE query.
     * Solves the N+1 problem for getCoursById.
     */
    @Query("SELECT c FROM Cours c LEFT JOIN FETCH c.image LEFT JOIN FETCH c.contenus WHERE c.id = :id")
    Optional<Cours> findByIdWithContenus(@Param("id") Long id);

    /**
     * Fetch all Cours with their ContenuPedagogique children in ONE query.
     * Uses DISTINCT to avoid cartesian product duplicates.
     */
    @Query("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.image LEFT JOIN FETCH c.contenus")
    List<Cours> findAllWithContenus();

    /**
     * Paginated Cours listing (without fetching children — lightweight).
     */
    @Query("SELECT c FROM Cours c")
    Page<Cours> findAllPaged(Pageable pageable);
}
