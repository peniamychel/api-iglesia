package com.mcmm.model.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventoDto {
    private Long id;

    private Long tipoEventoId; // FK to TipoEvento

    private Long iglesiaId; // FK to Iglesia

    @NotBlank
    @Size(max = 254)
    private String nombre;

    @Size(max = 254)
    private String motivo;

    @Size(max = 254)
    private String uriFoto;

    @Size(max = 254)
    private String ubicacion;

    private Date fechaInicio;
    private Date fechaFin;

    private Boolean estado;
    private String alcance;
    private Boolean mostrarEnCalendario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}