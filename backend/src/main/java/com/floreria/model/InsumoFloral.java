package com.floreria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "insumos_florales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoFloral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stockDisponible;

    @NotBlank
    private String unidadMedida;  // unidad, docena, metros, etc.

    @Min(value = 0)
    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 5;  // umbral para alerta de reabastecimiento
}
