-- Datos de prueba - Capit@l Humano
-- Se corre después de esquema.sql:  psql -d capital_humano -f esquema.sql -f datos-prueba.sql
--
-- Qué permite probar:
--   CU020-CU023  preguntas con sus opciones (9 factores, 25 preguntas)
--   CU024-CU031  3 plantillas de respuesta
--   CU025        puestos PU01 y PU02 evaluables; PU03 NO evaluable (Negociación tiene 1 sola pregunta -> 9.A)
--                candidatos 1006 y 1007 ya tienen un cuestionario ACTIVO -> 6.A
--   CU026        candidato 1006 (DNI 30000006, clave ACT00006) y 1007 (DNI 30000007, clave ACT00007)
--   CU028        evaluación 1 (puesto PU01) con cuestionarios COMPLETO (apto y no apto), INCOMPLETO y SIN_CONTESTAR
--
-- Escala de opciones: 4 opciones con ponderaciones 0, 2, 3 y 5 (suman 10). El total de un factor
-- va de 0 a 5 (suma de 2 preguntas / 2), y las ponderaciones mínimas de los puestos están en esa escala.

BEGIN;

-- ---------------- Configuración ----------------
INSERT INTO parametro (clave, valor) VALUES
 ('instrucciones', 'Lea cada pregunta y elija la opción que mejor lo describe. Las preguntas se muestran en bloques; debe responder todas las del bloque para avanzar. Si sale, puede volver a ingresar con la misma clave mientras no venza el plazo ni supere la cantidad máxima de accesos.'),
 ('preguntas_por_bloque', '4'),
 ('tiempo_maximo_minutos', '60'),
 ('plazo_activo_dias', '7'),
 ('max_accesos', '3'),
 -- Candidatos por página en la búsqueda de CU025. Valores admitidos: 10, 20 o 50
 ('candidatos_por_pagina', '10');

-- ---------------- Consultores (el login real va por LDAP) ----------------
INSERT INTO consultor (nombre_usuario) VALUES ('consultor'), ('mmeyer');

-- ---------------- Candidatos ----------------
INSERT INTO candidato (nro_candidato, apellido, nombre, tipo_documento, nro_documento, fecha_nacimiento, genero, nacionalidad, email, escolaridad) VALUES
 (1001, 'Gómez',     'Lucía',     'DNI', '30000001', '1995-03-12', 'F', 'Argentina', 'lucia.gomez@mail.com',     'Universitario completo'),
 (1002, 'Pérez',     'Martín',    'DNI', '30000002', '1992-07-25', 'M', 'Argentina', 'martin.perez@mail.com',    'Universitario completo'),
 (1003, 'Fernández', 'Carla',     'DNI', '30000003', '1998-11-02', 'F', 'Argentina', 'carla.fernandez@mail.com', 'Universitario incompleto'),
 (1004, 'López',     'Diego',     'DNI', '30000004', '1990-01-30', 'M', 'Argentina', 'diego.lopez@mail.com',     'Terciario completo'),
 (1005, 'Martínez',  'Sofía',     'DNI', '30000005', '1996-05-18', 'F', 'Uruguaya',  'sofia.martinez@mail.com',  'Universitario completo'),
 (1006, 'Romero',    'Juan',      'DNI', '30000006', '1993-09-09', 'M', 'Argentina', 'juan.romero@mail.com',     'Secundario completo'),
 (1007, 'Sosa',      'Valentina', 'DNI', '30000007', '1999-12-01', 'F', 'Argentina', 'valentina.sosa@mail.com',  'Universitario incompleto'),
 (1008, 'Díaz',      'Tomás',     'DNI', '30000008', '1997-04-14', 'M', 'Argentina', 'tomas.diaz@mail.com',      'Universitario completo'),
 (1009, 'Álvarez',   'Camila',    'PP',  'AB123456', '1994-08-21', 'F', 'Chilena',   'camila.alvarez@mail.com',  'Terciario completo'),
 (1010, 'Torres',    'Nicolás',   'DNI', '30000010', '1991-02-27', 'M', 'Argentina', 'nicolas.torres@mail.com',  'Universitario completo');

