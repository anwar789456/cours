package tn.esprit.cours.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "story_quizzes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "blanks")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StoryQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String title;

    @Column(length = 5000)
    private String storyTemplate;

    private String illustration;

    private int xpReward;

    private String difficulty;

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @OneToMany(mappedBy = "storyQuiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<StoryBlank> blanks = new ArrayList<>();
}
