-- =====================================================================
-- SwapCloset - datos de prueba
-- Contraseña de TODOS los usuarios de prueba:  Password1!
-- Las fechas son relativas a hoy (CURRENT_DATE) para que los casos límite
-- (vencidos, caducables...) sigan siéndolo cuando pase el tiempo.
-- =====================================================================

-- ---------------------------------------------------------------------
-- USUARIOS (12): 1 ADMIN, 2 MODERADOR, 9 USUARIO
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id, username, email, password, fecha_nacimiento, ciudad, biografia, talla_camiseta, talla_pantalon, talla_calzado, rol)
SELECT v.id, v.username, v.email, p.hash, v.fecha_nacimiento, v.ciudad, v.biografia, v.talla_camiseta, v.talla_pantalon, v.talla_calzado, v.rol
FROM (VALUES
    (1,  'mariana',   'mariana@swapcloset.es',    DATE '2007-03-14', 'Sevilla',   'Fundadora de SwapCloset. Me encanta variar de estilo.',        'M',  38, 39, 'ADMIN'),
    (2,  'laura_mod', 'laura.mod@swapcloset.es',  DATE '2000-05-20', 'Madrid',    'Moderadora de la comunidad.',                                  'S',  36, 38, 'MODERADOR'),
    (3,  'david_mod', 'david.mod@swapcloset.es',  DATE '1999-11-02', 'Barcelona', 'Moderador de la comunidad.',                                   'L',  42, 43, 'MODERADOR'),
    (4,  'lucia_s',   'lucia@correo.es',          DATE '2006-09-21', 'Sevilla',   'Amante del vintage y de los mercadillos.',                     'S',  36, 37, 'USUARIO'),
    (5,  'carlos_r',  'carlos@correo.es',         DATE '2005-01-30', 'Sevilla',   'Streetwear y deporte.',                                        'L',  44, 43, 'USUARIO'),
    (6,  'sofia_m',   'sofia@correo.es',          DATE '2007-07-07', 'Madrid',    'Siempre estoy pidiendo prendas prestadas para eventos.',       'M',  38, 39, 'USUARIO'),
    (7,  'pablo_v',   'pablo@correo.es',          DATE '2004-12-12', 'Valencia',  'Cinco estilos y ningún armario suficiente.',                   'L',  46, 44, 'USUARIO'),
    (8,  'ana_g',     'ana@correo.es',            DATE '2008-02-18', 'Sevilla',   'Casual y minimalista.',                                        'S',  36, 38, 'USUARIO'),
    (9,  'javi_t',    'javi@correo.es',           DATE '2003-06-25', 'Barcelona', 'Grunge de toda la vida.',                                      'XL', 48, 45, 'USUARIO'),
    (10, 'marta_l',   'marta@correo.es',          DATE '2006-04-09', 'Madrid',    'Me gusta el estilo formal y los abrigos largos.',              'M',  38, 40, 'USUARIO'),
    (11, 'diego_p',   'diego@correo.es',          DATE '2007-10-01', 'Sevilla',   'Casual y deportivo.',                                          'L',  42, 42, 'USUARIO'),
    (12, 'nuria_c',   'nuria@correo.es',          DATE '2010-01-15', 'Granada',   'Acabo de cumplir los 16 años justos para registrarme.',        'XS', 34, 36, 'USUARIO')
) AS v(id, username, email, fecha_nacimiento, ciudad, biografia, talla_camiseta, talla_pantalon, talla_calzado, rol)
CROSS JOIN (SELECT crypt('Password1!', gen_salt('bf', 10)) AS hash) AS p;

-- ---------------------------------------------------------------------
-- ESTILOS y relación usuarios-estilos (el usuario 7 tiene el máximo: 5)
-- ---------------------------------------------------------------------
INSERT INTO estilos (id, nombre) VALUES
    (1, 'casual'), (2, 'streetwear'), (3, 'vintage'), (4, 'formal'),
    (5, 'deportivo'), (6, 'boho'), (7, 'minimalista'), (8, 'grunge');

