package com.mcmm.model.dto.MiembroDto;

import com.mcmm.model.dto.personaDto.PersonaDto;
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

//    private Long iglesiaId;

    private Date fechaConvercion;
    private String lugarConvercion;
    private String interventores;
    private String detalles;

    private PersonaDto personaDto;
    private Long personaId; // Para propósitos de creación y actualizació

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
