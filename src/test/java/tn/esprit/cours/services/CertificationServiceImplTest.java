package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.Certification;
import tn.esprit.cours.entity.CertificationStatus;
import tn.esprit.cours.repository.CertificationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificationServiceImplTest {

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationServiceImpl certificationService;

    private Certification certification;

    @BeforeEach
    void setUp() {
        certification = new Certification();
        certification.setId(1L);
        certification.setTitle("Spring Boot Certification");
        certification.setSubtitle("Professional Level");
        certification.setStatus(CertificationStatus.ACTIVE);
        certification.setProgress("60%");
        certification.setDate("2026-06-01");
        certification.setEstimatedExam("2026-07-01");
        certification.setIcon("spring.png");
    }

    // ── createCertification ──

    @Test
    void createCertification_savesAndReturns() {
        when(certificationRepository.save(certification)).thenReturn(certification);

        Certification result = certificationService.createCertification(certification);

        assertNotNull(result);
        assertEquals("Spring Boot Certification", result.getTitle());
        verify(certificationRepository).save(certification);
    }

    // ── getCertificationById ──

    @Test
    void getCertificationById_found_returnsCertification() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));

        Certification result = certificationService.getCertificationById(1L);

        assertEquals(1L, result.getId());
        assertEquals(CertificationStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getCertificationById_notFound_throwsRuntimeException() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificationService.getCertificationById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllCertifications ──

    @Test
    void getAllCertifications_returnsList() {
        when(certificationRepository.findAll()).thenReturn(List.of(certification));

        List<Certification> result = certificationService.getAllCertifications();

        assertEquals(1, result.size());
        verify(certificationRepository).findAll();
    }

    @Test
    void getAllCertifications_emptyRepository_returnsEmptyList() {
        when(certificationRepository.findAll()).thenReturn(List.of());

        assertTrue(certificationService.getAllCertifications().isEmpty());
    }

    // ── updateCertification ──

    @Test
    void updateCertification_found_updatesAllFields() {
        Certification update = new Certification();
        update.setTitle("Updated Title");
        update.setSubtitle("Updated Subtitle");
        update.setStatus(CertificationStatus.PASSED);
        update.setProgress("100%");
        update.setDate("2026-08-01");
        update.setEstimatedExam("2026-09-01");
        update.setIcon("new.png");

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Certification result = certificationService.updateCertification(1L, update);

        assertEquals("Updated Title", result.getTitle());
        assertEquals(CertificationStatus.PASSED, result.getStatus());
        assertEquals("100%", result.getProgress());
        verify(certificationRepository).save(certification);
    }

    @Test
    void updateCertification_notFound_throwsRuntimeException() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> certificationService.updateCertification(99L, certification));
        verify(certificationRepository, never()).save(any());
    }

    // ── deleteCertification ──

    @Test
    void deleteCertification_found_deletesSuccessfully() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));

        certificationService.deleteCertification(1L);

        verify(certificationRepository).delete(certification);
    }

    @Test
    void deleteCertification_notFound_throwsRuntimeException() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> certificationService.deleteCertification(99L));
        verify(certificationRepository, never()).delete(any());
    }
}
