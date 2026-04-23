package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.ContentFile;
import tn.esprit.cours.entity.ContenuPedagogique;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.repository.ContenuPedagogiqueRepository;
import tn.esprit.cours.repository.CoursRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContenuPedagogiqueServiceImplTest {

    @Mock
    private ContenuPedagogiqueRepository contenuRepository;

    @Mock
    private CoursRepository coursRepository;

    @InjectMocks
    private ContenuPedagogiqueServiceImpl contenuService;

    private Cours cours;
    private ContenuPedagogique contenu;

    @BeforeEach
    void setUp() {
        cours = new Cours();
        cours.setId(1L);
        cours.setTitle("Java Basics");

        contenu = new ContenuPedagogique();
        contenu.setIdContent(10L);
        contenu.setTitleC("Chapter 1");
        contenu.setDuration(30);
        contenu.setContentType("VIDEO");
    }

    // ── createContenu ──

    @Test
    void createContenu_coursFound_setsCoursAndSaves() {
        contenu.setFiles(null);
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(contenuRepository.save(contenu)).thenReturn(contenu);

        ContenuPedagogique result = contenuService.createContenu(contenu, 1L);

        assertSame(cours, result.getCours());
        verify(contenuRepository).save(contenu);
    }

    @Test
    void createContenu_withFiles_setsBackReferences() {
        ContentFile file = new ContentFile();
        contenu.setFiles(new ArrayList<>(List.of(file)));

        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(contenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        contenuService.createContenu(contenu, 1L);

        assertSame(contenu, file.getContenu());
    }

    @Test
    void createContenu_coursNotFound_throwsRuntimeException() {
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> contenuService.createContenu(contenu, 99L));
        verify(contenuRepository, never()).save(any());
    }

    // ── getContenuById ──

    @Test
    void getContenuById_found_returnsContenu() {
        when(contenuRepository.findById(10L)).thenReturn(Optional.of(contenu));

        ContenuPedagogique result = contenuService.getContenuById(10L);

        assertEquals("Chapter 1", result.getTitleC());
    }

    @Test
    void getContenuById_notFound_throwsRuntimeException() {
        when(contenuRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> contenuService.getContenuById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllContenus ──

    @Test
    void getAllContenus_returnsList() {
        when(contenuRepository.findAll()).thenReturn(List.of(contenu));

        assertEquals(1, contenuService.getAllContenus().size());
    }

    // ── getContenusByCoursId ──

    @Test
    void getContenusByCoursId_returnsFilteredList() {
        when(contenuRepository.findByCoursId(1L)).thenReturn(List.of(contenu));

        List<ContenuPedagogique> result = contenuService.getContenusByCoursId(1L);

        assertEquals(1, result.size());
        verify(contenuRepository).findByCoursId(1L);
    }

    @Test
    void getContenusByCoursId_noResults_returnsEmptyList() {
        when(contenuRepository.findByCoursId(99L)).thenReturn(List.of());

        assertTrue(contenuService.getContenusByCoursId(99L).isEmpty());
    }

    // ── updateContenu ──

    @Test
    void updateContenu_found_updatesFields() {
        ContenuPedagogique update = new ContenuPedagogique();
        update.setTitleC("Chapter 2");
        update.setDuration(60);
        update.setContentType("PDF");

        when(contenuRepository.findById(10L)).thenReturn(Optional.of(contenu));
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(contenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContenuPedagogique result = contenuService.updateContenu(10L, update, 1L);

        assertEquals("Chapter 2", result.getTitleC());
        assertEquals(60, result.getDuration());
        assertEquals("PDF", result.getContentType());
    }

    @Test
    void updateContenu_contenuNotFound_throwsRuntimeException() {
        when(contenuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contenuService.updateContenu(99L, contenu, 1L));
    }

    @Test
    void updateContenu_coursNotFound_throwsRuntimeException() {
        when(contenuRepository.findById(10L)).thenReturn(Optional.of(contenu));
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contenuService.updateContenu(10L, contenu, 99L));
    }

    // ── deleteContenu ──

    @Test
    void deleteContenu_found_deletesSuccessfully() {
        when(contenuRepository.findById(10L)).thenReturn(Optional.of(contenu));

        contenuService.deleteContenu(10L);

        verify(contenuRepository).delete(contenu);
    }

    @Test
    void deleteContenu_notFound_throwsRuntimeException() {
        when(contenuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> contenuService.deleteContenu(99L));
        verify(contenuRepository, never()).delete(any());
    }
}
