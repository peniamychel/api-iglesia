package com.mcmm.model.payload;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@ToString
@Builder
public class MessageResponse<T> {
    private String message;
    private T datos;
    private String nombreModelo;

//    public MessageResponse(String message, T datos){
//        this.message = message;
//        this.datos = datos;
//        this.nombreTabla = datos.getClass().getSimpleName();
//    }
//
//    public MessageResponse(String message){
//        this.message = message;
//        this.datos = null;
//        this.nombreModelo = "";
//    }

}
