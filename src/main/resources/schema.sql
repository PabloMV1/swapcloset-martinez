-- =====================================================================
-- SwapCloset - esquema de base de datos (PostgreSQL 16)
-- Se puede ejecutar varias veces: borra y recrea todo (solo para desarrollo).
-- =====================================================================

-- pgcrypto se usa en data.sql para generar hashes BCrypt de los usuarios de prueba
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS notificaciones    CASCADE;
DROP TABLE IF EXISTS valoraciones      CASCADE;
DROP TABLE IF EXISTS mensajes          CASCADE;
DROP TABLE IF EXISTS solicitudes       CASCADE;
DROP TABLE IF EXISTS fotos_prenda      CASCADE;
DROP TABLE IF EXISTS prendas           CASCADE;
DROP TABLE IF EXISTS categorias        CASCADE;
DROP TABLE IF EXISTS usuarios_estilos  CASCADE;
DROP TABLE IF EXISTS estilos           CASCADE;
DROP TABLE IF EXISTS usuarios          CASCADE;

-- ---------------------------------------------------------------------
-- USUARIOS
-- La edad mínima (16 años, RN-01) se valida en la API con un validador propio.
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id               BIGSERIAL     PRIMARY KEY,
    username         VARCHAR(30)   NOT NULL,
    email            VARCHAR(120)  NOT NULL,
    password         VARCHAR(100)  NOT NULL,
    fecha_nacimiento DATE          NOT NULL,
    ciudad           VARCHAR(80),
    biografia        VARCHAR(500),
    talla_camiseta   VARCHAR(3),
    talla_pantalon   INTEGER,
    talla_calzado    INTEGER,
    rol              VARCHAR(20)   NOT NULL DEFAULT 'USUARIO',
    creado_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_username UNIQUE (username),
    CONSTRAINT uq_usuarios_email    UNIQUE (email),
    CONSTRAINT ck_usuarios_rol      CHECK (rol IN ('ADMIN', 'MODERADOR', 'USUARIO')),
    CONSTRAINT ck_usuarios_camiseta CHECK (talla_camiseta IS NULL OR talla_camiseta IN ('XS', 'S', 'M', 'L', 'XL', 'XXL')),
    CONSTRAINT ck_usuarios_pantalon CHECK (talla_pantalon IS NULL OR talla_pantalon BETWEEN 34 AND 50),
    CONSTRAINT ck_usuarios_calzado  CHECK (talla_calzado  IS NULL OR talla_calzado  BETWEEN 35 AND 46)
);

-- ---------------------------------------------------------------------
-- ESTILOS (catálogo) y relación N:M con usuarios
-- Entre 1 y 5 estilos por perfil (RN-02): se controla en el servicio.
-- ---------------------------------------------------------------------
CREATE TABLE estilos (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(40)  NOT NULL,
    CONSTRAINT uq_estilos_nombre UNIQUE (nombre)
);

CREATE TABLE usuarios_estilos (
    usuario_id BIGINT NOT NULL,
    estilo_id  BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, estilo_id),
    CONSTRAINT fk_ue_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_ue_estilo  FOREIGN KEY (estilo_id)  REFERENCES estilos (id)  ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- CATEGORIAS
-- ---------------------------------------------------------------------
CREATE TABLE categorias (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL,
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
);

