package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.CourseProgress;
import tn.esprit.cours.services.ICourseProgressService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CourseProgressController {
    private final ICourseProgressService courseProgressService;

    // GET all
    @GetMapping("/courseprogresses/get-all-progresses")
    public ResponseEntity<List<CourseProgress>> getAll() {
        return ResponseEntity.ok(courseProgressService.getAll());
    }

    // GET by id
    @GetMapping("/courseprogress/get-progress-by-id/{id}")
    public ResponseEntity<CourseProgress> getById(@PathVariable Long id) {
        return courseProgressService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET by userId
    @GetMapping("/courseprogress/get-progress-by-user-id/{userId}")
    public ResponseEntity<List<CourseProgress>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(courseProgressService.getByUserId(userId));
    }

    // GET by coursId
    @GetMapping("/courseprogress/get-progress-by-cours-id/{coursId}")
    public ResponseEntity<List<CourseProgress>> getByCourseId(@PathVariable Long coursId) {
        return ResponseEntity.ok(courseProgressService.getByCourseId(coursId));
    }

    // GET by userId + coursId (specific progress entry)
    @GetMapping("/courseprogress/get-progress-by-user-id/{userId}/get-progress-by-cours-id/{coursId}")
    public ResponseEntity<CourseProgress> getByUserAndCourse(
            @PathVariable Long userId,
            @PathVariable Long coursId) {
        return courseProgressService.getByUserIdAndCourseId(userId, coursId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping("/courseprogress/create-progress")
    public ResponseEntity<CourseProgress> create(@RequestBody CourseProgress courseProgress) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseProgressService.create(courseProgress));
    }

    // PUT update
    @PutMapping("/courseprogress/update-progress-by-id/{id}")
    public ResponseEntity<CourseProgress> update(
            @PathVariable Long id,
            @RequestBody CourseProgress courseProgress) {
        return courseProgressService.getById(id)
                .map(existing -> ResponseEntity.ok(courseProgressService.update(id, courseProgress)))
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH update progress value only
    @PatchMapping("/courseprogress/patch-progress/{id}/progress")
    public ResponseEntity<CourseProgress> updateProgress(
            @PathVariable Long id,
            @RequestParam Double progress) {
        return courseProgressService.getById(id)
                .map(existing -> ResponseEntity.ok(courseProgressService.updateProgress(id, progress)))
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE by id
    @DeleteMapping("/courseprogress/delete-progress-by-id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return courseProgressService.getById(id)
                .map(existing -> {
                    courseProgressService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE by userId (remove all progress for a user)
    @DeleteMapping("/courseprogress/delete-progress-by-user-id/{userId}")
    public ResponseEntity<Void> deleteByUserId(@PathVariable Long userId) {
        courseProgressService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
    
}
