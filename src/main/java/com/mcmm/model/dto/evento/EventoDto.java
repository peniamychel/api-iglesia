package com.mcmm.model.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "El tipo de evento es obligatorio")
    private Long tipoEventoId;

    @NotNull(message = "La iglesia es obligatoria")
    private Long iglesiaId;

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres")
    private String nombre;

    @Size(max = 254)
    private String motivo;

    @Size(max = 254)
    private String ubicacion;

    @NotBlank(message = "La localidad es obligatoria")
    @Size(max = 254)
    private String localidad;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 254)
    private String provincia;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 254)
    private String departamento;

    private Date fechaInicio;
    private Date fechaFin;

    private Boolean estado;
    private String alcance;
    private Boolean mostrarEnCalendario;
    private Boolean habilitarInscripciones;
    private String iglesiasInvitadas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Entrada: si es true, al crear/editar el evento se genera automáticamente su certificado. */
    private Boolean generaCertificado;
    /** Solo respuesta: estado de archivado del evento. */
    private Boolean archivado;
    /** Solo respuesta: indica si el evento ya tiene un certificado asociado. */
    private Boolean tieneCertificado;
}