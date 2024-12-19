package com.mcmm.model.dto;

import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MiembroIglesiaDto {

    private Long id;
//    private MiembroDto miembroDto;
    private Long miembroId;
//    private IglesiaDto iglesiaDto;
    private Long iglesiaId;

    private Date fecha;
    private String motivoTraspaso;
    private Date fechaTraspaso;
    private String uriCartaTraspaso;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
