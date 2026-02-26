package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.services.ICoursService;
import tn.esprit.cours.services.AiService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final AiService aiService;
    private final ICoursService coursService;

    @PostMapping("/cours/create-cours")
    public ResponseEntity<Cours> createCours(@RequestBody Cours cours) {
        Cours created = coursService.createCours(cours);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/cours/get-cours-by-id/{id}")
    public ResponseEntity<Cours> getCoursById(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getCoursById(id));
    }

    @GetMapping("/cours/get-all-cours")
    public ResponseEntity<List<Cours>> getAllCours() {
        return ResponseEntity.ok(coursService.getAllCours());
    }

    @PutMapping("/cours/update-cours/{id}")
    public ResponseEntity<Cours> updateCours(@PathVariable Long id, @RequestBody Cours cours) {
        return ResponseEntity.ok(coursService.updateCours(id, cours));
    }

    @DeleteMapping("/cours/delete-cours/{id}")
    public ResponseEntity<Void> deleteCours(@PathVariable Long id) {
        coursService.deleteCours(id);
        return ResponseEntity.noContent().build();
    }

    // ================= AI GENERATED DESCRIPTION =================
    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        try {
            String description = aiService.generateDescription(title);
            return ResponseEntity.ok(Map.of("description", description));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "AI generation failed"));
        }
    }
}