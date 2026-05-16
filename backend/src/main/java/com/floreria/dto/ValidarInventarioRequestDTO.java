package com.floreria.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarInventarioRequestDTO {

    @NotNull
    private Long pedidoId;

    // Mapa de insumoId -> cantidadRequerida
    @NotEmpty(message = "Debe indicar al menos un insumo")
    private Map<Long, Integer> insumosRequeridos;
}
