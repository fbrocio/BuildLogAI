USE gestorobras;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO users (id, name, email, password, created_at)
VALUES
  (
    1,
    'Usuario Demo',
    'demo@example.com',
    '$2a$10$zIpk2UamOdQHBv7zIUcW9u1KbA8xau2ilReyZyYDrsnohxYM7Gvzq',
    '2026-05-22 09:00:00'
  ),
  (
    2,
    'Jefe de Obra',
    'jefe.obra@example.com',
    '$2a$10$zIpk2UamOdQHBv7zIUcW9u1KbA8xau2ilReyZyYDrsnohxYM7Gvzq',
    '2026-05-22 09:15:00'
  )
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  email = VALUES(email),
  password = VALUES(password);

INSERT INTO projects (id, name, description, created_by, created_at)
VALUES
  (
    1,
    'Reforma local comercial',
    'Seguimiento de obra para reforma interior de local comercial.',
    1,
    '2026-05-22 10:00:00'
  ),
  (
    2,
    'Edificio residencial Norte',
    'Control de avances, incidencias y pendientes de edificio residencial.',
    2,
    '2026-05-22 10:30:00'
  )
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  created_by = VALUES(created_by);

INSERT INTO project_users (project_id, user_id)
VALUES
  (1, 1),
  (1, 2),
  (2, 2)
ON DUPLICATE KEY UPDATE
  project_id = VALUES(project_id),
  user_id = VALUES(user_id);

INSERT INTO user_inputs (id, raw_text, project_id, source, created_at)
VALUES
  (
    1,
    'Han llegado rotas 5 luminarias de ArkosLight. Pedir reposicion antes del viernes.',
    1,
    'texto',
    '2026-05-22 11:00:00'
  ),
  (
    2,
    'Se ha completado el 60 por ciento de la tabiqueria en planta baja.',
    2,
    'texto',
    '2026-05-22 11:30:00'
  )
ON DUPLICATE KEY UPDATE
  raw_text = VALUES(raw_text),
  project_id = VALUES(project_id),
  source = VALUES(source);

INSERT INTO records (
  id,
  title,
  description,
  type,
  status,
  project_id,
  created_by,
  related_record_id,
  created_at,
  source_input_id,
  structured_data
)
VALUES
  (
    1,
    'Recepcion de luminarias defectuosas',
    'Han llegado 5 luminarias rotas de ArkosLight.',
    'INCIDENCIA',
    'ABIERTA',
    1,
    1,
    NULL,
    '2026-05-22 12:00:00',
    1,
    JSON_OBJECT(
      'company', 'ArkosLight',
      'subject', 'Luminarias defectuosas',
      'quantity', 5,
      'unit', 'uds'
    )
  ),
  (
    2,
    'Solicitar reposicion de luminarias',
    'Pedir reposicion de las luminarias defectuosas antes del viernes.',
    'PENDIENTE',
    'ABIERTA',
    1,
    1,
    1,
    '2026-05-22 12:05:00',
    1,
    JSON_OBJECT(
      'company', 'ArkosLight',
      'subject', 'Reposicion de luminarias'
    )
  ),
  (
    3,
    'Tabiqueria de planta baja al 60 por ciento',
    'Se ha completado el 60 por ciento de la tabiqueria en planta baja.',
    'AVANCE',
    'ABIERTA',
    2,
    2,
    NULL,
    '2026-05-22 12:30:00',
    2,
    JSON_OBJECT(
      'subject', 'Tabiqueria planta baja',
      'percentage', 60
    )
  ),
  (
    4,
    'Revision de remates en acceso principal',
    'Queda pendiente revisar los remates del acceso principal antes de la entrega.',
    'PENDIENTE',
    'CERRADA',
    1,
    2,
    NULL,
    '2026-05-22 13:00:00',
    NULL,
    JSON_OBJECT(
      'subject', 'Remates acceso principal'
    )
  )
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  type = VALUES(type),
  status = VALUES(status),
  project_id = VALUES(project_id),
  created_by = VALUES(created_by),
  related_record_id = VALUES(related_record_id),
  source_input_id = VALUES(source_input_id),
  structured_data = VALUES(structured_data);

INSERT INTO record_images (id, record_id, image_url, created_at)
VALUES
  (
    1,
    1,
    '/upload/records/demo-luminarias-defectuosas.jpg',
    '2026-05-22 12:10:00'
  ),
  (
    2,
    3,
    '/upload/records/demo-tabiqueria-planta-baja.jpg',
    '2026-05-22 12:40:00'
  )
ON DUPLICATE KEY UPDATE
  record_id = VALUES(record_id),
  image_url = VALUES(image_url);

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE users AUTO_INCREMENT = 100;
ALTER TABLE projects AUTO_INCREMENT = 100;
ALTER TABLE user_inputs AUTO_INCREMENT = 100;
ALTER TABLE records AUTO_INCREMENT = 100;
ALTER TABLE record_images AUTO_INCREMENT = 100;
