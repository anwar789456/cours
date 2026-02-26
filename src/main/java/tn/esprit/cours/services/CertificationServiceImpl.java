package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.Certification;
import tn.esprit.cours.repository.CertificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements ICertificationService {

    private final CertificationRepository certificationRepository;

    @Override
    public Certification createCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    @Override
    public Certification getCertificationById(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with id: " + id));
    }

    @Override
    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    @Override
    public Certification updateCertification(Long id, Certification certification) {
        Certification existing = certificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with id: " + id));
        existing.setTitle(certification.getTitle());
        existing.setSubtitle(certification.getSubtitle());
        existing.setStatus(certification.getStatus());
        existing.setProgress(certification.getProgress());
        existing.setDate(certification.getDate());
        existing.setEstimatedExam(certification.getEstimatedExam());
        existing.setIcon(certification.getIcon());
        return certificationRepository.save(existing);
    }

    @Override
    public void deleteCertification(Long id) {
        Certification existing = certificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with id: " + id));
        certificationRepository.delete(existing);
    }
}
