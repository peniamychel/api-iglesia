package com.mcmm.model.dto.iglesiaDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.mcmm.model.dto.CargoDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IglesiaDto implements java.io.Serializable {

    private Long id;
    private List<CargoDto> cargos;

    @NotBlank(message = "El nombre de la iglesia no puede estar vacío.")
    @Size(min = 3, max = 255, message = "El nombre de la iglesia debe tener entre 3 y 255 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La direccion no debe exceder 500 caracteres.")
    private String direccion;

    private Long telefono;
    private Date fechaFundacion;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String uriFoto;
    private Double latitud;
    private Double longitud;
    private Integer orden;
}
