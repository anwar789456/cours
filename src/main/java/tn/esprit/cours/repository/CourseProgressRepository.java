package tn.esprit.cours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cours.entity.CourseProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
    List<CourseProgress> findByUserId(Long userId);
    List<CourseProgress> findByCoursId(Long coursId);
    Optional<CourseProgress> findByUserIdAndCoursId(Long userId, Long coursId);
    void deleteByUserId(Long userId);
}