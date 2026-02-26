package tn.esprit.cours.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "questions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private QuizLevel level;

    private LocalDate dateStart;

    private LocalDate dateEnd;

    @Enumerated(EnumType.STRING)
    private QuizStatus status;

    private Long courseId;

    private Integer xpReward;

    // Relationship with questions — kept LAZY (default for @OneToMany)
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<QuestionQuiz> questions = new ArrayList<>();
}
