package com.floreria.controller;

import com.floreria.model.Cliente;
import com.floreria.model.Empleado;
import com.floreria.model.RolEmpleado;
import com.floreria.repository.ClienteRepository;
import com.floreria.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        if (clienteRepository.existsByCedula(cliente.getCedula())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteRepository.save(cliente));
    }
}


@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;

    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoRepository.findAll());
    }

    @GetMapping("/disponibles/{rol}")
    public ResponseEntity<List<Empleado>> disponiblesPorRol(@PathVariable RolEmpleado rol) {
        return ResponseEntity.ok(empleadoRepository.findByRolAndDisponibleTrue(rol));
    }

    @PostMapping
    public ResponseEntity<Empleado> crear(@Valid @RequestBody Empleado empleado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoRepository.save(empleado));
    }
}
