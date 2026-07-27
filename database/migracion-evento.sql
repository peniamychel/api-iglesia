-- Script de migración para agregar las columnas alcance y mostrar_en_calendario a la tabla evento
ALTER TABLE evento 
    ADD COLUMN IF NOT EXISTS alcance VARCHAR(50) DEFAULT 'LOCAL',
    ADD COLUMN IF NOT EXISTS mostrar_en_calendario BIT DEFAULT 1;
