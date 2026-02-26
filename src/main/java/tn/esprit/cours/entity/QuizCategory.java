package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Integer totalSets;

    private String icon;
}
