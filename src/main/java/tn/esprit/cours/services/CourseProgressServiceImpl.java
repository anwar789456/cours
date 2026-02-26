package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.CourseProgress;
import tn.esprit.cours.repository.CourseProgressRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseProgressServiceImpl implements ICourseProgressService {

    private final CourseProgressRepository courseProgressRepository;

    @Override
    public List<CourseProgress> getAll() {
        return courseProgressRepository.findAll();
    }

    @Override
    public Optional<CourseProgress> getById(Long id) {
        return courseProgressRepository.findById(id);
    }

    @Override
    public List<CourseProgress> getByUserId(Long userId) {
        return courseProgressRepository.findByUserId(userId);
    }

    @Override
    public List<CourseProgress> getByCourseId(Long coursId) {
        return courseProgressRepository.findByCoursId(coursId);
    }

    @Override
    public Optional<CourseProgress> getByUserIdAndCourseId(Long userId, Long coursId) {
        return courseProgressRepository.findByUserIdAndCoursId(userId, coursId);
    }

    @Override
    public CourseProgress create(CourseProgress courseProgress) {
        return courseProgressRepository.save(courseProgress);
    }

    @Override
    public CourseProgress update(Long id, CourseProgress courseProgress) {
        courseProgress.setId(id);
        return courseProgressRepository.save(courseProgress);
    }

    @Override
    public CourseProgress updateProgress(Long id, Double progress) {
        CourseProgress existing = courseProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseProgress not found with id: " + id));
        existing.setProgress(progress);
        return courseProgressRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        courseProgressRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        courseProgressRepository.deleteByUserId(userId);
    }
}