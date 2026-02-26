package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.Session;
import tn.esprit.cours.services.ISessionService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class SessionController {

    private final ISessionService sessionService;

    @PostMapping("/sessions/create-session")
    public ResponseEntity<Session> createSession(@RequestBody Session session) {
        Session created = sessionService.createSession(session);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/get-session-by-id/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/sessions/get-all-sessions")
    public ResponseEntity<List<Session>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @PutMapping("/sessions/update-session/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        return ResponseEntity.ok(sessionService.updateSession(id, session));
    }

    @DeleteMapping("/sessions/delete-session/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
