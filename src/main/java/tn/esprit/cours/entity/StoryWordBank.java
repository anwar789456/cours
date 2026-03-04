package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "story_word_bank")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoryWordBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storyQuizId;

    @ElementCollection
    @CollectionTable(name = "word_bank_words", joinColumns = @JoinColumn(name = "word_bank_id"))
    @Column(name = "word")
    @Builder.Default
    private List<String> words = new ArrayList<>();
}
