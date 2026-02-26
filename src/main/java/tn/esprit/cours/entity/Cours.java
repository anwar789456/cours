package tn.esprit.cours.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idprofessor")
    private Long idProfessor; //new column

    @Column(name = "title")
    private String title;

    @Column(length = 1000, name="description")
    private String description;

    @Column(length = 5000, name="content")
    private String content;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ContentFile image; //new column

    // Relationship with ContenuPedagogique — kept LAZY (default for @OneToMany)
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<ContenuPedagogique> contenus = new ArrayList<>();
}
