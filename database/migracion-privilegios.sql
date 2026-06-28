-- ============================================================================
-- Migración: Privilegios "Gestionar X" → Modelo Ver/Escribir (2 niveles)
-- ============================================================================
-- Proyecto: api-iglesia (MariaDB — schema iglev3)
-- Fecha:    Junio 2026
--
-- OBJETIVO:
--   Reemplazar los 7 privilegios gruesos "Gestionar X" (todo-o-nada) por un
--   modelo de 2 niveles por entidad:
--     - Ver X       → permite entrar a la página del módulo (GET)
--     - Escribir X  → permite crear/editar/eliminar/estado/actas/fotos (POST/PUT/DELETE)
--
--   Los módulos de SOLO LECTURA ya existentes (Dashboard, Reportes, Bitácora,
--   Ayuda) se conservan sin cambios porque no tienen operaciones de escritura.
--
-- ADVERTENCIA: Hace COMMIT al final. Revisar antes de ejecutar en producción.
-- ============================================================================

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- Paso 1: Limpiar asignaciones y privilegios viejos
-- ---------------------------------------------------------------------------

-- 1a. Quitar TODAS las asignaciones rol↔privilegio actuales
--     (se reasignan en el paso 3 con los nuevos privilegios)
DELETE FROM rol_cargo_privilegio;

-- 1b. Eliminar los privilegios "Gestionar X" viejos (cualquier casing)
DELETE FROM privilegio
WHERE LOWER(nombre) LIKE 'gestionar %';

-- ---------------------------------------------------------------------------
-- Paso 2: Insertar los 17 privilegios nuevos (Ver / Escribir × 8 entidades + Ofrendas)
-- ---------------------------------------------------------------------------
-- El campo "acto" es metadato descriptivo (no se usa en seguridad, sólo en BD).

INSERT INTO privilegio (nombre, acto, estado, created_at, updated_at) VALUES
-- Miembros
('Ver Miembros',      'Consultar y navegar el módulo de miembros', 1, NOW(), NOW()),
('Escribir Miembros', 'Crear, editar, eliminar, cambiar estado y subir fotos de miembros', 1, NOW(), NOW()),
-- Iglesias
('Ver Iglesias',      'Consultar y navegar el módulo de iglesias', 1, NOW(), NOW()),
('Escribir Iglesias', 'Crear, editar, eliminar, cambiar estado y subir fotos de iglesias', 1, NOW(), NOW()),
-- MiembroIglesia
('Ver MiembroIglesia',      'Consultar membresías y solicitudes de traspaso', 1, NOW(), NOW()),
('Escribir MiembroIglesia', 'Asignar, quitar y gestionar traspasos de miembros entre iglesias', 1, NOW(), NOW()),
-- Cargos (Obreros)
('Ver Cargos',      'Consultar el módulo de obreros/cargos y tipos de ministerio', 1, NOW(), NOW()),
('Escribir Cargos', 'Designar, editar, desvincular obreros y gestionar actas y tipos de ministerio', 1, NOW(), NOW()),
-- Eventos
('Ver Eventos',      'Consultar eventos, tipos de evento, participaciones y responsables', 1, NOW(), NOW()),
('Escribir Eventos', 'Crear, editar, eliminar y cambiar estado de eventos y sus submódulos', 1, NOW(), NOW()),
-- Certificados
('Ver Certificados',      'Consultar certificados, tipos y plantillas', 1, NOW(), NOW()),
('Escribir Certificados', 'Emitir, editar, eliminar certificados y diseñar plantillas', 1, NOW(), NOW()),
-- Usuarios
('Ver Usuarios',      'Consultar usuarios del sistema', 1, NOW(), NOW()),
('Escribir Usuarios', 'Crear, editar, eliminar usuarios y gestionar fotos de perfil', 1, NOW(), NOW()),
-- Privilegios
('Ver Privilegios',      'Consultar privilegios y su asignación a roles', 1, NOW(), NOW()),
('Escribir Privilegios', 'Crear, editar, eliminar privilegios y asignarlos a roles', 1, NOW(), NOW()),
-- Ofrendas (futuro módulo)
('Ver Ofrendas', 'Consultar el módulo de ofrendas', 1, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- Paso 3: Reasignar privilegios por defecto a cada rol_cargo
-- ---------------------------------------------------------------------------
-- Estos son VALORES POR DEFECTO razonables. El administrador puede ajustarlos
-- desde la UI (Usuarios → matriz de permisos) sin tocar este script.

-- ADMIN (super-admin): NO necesita filas.
-- UserDetailsServiceImpl le asigna TODOS los privilegios por código cuando
-- usuario.miembro == null.

-- ENCARGADO_IGLESIA: gestión completa de su iglesia
INSERT INTO rol_cargo_privilegio (rol_cargo_id, privilegio_id)
SELECT rc.id, p.id
FROM rol_cargo rc
CROSS JOIN privilegio p
WHERE rc.nombre_rol = 'ENCARGADO_IGLESIA'
  AND p.nombre IN (
    'Ver Miembros',        'Escribir Miembros',
    'Ver Iglesias',        'Escribir Iglesias',
    'Ver MiembroIglesia',  'Escribir MiembroIglesia',
    'Ver Cargos',          'Escribir Cargos'
  );

-- PASTOR: misma caja de herramientas que el encargado de iglesia
INSERT INTO rol_cargo_privilegio (rol_cargo_id, privilegio_id)
SELECT rc.id, p.id
FROM rol_cargo rc
CROSS JOIN privilegio p
WHERE rc.nombre_rol = 'PASTOR'
  AND p.nombre IN (
    'Ver Miembros',        'Escribir Miembros',
    'Ver Iglesias',        'Escribir Iglesias',
    'Ver MiembroIglesia',  'Escribir MiembroIglesia',
    'Ver Cargos',          'Escribir Cargos'
  );

-- ENCARGADO_EVENTO: gestión de eventos y certificados + consulta de miembros
INSERT INTO rol_cargo_privilegio (rol_cargo_id, privilegio_id)
SELECT rc.id, p.id
FROM rol_cargo rc
CROSS JOIN privilegio p
WHERE rc.nombre_rol = 'ENCARGADO_EVENTO'
  AND p.nombre IN (
    'Ver Eventos',         'Escribir Eventos',
    'Ver Certificados',    'Escribir Certificados',
    'Ver Miembros'         -- puede consultar miembros pero no editarlos
  );

COMMIT;

-- ============================================================================
-- Verificación post-migración (ejecutar a mano para confirmar)
-- ============================================================================

-- Total de privilegios nuevos:
-- SELECT nombre, estado FROM privilegio WHERE nombre LIKE 'Ver %' OR nombre LIKE 'Escribir %' ORDER BY nombre;

-- Matriz rol↔privilegio resultante:
-- SELECT rc.nombre_rol, p.nombre
-- FROM rol_cargo rc
-- JOIN rol_cargo_privilegio rcp ON rc.id = rcp.rol_cargo_id
-- JOIN privilegio p ON rcp.privilegio_id = p.id
-- ORDER BY rc.nombre_rol, p.nombre;
