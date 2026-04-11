package com.mcmm.model.payload;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ApiResponse<T> {
    @Builder.Default
    private boolean success = true;// Siempre estará en true, incluso al usar @Builder
    private String message;
    private T datos;
    private String nombreModelo;

}
