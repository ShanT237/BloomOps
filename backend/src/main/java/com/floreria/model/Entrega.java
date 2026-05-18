package com.floreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    @ToString.Exclude
    private Pedido pedido;

    private LocalDateTime fechaHoraEntrega;

    // Estimación de llegada (ETA) para la entrega cuando fue despachada
    private LocalDateTime etaEntrega;

    private String nombreReceptor;

    // Base64 de la firma digital (o URL si usas storage)
    @Column(columnDefinition = "TEXT")
    private String firmaReceptor;

    private String observaciones;

    @Builder.Default
    private boolean entregaExitosa = false;

    // Si no se pudo entregar
    private String motivoNoEntrega;
}
