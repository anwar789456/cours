package tn.esprit.cours.entity.dto;

import lombok.Data;

@Data
public class AvatarChatRequest {
    private String message;
    private Long userId;
    private String currentPage;
}
