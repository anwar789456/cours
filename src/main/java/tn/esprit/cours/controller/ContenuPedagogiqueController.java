package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.ContenuPedagogique;
import tn.esprit.cours.services.IContenuPedagogiqueService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class ContenuPedagogiqueController {

    private final IContenuPedagogiqueService contenuService;// content service

    @PostMapping("/contenus/create-contenu/{coursId}")
    public ResponseEntity<ContenuPedagogique> createContenu(@PathVariable Long coursId,
            @RequestBody ContenuPedagogique contenu) {
        ContenuPedagogique created = contenuService.createContenu(contenu, coursId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/contenus/get-contenu-by-id/{id}")
    public ResponseEntity<ContenuPedagogique> getContenuById(@PathVariable Long id) {
        return ResponseEntity.ok(contenuService.getContenuById(id));
    }

    @GetMapping("/contenus/get-all-contenus")
    public ResponseEntity<List<ContenuPedagogique>> getAllContenus() {
        return ResponseEntity.ok(contenuService.getAllContenus());
    }

    @GetMapping("/contenus/get-contenus-by-cours-id/{coursId}")
    public ResponseEntity<List<ContenuPedagogique>> getContenusByCoursId(@PathVariable Long coursId) {
        return ResponseEntity.ok(contenuService.getContenusByCoursId(coursId));
    }

    @PutMapping("/contenus/update-contenu/{id}/{coursId}")
    public ResponseEntity<ContenuPedagogique> updateContenu(@PathVariable Long id,
            @PathVariable Long coursId,
            @RequestBody ContenuPedagogique contenu) {
        return ResponseEntity.ok(contenuService.updateContenu(id, contenu, coursId));
    }

    @DeleteMapping("/contenus/delete-contenu/{id}")
    public ResponseEntity<Void> deleteContenu(@PathVariable Long id) {
        contenuService.deleteContenu(id);
        return ResponseEntity.noContent().build();
    }
}
