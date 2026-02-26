package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "practice_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PracticeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String color;
}
