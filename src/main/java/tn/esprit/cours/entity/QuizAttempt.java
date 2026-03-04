package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long quizId;

    private int score;

    private int totalQuestions;

    private int answeredQuestions;

    private boolean completed;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @ElementCollection
    @CollectionTable(name = "quiz_attempt_answers", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "question_id")
    @Column(name = "chosen_answer")
    @Builder.Default
    private Map<Long, String> answers = new HashMap<>();
}
