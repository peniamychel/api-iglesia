package com.mcmm.model.dto.MiembroDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MiembroDtoNew {

    private Date fechaConvercion;
    private String lugarConvercion;
    private String interventores;
    private String detalles;

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio.")
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
