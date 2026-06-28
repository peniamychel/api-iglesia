-- Script de migración para la creación de la tabla de activos / bienes de las iglesias
CREATE TABLE IF NOT EXISTS activo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    cantidad INT NOT NULL DEFAULT 1,
    estado_conservacion VARCHAR(50) DEFAULT 'BUENO',
    valor_estimado DOUBLE,
    fecha_adquisicion DATE,
    iglesia_id BIGINT NOT NULL,
    CONSTRAINT fk_activo_iglesia FOREIGN KEY (iglesia_id) REFERENCES iglesia (id)
);
