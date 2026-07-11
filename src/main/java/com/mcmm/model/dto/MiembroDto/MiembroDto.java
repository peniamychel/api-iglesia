package com.mcmm.model.dto.MiembroDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MiembroDto {

    private Long id;

    private Date fechaConvercion;

    @Size(max = 254, message = "El lugar de conversion no debe exceder 254 caracteres.")
    private String lugarConvercion;

    @Size(max = 500, message = "Los interventores no deben exceder 500 caracteres.")
    private String interventores;

    @Size(max = 1000, message = "Los detalles no deben exceder 1000 caracteres.")
    private String detalles;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres.")
    private String apellido;

    @Size(max = 20, message = "El CI no debe exceder 20 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "El CI solo puede contener letras, numeros y guiones.")
    private String ci;

    private Date fechaNac;

    @Size(max = 20, message = "El celular no debe exceder 20 caracteres.")
    @Pattern(regexp = "^[+0-9- ]*$", message = "El celular solo puede contener numeros, +, - y espacios.")
    private String celular;

    @Size(max = 20, message = "El sexo no debe exceder 20 caracteres.")
    private String sexo;

    @Size(max = 500, message = "La direccion no debe exceder 500 caracteres.")
    private String direccion;

    @Size(max = 254, message = "La uriFoto no debe exceder 254 caracteres.")
    private String uriFoto;

    private String iglesiaNombre;
    private String cargoNombre;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
