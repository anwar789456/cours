package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.Certification;
import tn.esprit.cours.services.ICertificationService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CertificationController {

    private final ICertificationService certificationService;

    @PostMapping("/certifications/create-certification")
    public ResponseEntity<Certification> createCertification(@RequestBody Certification certification) {
        Certification created = certificationService.createCertification(certification);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/certifications/get-certification-by-id/{id}")
    public ResponseEntity<Certification> getCertificationById(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getCertificationById(id));
    }

    @GetMapping("/certifications/get-all-certifications")
    public ResponseEntity<List<Certification>> getAllCertifications() {
        return ResponseEntity.ok(certificationService.getAllCertifications());
    }

    @PutMapping("/certifications/update-certification/{id}")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long id,
            @RequestBody Certification certification) {
        return ResponseEntity.ok(certificationService.updateCertification(id, certification));
    }

    @DeleteMapping("/certifications/delete-certification/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }
}