-- ---------------- Competencias y factores ----------------
INSERT INTO competencia (codigo, nombre, descripcion, tipo) VALUES
 ('C01', 'Liderazgo',          'Capacidad de guiar y motivar a un equipo hacia objetivos comunes.', 'ACTITUDINAL'),
 ('C02', 'Trabajo en equipo',  'Capacidad de colaborar con otros y aportar al resultado del grupo.', 'ACTITUDINAL'),
 ('C03', 'Comunicación',       'Capacidad de transmitir ideas con claridad y escuchar a los demás.',  'ACTITUDINAL'),
 ('C04', 'Programación Java',  'Conocimiento técnico de desarrollo en Java.',                         'TECNICA'),
 ('C05', 'Negociación',        'Capacidad de llegar a acuerdos convenientes para las partes.',        'ACTITUDINAL');

INSERT INTO factor (codigo, codigo_competencia, nombre, descripcion, nro_orden) VALUES
 ('F01', 'C01', 'Toma de decisiones',        'Decide con criterio y en tiempo.',               1),
 ('F02', 'C01', 'Motivación del equipo',     'Genera compromiso en los demás.',                 2),
 ('F03', 'C02', 'Cooperación',               'Ayuda y comparte información con el equipo.',     1),
 ('F04', 'C02', 'Resolución de conflictos',  'Maneja desacuerdos de forma constructiva.',       2),
 ('F05', 'C03', 'Comunicación oral',         'Se expresa con claridad.',                        1),
 ('F06', 'C03', 'Escucha activa',            'Presta atención y comprende a los demás.',        2),
 ('F07', 'C04', 'Orientación a objetos',     'Aplica correctamente los conceptos de POO.',      1),
 ('F08', 'C04', 'Buenas prácticas',          'Escribe código claro, probado y mantenible.',     2),
 ('F09', 'C05', 'Persuasión',                'Convence con argumentos.',                        1);

-- ---------------- Puestos y competencias requeridas ----------------
INSERT INTO puesto (codigo, nombre, descripcion, empresa) VALUES
 ('PU01', 'Desarrollador Java Senior', 'Desarrollo y mantenimiento de sistemas en Java.',        'TechSur S.A.'),
 ('PU02', 'Líder de Proyecto',         'Coordinación de equipos de desarrollo.',                 'Consultora Litoral'),
 ('PU03', 'Ejecutivo Comercial',       'Venta de servicios de software a clientes corporativos.', 'TechSur S.A.');

INSERT INTO competencia_requerida (codigo_puesto, codigo_competencia, ponderacion_minima) VALUES
 ('PU01', 'C02', 3.0), ('PU01', 'C03', 3.0), ('PU01', 'C04', 4.0),
 ('PU02', 'C01', 4.0), ('PU02', 'C02', 3.5), ('PU02', 'C03', 3.5),
 ('PU03', 'C03', 3.0), ('PU03', 'C05', 3.0);

