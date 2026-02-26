package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.PracticeItem;
import tn.esprit.cours.repository.PracticeItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeItemServiceImpl implements IPracticeItemService {

    private final PracticeItemRepository practiceItemRepository;

    @Override
    public PracticeItem createPracticeItem(PracticeItem practiceItem) {
        return practiceItemRepository.save(practiceItem);
    }

    @Override
    public PracticeItem getPracticeItemById(Long id) {
        return practiceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PracticeItem not found with id: " + id));
    }

    @Override
    public List<PracticeItem> getAllPracticeItems() {
        return practiceItemRepository.findAll();
    }

    @Override
    public PracticeItem updatePracticeItem(Long id, PracticeItem practiceItem) {
        PracticeItem existing = practiceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PracticeItem not found with id: " + id));
        existing.setTitle(practiceItem.getTitle());
        existing.setDescription(practiceItem.getDescription());
        existing.setColor(practiceItem.getColor());
        return practiceItemRepository.save(existing);
    }

    @Override
    public void deletePracticeItem(Long id) {
        PracticeItem existing = practiceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PracticeItem not found with id: " + id));
        practiceItemRepository.delete(existing);
    }
}
