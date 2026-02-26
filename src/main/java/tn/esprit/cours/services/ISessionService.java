package tn.esprit.cours.services;

import tn.esprit.cours.entity.Session;

import java.util.List;

public interface ISessionService {
    Session createSession(Session session);

    Session getSessionById(Long id);

    List<Session> getAllSessions();

    Session updateSession(Long id, Session session);
    void deleteSession(Long id);
}