-- ---------------- Preguntas (con códigos fijos para poder referenciarlas) ----------------
INSERT INTO pregunta (codigo, codigo_factor, nombre, enunciado, descripcion) VALUES
 ('P00001', 'F01', 'Decisión bajo presión',      '¿Toma decisiones rápidas cuando el equipo lo necesita?',            NULL),
 ('P00002', 'F01', 'Análisis de alternativas',   '¿Analiza varias alternativas antes de decidir?',                    NULL),
 ('P00003', 'F01', 'Asumir responsabilidad',     '¿Se hace cargo de las consecuencias de sus decisiones?',            NULL),
 ('P00004', 'F02', 'Reconocimiento',             '¿Reconoce públicamente los logros de su equipo?',                   NULL),
 ('P00005', 'F02', 'Objetivos compartidos',      '¿Explica al equipo para qué sirve cada tarea?',                     NULL),
 ('P00006', 'F02', 'Apoyo ante dificultades',    '¿Acompaña a quien tiene problemas para avanzar?',                   NULL),
 ('P00007', 'F03', 'Compartir información',      '¿Comparte la información que puede servir a otros?',                NULL),
 ('P00008', 'F03', 'Ayuda a compañeros',         '¿Ofrece ayuda cuando un compañero está atrasado?',                  NULL),
 ('P00009', 'F03', 'Metas del grupo',            '¿Prioriza las metas del grupo sobre las propias?',                  NULL),
 ('P00010', 'F04', 'Escuchar posturas',          '¿Escucha todas las posturas antes de opinar en un conflicto?',       NULL),
 ('P00011', 'F04', 'Buscar acuerdos',            '¿Propone soluciones que sirvan a ambas partes?',                    NULL),
 ('P00012', 'F04', 'Control emocional',          '¿Mantiene la calma en discusiones tensas?',                         NULL),
 ('P00013', 'F05', 'Claridad al explicar',       '¿Explica sus ideas de forma clara y ordenada?',                      NULL),
 ('P00014', 'F05', 'Adaptar el lenguaje',        '¿Adapta su forma de hablar según quién lo escucha?',                NULL),
 ('P00015', 'F05', 'Presentaciones',             '¿Se siente cómodo presentando frente a un grupo?',                  NULL),
 ('P00016', 'F06', 'Atención al interlocutor',   '¿Evita interrumpir cuando otro está hablando?',                     NULL),
 ('P00017', 'F06', 'Repreguntar',                '¿Hace preguntas para confirmar que entendió bien?',                 NULL),
 ('P00018', 'F06', 'Considerar opiniones',       '¿Tiene en cuenta las opiniones distintas a la suya?',               NULL),
 ('P00019', 'F07', 'Herencia y polimorfismo',    'Nivel de uso de herencia y polimorfismo en sus diseños',            NULL),
 ('P00020', 'F07', 'Encapsulamiento',            'Nivel de aplicación del encapsulamiento',                           NULL),
 ('P00021', 'F07', 'Interfaces',                 'Nivel de uso de interfaces para desacoplar clases',                 NULL),
 ('P00022', 'F08', 'Tests unitarios',            'Nivel de experiencia escribiendo tests unitarios',                  NULL),
 ('P00023', 'F08', 'Código limpio',              'Nivel de aplicación de nombres claros y métodos cortos',            NULL),
 ('P00024', 'F08', 'Control de versiones',       'Nivel de uso de Git (ramas, merges, pull requests)',                NULL),
 ('P00025', 'F09', 'Argumentación',              '¿Sostiene su postura con datos concretos?',                         NULL);
SELECT setval('seq_pregunta', 25);

-- Opciones: escala de frecuencia para las actitudinales, escala de nivel para las técnicas
INSERT INTO opcion_respuesta (codigo_pregunta, orden_visualizacion, descripcion, ponderacion)
SELECT p.codigo, o.orden, o.descripcion, o.ponderacion
FROM pregunta p
JOIN factor f ON f.codigo = p.codigo_factor
JOIN competencia c ON c.codigo = f.codigo_competencia
JOIN (VALUES ('ACTITUDINAL', 1, 'Nunca', 0), ('ACTITUDINAL', 2, 'A veces', 2),
             ('ACTITUDINAL', 3, 'Frecuentemente', 3), ('ACTITUDINAL', 4, 'Siempre', 5),
             ('TECNICA', 1, 'Ninguno', 0), ('TECNICA', 2, 'Básico', 2),
             ('TECNICA', 3, 'Intermedio', 3), ('TECNICA', 4, 'Avanzado', 5))
     AS o(tipo, orden, descripcion, ponderacion) ON o.tipo = c.tipo;

