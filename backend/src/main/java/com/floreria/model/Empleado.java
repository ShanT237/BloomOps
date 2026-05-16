package com.floreria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolEmpleado rol;

    @Builder.Default
    private boolean disponible = true;
}
