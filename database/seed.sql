-- PostgreSQL demo data. Re-running it updates the same primary keys.
INSERT INTO users (id, name, email, password, created_at, verified)
VALUES
 (1, 'Usuario Demo', 'demo@example.com', '$2a$10$zIpk2UamOdQHBv7zIUcW9u1KbA8xau2ilReyZyYDrsnohxYM7Gvzq', '2026-05-22 09:00:00', TRUE),
 (2, 'Jefe de Obra', 'jefe.obra@example.com', '$2a$10$zIpk2UamOdQHBv7zIUcW9u1KbA8xau2ilReyZyYDrsnohxYM7Gvzq', '2026-05-22 09:15:00', TRUE)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, password = EXCLUDED.password;
INSERT INTO projects (id, name, description, created_by, created_at)
VALUES (1, 'Reforma local comercial', 'Seguimiento de obra para reforma interior.', 1, '2026-05-22 10:00:00'),
       (2, 'Edificio residencial Norte', 'Control de avances e incidencias.', 2, '2026-05-22 10:30:00')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, created_by = EXCLUDED.created_by;
INSERT INTO project_users (project_id, user_id) VALUES (1, 1), (1, 2), (2, 2) ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('users', 'id'), 100, true);
SELECT setval(pg_get_serial_sequence('projects', 'id'), 100, true);
