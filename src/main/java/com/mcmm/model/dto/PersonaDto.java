package com.mcmm.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PersonaDto implements java.io.Serializable{

    private Long id;
    private String nombre;
    private String apellido;
    private Integer ci;
    private Date fechaNac;
    private Integer celular;
    private String sexo;
    private String direccion;
    private String uriFoto;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
