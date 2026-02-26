package tn.esprit.cours.services;

import tn.esprit.cours.entity.PracticeItem;

import java.util.List;

public interface IPracticeItemService {
    PracticeItem createPracticeItem(PracticeItem practiceItem);

    PracticeItem getPracticeItemById(Long id);

    List<PracticeItem> getAllPracticeItems();

    PracticeItem updatePracticeItem(Long id, PracticeItem practiceItem);
    void deletePracticeItem(Long id);
}