INSERT INTO usuarios_estilos (usuario_id, estilo_id) VALUES
    (1, 1), (1, 7),
    (2, 4),
    (3, 4), (3, 7),
    (4, 3), (4, 6), (4, 1),
    (5, 2), (5, 5), (5, 8),
    (6, 6), (6, 3), (6, 7),
    (7, 2), (7, 1), (7, 3), (7, 8), (7, 5),
    (8, 1), (8, 7), (8, 4),
    (9, 8), (9, 2), (9, 3),
    (10, 7), (10, 4), (10, 1),
    (11, 1), (11, 5), (11, 2),
    (12, 6), (12, 3);

-- ---------------------------------------------------------------------
-- CATEGORIAS
-- ---------------------------------------------------------------------
INSERT INTO categorias (id, nombre) VALUES
    (1, 'Casual'), (2, 'Formal'), (3, 'Deportiva'), (4, 'Calzado'), (5, 'Accesorios'), (6, 'Abrigos');

-- ---------------------------------------------------------------------
-- PRENDAS (23)
--   20 = BORRADOR (sin fotos) | 19 = retirada (activa = false)
--   4 y 21 RESERVADAS (intercambio aceptado) | 3, 7 y 8 PRESTADAS
--   22 y 23 ya cambiaron de dueña tras un intercambio finalizado
-- ---------------------------------------------------------------------
INSERT INTO prendas (id, propietaria_id, categoria_id, estilo_id, titulo, descripcion, marca, talla, estado, modalidad, disponibilidad, activa) VALUES
    (1,  4,  6, 3, 'Chaqueta vaquera vintage',       'Vaquera de los 90, muy cómoda y con mucho carácter.',       'Levi''s',        'M',     'BUEN_ESTADO',        'AMBAS',       'DISPONIBLE', TRUE),
    (2,  4,  1, 1, 'Camiseta básica blanca',         'Algodón grueso, perfecta para combinar.',                   'Zara',           'S',     'COMO_NUEVA',         'INTERCAMBIO', 'DISPONIBLE', TRUE),
    (3,  5,  2, 4, 'Blazer negro',                   'Corte recto, ideal para entrevistas y eventos.',            'Mango',          'M',     'BUEN_ESTADO',        'PRESTAMO',    'PRESTADA',   TRUE),
    (4,  5,  4, 2, 'Zapatillas blancas de cuero',    'Usadas pero cuidadas.',                                     'Nike',           '42',    'USADA',              'INTERCAMBIO', 'RESERVADA',  TRUE),
    (5,  6,  1, 6, 'Vestido boho estampado',         'Con etiqueta, nunca me lo he puesto.',                      NULL,             'S',     'NUEVA_CON_ETIQUETA', 'AMBAS',       'DISPONIBLE', TRUE),
    (6,  7,  6, 2, 'Sudadera oversize gris',         'Muy calentita, corte ancho.',                               'Bershka',        'L',     'BUEN_ESTADO',        'AMBAS',       'DISPONIBLE', TRUE),
    (7,  7,  5, 7, 'Bolso de mano beige',            'Cabe de todo, cierre magnético.',                           NULL,             'Única', 'COMO_NUEVA',         'PRESTAMO',    'PRESTADA',   TRUE),
    (8,  8,  2, 4, 'Vestido negro de fiesta',        'Largo hasta la rodilla, con espalda abierta.',              'H&M',            'M',     'COMO_NUEVA',         'PRESTAMO',    'PRESTADA',   TRUE),
    (9,  8,  3, 5, 'Leggings deportivos',            'Cintura alta, muy elásticos.',                              'Adidas',         'S',     'USADA',              'INTERCAMBIO', 'DISPONIBLE', TRUE),
    (10, 9,  1, 8, 'Camiseta grunge de banda',       'Estampado desgastado, muy auténtica.',                      NULL,             'L',     'USADA',              'AMBAS',       'DISPONIBLE', TRUE),
    (11, 9,  4, 3, 'Botas Dr. Martens',              'Negras, suela gruesa.',                                     'Dr. Martens',    '40',    'BUEN_ESTADO',        'PRESTAMO',    'DISPONIBLE', TRUE),
    (12, 10, 6, 7, 'Abrigo largo camel',             'Lana mezcla, forro interior.',                              'Massimo Dutti',  'M',     'COMO_NUEVA',         'AMBAS',       'DISPONIBLE', TRUE),
    (13, 10, 5, 2, 'Gorra negra',                    'Visera curva, ajustable.',                                  'New Era',        'Única', 'BUEN_ESTADO',        'INTERCAMBIO', 'DISPONIBLE', TRUE),
    (14, 11, 1, 1, 'Vaqueros rectos',                'Azul medio, tiro alto.',                                    'Pull&Bear',      '38',    'BUEN_ESTADO',        'AMBAS',       'DISPONIBLE', TRUE),
    (15, 11, 3, 5, 'Chándal completo',               'Chaqueta y pantalón a juego.',                              'Puma',           'M',     'USADA',              'INTERCAMBIO', 'DISPONIBLE', TRUE),
    (16, 12, 1, 6, 'Kimono estampado',               'Ligero, para entretiempo.',                                 NULL,             'Única', 'COMO_NUEVA',         'AMBAS',       'DISPONIBLE', TRUE),
    (17, 12, 2, 4, 'Camisa blanca de vestir',        'Con etiqueta, sin estrenar.',                               'Zara',           'M',     'NUEVA_CON_ETIQUETA', 'PRESTAMO',    'DISPONIBLE', TRUE),
    (18, 4,  5, 3, 'Pañuelo de seda vintage',        'Estampado floral de mercadillo.',                           NULL,             'Única', 'BUEN_ESTADO',        'AMBAS',       'DISPONIBLE', TRUE),
    (19, 5,  1, 2, 'Camiseta streetwear estampada',  'Retirada por incumplir las normas (foto con marca).',       NULL,             'L',     'USADA',              'INTERCAMBIO', 'DISPONIBLE', FALSE),
    (20, 6,  1, 1, 'Jersey de punto',                'Aún sin fotos: queda en borrador.',                         NULL,             'M',     'BUEN_ESTADO',        'AMBAS',       'BORRADOR',   TRUE),
    (21, 9,  1, 1, 'Polo azul marino',               'Clásico, de algodón piqué.',                                'Lacoste',        'M',     'BUEN_ESTADO',        'INTERCAMBIO', 'RESERVADA',  TRUE),
    (22, 10, 6, 2, 'Cazadora bomber',                'Ahora de Marta tras el intercambio con Lucía.',             'Alpha Industries','M',    'BUEN_ESTADO',        'AMBAS',       'DISPONIBLE', TRUE),
    (23, 4,  1, 1, 'Sudadera verde',                 'Ahora de Lucía tras el intercambio con Marta.',             NULL,             'S',     'COMO_NUEVA',         'AMBAS',       'DISPONIBLE', TRUE);

