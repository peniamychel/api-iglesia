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
    @Size(min = 3, message = "El nombre de la iglesia debe tener mas de 3 caracteres.")
    @Size(max = 255, message = "El nombre de la iglesia debe no debe exede los 10 caracteres.")
    private String nombre;

    private String direccion;

    private Long telefono;
    private Date fechaFundacion;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
