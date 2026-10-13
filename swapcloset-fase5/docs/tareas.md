# Tareas programadas

## Mantenimiento diario de solicitudes

| | |
|---|---|
| Clase | `SolicitudesScheduler` → `MantenimientoSolicitudesService.procesar(...)` |
| Cuándo | Todos los días a las **08:00** (zona Europe/Madrid). Cron en `application.yml`: `app.scheduling.cron-solicitudes: "0 0 8 * * *"` |
| Para probarlo | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (perfil `dev`: se ejecuta cada minuto) |

Pasos, en este orden:

1. **Aviso del día antes.** Los préstamos `ACEPTADA` cuya `fechaFin` es mañana generan una notificación `PRESTAMO_VENCE_MANANA` a la solicitante y a la propietaria.
2. **Retrasos.** Los préstamos `ACEPTADA` con `fechaFin` anterior a hoy pasan a `RETRASADA` (una sola `UPDATE` masiva, consulta Q4) y se avisa a las dos personas con `PRESTAMO_RETRASADO`.
3. **Caducidad.** Las solicitudes `PENDIENTE` de más de 7 días pasan a `CADUCADA` (otra `UPDATE` masiva, consulta Q4).

### Por qué se puede ejecutar varias veces sin duplicar nada (idempotencia)

- Los pasos 2 y 3 solo buscan solicitudes en el estado de origen (`ACEPTADA`, `PENDIENTE`): una solicitud ya `RETRASADA` o `CADUCADA` no vuelve a procesarse.
- Los avisos se crean con `notificarUnaVez`, que comprueba antes si esa persona ya tiene una notificación del mismo tipo para esa solicitud. Además, un **índice único parcial** en la base de datos (`uq_notif_aviso_unico`) lo garantiza aunque dos ejecuciones coincidieran.
- Si una ejecución falla, se registra el error y la aplicación sigue: la siguiente ejecución retoma lo pendiente.

Test: `MantenimientoSolicitudesTest` (la segunda ejecución devuelve todo a 0).