-- ---------------- Plantillas de respuesta (CU024, CU029-031) ----------------
INSERT INTO plantilla_respuesta (nombre, descripcion) VALUES
 ('Frecuencia',            'Con qué frecuencia ocurre una conducta.'),
 ('Nivel de conocimiento', 'Grado de dominio de un tema técnico.'),
 ('Grado de acuerdo',      'Escala de acuerdo de cinco opciones.');
INSERT INTO item_plantilla (id_plantilla, orden_visualizacion, descripcion)
SELECT pr.id_plantilla, i.orden, i.descripcion
FROM plantilla_respuesta pr
JOIN (VALUES ('Frecuencia', 1, 'Nunca'), ('Frecuencia', 2, 'A veces'), ('Frecuencia', 3, 'Frecuentemente'), ('Frecuencia', 4, 'Siempre'),
             ('Nivel de conocimiento', 1, 'Ninguno'), ('Nivel de conocimiento', 2, 'Básico'), ('Nivel de conocimiento', 3, 'Intermedio'), ('Nivel de conocimiento', 4, 'Avanzado'),
             ('Grado de acuerdo', 1, 'Totalmente en desacuerdo'), ('Grado de acuerdo', 2, 'En desacuerdo'), ('Grado de acuerdo', 3, 'Neutral'),
             ('Grado de acuerdo', 4, 'De acuerdo'), ('Grado de acuerdo', 5, 'Totalmente de acuerdo'))
     AS i(plantilla, orden, descripcion) ON i.plantilla = pr.nombre;

-- ---------------- Funciones auxiliares (solo para armar estos datos; se borran al final) ----------------

-- Copia el modelo de la evaluación: preguntas activas de los factores con 2 o más preguntas
CREATE FUNCTION tmp_copiar_modelo(p_eval INTEGER) RETURNS VOID AS $$
BEGIN
  INSERT INTO pregunta_evaluacion (id_evaluacion, codigo_pregunta, enunciado)
  SELECT p_eval, p.codigo, p.enunciado
  FROM evaluacion e
  JOIN competencia_requerida cr ON cr.codigo_puesto = e.codigo_puesto
  JOIN factor f ON f.codigo_competencia = cr.codigo_competencia AND NOT f.eliminado
  JOIN pregunta p ON p.codigo_factor = f.codigo AND NOT p.eliminado
  WHERE e.id_evaluacion = p_eval
    AND (SELECT count(*) FROM pregunta p2 WHERE p2.codigo_factor = f.codigo AND NOT p2.eliminado) >= 2;

  INSERT INTO opcion_evaluacion (id_pregunta_evaluacion, orden_visualizacion, descripcion, ponderacion)
  SELECT pe.id_pregunta_evaluacion, o.orden_visualizacion, o.descripcion, o.ponderacion
  FROM pregunta_evaluacion pe
  JOIN opcion_respuesta o ON o.codigo_pregunta = pe.codigo_pregunta
  WHERE pe.id_evaluacion = p_eval;
END $$ LANGUAGE plpgsql;

-- Arma el cuestionario (2 preguntas por factor, mezcladas, bloques de 4) y responde los primeros
-- p_bloques_resp bloques. p_orden_tecnica / p_orden_otras: orden de la opción que elige el candidato.
-- Si responde todo, calcula los resultados como en CU026 (obs. 3).
CREATE FUNCTION tmp_armar_cuestionario(p_cuest INTEGER, p_orden_tecnica INTEGER, p_orden_otras INTEGER,
                                       p_bloques_resp INTEGER) RETURNS VOID AS $$
DECLARE
  v_eval INTEGER; v_tam INTEGER; v_total INTEGER; r RECORD; v_rc INTEGER;
