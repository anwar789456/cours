package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.ContenuPedagogique;

import java.util.List;

@Repository
public interface ContenuPedagogiqueRepository extends JpaRepository<ContenuPedagogique, Long> {

    /**
     * Find all contenus for a given cours, fetching the cours reference in one
     * query.
     */
    @Query("SELECT cp FROM ContenuPedagogique cp JOIN FETCH cp.cours WHERE cp.cours.id = :coursId")
    List<ContenuPedagogique> findByCoursIdWithCours(@Param("coursId") Long coursId);

    List<ContenuPedagogique> findByCoursId(Long coursId);
}
