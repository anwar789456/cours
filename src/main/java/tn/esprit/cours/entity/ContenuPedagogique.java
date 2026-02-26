package tn.esprit.cours.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenu_pedagogique")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContenuPedagogique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContent;

    @Column(name = "titlec")
    private String titleC;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "contenttype")
    private String contentType;

    // 🔹 One content can have MANY files
    @OneToMany(mappedBy = "contenu", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentFile> files;

    // Many contents belong to one course — LAZY by default for @ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    @JsonBackReference
    private Cours cours;
}
