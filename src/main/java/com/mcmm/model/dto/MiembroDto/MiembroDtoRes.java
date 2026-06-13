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
    private String uriFoto;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
