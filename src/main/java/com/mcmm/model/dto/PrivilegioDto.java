package com.mcmm.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PrivilegioDto implements java.io.Serializable {

    private Long id;
    private String nombre;
    private String acto;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}