package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_cards")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer totalQuestions;

    private String level;

    private Integer progress;

    @Enumerated(EnumType.STRING)
    private QuizCardStatus status;

    private String icon;

    private Integer xpRequired;
}
