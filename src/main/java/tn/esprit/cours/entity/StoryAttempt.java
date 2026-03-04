package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "story_attempts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long storyQuizId;

    private boolean completed;

    private int score;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @ElementCollection
    @CollectionTable(name = "story_attempt_answers", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "blank_index")
    @Column(name = "chosen_word")
    @Builder.Default
    private Map<Integer, String> answers = new HashMap<>();
}