-- ---------------------------------------------------------------------
-- PRENDAS
--   estado          = estado de conservación (lo que filtra el cliente)
--   disponibilidad  = situación en la plataforma (RN-03, RN-07)
--   activa          = baja lógica (RN-10)
-- ---------------------------------------------------------------------
CREATE TABLE prendas (
    id              BIGSERIAL     PRIMARY KEY,
    propietaria_id  BIGINT        NOT NULL,
    categoria_id    BIGINT        NOT NULL,
    estilo_id       BIGINT        NOT NULL,
    titulo          VARCHAR(100)  NOT NULL,
    descripcion     VARCHAR(1000),
    marca           VARCHAR(60),
    talla           VARCHAR(10)   NOT NULL,
    estado          VARCHAR(25)   NOT NULL,
    modalidad       VARCHAR(15)   NOT NULL,
    disponibilidad  VARCHAR(15)   NOT NULL DEFAULT 'BORRADOR',
    activa          BOOLEAN       NOT NULL DEFAULT TRUE,
    creada_en       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prendas_propietaria FOREIGN KEY (propietaria_id) REFERENCES usuarios (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_prendas_categoria   FOREIGN KEY (categoria_id)   REFERENCES categorias (id) ON DELETE RESTRICT,
    CONSTRAINT fk_prendas_estilo      FOREIGN KEY (estilo_id)      REFERENCES estilos (id)    ON DELETE RESTRICT,
    CONSTRAINT ck_prendas_estado         CHECK (estado IN ('NUEVA_CON_ETIQUETA', 'COMO_NUEVA', 'BUEN_ESTADO', 'USADA')),
    CONSTRAINT ck_prendas_modalidad      CHECK (modalidad IN ('INTERCAMBIO', 'PRESTAMO', 'AMBAS')),
    CONSTRAINT ck_prendas_disponibilidad CHECK (disponibilidad IN ('BORRADOR', 'DISPONIBLE', 'RESERVADA', 'PRESTADA'))
);

-- Índices creados a mano: el listado filtra por categoría/disponibilidad y ordena por fecha
CREATE INDEX idx_prendas_busqueda      ON prendas (categoria_id, disponibilidad) WHERE activa;
CREATE INDEX idx_prendas_creada_en     ON prendas (creada_en DESC);
CREATE INDEX idx_prendas_propietaria   ON prendas (propietaria_id);

-- ---------------------------------------------------------------------
-- FOTOS_PRENDA (solo metadatos, el fichero está en disco)
-- ---------------------------------------------------------------------
CREATE TABLE fotos_prenda (
    id               BIGSERIAL     PRIMARY KEY,
    prenda_id        BIGINT        NOT NULL,
    nombre_original  VARCHAR(255)  NOT NULL,
    clave_disco      VARCHAR(100)  NOT NULL,
    tipo_contenido   VARCHAR(20)   NOT NULL,
    tamano_bytes     BIGINT        NOT NULL,
    orden            INTEGER       NOT NULL,
    creada_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fotos_prenda       FOREIGN KEY (prenda_id) REFERENCES prendas (id) ON DELETE CASCADE,
    CONSTRAINT uq_fotos_clave        UNIQUE (clave_disco),
    CONSTRAINT uq_fotos_prenda_orden UNIQUE (prenda_id, orden),
    CONSTRAINT ck_fotos_tipo         CHECK (tipo_contenido IN ('image/jpeg', 'image/png', 'image/webp')),
    CONSTRAINT ck_fotos_tamano       CHECK (tamano_bytes > 0 AND tamano_bytes <= 5242880),
    CONSTRAINT ck_fotos_orden        CHECK (orden BETWEEN 1 AND 5)
);

-- ---------------------------------------------------------------------
-- SOLICITUDES
-- propietaria_id se guarda aquí (desnormalización justificada): al finalizar un
-- intercambio la prenda cambia de dueña, pero la solicitud, su chat y sus
-- valoraciones deben seguir apuntando a las dos personas que participaron.
-- ---------------------------------------------------------------------
CREATE TABLE solicitudes (
    id                  BIGSERIAL    PRIMARY KEY,
    tipo                VARCHAR(15)  NOT NULL,
    estado              VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE',
    prenda_id           BIGINT       NOT NULL,
    solicitante_id      BIGINT       NOT NULL,
    propietaria_id      BIGINT       NOT NULL,
    prenda_ofrecida_id  BIGINT,                       -- opcional: solo en intercambios
    fecha_inicio        DATE,                         -- solo en préstamos
    fecha_fin           DATE,                         -- solo en préstamos
    creada_en           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sol_prenda         FOREIGN KEY (prenda_id)          REFERENCES prendas (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_sol_solicitante    FOREIGN KEY (solicitante_id)     REFERENCES usuarios (id)  ON DELETE RESTRICT,
    CONSTRAINT fk_sol_propietaria    FOREIGN KEY (propietaria_id)     REFERENCES usuarios (id)  ON DELETE RESTRICT,
    CONSTRAINT fk_sol_prenda_ofrecida FOREIGN KEY (prenda_ofrecida_id) REFERENCES prendas (id)  ON DELETE RESTRICT,
    CONSTRAINT ck_sol_tipo   CHECK (tipo IN ('INTERCAMBIO', 'PRESTAMO')),
    CONSTRAINT ck_sol_estado CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CANCELADA', 'CADUCADA', 'RETRASADA', 'FINALIZADA')),
    CONSTRAINT ck_sol_personas CHECK (solicitante_id <> propietaria_id),
    -- Intercambio: prenda ofrecida y sin fechas. Préstamo: fechas (máx. 30 días) y sin prenda ofrecida.
    CONSTRAINT ck_sol_coherencia CHECK (
        (tipo = 'INTERCAMBIO' AND prenda_ofrecida_id IS NOT NULL AND fecha_inicio IS NULL AND fecha_fin IS NULL)
        OR
        (tipo = 'PRESTAMO' AND prenda_ofrecida_id IS NULL AND fecha_inicio IS NOT NULL AND fecha_fin IS NOT NULL
            AND fecha_fin > fecha_inicio AND (fecha_fin - fecha_inicio) <= 30)
    )
);

-- RN-04: una persona no puede tener dos solicitudes pendientes de la misma prenda
CREATE UNIQUE INDEX uq_sol_pendiente_unica ON solicitudes (solicitante_id, prenda_id) WHERE estado = 'PENDIENTE';
-- La tarea programada busca por estado y antigüedad / fecha de fin
CREATE INDEX idx_sol_estado_creada ON solicitudes (estado, creada_en);
CREATE INDEX idx_sol_prenda        ON solicitudes (prenda_id);
CREATE INDEX idx_sol_solicitante   ON solicitudes (solicitante_id);
CREATE INDEX idx_sol_propietaria   ON solicitudes (propietaria_id);

-- ---------------------------------------------------------------------
-- MENSAJES (chat de cada solicitud)
-- ---------------------------------------------------------------------
CREATE TABLE mensajes (
    id            BIGSERIAL     PRIMARY KEY,
    solicitud_id  BIGINT        NOT NULL,
    autor_id      BIGINT        NOT NULL,
    texto         VARCHAR(1000) NOT NULL,
    enviado_en    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_autor     FOREIGN KEY (autor_id)     REFERENCES usuarios (id)    ON DELETE RESTRICT,
    CONSTRAINT ck_msg_texto     CHECK (char_length(btrim(texto)) BETWEEN 1 AND 1000)
);
CREATE INDEX idx_msg_solicitud_fecha ON mensajes (solicitud_id, enviado_en);

-- ---------------------------------------------------------------------
-- VALORACIONES (reputación)
-- ---------------------------------------------------------------------
CREATE TABLE valoraciones (
    id            BIGSERIAL     PRIMARY KEY,
    solicitud_id  BIGINT        NOT NULL,
    autor_id      BIGINT        NOT NULL,
    valorado_id   BIGINT        NOT NULL,
    puntuacion    INTEGER       NOT NULL,
    comentario    VARCHAR(500),
    creada_en     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_val_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id) ON DELETE CASCADE,
    CONSTRAINT fk_val_autor     FOREIGN KEY (autor_id)     REFERENCES usuarios (id)    ON DELETE RESTRICT,
    CONSTRAINT fk_val_valorado  FOREIGN KEY (valorado_id)  REFERENCES usuarios (id)    ON DELETE RESTRICT,
    CONSTRAINT uq_val_solicitud_autor UNIQUE (solicitud_id, autor_id),
    CONSTRAINT ck_val_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT ck_val_personas   CHECK (autor_id <> valorado_id)
);
CREATE INDEX idx_val_valorado ON valoraciones (valorado_id);

-- ---------------------------------------------------------------------
-- NOTIFICACIONES
-- ---------------------------------------------------------------------
CREATE TABLE notificaciones (
    id          BIGSERIAL     PRIMARY KEY,
    usuario_id  BIGINT        NOT NULL,
    tipo        VARCHAR(30)   NOT NULL,
    mensaje     VARCHAR(255)  NOT NULL,
    recurso     VARCHAR(30),
    recurso_id  BIGINT,
    leida       BOOLEAN       NOT NULL DEFAULT FALSE,
    creada_en   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_notif_tipo CHECK (tipo IN (
        'SOLICITUD_RECIBIDA', 'SOLICITUD_ACEPTADA', 'SOLICITUD_RECHAZADA', 'NUEVO_MENSAJE',
        'PRESTAMO_VENCE_MANANA', 'PRESTAMO_RETRASADO', 'NUEVA_VALORACION'))
);
CREATE INDEX idx_notif_usuario_leida ON notificaciones (usuario_id, leida, creada_en DESC);

-- Idempotencia de la tarea programada: cada aviso de vencimiento/retraso se genera una sola vez
CREATE UNIQUE INDEX uq_notif_aviso_unico ON notificaciones (usuario_id, tipo, recurso_id)
    WHERE tipo IN ('PRESTAMO_VENCE_MANANA', 'PRESTAMO_RETRASADO');
