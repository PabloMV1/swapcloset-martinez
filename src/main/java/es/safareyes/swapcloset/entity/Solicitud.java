package es.safareyes.swapcloset.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prenda_id", nullable = false)
    private Prenda prenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    /** Desnormalizada a propósito: la prenda puede cambiar de dueña al finalizar un intercambio. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietaria_id", nullable = false)
    private Usuario propietaria;

    /** Relación opcional: solo en intercambios. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prenda_ofrecida_id")
    private Prenda prendaOfrecida;

    /** Solo en préstamos. */
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    /** Solo en préstamos. */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

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
}
