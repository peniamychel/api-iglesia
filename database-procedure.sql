-- ObtenerIglesiasConMasMiembros
DELIMITER //

CREATE PROCEDURE ObtenerIglesiasConMasMiembros(IN limite INT)
BEGIN
SELECT
    i.id AS iglesia_id,
    i.nombre AS nombre_iglesia,
    i.direccion AS direccion,
    COUNT(m.id) AS cantidad_miembros
FROM
    iglesia i
        JOIN
    miembros_iglesia mi ON i.id = mi.id_iglesia
        JOIN
    miembro m ON mi.id_miembros = m.id
WHERE
        mi.estado = true
GROUP BY
    i.id, i.nombre, i.direccion
ORDER BY
    cantidad_miembros DESC
    LIMIT limite;
END //

DELIMITER ;

CALL ObtenerIglesiasConMasMiembros(4);



