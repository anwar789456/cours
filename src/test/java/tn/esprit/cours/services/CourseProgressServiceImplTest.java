package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.CourseProgress;
import tn.esprit.cours.repository.CourseProgressRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseProgressServiceImplTest {

    @Mock
    private CourseProgressRepository courseProgressRepository;

    @InjectMocks
    private CourseProgressServiceImpl courseProgressService;

    private CourseProgress progress;

    @BeforeEach
    void setUp() {
        progress = new CourseProgress();
        progress.setId(1L);
        progress.setUserId(10L);
        progress.setCoursId(20L);
        progress.setProgress(45.0);
    }

    // ── getAll ──

    @Test
    void getAll_returnsList() {
        when(courseProgressRepository.findAll()).thenReturn(List.of(progress));

        List<CourseProgress> result = courseProgressService.getAll();

        assertEquals(1, result.size());
        verify(courseProgressRepository).findAll();
    }

    // ── getById ──

    @Test
    void getById_found_returnsOptional() {
        when(courseProgressRepository.findById(1L)).thenReturn(Optional.of(progress));

        Optional<CourseProgress> result = courseProgressService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getById_notFound_returnsEmpty() {
        when(courseProgressRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(courseProgressService.getById(99L).isEmpty());
    }

    // ── getByUserId ──

    @Test
    void getByUserId_returnsList() {
        when(courseProgressRepository.findByUserId(10L)).thenReturn(List.of(progress));

        List<CourseProgress> result = courseProgressService.getByUserId(10L);

        assertEquals(1, result.size());
        verify(courseProgressRepository).findByUserId(10L);
    }

    // ── getByCourseId ──

    @Test
    void getByCourseId_returnsList() {
        when(courseProgressRepository.findByCoursId(20L)).thenReturn(List.of(progress));

        List<CourseProgress> result = courseProgressService.getByCourseId(20L);

        assertEquals(1, result.size());
        verify(courseProgressRepository).findByCoursId(20L);
    }

    // ── getByUserIdAndCourseId ──

    @Test
    void getByUserIdAndCourseId_found_returnsOptional() {
        when(courseProgressRepository.findByUserIdAndCoursId(10L, 20L))
                .thenReturn(Optional.of(progress));

        Optional<CourseProgress> result = courseProgressService.getByUserIdAndCourseId(10L, 20L);

        assertTrue(result.isPresent());
    }

    @Test
    void getByUserIdAndCourseId_notFound_returnsEmpty() {
        when(courseProgressRepository.findByUserIdAndCoursId(1L, 1L)).thenReturn(Optional.empty());

        assertTrue(courseProgressService.getByUserIdAndCourseId(1L, 1L).isEmpty());
    }

    // ── create ──

    @Test
    void create_savesAndReturns() {
        when(courseProgressRepository.save(progress)).thenReturn(progress);

        CourseProgress result = courseProgressService.create(progress);

        assertNotNull(result);
        assertEquals(45.0, result.getProgress());
        verify(courseProgressRepository).save(progress);
    }

    // ── update ──

    @Test
    void update_setsIdAndSaves() {
        CourseProgress updated = new CourseProgress();
        updated.setUserId(10L);
        updated.setCoursId(20L);
        updated.setProgress(80.0);

        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.update(1L, updated);

        assertEquals(1L, result.getId());
        assertEquals(80.0, result.getProgress());
    }

    // ── updateProgress ──

    @Test
    void updateProgress_found_updatesProgressField() {
        when(courseProgressRepository.findById(1L)).thenReturn(Optional.of(progress));
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.updateProgress(1L, 90.0);

        assertEquals(90.0, result.getProgress());
        verify(courseProgressRepository).save(progress);
    }

    @Test
    void updateProgress_notFound_throwsRuntimeException() {
        when(courseProgressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> courseProgressService.updateProgress(99L, 50.0));
    }

    // ── delete ──

    @Test
    void delete_callsDeleteById() {
        courseProgressService.delete(1L);

        verify(courseProgressRepository).deleteById(1L);
    }

    // ── deleteByUserId ──

    @Test
    void deleteByUserId_callsDeleteByUserId() {
        courseProgressService.deleteByUserId(10L);

        verify(courseProgressRepository).deleteByUserId(10L);
    }
}
