package tn.esprit.cours.services;

import tn.esprit.cours.entity.CourseProgress;
import java.util.Optional;
import java.util.List;

public interface ICourseProgressService {
    List<CourseProgress> getAll();
    Optional<CourseProgress> getById(Long id);
    List<CourseProgress> getByUserId(Long userId);
    List<CourseProgress> getByCourseId(Long coursId);
    Optional<CourseProgress> getByUserIdAndCourseId(Long userId, Long coursId);
    CourseProgress create(CourseProgress courseProgress);
    CourseProgress update(Long id, CourseProgress courseProgress);
    CourseProgress updateProgress(Long id, Double progress);
    void delete(Long id);
    void deleteByUserId(Long userId);
}
