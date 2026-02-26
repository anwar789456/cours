package tn.esprit.cours.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "content_files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileUrl;   // https://minolingo.online/uploads/xyz.pdf

    private String fileType;  // image, video, pdf, doc...

    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    @JsonBackReference
    private ContenuPedagogique contenu;
}