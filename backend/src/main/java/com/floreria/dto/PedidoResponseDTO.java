package com.floreria.dto;

import com.floreria.model.EstadoPedido;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {

    private Long id;
    private String numeroPedido;
    private String nombreCliente;
    private String nombreDestinatario;
    private String direccionEntrega;
    private String telefonoDestinatario;
    private String tipoArreglo;
    private String colores;
    private String mensajeTarjeta;
    private LocalDate fechaEspecial;
    private String franjaHoraria;
    private EstadoPedido estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private Integer duracionProduccionMinutos;
    private LocalDateTime fechaFinProduccionEstimada;
    private String floristaNombre;
    private String domiciliarioNombre;
}
