package com.mcmm.model.dto.personaDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PersonaDtoUpdate implements java.io.Serializable{

    private Long id;
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