-- Fechas de publicación escalonadas para probar el orden "lo último primero"
UPDATE prendas
   SET creada_en = CURRENT_TIMESTAMP - id * INTERVAL '1 day',
       actualizada_en = CURRENT_TIMESTAMP - id * INTERVAL '1 day';

-- ---------------------------------------------------------------------
-- FOTOS (metadatos). Los ficheros de estas fotos de prueba NO existen en disco:
-- sirven para probar listados y detalle. Sube fotos reales con el endpoint.
-- La prenda 20 (borrador) no tiene fotos. La prenda 1 tiene el máximo: 5.
-- ---------------------------------------------------------------------
INSERT INTO fotos_prenda (prenda_id, nombre_original, clave_disco, tipo_contenido, tamano_bytes, orden)
SELECT p.id, 'foto_' || p.id || '.jpg', gen_random_uuid()::text || '.jpg', 'image/jpeg', 150000 + p.id * 1000, 1
FROM prendas p
WHERE p.id <> 20;

INSERT INTO fotos_prenda (prenda_id, nombre_original, clave_disco, tipo_contenido, tamano_bytes, orden)
SELECT 1, 'foto_1_' || g || '.png', gen_random_uuid()::text || '.png', 'image/png', 200000 + g * 1000, g
FROM generate_series(2, 5) AS g;

