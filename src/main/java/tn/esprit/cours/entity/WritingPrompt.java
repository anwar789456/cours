package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "writing_prompts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WritingPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String difficulty;

    private int xpReward;

    private int minWords;

    private int maxWords;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean archived = false;
}
