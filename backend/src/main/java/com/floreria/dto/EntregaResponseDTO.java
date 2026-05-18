package com.floreria.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregaResponseDTO {

    private Long id;
    private Long pedidoId;
    private String numeroPedido;
    private String nombreCliente;
    private String nombreDestinatario;
    private String direccionEntrega;
    private String tipoArreglo;
    private LocalDateTime fechaHoraEntrega;
    private String nombreReceptor;
    private String observaciones;
    private LocalDateTime etaEntrega;
    private boolean entregaExitosa;
    private String motivoNoEntrega;
    private String estado;  // "Pendiente", "En ruta", "Entregada", "No entregada"
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private boolean tieneSignatura;
}