-- ---------------------------------------------------------------------
-- SOLICITUDES (11), una por cada estado y casos límite
--  1 PENDIENTE de hace 10 días  -> la tarea programada la caduca
--  2 PENDIENTE reciente (préstamo)
--  3 ACEPTADA intercambio       -> prendas 4 y 21 RESERVADAS
--  4 ACEPTADA préstamo que vence MAÑANA
--  5 ACEPTADA préstamo VENCIDO  -> la tarea la pasa a RETRASADA
--  6 RETRASADA ya marcada
--  4, 5 y 6: la usuaria 6 tiene 3 préstamos activos (límite de RN-05)
--  7 FINALIZADA intercambio (las prendas 22 y 23 cambiaron de dueña)
--  8 FINALIZADA préstamo | 9 RECHAZADA | 10 CANCELADA | 11 CADUCADA
-- ---------------------------------------------------------------------
INSERT INTO solicitudes (id, tipo, estado, prenda_id, solicitante_id, propietaria_id, prenda_ofrecida_id, fecha_inicio, fecha_fin, creada_en, actualizada_en) VALUES
    (1,  'INTERCAMBIO', 'PENDIENTE',  6,  4,  7,  18,   NULL,                    NULL,                     CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    (2,  'PRESTAMO',    'PENDIENTE',  12, 8,  10, NULL, CURRENT_DATE + 2,        CURRENT_DATE + 9,         CURRENT_TIMESTAMP - INTERVAL '1 day',   CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (3,  'INTERCAMBIO', 'ACEPTADA',   4,  9,  5,  21,   NULL,                    NULL,                     CURRENT_TIMESTAMP - INTERVAL '3 days',  CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (4,  'PRESTAMO',    'ACEPTADA',   7,  6,  7,  NULL, CURRENT_DATE - 6,        CURRENT_DATE + 1,         CURRENT_TIMESTAMP - INTERVAL '7 days',  CURRENT_TIMESTAMP - INTERVAL '6 days'),
    (5,  'PRESTAMO',    'ACEPTADA',   3,  6,  5,  NULL, CURRENT_DATE - 14,       CURRENT_DATE - 3,         CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '14 days'),
    (6,  'PRESTAMO',    'RETRASADA',  8,  6,  8,  NULL, CURRENT_DATE - 20,       CURRENT_DATE - 5,         CURRENT_TIMESTAMP - INTERVAL '21 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (7,  'INTERCAMBIO', 'FINALIZADA', 22, 10, 4,  23,   NULL,                    NULL,                     CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (8,  'PRESTAMO',    'FINALIZADA', 14, 12, 11, NULL, CURRENT_DATE - 25,       CURRENT_DATE - 15,        CURRENT_TIMESTAMP - INTERVAL '26 days', CURRENT_TIMESTAMP - INTERVAL '14 days'),
    (9,  'INTERCAMBIO', 'RECHAZADA',  13, 11, 10, 15,   NULL,                    NULL,                     CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '11 days'),
    (10, 'PRESTAMO',    'CANCELADA',  17, 5,  12, NULL, CURRENT_DATE + 3,        CURRENT_DATE + 10,        CURRENT_TIMESTAMP - INTERVAL '5 days',  CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (11, 'INTERCAMBIO', 'CADUCADA',   9,  7,  8,  6,    NULL,                    NULL,                     CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '13 days');

-- ---------------------------------------------------------------------
-- MENSAJES (solo los dos participantes de cada solicitud)
-- ---------------------------------------------------------------------
INSERT INTO mensajes (solicitud_id, autor_id, texto, enviado_en) VALUES
    (3,  9,  '¡Hola! Me encantan tus zapatillas, ¿las cambiamos por mi polo?',        CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (3,  5,  'Trato hecho. ¿Quedamos el sábado en la facultad?',                      CURRENT_TIMESTAMP - INTERVAL '2 days 20 hours'),
    (3,  9,  'Perfecto, nos vemos allí a las 12.',                                    CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (4,  6,  '¿Me la puedes dejar hasta el domingo? Es para una boda.',               CURRENT_TIMESTAMP - INTERVAL '7 days'),
    (4,  7,  'Claro, te la paso mañana por la tarde.',                                CURRENT_TIMESTAMP - INTERVAL '6 days 22 hours'),
    (5,  6,  'Siento el retraso, te la devuelvo esta semana sin falta.',              CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (5,  5,  'Vale, avísame cuando puedas quedar.',                                   CURRENT_TIMESTAMP - INTERVAL '20 hours'),
    (7,  10, '¿Intercambiamos mi bomber por tu sudadera verde?',                      CURRENT_TIMESTAMP - INTERVAL '30 days'),
    (7,  4,  'Sí, me encanta. ¿Te va bien el viernes?',                               CURRENT_TIMESTAMP - INTERVAL '29 days 23 hours');

-- ---------------------------------------------------------------------
-- VALORACIONES (solo en solicitudes FINALIZADAS)
-- ---------------------------------------------------------------------
INSERT INTO valoraciones (solicitud_id, autor_id, valorado_id, puntuacion, comentario) VALUES
    (7, 10, 4,  5, 'Muy puntual y la prenda era tal como en las fotos.'),
    (7, 4,  10, 4, 'Todo bien, la cazadora algo más usada de lo esperado.'),
    (8, 12, 11, 5, 'Cuidó muy bien la prenda y la devolvió a tiempo.'),
    (8, 11, 12, 3, NULL);

-- ---------------------------------------------------------------------
-- NOTIFICACIONES. Los avisos de vencimiento/retraso ya "enviados" permiten
-- comprobar que la tarea programada es idempotente (no los duplica).
-- ---------------------------------------------------------------------
INSERT INTO notificaciones (usuario_id, tipo, mensaje, recurso, recurso_id, leida, creada_en) VALUES
    (7,  'SOLICITUD_RECIBIDA',      'Nueva solicitud para «Sudadera oversize gris»',        'SOLICITUD', 1, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days'),
    (10, 'SOLICITUD_RECIBIDA',      'Nueva solicitud para «Abrigo largo camel»',            'SOLICITUD', 2, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (9,  'SOLICITUD_ACEPTADA',      'Tu solicitud de «Zapatillas blancas de cuero» ha sido aceptada', 'SOLICITUD', 3, TRUE,  CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (6,  'NUEVO_MENSAJE',           'Nuevo mensaje en tu solicitud de «Bolso de mano beige»', 'SOLICITUD', 4, FALSE, CURRENT_TIMESTAMP - INTERVAL '6 days 22 hours'),
    (6,  'PRESTAMO_VENCE_MANANA',   '«Bolso de mano beige» vence mañana',                   'SOLICITUD', 4, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    (7,  'PRESTAMO_VENCE_MANANA',   '«Bolso de mano beige» vence mañana',                   'SOLICITUD', 4, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    (6,  'PRESTAMO_RETRASADO',      'El préstamo de «Vestido negro de fiesta» está retrasado', 'SOLICITUD', 6, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (8,  'PRESTAMO_RETRASADO',      'El préstamo de «Vestido negro de fiesta» está retrasado', 'SOLICITUD', 6, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (4,  'NUEVA_VALORACION',        'Has recibido una valoración de 5 estrellas',           'SOLICITUD', 7, TRUE,  CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (10, 'NUEVA_VALORACION',        'Has recibido una valoración de 4 estrellas',           'SOLICITUD', 7, TRUE,  CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (11, 'NUEVA_VALORACION',        'Has recibido una valoración de 5 estrellas',           'SOLICITUD', 8, FALSE, CURRENT_TIMESTAMP - INTERVAL '14 days'),
    (12, 'NUEVA_VALORACION',        'Has recibido una valoración de 3 estrellas',           'SOLICITUD', 8, FALSE, CURRENT_TIMESTAMP - INTERVAL '14 days');

-- ---------------------------------------------------------------------
-- Ajustar las secuencias tras insertar ids explícitos
-- ---------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('usuarios', 'id'),     (SELECT MAX(id) FROM usuarios));
SELECT setval(pg_get_serial_sequence('estilos', 'id'),      (SELECT MAX(id) FROM estilos));
SELECT setval(pg_get_serial_sequence('categorias', 'id'),   (SELECT MAX(id) FROM categorias));
SELECT setval(pg_get_serial_sequence('prendas', 'id'),      (SELECT MAX(id) FROM prendas));
SELECT setval(pg_get_serial_sequence('solicitudes', 'id'),  (SELECT MAX(id) FROM solicitudes));
