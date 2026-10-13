package es.safareyes.swapcloset.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prendas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"propietaria", "categoria", "estilo", "fotos"})
public class Prenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietaria_id", nullable = false)
    private Usuario propietaria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estilo_id", nullable = false)
    private Estilo estilo;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 60)
    private String marca;

    @Column(nullable = false, length = 10)
    private String talla;

    /** Estado de conservación. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoPrenda estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private Disponibilidad disponibilidad = Disponibilidad.BORRADOR;

    /** false = retirada (baja lógica, RN-10). */
    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @OneToMany(mappedBy = "prenda", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<FotoPrenda> fotos = new ArrayList<>();

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn;

    @PrePersist
    void antesDeCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        this.creadaEn = ahora;
        this.actualizadaEn = ahora;
    }

    @PreUpdate
    void antesDeActualizar() {
        this.actualizadaEn = LocalDateTime.now();
    }

    /** Mantiene los dos lados de la relación sincronizados. */
    public void agregarFoto(FotoPrenda foto) {
        fotos.add(foto);
        foto.setPrenda(this);
    }

    public void quitarFoto(FotoPrenda foto) {
        fotos.remove(foto);
        foto.setPrenda(null);
    }
}
