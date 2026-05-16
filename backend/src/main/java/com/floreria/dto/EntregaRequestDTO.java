package com.floreria.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregaRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotBlank(message = "El nombre del receptor es obligatorio")
    private String nombreReceptor;

    @NotBlank(message = "La firma del receptor es obligatoria")
    private String firmaReceptor;  // Base64 de la firma digital

    private String observaciones;
}
