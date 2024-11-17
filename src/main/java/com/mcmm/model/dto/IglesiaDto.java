package com.mcmm.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IglesiaDto implements java.io.Serializable{

    private Long id;

//    private Long miembroId;

    private String nombre;
    private String direccion;
    private Integer telefono;
    private Date fechaFundacion;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
