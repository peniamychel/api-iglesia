package com.mcmm.model.dto.MiembroDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MiembroDtoRes {

    private Long id;

    private Date fechaConvercion;
    private String lugarConvercion;
    private String interventores;
    private String detalles;

    private String nombre;
    private String apellido;
    private String ci;
    private Date fechaNac;
    private String celular;
    private String sexo;
    private String direccion;

    // ── Datos adicionales (opcionales) ──
    private String localidadNacimiento;
    private String provincia;
    private String departamento;
    private String nombrePadre;
    private String nombreMadre;

    private String uriFoto;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
