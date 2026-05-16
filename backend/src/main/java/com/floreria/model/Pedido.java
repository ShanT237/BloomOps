package com.floreria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", nullable = false, unique = true)
    private String numeroPedido;  // Generado: "PED-2024-0001"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    private Cliente cliente;

    // Datos del destinatario
    @NotBlank(message = "El nombre del destinatario es obligatorio")
    private String nombreDestinatario;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El teléfono del destinatario es obligatorio")
    private String telefonoDestinatario;

    // Especificaciones del arreglo
    @NotBlank(message = "El tipo de arreglo es obligatorio")
    private String tipoArreglo;

    private String colores;

    private String mensajeTarjeta;

    // Fechas
    @NotNull(message = "La fecha especial es obligatoria")
    @Future(message = "La fecha de entrega debe ser futura")
    private LocalDate fechaEspecial;

    @NotBlank(message = "La franja horaria es obligatoria")
    private String franjaHoraria;  // Ej: "09:00-12:00"

    // Estado y trazabilidad
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.REGISTRADO;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Asignaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "florista_id")
    @ToString.Exclude
    private Empleado florista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domiciliario_id")
    @ToString.Exclude
    private Empleado domiciliario;

    // Entrega
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Entrega entrega;

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
