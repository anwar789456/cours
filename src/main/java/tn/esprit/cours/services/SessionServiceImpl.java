package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.Session;
import tn.esprit.cours.repository.SessionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements ISessionService {

    private final SessionRepository sessionRepository;

    @Override
    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session updateSession(Long id, Session session) {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        existing.setTitle(session.getTitle());
        existing.setLevel(session.getLevel());
        existing.setDate(session.getDate());
        existing.setTime(session.getTime());
        existing.setDuration(session.getDuration());
        existing.setReadinessScore(session.getReadinessScore());
        existing.setStatus(session.getStatus());
        existing.setImage(session.getImage());
        existing.setTip(session.getTip());
        return sessionRepository.save(existing);
    }

    @Override
    public void deleteSession(Long id) {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        sessionRepository.delete(existing);
    }
}
