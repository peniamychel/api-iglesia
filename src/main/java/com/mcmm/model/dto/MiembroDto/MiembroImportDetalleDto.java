package com.mcmm.model.dto.MiembroDto;

import lombok.*;

/**
 * Detalle de una fila procesada en la importación masiva de miembros.
 * Se usa tanto para las filas importadas como para las rechazadas; en estas
 * últimas {@code motivo} explica por qué no se pudo importar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiembroImportDetalleDto {

    /** Número de fila en el Excel (1 = encabezado), para que el usuario la ubique. */
    private Integer fila;
    private String ci;
    private String nombre;
    private String apellido;
    private String iglesia;
    /** Motivo del rechazo; null cuando la fila se importó correctamente. */
    private String motivo;
}
