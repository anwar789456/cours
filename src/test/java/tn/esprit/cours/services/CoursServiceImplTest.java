package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.entity.ContenuPedagogique;
import tn.esprit.cours.entity.ContentFile;
import tn.esprit.cours.repository.CoursRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursServiceImplTest {

    @Mock
    private CoursRepository coursRepository;

    @InjectMocks
    private CoursServiceImpl coursService;

    private Cours cours;

    @BeforeEach
    void setUp() {
        cours = new Cours();
        cours.setId(1L);
        cours.setTitle("Java Basics");
        cours.setDescription("Introduction to Java");
        cours.setContent("Java content");
        cours.setArchived(false);
        cours.setContenus(new ArrayList<>());
    }

    // ── createCours ──

    @Test
    void createCours_savesAndReturnsCours() {
        when(coursRepository.save(cours)).thenReturn(cours);

        Cours result = coursService.createCours(cours);

        assertNotNull(result);
        assertEquals("Java Basics", result.getTitle());
        verify(coursRepository).save(cours);
    }

    @Test
    void createCours_withContenus_setsBackReferences() {
        ContentFile file = new ContentFile();
        ContenuPedagogique contenu = new ContenuPedagogique();
        contenu.setFiles(List.of(file));

        Cours coursWithContenu = new Cours();
        coursWithContenu.setContenus(new ArrayList<>(List.of(contenu)));

        when(coursRepository.save(any(Cours.class))).thenAnswer(inv -> inv.getArgument(0));

        coursService.createCours(coursWithContenu);

        assertSame(coursWithContenu, contenu.getCours());
        assertSame(contenu, file.getContenu());
    }

    // ── getCoursById ──

    @Test
    void getCoursById_existingId_returnsCours() {
        when(coursRepository.findByIdWithContenus(1L)).thenReturn(Optional.of(cours));

        Cours result = coursService.getCoursById(1L);

        assertEquals(1L, result.getId());
        verify(coursRepository).findByIdWithContenus(1L);
    }

    @Test
    void getCoursById_notFound_throwsRuntimeException() {
        when(coursRepository.findByIdWithContenus(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getCoursById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllCours ──

    @Test
    void getAllCours_returnsList() {
        List<Cours> list = List.of(cours);
        when(coursRepository.findAllWithContenus()).thenReturn(list);

        List<Cours> result = coursService.getAllCours();

        assertEquals(1, result.size());
        verify(coursRepository).findAllWithContenus();
    }

    // ── updateCours ──

    @Test
    void updateCours_existingId_updatesFields() {
        Cours update = new Cours();
        update.setTitle("Advanced Java");
        update.setDescription("Deep dive");
        update.setContent("Advanced content");
        update.setContenus(new ArrayList<>());

        when(coursRepository.findByIdWithContenus(1L)).thenReturn(Optional.of(cours));
        when(coursRepository.save(any(Cours.class))).thenAnswer(inv -> inv.getArgument(0));

        Cours result = coursService.updateCours(1L, update);

        assertEquals("Advanced Java", result.getTitle());
        assertEquals("Deep dive", result.getDescription());
        verify(coursRepository).save(cours);
    }

    @Test
    void updateCours_notFound_throwsRuntimeException() {
        when(coursRepository.findByIdWithContenus(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> coursService.updateCours(99L, cours));
    }

    // ── deleteCours ──

    @Test
    void deleteCours_existingId_deletesSuccessfully() {
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));

        coursService.deleteCours(1L);

        verify(coursRepository).delete(cours);
    }

    @Test
    void deleteCours_notFound_throwsRuntimeException() {
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> coursService.deleteCours(99L));
        verify(coursRepository, never()).delete(any());
    }

    // ── archiveCours ──

    @Test
    void archiveCours_setsArchivedTrue() {
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(coursRepository.save(cours)).thenReturn(cours);

        Cours result = coursService.archiveCours(1L, true);

        assertTrue(result.isArchived());
        verify(coursRepository).save(cours);
    }

    @Test
    void archiveCours_setsArchivedFalse() {
        cours.setArchived(true);
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(coursRepository.save(cours)).thenReturn(cours);

        Cours result = coursService.archiveCours(1L, false);

        assertFalse(result.isArchived());
    }

    @Test
    void archiveCours_notFound_throwsRuntimeException() {
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> coursService.archiveCours(99L, true));
    }
}