BEGIN
  SELECT id_evaluacion INTO v_eval FROM cuestionario WHERE id_cuestionario = p_cuest;
  SELECT valor::INTEGER INTO v_tam FROM parametro WHERE clave = 'preguntas_por_bloque';

  CREATE TEMP TABLE tmp_sel ON COMMIT DROP AS
  SELECT pe.id_pregunta_evaluacion, f.codigo AS factor, c.tipo,
         row_number() OVER (ORDER BY md5(pe.codigo_pregunta || p_cuest)) AS orden
  FROM (SELECT pe.*, row_number() OVER (PARTITION BY p.codigo_factor ORDER BY md5(p.codigo || p_cuest)) AS n
        FROM pregunta_evaluacion pe JOIN pregunta p ON p.codigo = pe.codigo_pregunta
        WHERE pe.id_evaluacion = v_eval) pe
  JOIN pregunta p ON p.codigo = pe.codigo_pregunta
  JOIN factor f ON f.codigo = p.codigo_factor
  JOIN competencia c ON c.codigo = f.codigo_competencia
  WHERE pe.n <= 2;

  SELECT count(*) INTO v_total FROM tmp_sel;
  INSERT INTO bloque_preguntas (id_cuestionario, nro_bloque, respondido)
  SELECT p_cuest, b, b <= p_bloques_resp FROM generate_series(1, ceil(v_total::NUMERIC / v_tam)::INTEGER) b;

  INSERT INTO pregunta_cuestionario (id_cuestionario, nro_bloque, id_pregunta_evaluacion, orden_presentacion,
                                     id_opcion_elegida, fecha_hora_respuesta, descripcion_opcion_elegida, ponderacion_obtenida)
  SELECT p_cuest, ((s.orden - 1) / v_tam) + 1, s.id_pregunta_evaluacion, s.orden,
         CASE WHEN ((s.orden - 1) / v_tam) + 1 <= p_bloques_resp THEN oe.id_opcion_evaluacion END,
         CASE WHEN ((s.orden - 1) / v_tam) + 1 <= p_bloques_resp THEN now() - interval '9 days' END,
         CASE WHEN ((s.orden - 1) / v_tam) + 1 <= p_bloques_resp THEN oe.descripcion END,
         CASE WHEN ((s.orden - 1) / v_tam) + 1 <= p_bloques_resp THEN oe.ponderacion END
  FROM tmp_sel s
  JOIN opcion_evaluacion oe ON oe.id_pregunta_evaluacion = s.id_pregunta_evaluacion
   AND oe.orden_visualizacion = CASE WHEN s.tipo = 'TECNICA' THEN p_orden_tecnica ELSE p_orden_otras END;

  IF p_bloques_resp * v_tam >= v_total THEN
    FOR r IN SELECT DISTINCT f.codigo_competencia AS comp
             FROM tmp_sel s JOIN factor f ON f.codigo = s.factor LOOP
      INSERT INTO resultado_competencia (id_cuestionario, codigo_competencia, total_competencia)
      SELECT p_cuest, r.comp, avg(tf.total)
      FROM (SELECT s.factor, sum(pc.ponderacion_obtenida) / 2 AS total
            FROM tmp_sel s JOIN pregunta_cuestionario pc
              ON pc.id_cuestionario = p_cuest AND pc.id_pregunta_evaluacion = s.id_pregunta_evaluacion
            JOIN factor f ON f.codigo = s.factor
            WHERE f.codigo_competencia = r.comp GROUP BY s.factor) tf
      RETURNING id_resultado_competencia INTO v_rc;

      INSERT INTO resultado_factor (id_resultado_competencia, codigo_factor, total_factor)
      SELECT v_rc, s.factor, sum(pc.ponderacion_obtenida) / 2
      FROM tmp_sel s JOIN pregunta_cuestionario pc
        ON pc.id_cuestionario = p_cuest AND pc.id_pregunta_evaluacion = s.id_pregunta_evaluacion
      JOIN factor f ON f.codigo = s.factor
      WHERE f.codigo_competencia = r.comp GROUP BY s.factor;
    END LOOP;
  END IF;
  DROP TABLE tmp_sel;
