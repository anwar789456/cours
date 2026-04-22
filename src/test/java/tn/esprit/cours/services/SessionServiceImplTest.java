package tn.esprit.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.cours.entity.Session;
import tn.esprit.cours.entity.SessionStatus;
import tn.esprit.cours.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.setId(1L);
        session.setTitle("Java Live Session");
        session.setLevel("BEGINNER");
        session.setDate(LocalDateTime.of(2026, 5, 10, 10, 0));
        session.setTime(LocalDateTime.of(2026, 5, 10, 10, 0));
        session.setDuration("1h30");
        session.setReadinessScore(80);
        session.setStatus(SessionStatus.UPCOMING);
        session.setImage("session.png");
        session.setTip("Prepare your IDE before the session.");
    }

    // ── createSession ──

    @Test
    void createSession_savesAndReturnsSession() {
        when(sessionRepository.save(session)).thenReturn(session);

        Session result = sessionService.createSession(session);

        assertNotNull(result);
        assertEquals("Java Live Session", result.getTitle());
        verify(sessionRepository).save(session);
    }

    // ── getSessionById ──

    @Test
    void getSessionById_existingId_returnsSession() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Session result = sessionService.getSessionById(1L);

        assertEquals(1L, result.getId());
        assertEquals(SessionStatus.UPCOMING, result.getStatus());
        verify(sessionRepository).findById(1L);
    }

    @Test
    void getSessionById_notFound_throwsRuntimeException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.getSessionById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getAllSessions ──

    @Test
    void getAllSessions_returnsList() {
        when(sessionRepository.findAll()).thenReturn(List.of(session));

        List<Session> result = sessionService.getAllSessions();

        assertEquals(1, result.size());
        verify(sessionRepository).findAll();
    }

    @Test
    void getAllSessions_emptyRepository_returnsEmptyList() {
        when(sessionRepository.findAll()).thenReturn(List.of());

        List<Session> result = sessionService.getAllSessions();

        assertTrue(result.isEmpty());
    }

    // ── updateSession ──

    @Test
    void updateSession_existingId_updatesAllFields() {
        Session update = new Session();
        update.setTitle("Advanced Session");
        update.setLevel("ADVANCED");
        update.setDate(LocalDateTime.of(2026, 6, 1, 9, 0));
        update.setTime(LocalDateTime.of(2026, 6, 1, 9, 0));
        update.setDuration("2h");
        update.setReadinessScore(95);
        update.setStatus(SessionStatus.COMPLETED);
        update.setImage("new.png");
        update.setTip("Review the slides.");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Session result = sessionService.updateSession(1L, update);

        assertEquals("Advanced Session", result.getTitle());
        assertEquals("ADVANCED", result.getLevel());
        assertEquals(SessionStatus.COMPLETED, result.getStatus());
        assertEquals(95, result.getReadinessScore());
        verify(sessionRepository).save(session);
    }

    @Test
    void updateSession_notFound_throwsRuntimeException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.updateSession(99L, session));
        verify(sessionRepository, never()).save(any());
    }

    // ── deleteSession ──

    @Test
    void deleteSession_existingId_deletesSuccessfully() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.deleteSession(1L);

        verify(sessionRepository).delete(session);
    }

    @Test
    void deleteSession_notFound_throwsRuntimeException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.deleteSession(99L));
        verify(sessionRepository, never()).delete(any());
    }
}
