package com.floreria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La cédula es obligatoria")
    @Column(nullable = false, unique = true)
    private String cedula;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Email(message = "Email inválido")
    private String email;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Pedido> pedidos;
}
