-- ============================================================================
-- SCRIPT DE MIGRACIÓN: MODELO SERVICIO - ACCIÓN (RBAC POR SERVICIO/ACCIÓN)
-- Base de datos: iglev3
-- ============================================================================

-- 1. Crear tabla SERVICIO
CREATE TABLE IF NOT EXISTS servicio (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    icono VARCHAR(100),
    ruta VARCHAR(255),
    orden INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Crear tabla ACCION
CREATE TABLE IF NOT EXISTS accion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    servicio_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accion_servicio
        FOREIGN KEY (servicio_id)
        REFERENCES servicio(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_accion_servicio_codigo
        UNIQUE(servicio_id, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Crear tabla ROL_CARGO_ACCION
CREATE TABLE IF NOT EXISTS rol_cargo_accion (
    rol_cargo_id BIGINT NOT NULL,
    accion_id BIGINT NOT NULL,
    PRIMARY KEY (rol_cargo_id, accion_id),
    CONSTRAINT fk_rca_rol_cargo
        FOREIGN KEY (rol_cargo_id)
        REFERENCES rol_cargo(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rca_accion
        FOREIGN KEY (accion_id)
        REFERENCES accion(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- DATOS SEMILLA DE SERVICIOS
-- ============================================================================
INSERT INTO servicio (id, codigo, nombre, descripcion, icono, ruta, orden, activo) VALUES
(1, 'MIEMBROS', 'Gestión de Miembros', 'Módulo de miembros e historial de membresía', 'group', '/admin/miembros', 1, TRUE),
(2, 'IGLESIAS', 'Gestión de Iglesias', 'Módulo de administración de iglesias y sedes', 'church', '/admin/igles', 2, TRUE),
(3, 'OBREROS', 'Gestión de Obreros y Cargos', 'Módulo de obreros, asignaciones y actas', 'badge', '/admin/cargos', 3, TRUE),
(4, 'EVENTOS', 'Gestión de Eventos', 'Módulo de eventos y conferencias', 'event', '/admin/eventos', 4, TRUE),
(5, 'CERTIFICADOS', 'Certificados y Actas', 'Módulo de generación de certificados', 'card_membership', '/admin/certificados', 5, TRUE),
(6, 'USUARIOS', 'Usuarios y Seguridad', 'Módulo de administración de usuarios y roles', 'manage_accounts', '/admin/usuario-sistema', 6, TRUE),
(7, 'DASHBOARD', 'Panel de Control', 'Módulo principal con métricas y estadísticas', 'dashboard', '/admin/dashboard', 0, TRUE),
(8, 'BITACORA', 'Bitácora del Sistema', 'Historial de auditoría y cambios', 'history', '/admin/bitacora', 7, TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), descripcion=VALUES(descripcion);

-- ============================================================================
-- DATOS SEMILLA DE ACCIONES POR SERVICIO
-- ============================================================================
-- Miembros (servicio 1)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(1, 'VER', 'Ver Miembros', 'Permite visualizar la lista y detalle de miembros'),
(1, 'CREAR', 'Crear Miembro', 'Permite registrar nuevos miembros'),
(1, 'EDITAR', 'Editar Miembro', 'Permite modificar la información de miembros'),
(1, 'ELIMINAR', 'Eliminar Miembro', 'Permite eliminar registros de miembros'),
(1, 'SUBIR_FOTO', 'Subir Foto', 'Permite cambiar la foto de perfil del miembro'),
(1, 'IMPRIMIR_HISTORIAL', 'Imprimir Historial', 'Permite exportar el PDF del historial del miembro')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Iglesias (servicio 2)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(2, 'VER', 'Ver Iglesias', 'Permite ver el listado y fichas de iglesias'),
(2, 'CREAR', 'Crear Iglesia', 'Permite registrar nuevas iglesias'),
(2, 'EDITAR', 'Editar Iglesia', 'Permite actualizar datos de iglesias'),
(2, 'ELIMINAR', 'Eliminar Iglesia', 'Permite dar de baja o eliminar iglesias'),
(2, 'ASIGNAR_PASTOR', 'Asignar Pastor', 'Permite asignar pastor responsable a iglesia')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Obreros (servicio 3)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(3, 'VER', 'Ver Obreros', 'Permite ver obreros y cargos asignados'),
(3, 'DESIGNAR', 'Designar Obrero', 'Permite asignar nuevos cargos u obreros'),
(3, 'EDITAR', 'Editar Cargo', 'Permite modificar datos del cargo'),
(3, 'DESVINCULAR', 'Desvincular Obrero', 'Permite finalizar la asignación de un obrero'),
(3, 'SUBIR_ACTA', 'Subir Acta', 'Permite adjuntar acta de asignación o deslindación')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Eventos (servicio 4)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(4, 'VER', 'Ver Eventos', 'Permite consultar eventos registrados'),
(4, 'CREAR', 'Crear EventO', 'Permite crear nuevos eventos'),
(4, 'EDITAR', 'Editar Evento', 'Permite modificar eventos'),
(4, 'ELIMINAR', 'Eliminar Evento', 'Permite cancelar o eliminar eventos')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Certificados (servicio 5)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(5, 'VER', 'Ver Certificados', 'Permite listar certificados emitidos'),
(5, 'GENERAR', 'Generar Certificado', 'Permite emitir nuevos certificados'),
(5, 'IMPRIMIR', 'Imprimir Certificado', 'Permite descargar en PDF certificados')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Usuarios (servicio 6)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(6, 'VER', 'Ver Usuarios', 'Permite ver usuarios del sistema'),
(6, 'CREAR', 'Crear Usuario', 'Permite crear cuentas de usuario'),
(6, 'EDITAR', 'Editar Usuario', 'Permite modificar usuarios y roles'),
(6, 'CAMBIAR_PASSWORD', 'Cambiar Contraseña', 'Permite restablecer contraseñas')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Dashboard (servicio 7)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(7, 'VER', 'Ver Dashboard', 'Permite acceder al panel principal')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Bitácora (servicio 8)
INSERT INTO accion (servicio_id, codigo, nombre, descripcion) VALUES
(8, 'VER', 'Ver Bitácora', 'Permite auditar el historial de acciones')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Asignación por defecto de todas las acciones a todos los roles existentes
INSERT IGNORE INTO rol_cargo_accion (rol_cargo_id, accion_id)
SELECT rc.id, a.id 
FROM rol_cargo rc 
CROSS JOIN accion a;
