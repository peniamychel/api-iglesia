package com.mcmm.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MiembroIglesiaDto {

    private Long id;

    @NotNull(message = "El ID del miembro es obligatorio.")
    private Long miembroId;

    @NotNull(message = "El ID de la iglesia es obligatorio.")
    private Long iglesiaId;

    private Date fecha;
    private String motivoTraspaso;
    private Date fechaTraspaso;
    private String uriCartaTraspaso;
    private Long iglesiaDestinoId;
    private String estadoTraspaso;
    /** Solo respuesta: false si la iglesia de origen aun no vio el resultado. */
    private Boolean respuestaVista;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
