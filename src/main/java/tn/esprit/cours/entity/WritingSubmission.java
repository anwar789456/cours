package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "writing_submissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WritingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long writingPromptId;

    @Column(length = 5000)
    private String submittedText;

    private int overallScore;

    private int grammarScore;

    private int spellingScore;

    private int contentScore;

    @Column(length = 5000)
    private String overallFeedback;

    @Column(length = 10000)
    private String feedbackJson;

    private boolean completed;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
