package tn.esprit.cours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String subtitle;

    @Enumerated(EnumType.STRING)
    private CertificationStatus status;

    private String progress;

    private String date;

    private String estimatedExam;

    private String icon;
}
