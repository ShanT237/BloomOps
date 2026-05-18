package com.floreria.controller;

import com.floreria.dto.EntregaRequestDTO;
import com.floreria.dto.EntregaResponseDTO;
import com.floreria.service.EntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class EntregaController {

    private final EntregaService entregaService;

    // ==================== CREATE ====================
    /**
     * POST: Crear nueva entrega
     * Endpoint: POST /api/entregas
     */
    @PostMapping
    public ResponseEntity<EntregaResponseDTO> crear(
            @Valid @RequestBody EntregaRequestDTO dto) {
        EntregaResponseDTO respuesta = entregaService.crearEntrega(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // ==================== READ ====================
    /**
     * GET: Obtener entrega por ID
     * Endpoint: GET /api/entregas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntregaResponseDTO> obtenerPorId(@PathVariable Long id) {
        EntregaResponseDTO respuesta = entregaService.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET: Obtener entrega por ID de pedido
     * Endpoint: GET /api/entregas/pedido/{pedidoId}
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntregaResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        EntregaResponseDTO respuesta = entregaService.obtenerPorPedidoId(pedidoId);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET: Listar todas las entregas
     * Endpoint: GET /api/entregas
     */
    @GetMapping
    public ResponseEntity<List<EntregaResponseDTO>> listarTodas() {
        List<EntregaResponseDTO> respuesta = entregaService.listarTodas();
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET: Listar entregas pendientes
     * Endpoint: GET /api/entregas/estado/pendientes
     */
    @GetMapping("/estado/pendientes")
    public ResponseEntity<List<EntregaResponseDTO>> listarPendientes() {
        List<EntregaResponseDTO> respuesta = entregaService.listarPendientes();
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET: Listar entregas completadas
     * Endpoint: GET /api/entregas/estado/entregadas
     */
    @GetMapping("/estado/entregadas")
    public ResponseEntity<List<EntregaResponseDTO>> listarEntregadas() {
        List<EntregaResponseDTO> respuesta = entregaService.listarEntregadas();
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET: Listar entregas fallidas
     * Endpoint: GET /api/entregas/estado/no-entregadas
     */
    @GetMapping("/estado/no-entregadas")
    public ResponseEntity<List<EntregaResponseDTO>> listarNoEntregadas() {
        List<EntregaResponseDTO> respuesta = entregaService.listarNoEntregadas();
        return ResponseEntity.ok(respuesta);
    }

    // ==================== UPDATE ====================
    /**
     * PUT: Editar entrega
     * Endpoint: PUT /api/entregas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EntregaResponseDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody EntregaRequestDTO dto) {
        EntregaResponseDTO respuesta = entregaService.editarEntrega(id, dto);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * POST: Confirmar entrega exitosa
     * Endpoint: POST /api/entregas/{id}/confirmar
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<EntregaResponseDTO> confirmarEntrega(
            @PathVariable Long id,
            @Valid @RequestBody EntregaRequestDTO dto) {
        EntregaResponseDTO respuesta = entregaService.confirmarEntrega(id, dto);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * POST: Registrar entrega fallida
     * Endpoint: POST /api/entregas/{id}/rechazar
     */
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<EntregaResponseDTO> rechazarEntrega(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "No especificado");
        EntregaResponseDTO respuesta = entregaService.registrarEntregaFallida(id, motivo);
        return ResponseEntity.ok(respuesta);
    }

    // ==================== DELETE ====================
    /**
     * DELETE: Cancelar/Eliminar entrega
     * Endpoint: DELETE /api/entregas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelar(@PathVariable Long id) {
        entregaService.cancelarEntrega(id);
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Entrega cancelada exitosamente");
        return ResponseEntity.ok(respuesta);
    }
}
