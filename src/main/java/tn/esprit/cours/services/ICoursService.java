package tn.esprit.cours.services;

import tn.esprit.cours.entity.Cours;

import java.util.List;

public interface ICoursService {
    Cours createCours(Cours cours);

    Cours getCoursById(Long id);

    List<Cours> getAllCours();

    Cours updateCours(Long id, Cours cours);
    void deleteCours(Long id);
}
