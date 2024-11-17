package com.mcmm.model.payload;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@ToString
@Builder
public class MessageResponseLogin<T> {
    private boolean success;
    private String message;
    private T datos;
}
