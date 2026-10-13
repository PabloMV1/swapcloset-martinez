## Tabla de consultas (sección 3.3)

| # | Formato | Repositorio | Método | Lo usa |
|---|---|---|---|---|
| 1 | Q1 derivada | UsuarioRepository | `existsByEmailIgnoreCase`, `existsByUsernameIgnoreCase` | Registro (RN-01, 409) |
| 2 | Q1 derivada | UsuarioRepository | `findByUsernameIgnoreCase`, `findByEmailIgnoreCase` | Login (UserDetailsService) |
| 3 | Q1 derivada | SolicitudRepository | `countBySolicitanteIdAndTipoAndEstadoIn` | Límite de 3 préstamos (RN-05) |
| 4 | Q1 derivada | SolicitudRepository | `existsBySolicitanteIdAndPrendaIdAndEstado` | No duplicar pendientes (RN-04) |
| 5 | Q1 derivada | PrendaRepository | `findTop5By...ContainingIgnoreCase...OrderBy`, `findByCreadaEnBetween...` | Endpoint 1 / sugerencias |
| 6 | Q1 derivada | MensajeRepository / ValoracionRepository / NotificacionRepository / FotoPrendaRepository | `findBySolicitudIdOrderByEnviadoEnAsc`, `existsBySolicitudIdAndAutorId`, `existsByUsuarioIdAndTipoAndRecursoId`, `findFirstByPrendaIdOrderByOrdenDesc` | Endpoints 13-15, tarea programada, fotos |
| 7 | Q2 JPQL JOIN + COUNT + GROUP BY | UsuarioRepository | `buscarAfines` | Endpoint 9 |
| 8 | Q2 JPQL AVG | ValoracionRepository | `reputacionMedia` | Endpoints 2 y 7 |
| 9 | Q2 JPQL JOIN + COUNT + GROUP BY (left join con ON) | CategoriaRepository | `contarPrendasPorCategoria` | Endpoint 6 |
| 10 | Q2 JPQL JOIN | SolicitudRepository | `findRecibidasPorDuenaDePrenda` | Endpoint 11 |
| 11 | Q3 nativa (DATE_TRUNC, FILTER) | SolicitudRepository | `actividadPorCategoriaYMes` | Endpoint 16 |
| 12 | Q3 nativa (ranking) | SolicitudRepository | `rankingUsuariasActivas` | Endpoint 16 |
| 13 | Q4 @Modifying | SolicitudRepository | `cambiarEstadoPendientesDePrenda` | Aceptar solicitud (RN-07) |
| 14 | Q4 @Modifying | SolicitudRepository | `caducarPendientesAnterioresA` | Tarea de las 08:00 |
| 15 | Q4 @Modifying | SolicitudRepository | `marcarPrestamosRetrasados` | Tarea de las 08:00 |
| 16 | Q5 interfaz + Q6 Pageable | PrendaRepository | `listarTarjetasDePropietaria` (`PrendaTarjetaView`) | Endpoint 7 |
| 17 | Q5 record `SELECT new` | SolicitudRepository | `resumenPorCategoria` (`ActividadCategoriaDto`) | Endpoint 16 |
| 18 | Q6 paginación | MensajeRepository / NotificacionRepository / SolicitudRepository | `findBySolicitudId`, `findByUsuarioIdAndLeida`, `findBySolicitanteId...` | Endpoints 11, 14 y notificaciones |
| 19 | Q7 Specification | PrendaRepository + `PrendaSpecs` | `findAll(Specification, Pageable)` | Endpoint 1 |
| 20 | Q8 @EntityGraph | PrendaRepository | `findDetalleById` | Endpoint 2 |
| 21 | Q8 @EntityGraph | UsuarioRepository | `findConEstilosById` | Endpoint 7 / perfil |

Probadas todas en `RepositoriosTest` (`./mvnw test` con la base de datos arrancada).
