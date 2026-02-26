package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.ContentFile;
import tn.esprit.cours.entity.ContenuPedagogique;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.repository.CoursRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursServiceImpl implements ICoursService {

    private final CoursRepository coursRepository;

    @Override
    public Cours createCours(Cours cours) {
        if (cours.getContenus() != null) {
            for (ContenuPedagogique contenu : cours.getContenus()) {
                contenu.setCours(cours);
                if (contenu.getFiles() != null) {
                    for (ContentFile file : contenu.getFiles()) {
                        file.setContenu(contenu);
                    }
                }
            }
        }
        return coursRepository.save(cours);
    }

    @Override
    public Cours getCoursById(Long id) {
        return coursRepository.findByIdWithContenus(id)
                .orElseThrow(() -> new RuntimeException("Cours not found with id: " + id));
    }

    @Override
    public List<Cours> getAllCours() {
        return coursRepository.findAllWithContenus();
    }

    @Override
    public Cours updateCours(Long id, Cours cours) {
        Cours existing = coursRepository.findByIdWithContenus(id)
                .orElseThrow(() -> new RuntimeException("Cours not found with id: " + id));
        existing.setTitle(cours.getTitle());
        existing.setDescription(cours.getDescription());
        existing.setContent(cours.getContent());
        existing.setImage(cours.getImage());

        if (cours.getContenus() != null) {
            existing.getContenus().clear();
            for (ContenuPedagogique contenu : cours.getContenus()) {
                contenu.setCours(existing);
                if (contenu.getFiles() != null) {
                    for (ContentFile file : contenu.getFiles()) {
                        file.setContenu(contenu);
                    }
                }
                existing.getContenus().add(contenu);
            }
        }

        return coursRepository.save(existing);
    }

    @Override
    public void deleteCours(Long id) {
        Cours existing = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours not found with id: " + id));
        coursRepository.delete(existing);
    }
}
