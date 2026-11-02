-- Esquema relacional - Capit@l Humano (PostgreSQL 16)
-- Derivado del DER final. Un archivo, se corre de arriba hacia abajo.

CREATE TABLE consultor (
    nombre_usuario      VARCHAR(50) PRIMARY KEY
);

CREATE TABLE registro_auditoria (
    id_registro         SERIAL PRIMARY KEY,
    fecha_hora          TIMESTAMP    NOT NULL,
    accion              VARCHAR(100) NOT NULL,
    entidad_afectada    VARCHAR(100) NOT NULL,
    nombre_usuario      VARCHAR(50)  REFERENCES consultor   -- NULL: lo generó el proceso batch
);

CREATE TABLE candidato (
    nro_candidato       INTEGER PRIMARY KEY,
    apellido            VARCHAR(50)  NOT NULL,
    nombre              VARCHAR(50)  NOT NULL,
    tipo_documento      VARCHAR(3)   NOT NULL CHECK (tipo_documento IN ('DNI','LE','LC','PP')),
    nro_documento       VARCHAR(20)  NOT NULL,
    fecha_nacimiento    DATE,
    genero              CHAR(1),
    nacionalidad        VARCHAR(50),
    email               VARCHAR(100),
    escolaridad         VARCHAR(50),
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tipo_documento, nro_documento)
);

CREATE TABLE puesto (
    codigo              VARCHAR(20)  PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    descripcion         VARCHAR(500) NOT NULL,
    empresa             VARCHAR(100) NOT NULL,
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE competencia (
    codigo              VARCHAR(20)  PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    descripcion         VARCHAR(500),
    tipo                VARCHAR(11)  NOT NULL CHECK (tipo IN ('TECNICA','ACTITUDINAL')),
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE
);

-- Relación "requiere" (Puesto N:M Competencia) con su atributo
CREATE TABLE competencia_requerida (
    codigo_puesto       VARCHAR(20) REFERENCES puesto,
    codigo_competencia  VARCHAR(20) REFERENCES competencia,
    ponderacion_minima  NUMERIC(4,2) NOT NULL CHECK (ponderacion_minima BETWEEN 0 AND 10),
    PRIMARY KEY (codigo_puesto, codigo_competencia)
);

CREATE TABLE factor (
    codigo              VARCHAR(20)  PRIMARY KEY,
    codigo_competencia  VARCHAR(20)  NOT NULL REFERENCES competencia,
    nombre              VARCHAR(100) NOT NULL,
    descripcion         VARCHAR(500),
    nro_orden           INTEGER      NOT NULL,
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (codigo_competencia, nro_orden)
);

CREATE SEQUENCE seq_pregunta;
CREATE TABLE pregunta (
    codigo              VARCHAR(20)  PRIMARY KEY DEFAULT ('P' || LPAD(nextval('seq_pregunta')::TEXT, 5, '0')),
    codigo_factor       VARCHAR(20)  NOT NULL REFERENCES factor,
    nombre              VARCHAR(40)  NOT NULL,
    enunciado           VARCHAR(80)  NOT NULL,
    descripcion         VARCHAR(100),
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE
);
-- La pregunta no se repite en el factor, ni por nombre ni por enunciado (CU021, obs. 1)
CREATE UNIQUE INDEX ux_pregunta_nombre    ON pregunta (codigo_factor, nombre)    WHERE NOT eliminado;
CREATE UNIQUE INDEX ux_pregunta_enunciado ON pregunta (codigo_factor, enunciado) WHERE NOT eliminado;

-- Entidad débil de Pregunta: cada pregunta tiene sus propias opciones
CREATE TABLE opcion_respuesta (
    id_opcion           SERIAL PRIMARY KEY,
    codigo_pregunta     VARCHAR(20)  NOT NULL REFERENCES pregunta ON DELETE CASCADE,
    orden_visualizacion INTEGER      NOT NULL,
    descripcion         VARCHAR(50)  NOT NULL,
    ponderacion         NUMERIC(4,2) NOT NULL CHECK (ponderacion >= 0),
    UNIQUE (codigo_pregunta, orden_visualizacion),
    UNIQUE (codigo_pregunta, descripcion)
);

CREATE TABLE evaluacion (
    id_evaluacion       SERIAL PRIMARY KEY,
    fecha_creacion      TIMESTAMP   NOT NULL,
    codigo_puesto       VARCHAR(20) NOT NULL REFERENCES puesto,
    nombre_usuario      VARCHAR(50) NOT NULL REFERENCES consultor
);

-- Modelo de la evaluación: copia de cada pregunta y sus opciones al generarla (CU025).
-- Los cuestionarios usan esta copia, así no cambian si se edita o se da de baja la original.
CREATE TABLE pregunta_evaluacion (
    id_pregunta_evaluacion  SERIAL PRIMARY KEY,
    id_evaluacion           INTEGER     NOT NULL REFERENCES evaluacion ON DELETE CASCADE,
    codigo_pregunta         VARCHAR(20) NOT NULL REFERENCES pregunta,   -- "copia de" (de ahí sale el factor)
    enunciado               VARCHAR(80) NOT NULL,
    UNIQUE (id_evaluacion, codigo_pregunta)
);

CREATE TABLE opcion_evaluacion (
    id_opcion_evaluacion    SERIAL PRIMARY KEY,
    id_pregunta_evaluacion  INTEGER      NOT NULL REFERENCES pregunta_evaluacion ON DELETE CASCADE,
    orden_visualizacion     INTEGER      NOT NULL,
    descripcion             VARCHAR(50)  NOT NULL,
    ponderacion             NUMERIC(4,2) NOT NULL CHECK (ponderacion >= 0),
    UNIQUE (id_pregunta_evaluacion, orden_visualizacion)
);

CREATE TABLE cuestionario (
    id_cuestionario         SERIAL PRIMARY KEY,
    id_evaluacion           INTEGER NOT NULL REFERENCES evaluacion ON DELETE CASCADE,
    nro_candidato           INTEGER NOT NULL REFERENCES candidato,
    clave                   CHAR(8) NOT NULL,
    estado                  VARCHAR(13) NOT NULL
                            CHECK (estado IN ('ACTIVO','EN_PROCESO','COMPLETO','INCOMPLETO','SIN_CONTESTAR')),
    fecha_hora_creacion     TIMESTAMP NOT NULL,
    fecha_hora_comienzo     TIMESTAMP,
    fecha_hora_fin          TIMESTAMP,          -- cuándo terminó o venció (ver estado)
    ultimo_ingreso          TIMESTAMP,
    cantidad_accesos        INTEGER NOT NULL DEFAULT 0,
    tiempo_maximo           INTEGER NOT NULL,   -- minutos (desde el primer ingreso, CU026 obs. 2)
    plazo_activo            INTEGER NOT NULL,   -- días (desde la creación, mientras está ACTIVO)
    max_accesos             INTEGER NOT NULL,
    UNIQUE (id_evaluacion, nro_candidato)
);
-- A lo sumo un cuestionario ACTIVO o EN_PROCESO por candidato
CREATE UNIQUE INDEX ux_cuestionario_activo ON cuestionario (nro_candidato)
    WHERE estado IN ('ACTIVO','EN_PROCESO');

CREATE TABLE bloque_preguntas (
    id_cuestionario     INTEGER REFERENCES cuestionario ON DELETE CASCADE,
    nro_bloque          INTEGER,
    respondido          BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_cuestionario, nro_bloque)
);

