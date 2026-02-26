package tn.esprit.cours.services;

import tn.esprit.cours.entity.Certification;

import java.util.List;

public interface ICertificationService {
    Certification createCertification(Certification certification);

    Certification getCertificationById(Long id);

    List<Certification> getAllCertifications();

    Certification updateCertification(Long id, Certification certification);
    void deleteCertification(Long id);
}
