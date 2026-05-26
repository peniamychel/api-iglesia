package com.mcmm.model.dto;

import com.mcmm.model.entity.ERole;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RolDto {

    private Long id;
    private ERole name;
    private Set<PrivilegioDto> privilegios;
}
