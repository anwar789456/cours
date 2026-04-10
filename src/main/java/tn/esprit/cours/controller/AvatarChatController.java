package tn.esprit.cours.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import tn.esprit.cours.entity.dto.AvatarChatRequest;
import tn.esprit.cours.entity.dto.AvatarChatResponse;
import tn.esprit.cours.services.AvatarChatService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/cours/ai-avatar")
@RequiredArgsConstructor
public class AvatarChatController {

    private final AvatarChatService avatarChatService;

    // ── Non-streaming endpoint (floating widget) ──────────────────────────────
    @PostMapping("/chat")
    public ResponseEntity<AvatarChatResponse> chat(@RequestBody AvatarChatRequest request) {
        return ResponseEntity.ok(avatarChatService.chat(request));
    }

    // ── Prompt-preparation endpoint (fast-path for tutor page) ───────────────
    @PostMapping("/prepare")
    public ResponseEntity<Map<String, String>> prepare(@RequestBody AvatarChatRequest request) {
        String prompt = avatarChatService.buildPrompt(request);
        return ResponseEntity.ok(Map.of(
                "prompt", prompt,
                "model",  avatarChatService.getModel()
        ));
    }

    // ── Streaming endpoint (tutor page) ───────────────────────────────────────
    // GET so EventSource in the browser can connect (EventSource only supports GET)
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "0") Long userId,
            @RequestParam(required = false, defaultValue = "") String currentPage,
            HttpServletResponse response
    ) {
        // Tell nginx not to buffer this SSE response
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");

        SseEmitter emitter = new SseEmitter(60_000L);

        AvatarChatRequest req = new AvatarChatRequest();
        req.setMessage(message);
        req.setUserId(userId > 0 ? userId : null);
        req.setCurrentPage(currentPage);

        AtomicReference<Disposable> dispRef = new AtomicReference<>();

        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> {
            Disposable d = dispRef.get();
            if (d != null && !d.isDisposed()) d.dispose();
        });

        Disposable disposable = avatarChatService.streamChat(req)
                .subscribe(
                        token -> {
                            try {
                                if ("[ERROR]".equals(token)) {
                                    emitter.send(SseEmitter.event().name("error")
                                            .data("Oops! Try asking again."));
                                    emitter.complete();
                                } else {
                                    String safe = token.replace('\n', ' ').replace('\r', ' ');
                                    emitter.send(" " + safe);
                                }
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            try { emitter.send(SseEmitter.event().name("error").data("Connection failed")); }
                            catch (IOException ignored) {}
                            emitter.complete();
                        },
                        () -> {
                            try { emitter.send(SseEmitter.event().name("done").data("[DONE]")); }
                            catch (IOException ignored) {}
                            emitter.complete();
                        }
                );

        dispRef.set(disposable);
        return emitter;
    }
}
