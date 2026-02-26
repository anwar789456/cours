package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String level;

    private LocalDateTime date;

    private LocalDateTime time;

    private String duration;

    private Integer readinessScore;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private String image;

    @Column(length = 1000)
    private String tip;
}
