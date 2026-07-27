package com.mcmm.model.dto.MiembroDto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de una importación masiva de miembros vía Excel.
 * {@code imported} = filas registradas correctamente.
 * {@code omitidos} = filas rechazadas (CI duplicado/faltante, datos incompletos,
 * o iglesia inválida/faltante en la carga del administrador).
 * Las listas {@code importados} y {@code errores} llevan el detalle por fila para
 * mostrar el informe de la importación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiembroImportResultDto {

    private int imported;
    private int omitidos;

    @Builder.Default
    private List<MiembroImportDetalleDto> importados = new ArrayList<>();
    @Builder.Default
    private List<MiembroImportDetalleDto> errores = new ArrayList<>();
}