-- Pregunta del modelo asignada a un bloque + respuesta del candidato (Bloque–PreguntaEvaluacion–OpcionEvaluacion)
CREATE TABLE pregunta_cuestionario (
    id_pregunta_cuestionario    SERIAL PRIMARY KEY,
    id_cuestionario             INTEGER     NOT NULL,
    nro_bloque                  INTEGER     NOT NULL,
    id_pregunta_evaluacion      INTEGER     NOT NULL REFERENCES pregunta_evaluacion,
    orden_presentacion          INTEGER     NOT NULL,
    id_opcion_elegida           INTEGER     REFERENCES opcion_evaluacion,  -- NULL hasta responder
    fecha_hora_respuesta        TIMESTAMP,
    descripcion_opcion_elegida  VARCHAR(50),
    ponderacion_obtenida        NUMERIC(4,2),
    FOREIGN KEY (id_cuestionario, nro_bloque) REFERENCES bloque_preguntas ON DELETE CASCADE,
    UNIQUE (id_cuestionario, id_pregunta_evaluacion),
    UNIQUE (id_cuestionario, orden_presentacion)
);

CREATE TABLE resultado_competencia (
    id_resultado_competencia SERIAL PRIMARY KEY,
    id_cuestionario     INTEGER     NOT NULL REFERENCES cuestionario ON DELETE CASCADE,
    codigo_competencia  VARCHAR(20) NOT NULL REFERENCES competencia,
    total_competencia   NUMERIC(4,2) NOT NULL,
    UNIQUE (id_cuestionario, codigo_competencia)
);

CREATE TABLE resultado_factor (
    id_resultado_factor SERIAL PRIMARY KEY,
    id_resultado_competencia INTEGER NOT NULL REFERENCES resultado_competencia ON DELETE CASCADE,
    codigo_factor       VARCHAR(20) NOT NULL REFERENCES factor,
    total_factor        NUMERIC(4,2) NOT NULL,
    UNIQUE (id_resultado_competencia, codigo_factor)
);

-- Plantillas de respuesta (ABM CU024, CU029-031). Solo precargan la grilla de CU021:
-- las opciones se copian a opcion_respuesta y la pregunta no queda vinculada a la plantilla.
CREATE TABLE plantilla_respuesta (
    id_plantilla        SERIAL PRIMARY KEY,
    nombre              VARCHAR(50)  NOT NULL,
    descripcion         VARCHAR(200) NOT NULL,
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX ux_plantilla_nombre ON plantilla_respuesta (nombre) WHERE NOT eliminado;

CREATE TABLE item_plantilla (
    id_plantilla        INTEGER     REFERENCES plantilla_respuesta ON DELETE CASCADE,
    orden_visualizacion INTEGER,
    descripcion         VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_plantilla, orden_visualizacion),
    UNIQUE (id_plantilla, descripcion)
);

-- Repositorio de configuración (instrucciones y parámetros). Claves usadas:
--   instrucciones (texto), preguntas_por_bloque, tiempo_maximo_minutos,
--   plazo_activo_dias, max_accesos. Al crear cada cuestionario (CU025) se copian
--   tiempo_maximo, plazo_activo y max_accesos al cuestionario.
CREATE TABLE parametro (
    clave               VARCHAR(50) PRIMARY KEY,
    valor               TEXT NOT NULL
);
