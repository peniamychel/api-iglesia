-- Script de migración para la creación de la tabla de ofrendas (ingresos/egresos)
CREATE TABLE IF NOT EXISTS ofrenda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iglesia_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    monto DOUBLE NOT NULL,
    fecha_recaudacion DATE NOT NULL,
    fecha_registro DATETIME,
    concepto_detalle VARCHAR(500),
    usuario_id BIGINT,
    CONSTRAINT fk_ofrenda_iglesia FOREIGN KEY (iglesia_id) REFERENCES iglesia (id),
    CONSTRAINT fk_ofrenda_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
