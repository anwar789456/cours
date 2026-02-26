package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.ContentFile;
import tn.esprit.cours.entity.ContenuPedagogique;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.repository.ContenuPedagogiqueRepository;
import tn.esprit.cours.repository.CoursRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContenuPedagogiqueServiceImpl implements IContenuPedagogiqueService {

    private final ContenuPedagogiqueRepository contenuRepository;
    private final CoursRepository coursRepository;

    @Override
    public ContenuPedagogique createContenu(ContenuPedagogique contenu, Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours not found with id: " + coursId));
        contenu.setCours(cours);
        if (contenu.getFiles() != null) {
            for (ContentFile file : contenu.getFiles()) {
                file.setContenu(contenu);
            }
        }
        return contenuRepository.save(contenu);
    }

    @Override
    public ContenuPedagogique getContenuById(Long id) {
        return contenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContenuPedagogique not found with id: " + id));
    }

    @Override
    public List<ContenuPedagogique> getAllContenus() {
        return contenuRepository.findAll();
    }

    @Override
    public List<ContenuPedagogique> getContenusByCoursId(Long coursId) {
        return contenuRepository.findByCoursId(coursId);
    }

    @Override
    public ContenuPedagogique updateContenu(Long id, ContenuPedagogique contenu, Long coursId) {
        ContenuPedagogique existing = contenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContenuPedagogique not found with id: " + id));
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours not found with id: " + coursId));
        existing.setTitleC(contenu.getTitleC());
        existing.setDuration(contenu.getDuration());
        existing.setContentType(contenu.getContentType());
        existing.setCours(cours);
        return contenuRepository.save(existing);
    }

    @Override
    public void deleteContenu(Long id) {
        ContenuPedagogique existing = contenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContenuPedagogique not found with id: " + id));
        contenuRepository.delete(existing);
    }
}