END $$ LANGUAGE plpgsql;

-- ---------------- Evaluación 1: PU01, ya terminada (para CU028) ----------------
INSERT INTO evaluacion (id_evaluacion, fecha_creacion, codigo_puesto, nombre_usuario)
VALUES (1, now() - interval '10 days', 'PU01', 'consultor');
SELECT tmp_copiar_modelo(1);

INSERT INTO cuestionario (id_cuestionario, id_evaluacion, nro_candidato, clave, estado, fecha_hora_creacion,
                          fecha_hora_comienzo, fecha_hora_fin, ultimo_ingreso, cantidad_accesos,
                          tiempo_maximo, plazo_activo, max_accesos) VALUES
 -- elige "Siempre/Avanzado" en todo: apto
 (1, 1, 1001, 'CLV00001', 'COMPLETO',      now() - interval '10 days', now() - interval '9 days', now() - interval '9 days' + interval '35 minutes', now() - interval '9 days', 1, 60, 7, 3),
 -- elige la tercera opción en todo: Programación Java = 3 < 4, no apto
 (2, 1, 1002, 'CLV00002', 'COMPLETO',      now() - interval '10 days', now() - interval '9 days', now() - interval '9 days' + interval '50 minutes', now() - interval '9 days', 2, 60, 7, 3),
 -- técnica "Avanzado", el resto tercera opción: 5 >= 4 y 3 >= 3, apto
 (3, 1, 1003, 'CLV00003', 'COMPLETO',      now() - interval '10 days', now() - interval '8 days', now() - interval '8 days' + interval '40 minutes', now() - interval '8 days', 1, 60, 7, 3),
 -- empezó, respondió 1 bloque y superó el tiempo máximo
 (4, 1, 1004, 'CLV00004', 'INCOMPLETO',    now() - interval '10 days', now() - interval '9 days', now() - interval '9 days' + interval '61 minutes', now() - interval '9 days', 2, 60, 7, 3),
 -- nunca entró y venció el plazo del estado activo
 (5, 1, 1005, 'CLV00005', 'SIN_CONTESTAR', now() - interval '10 days', NULL,                       now() - interval '3 days',                          NULL,                       0, 60, 7, 3);

SELECT tmp_armar_cuestionario(1, 4, 4, 99);
SELECT tmp_armar_cuestionario(2, 3, 3, 99);
SELECT tmp_armar_cuestionario(3, 4, 3, 99);
SELECT tmp_armar_cuestionario(4, 4, 4, 1);

-- ---------------- Evaluación 2: PU02, en curso (para CU026 y la regla 6.A de CU025) ----------------
INSERT INTO evaluacion (id_evaluacion, fecha_creacion, codigo_puesto, nombre_usuario)
VALUES (2, now() - interval '1 hour', 'PU02', 'consultor');
SELECT tmp_copiar_modelo(2);

INSERT INTO cuestionario (id_cuestionario, id_evaluacion, nro_candidato, clave, estado, fecha_hora_creacion,
                          cantidad_accesos, tiempo_maximo, plazo_activo, max_accesos) VALUES
 (6, 2, 1006, 'ACT00006', 'ACTIVO', now() - interval '1 hour', 0, 60, 7, 3),
 (7, 2, 1007, 'ACT00007', 'ACTIVO', now() - interval '1 hour', 0, 60, 7, 3);

SELECT setval(pg_get_serial_sequence('evaluacion', 'id_evaluacion'), 2);
SELECT setval(pg_get_serial_sequence('cuestionario', 'id_cuestionario'), 7);

DROP FUNCTION tmp_armar_cuestionario(INTEGER, INTEGER, INTEGER, INTEGER);
DROP FUNCTION tmp_copiar_modelo(INTEGER);

COMMIT;
