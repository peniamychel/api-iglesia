package com.mcmm.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class BitacoraDto implements java.io.Serializable {

    private Long id;
    private Long usuarioId;
    private String username;
    private String userFullName;
    private String accion;
    private String modulo;
    private String descripcion;
    private LocalDateTime fecha;
    private String ipAddress;
}
