package tn.esprit.cours.services;

import tn.esprit.cours.entity.ContenuPedagogique;

import java.util.List;

public interface IContenuPedagogiqueService {
    ContenuPedagogique createContenu(ContenuPedagogique contenu, Long coursId);

    ContenuPedagogique getContenuById(Long id);

    List<ContenuPedagogique> getAllContenus();

    List<ContenuPedagogique> getContenusByCoursId(Long coursId);

    ContenuPedagogique updateContenu(Long id, ContenuPedagogique contenu, Long coursId);
    void deleteContenu(Long id);
}
