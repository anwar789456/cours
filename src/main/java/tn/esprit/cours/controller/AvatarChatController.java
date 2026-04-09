package tn.esprit.cours.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cours.entity.dto.AvatarChatRequest;
import tn.esprit.cours.entity.dto.AvatarChatResponse;
import tn.esprit.cours.services.AvatarChatService;

@RestController
@RequestMapping("/api/cours/ai-avatar")
@RequiredArgsConstructor
public class AvatarChatController {

    private final AvatarChatService avatarChatService;

    @PostMapping("/chat")
    public ResponseEntity<AvatarChatResponse> chat(@RequestBody AvatarChatRequest request) {
        AvatarChatResponse response = avatarChatService.chat(request);
        return ResponseEntity.ok(response);
    }
}
