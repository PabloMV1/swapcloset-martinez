package es.safareyes.swapcloset.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estilos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString
public class Estilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String nombre;
}
