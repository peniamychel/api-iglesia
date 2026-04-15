package com.mcmm.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GraficoDataDto implements java.io.Serializable {

    private Long id;
    private String nombre;
    private Long valor;

}
