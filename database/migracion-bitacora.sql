-- Script de migración para la creación de la tabla de bitácora (auditoría / logs del sistema)
CREATE TABLE IF NOT EXISTS bitacora (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    username VARCHAR(255),
    accion VARCHAR(100) NOT NULL,
    modulo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000),
    fecha DATETIME NOT NULL,
    ip_address VARCHAR(50),
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
