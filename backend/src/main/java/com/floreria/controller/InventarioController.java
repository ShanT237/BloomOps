package com.floreria.controller;

import com.floreria.model.InsumoFloral;
import com.floreria.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<InsumoFloral>> listar() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<InsumoFloral>> alertas() {
        return ResponseEntity.ok(inventarioService.obtenerAlertas());
    }

    @PostMapping
    public ResponseEntity<InsumoFloral> crear(@RequestBody InsumoFloral insumo) {
        return ResponseEntity.ok(inventarioService.crearInsumo(insumo));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<InsumoFloral> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(inventarioService.actualizarStock(id, body.get("stock")));
    }
}
