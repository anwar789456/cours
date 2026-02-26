package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_progress")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="userid")
    private Long userId;

    @Column(name="coursid")
    private Long coursId;

    @Column(name="progress")
    private Double progress;
}