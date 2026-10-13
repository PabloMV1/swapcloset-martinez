package es.safareyes.swapcloset.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Solo metadatos: el fichero vive en disco con el nombre de claveDisco. */
@Entity
@Table(name = "fotos_prenda")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "prenda")
public class FotoPrenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prenda_id", nullable = false)
    private Prenda prenda;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "clave_disco", nullable = false, length = 100)
    private String claveDisco;

    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @PrePersist
    void antesDeCrear() {
        this.creadaEn = LocalDateTime.now();
    }
}
