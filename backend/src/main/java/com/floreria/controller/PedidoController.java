package com.floreria.controller;

import com.floreria.dto.*;
import com.floreria.model.EstadoPedido;
import com.floreria.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class PedidoController {

    private final PedidoService pedidoService;

    // REQ001: Registrar pedido
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> registrar(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.registrarPedido(dto));
    }

    // REQ002: Consultar pedido
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{numeroPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPorNumero(@PathVariable String numeroPedido) {
        return ResponseEntity.ok(pedidoService.buscarPorNumero(numeroPedido));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<PedidoResponseDTO>> pedidosHoy() {
        return ResponseEntity.ok(pedidoService.pedidosActivosHoy());
    }

    // REQ003: Validar inventario
    @PostMapping("/validar-inventario")
    public ResponseEntity<PedidoResponseDTO> validarInventario(
            @Valid @RequestBody ValidarInventarioRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.validarInventario(dto));
    }

    // REQ004: Programar producción (asignar florista)
    @PostMapping("/programar-produccion")
    public ResponseEntity<PedidoResponseDTO> programarProduccion(
            @Valid @RequestBody AsignarProduccionRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.programarProduccion(dto));
    }

    // Aprobar calidad + asignar domiciliario
    @PostMapping("/{pedidoId}/aprobar-despacho")
    public ResponseEntity<PedidoResponseDTO> aprobarDespacho(
            @PathVariable Long pedidoId,
            @RequestBody Map<String, Long> body) {
        Long domiciliarioId = body.get("domiciliarioId");
        return ResponseEntity.ok(pedidoService.aprobarCalidadYAsignarDomiciliario(pedidoId, domiciliarioId));
    }

    // REQ006: Confirmar entrega
    @PostMapping("/confirmar-entrega")
    public ResponseEntity<PedidoResponseDTO> confirmarEntrega(
            @Valid @RequestBody EntregaRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.confirmarEntrega(dto));
    }
}
