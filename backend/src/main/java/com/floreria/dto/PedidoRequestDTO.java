package com.floreria.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    private String nombreDestinatario;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El teléfono del destinatario es obligatorio")
    private String telefonoDestinatario;

    @NotBlank(message = "El tipo de arreglo es obligatorio")
    private String tipoArreglo;

    private String colores;

    private String mensajeTarjeta;

    @NotNull(message = "La fecha especial es obligatoria")
    @Future(message = "La fecha de entrega debe ser futura")
    private LocalDate fechaEspecial;

    @NotBlank(message = "La franja horaria es obligatoria")
    private String franjaHoraria;
}
