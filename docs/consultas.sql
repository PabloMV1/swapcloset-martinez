-- Tres consultas SQL de ejemplo que responden a preguntas del cliente.
-- Son la base de las consultas nativas (Q3) y JPQL (Q2) de la Fase 2.

-- 1) "Como administradora quiero ver cuánta actividad hay cada mes por categoría."
--    Publicaciones, intercambios y préstamos por categoría y mes.
SELECT DATE_TRUNC('month', p.creada_en)::date AS mes,
       c.nombre                                AS categoria,
       COUNT(*)                                AS prendas_publicadas
FROM prendas p
JOIN categorias c ON c.id = p.categoria_id
WHERE p.disponibilidad <> 'BORRADOR'
GROUP BY DATE_TRUNC('month', p.creada_en), c.nombre
ORDER BY mes, categoria;

SELECT DATE_TRUNC('month', s.creada_en)::date                 AS mes,
       c.nombre                                               AS categoria,
       COUNT(*) FILTER (WHERE s.tipo = 'INTERCAMBIO')         AS intercambios,
       COUNT(*) FILTER (WHERE s.tipo = 'PRESTAMO')            AS prestamos
FROM solicitudes s
JOIN prendas p     ON p.id = s.prenda_id
JOIN categorias c  ON c.id = p.categoria_id
WHERE s.estado = 'FINALIZADA'
GROUP BY DATE_TRUNC('month', s.creada_en), c.nombre
ORDER BY mes, categoria;

-- 2) "Me encantaría que la app me sugiriera personas con mi estilo."
--    Perfiles con más estilos en común con la usuaria 4 y mejor reputación.
SELECT u.id,
       u.username,
       COUNT(ue2.estilo_id) AS estilos_comunes,
       COALESCE((SELECT ROUND(AVG(v.puntuacion), 1)
                 FROM valoraciones v
                 WHERE v.valorado_id = u.id), 0) AS reputacion
FROM usuarios u
JOIN usuarios_estilos ue2 ON ue2.usuario_id = u.id
JOIN usuarios_estilos ue1 ON ue1.estilo_id = ue2.estilo_id AND ue1.usuario_id = 4
WHERE u.id <> 4
GROUP BY u.id, u.username
ORDER BY estilos_comunes DESC, reputacion DESC;

-- 3) "Si se pasa la fecha y no la han devuelto, la app tiene que avisar a las dos personas."
--    Préstamos vencidos y sin devolver, con los días de retraso.
SELECT s.id                       AS solicitud,
       p.titulo                   AS prenda,
       sol.username               AS quien_la_tiene,
       prop.username              AS propietaria,
       s.fecha_fin,
       CURRENT_DATE - s.fecha_fin AS dias_de_retraso
FROM solicitudes s
JOIN prendas p     ON p.id = s.prenda_id
JOIN usuarios sol  ON sol.id = s.solicitante_id
JOIN usuarios prop ON prop.id = s.propietaria_id
WHERE s.tipo = 'PRESTAMO'
  AND s.estado IN ('ACEPTADA', 'RETRASADA')
  AND s.fecha_fin < CURRENT_DATE
ORDER BY dias_de_retraso DESC;
