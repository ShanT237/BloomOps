package com.floreria.service;

import com.floreria.model.InsumoFloral;
import com.floreria.repository.InsumoFlorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InsumoFlorRepository insumoFlorRepository;

    public List<InsumoFloral> listarTodos() {
        return insumoFlorRepository.findAll();
    }

    public List<InsumoFloral> obtenerAlertas() {
        return insumoFlorRepository.findInsumosConAlerta();
    }

    @Transactional
    public InsumoFloral crearInsumo(InsumoFloral insumo) {
        return insumoFlorRepository.save(insumo);
    }

    @Transactional
    public InsumoFloral actualizarStock(Long id, int nuevoStock) {
        InsumoFloral insumo = insumoFlorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        insumo.setStockDisponible(nuevoStock);
        return insumoFlorRepository.save(insumo);
    }
}
