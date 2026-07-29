package com.mcmm.model.dto.participacionEvento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos que se asientan en el momento de entregar un certificado: el certificado
 * emitido y el libro/folio del registro físico donde queda constancia.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistroEntregaDto {

    @NotNull(message = "El certificado es obligatorio")
    private Long certificadoId;

    @NotBlank(message = "El número de libro es obligatorio")
    @Size(max = 50, message = "El número de libro no debe exceder 50 caracteres")
    private String numeroLibro;

    @NotBlank(message = "El número de folio es obligatorio")
    @Size(max = 50, message = "El número de folio no debe exceder 50 caracteres")
    private String numeroFolio;
}
