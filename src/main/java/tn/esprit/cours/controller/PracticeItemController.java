package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.PracticeItem;
import tn.esprit.cours.services.IPracticeItemService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class PracticeItemController {

    private final IPracticeItemService practiceItemService;

    @PostMapping("/practice-items/create-practice-item")
    public ResponseEntity<PracticeItem> createPracticeItem(@RequestBody PracticeItem practiceItem) {
        PracticeItem created = practiceItemService.createPracticeItem(practiceItem);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/practice-items/get-practice-item-by-id/{id}")
    public ResponseEntity<PracticeItem> getPracticeItemById(@PathVariable Long id) {
        return ResponseEntity.ok(practiceItemService.getPracticeItemById(id));
    }

    @GetMapping("/practice-items/get-all-practice-items")
    public ResponseEntity<List<PracticeItem>> getAllPracticeItems() {
        return ResponseEntity.ok(practiceItemService.getAllPracticeItems());
    }

    @PutMapping("/practice-items/update-practice-item/{id}")
    public ResponseEntity<PracticeItem> updatePracticeItem(@PathVariable Long id, @RequestBody PracticeItem practiceItem) {
        return ResponseEntity.ok(practiceItemService.updatePracticeItem(id, practiceItem));
    }

    @DeleteMapping("/practice-items/delete-practice-item/{id}")
    public ResponseEntity<Void> deletePracticeItem(@PathVariable Long id) {
        practiceItemService.deletePracticeItem(id);
        return ResponseEntity.noContent().build();
    }
}
