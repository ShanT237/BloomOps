package com.floreria.service;

import com.floreria.dto.EntregaRequestDTO;
import com.floreria.dto.EntregaResponseDTO;
import com.floreria.exception.RecursoNoEncontradoException;
import com.floreria.exception.ReglaDeNegocioException;
import com.floreria.model.Entrega;
import com.floreria.model.Pedido;
import com.floreria.repository.EntregaRepository;
import com.floreria.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * CREATE: Crear nueva entrega
     */
    @Transactional
    public EntregaResponseDTO crearEntrega(EntregaRequestDTO dto) {
        log.info("Creando nueva entrega para pedido: {}", dto.getPedidoId());

        // Validar que el pedido existe
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pedido no encontrado con ID: " + dto.getPedidoId()));

        // Validar que no existe entrega previa para este pedido
        if (entregaRepository.findByPedidoId(dto.getPedidoId()).isPresent()) {
            throw new ReglaDeNegocioException(
                    "Ya existe una entrega registrada para este pedido");
        }

        Entrega entrega = Entrega.builder()
                .pedido(pedido)
                .nombreReceptor(dto.getNombreReceptor())
                .firmaReceptor(dto.getFirmaReceptor())
                .observaciones(dto.getObservaciones())
                .entregaExitosa(false)
                .build();

        Entrega saved = entregaRepository.save(entrega);
        log.info("Entrega creada con ID: {}", saved.getId());

        return mapToResponseDTO(saved, pedido);
    }

    /**
     * READ: Obtener entrega por ID
     */
    @Transactional(readOnly = true)
    public EntregaResponseDTO obtenerPorId(Long id) {
        log.info("Obteniendo entrega con ID: {}", id);

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Entrega no encontrada con ID: " + id));

        return mapToResponseDTO(entrega, entrega.getPedido());
    }

    /**
     * READ: Obtener entrega por ID de pedido
     */
    @Transactional(readOnly = true)
    public EntregaResponseDTO obtenerPorPedidoId(Long pedidoId) {
        log.info("Obteniendo entrega para pedido: {}", pedidoId);

        Entrega entrega = entregaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe entrega para el pedido ID: " + pedidoId));

        return mapToResponseDTO(entrega, entrega.getPedido());
    }

    /**
     * READ: Listar todas las entregas
     */
    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarTodas() {
        log.info("Listando todas las entregas");

        return entregaRepository.findAll()
                .stream()
                .map(entrega -> mapToResponseDTO(entrega, entrega.getPedido()))
                .collect(Collectors.toList());
    }

    /**
     * READ: Listar entregas pendientes
     */
    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarPendientes() {
        log.info("Listando entregas pendientes");

        return entregaRepository.findAll()
                .stream()
                .filter(e -> !e.isEntregaExitosa() && e.getFechaHoraEntrega() == null)
                .map(entrega -> mapToResponseDTO(entrega, entrega.getPedido()))
                .collect(Collectors.toList());
    }

    /**
     * READ: Listar entregas entregadas
     */
    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarEntregadas() {
        log.info("Listando entregas exitosas");

        return entregaRepository.findAll()
                .stream()
                .filter(Entrega::isEntregaExitosa)
                .map(entrega -> mapToResponseDTO(entrega, entrega.getPedido()))
                .collect(Collectors.toList());
    }

    /**
     * READ: Listar entregas no entregadas
     */
    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarNoEntregadas() {
        log.info("Listando entregas fallidas");

        return entregaRepository.findAll()
                .stream()
                .filter(e -> !e.isEntregaExitosa() && e.getFechaHoraEntrega() != null)
                .map(entrega -> mapToResponseDTO(entrega, entrega.getPedido()))
                .collect(Collectors.toList());
    }

    /**
     * UPDATE: Confirmar entrega exitosa
     */
    @Transactional
    public EntregaResponseDTO confirmarEntrega(Long id, EntregaRequestDTO dto) {
        log.info("Confirmando entrega con ID: {}", id);

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Entrega no encontrada con ID: " + id));

        entrega.setFechaHoraEntrega(LocalDateTime.now());
        entrega.setNombreReceptor(dto.getNombreReceptor());
        entrega.setFirmaReceptor(dto.getFirmaReceptor());
        entrega.setObservaciones(dto.getObservaciones());
        entrega.setEntregaExitosa(true);
        entrega.setMotivoNoEntrega(null);

        Entrega updated = entregaRepository.save(entrega);
        log.info("Entrega confirmada con éxito: {}", id);

        return mapToResponseDTO(updated, updated.getPedido());
    }

    /**
     * UPDATE: Registrar entrega fallida
     */
    @Transactional
    public EntregaResponseDTO registrarEntregaFallida(Long id, String motivo) {
        log.warn("Registrando entrega fallida con ID: {} - Motivo: {}", id, motivo);

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Entrega no encontrada con ID: " + id));

        entrega.setFechaHoraEntrega(LocalDateTime.now());
        entrega.setEntregaExitosa(false);
        entrega.setMotivoNoEntrega(motivo);

        Entrega updated = entregaRepository.save(entrega);
        return mapToResponseDTO(updated, updated.getPedido());
    }

    /**
     * UPDATE: Editar detalles de entrega
     */
    @Transactional
    public EntregaResponseDTO editarEntrega(Long id, EntregaRequestDTO dto) {
        log.info("Editando entrega con ID: {}", id);

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Entrega no encontrada con ID: " + id));

        // Validar que no haya sido entregada
        if (entrega.isEntregaExitosa()) {
            throw new ReglaDeNegocioException(
                    "No se puede editar una entrega ya confirmada");
        }

        entrega.setNombreReceptor(dto.getNombreReceptor());
        entrega.setFirmaReceptor(dto.getFirmaReceptor());
        entrega.setObservaciones(dto.getObservaciones());

        Entrega updated = entregaRepository.save(entrega);
        log.info("Entrega actualizada: {}", id);

        return mapToResponseDTO(updated, updated.getPedido());
    }

    /**
     * DELETE: Cancelar/eliminar entrega
     */
    @Transactional
    public void cancelarEntrega(Long id) {
        log.warn("Cancelando entrega con ID: {}", id);

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Entrega no encontrada con ID: " + id));

        if (entrega.isEntregaExitosa()) {
            throw new ReglaDeNegocioException(
                    "No se puede cancelar una entrega ya confirmada");
        }

        entregaRepository.deleteById(id);
        log.info("Entrega cancelada: {}", id);
    }

    /**
     * Mapear Entrega a EntregaResponseDTO
     */
    private EntregaResponseDTO mapToResponseDTO(Entrega entrega, Pedido pedido) {
        String estado = determinarEstado(entrega);

        return EntregaResponseDTO.builder()
                .id(entrega.getId())
                .pedidoId(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .nombreCliente(pedido.getCliente().getNombre())
                .nombreDestinatario(pedido.getNombreDestinatario())
                .direccionEntrega(pedido.getDireccionEntrega())
                .tipoArreglo(pedido.getTipoArreglo())
            .fechaHoraEntrega(entrega.getFechaHoraEntrega())
            .etaEntrega(entrega.getEtaEntrega())
                .nombreReceptor(entrega.getNombreReceptor())
                .observaciones(entrega.getObservaciones())
                .entregaExitosa(entrega.isEntregaExitosa())
                .motivoNoEntrega(entrega.getMotivoNoEntrega())
                .estado(estado)
            .tieneSignatura(entrega.getFirmaReceptor() != null && !entrega.getFirmaReceptor().isEmpty())
            .fechaRegistro(pedido.getFechaRegistro())
            .fechaActualizacion(pedido.getFechaActualizacion())
                .build();
    }

    /**
     * Determinar estado de la entrega basado en su información
     */
    private String determinarEstado(Entrega entrega) {
        if (entrega.isEntregaExitosa()) {
            return "Entregada";
        } else if (entrega.getFechaHoraEntrega() != null && !entrega.isEntregaExitosa()) {
            return "No entregada";
        } else if (entrega.getFechaHoraEntrega() != null) {
            return "En ruta";
        } else {
            return "Pendiente";
        }
    }
}
