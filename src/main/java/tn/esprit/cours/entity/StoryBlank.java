package tn.esprit.cours.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_blanks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "storyQuiz")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StoryBlank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private int blankIndex;

    private String correctWord;

    private String hint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_quiz_id")
    @JsonBackReference
    private StoryQuiz storyQuiz;
}
