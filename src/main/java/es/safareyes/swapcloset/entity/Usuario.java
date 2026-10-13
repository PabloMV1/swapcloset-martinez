package es.safareyes.swapcloset.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Sin @Data a propósito: su equals/hashCode/toString recorrerían las relaciones
 * (estilos...) y provocarían cargas innecesarias y bucles. La contraseña tampoco
 * debe salir nunca en un toString.
 */
@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"password", "estilos"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 80)
    private String ciudad;

    @Column(length = 500)
    private String biografia;

    @Enumerated(EnumType.STRING)
    @Column(name = "talla_camiseta", length = 3)
    private TallaCamiseta tallaCamiseta;

    @Column(name = "talla_pantalon")
    private Integer tallaPantalon;

    @Column(name = "talla_calzado")
    private Integer tallaCalzado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Rol rol = Rol.USUARIO;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuarios_estilos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "estilo_id"))
    @Builder.Default
    private Set<Estilo> estilos = new HashSet<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    void antesDeCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }

    @PreUpdate
    void antesDeActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
