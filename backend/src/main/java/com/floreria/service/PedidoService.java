package com.floreria.service;

import com.floreria.dto.*;
import com.floreria.exception.RecursoNoEncontradoException;
import com.floreria.exception.ReglaDeNegocioException;
import com.floreria.model.*;
import com.floreria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final InsumoFlorRepository insumoFlorRepository;
    private final EntregaRepository entregaRepository;

    // ─────────────────────────────────────────
    // REQ001: REGISTRAR PEDIDO
    // Reglas: RN-001 (cliente registrado), RN-005 (fecha especial definida)
    // ─────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO registrarPedido(PedidoRequestDTO dto) {
        log.debug("Registrando pedido para cliente ID: {}", dto.getClienteId());

        // RN-001: el cliente debe estar registrado
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "RN-001: El cliente no está registrado en el sistema. " +
                        "Registre al cliente antes de crear el pedido."));

        // RN-005: la fecha especial debe estar definida (la validación @Future del DTO ya cubre esto)
        // Validación adicional: mínimo 48h de anticipación
        if (dto.getFechaEspecial().isBefore(LocalDate.now().plusDays(2))) {
            throw new ReglaDeNegocioException(
                    "RN-005: El pedido debe registrarse con mínimo 48 horas de anticipación. " +
                    "Fecha mínima permitida: " + LocalDate.now().plusDays(2));
        }

        Pedido pedido = Pedido.builder()
                .numeroPedido(generarNumeroPedido())
                .cliente(cliente)
                .nombreDestinatario(dto.getNombreDestinatario())
                .direccionEntrega(dto.getDireccionEntrega())
                .telefonoDestinatario(dto.getTelefonoDestinatario())
                .tipoArreglo(dto.getTipoArreglo())
                .colores(dto.getColores())
                .mensajeTarjeta(dto.getMensajeTarjeta())
                .fechaEspecial(dto.getFechaEspecial())
                .franjaHoraria(dto.getFranjaHoraria())
                .estado(EstadoPedido.REGISTRADO)
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido {} registrado exitosamente", guardado.getNumeroPedido());
        return toResponseDTO(guardado);
    }

    // ─────────────────────────────────────────
    // REQ003: VALIDAR INVENTARIO
    // Reglas: RN-002 (inventario antes de confirmar producción)
    // ─────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO validarInventario(ValidarInventarioRequestDTO dto) {
        Pedido pedido = obtenerPedidoPorId(dto.getPedidoId());

        if (pedido.getEstado() != EstadoPedido.REGISTRADO) {
            throw new ReglaDeNegocioException(
                    "El pedido debe estar en estado REGISTRADO para validar inventario. " +
                    "Estado actual: " + pedido.getEstado());
        }

        // RN-002: verificar cada insumo requerido
        StringBuilder alertas = new StringBuilder();
        boolean inventarioSuficiente = true;

        for (var entry : dto.getInsumosRequeridos().entrySet()) {
            Long insumoId = entry.getKey();
            int cantidadRequerida = entry.getValue();

            InsumoFloral insumo = insumoFlorRepository.findById(insumoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + insumoId));

            if (insumo.getStockDisponible() < cantidadRequerida) {
                inventarioSuficiente = false;
                alertas.append(String.format(
                        "⚠ ALERTA: %s — disponible: %d %s, requerido: %d %s. ",
                        insumo.getNombre(),
                        insumo.getStockDisponible(), insumo.getUnidadMedida(),
                        cantidadRequerida, insumo.getUnidadMedida()
                ));
            }
        }

        if (!inventarioSuficiente) {
            throw new ReglaDeNegocioException(
                    "RN-002: Inventario insuficiente. " + alertas +
                    "El pedido queda en espera hasta reabastecer.");
        }

        // Descontar del inventario y confirmar
        dto.getInsumosRequeridos().forEach((insumoId, cantidad) -> {
            InsumoFloral insumo = insumoFlorRepository.findById(insumoId).get();
            insumo.setStockDisponible(insumo.getStockDisponible() - cantidad);
            insumoFlorRepository.save(insumo);
        });

        pedido.setEstado(EstadoPedido.INVENTARIO_VALIDADO);
        log.info("Inventario validado para pedido {}", pedido.getNumeroPedido());
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    // ─────────────────────────────────────────
    // REQ004: PROGRAMAR PRODUCCIÓN (asignar florista)
    // Reglas: RN-007 (florista disponible)
    // ─────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO programarProduccion(AsignarProduccionRequestDTO dto) {
        Pedido pedido = obtenerPedidoPorId(dto.getPedidoId());

        if (pedido.getEstado() != EstadoPedido.INVENTARIO_VALIDADO) {
            throw new ReglaDeNegocioException(
                    "Debe validar el inventario antes de programar producción. " +
                    "Estado actual: " + pedido.getEstado());
        }

        // RN-007: florista debe estar disponible
        Empleado florista = empleadoRepository.findById(dto.getFloristaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Florista no encontrado"));

        if (florista.getRol() != RolEmpleado.FLORISTA) {
            throw new ReglaDeNegocioException("El empleado seleccionado no es florista.");
        }

        if (!florista.isDisponible()) {
            throw new ReglaDeNegocioException(
                    "RN-007: El florista " + florista.getNombre() +
                    " no está disponible. Seleccione otro florista.");
        }

        florista.setDisponible(false);
        empleadoRepository.save(florista);

        pedido.setFlorista(florista);
        pedido.setEstado(EstadoPedido.EN_PRODUCCION);

        log.info("Producción programada para pedido {}, florista: {}",
                pedido.getNumeroPedido(), florista.getNombre());
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    // ─────────────────────────────────────────
    // Aprobar calidad y asignar domiciliario
    // Reglas: RN-003 (revisión de calidad), RN-006 (dirección completa), RN-007 domiciliario
    // ─────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO aprobarCalidadYAsignarDomiciliario(Long pedidoId, Long domiciliarioId) {
        Pedido pedido = obtenerPedidoPorId(pedidoId);

        if (pedido.getEstado() != EstadoPedido.EN_PRODUCCION) {
            throw new ReglaDeNegocioException(
                    "El pedido debe estar EN_PRODUCCION para aprobar calidad. " +
                    "Estado actual: " + pedido.getEstado());
        }

        // RN-006: dirección completa ya validada en el registro

        // RN-003: marcar revisión de calidad
        Empleado domiciliario = empleadoRepository.findById(domiciliarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Domiciliario no encontrado"));

        if (domiciliario.getRol() != RolEmpleado.DOMICILIARIO) {
            throw new ReglaDeNegocioException("El empleado seleccionado no es domiciliario.");
        }

        if (!domiciliario.isDisponible()) {
            throw new ReglaDeNegocioException(
                    "El domiciliario " + domiciliario.getNombre() + " no está disponible.");
        }

        // Liberar al florista
        if (pedido.getFlorista() != null) {
            Empleado florista = pedido.getFlorista();
            florista.setDisponible(true);
            empleadoRepository.save(florista);
        }

        domiciliario.setDisponible(false);
        empleadoRepository.save(domiciliario);

        pedido.setDomiciliario(domiciliario);
        pedido.setEstado(EstadoPedido.DESPACHADO);

        log.info("Pedido {} aprobado y despachado con domiciliario {}",
                pedido.getNumeroPedido(), domiciliario.getNombre());
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    // ─────────────────────────────────────────
    // REQ006: CONFIRMAR ENTREGA
    // Reglas: RN-004 (franja horaria), RN-008 (firma del receptor)
    // ─────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO confirmarEntrega(EntregaRequestDTO dto) {
        Pedido pedido = obtenerPedidoPorId(dto.getPedidoId());

        if (pedido.getEstado() != EstadoPedido.DESPACHADO) {
            throw new ReglaDeNegocioException(
                    "El pedido debe estar DESPACHADO para confirmar entrega. " +
                    "Estado actual: " + pedido.getEstado());
        }

        // RN-008: la firma es obligatoria para cerrar el pedido
        if (dto.getFirmaReceptor() == null || dto.getFirmaReceptor().isBlank()) {
            throw new ReglaDeNegocioException(
                    "RN-008: La firma del receptor es obligatoria para cerrar el pedido. " +
                    "Una llamada verbal no es suficiente como evidencia de entrega.");
        }

        Entrega entrega = Entrega.builder()
                .pedido(pedido)
                .fechaHoraEntrega(LocalDateTime.now())
                .nombreReceptor(dto.getNombreReceptor())
                .firmaReceptor(dto.getFirmaReceptor())
                .observaciones(dto.getObservaciones())
                .entregaExitosa(true)
                .build();

        entregaRepository.save(entrega);

        // Liberar domiciliario
        if (pedido.getDomiciliario() != null) {
            Empleado domiciliario = pedido.getDomiciliario();
            domiciliario.setDisponible(true);
            empleadoRepository.save(domiciliario);
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        log.info("Pedido {} ENTREGADO exitosamente a {}", pedido.getNumeroPedido(), dto.getNombreReceptor());
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    // ─────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public PedidoResponseDTO buscarPorNumero(String numeroPedido) {
        return toResponseDTO(pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + numeroPedido)));
    }

    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream().map(this::toResponseDTO).toList();
    }

    public List<PedidoResponseDTO> pedidosActivosHoy() {
        return pedidoRepository.findPedidosActivosHoy(LocalDate.now())
                .stream().map(this::toResponseDTO).toList();
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con ID: " + id));
    }

    private String generarNumeroPedido() {
        String anio = String.valueOf(Year.now().getValue());
        Optional<String> ultimo = pedidoRepository.findUltimoNumeroPedidoDelAnio(anio);

        int siguiente = 1;
        if (ultimo.isPresent()) {
            String[] partes = ultimo.get().split("-");
            siguiente = Integer.parseInt(partes[partes.length - 1]) + 1;
        }
        return String.format("PED-%s-%04d", anio, siguiente);
    }

    private PedidoResponseDTO toResponseDTO(Pedido p) {
        return PedidoResponseDTO.builder()
                .id(p.getId())
                .numeroPedido(p.getNumeroPedido())
                .nombreCliente(p.getCliente() != null ? p.getCliente().getNombre() : null)
                .nombreDestinatario(p.getNombreDestinatario())
                .direccionEntrega(p.getDireccionEntrega())
                .telefonoDestinatario(p.getTelefonoDestinatario())
                .tipoArreglo(p.getTipoArreglo())
                .colores(p.getColores())
                .mensajeTarjeta(p.getMensajeTarjeta())
                .fechaEspecial(p.getFechaEspecial())
                .franjaHoraria(p.getFranjaHoraria())
                .estado(p.getEstado())
                .fechaRegistro(p.getFechaRegistro())
                .fechaActualizacion(p.getFechaActualizacion())
                .floristaNombre(p.getFlorista() != null ? p.getFlorista().getNombre() : null)
                .domiciliarioNombre(p.getDomiciliario() != null ? p.getDomiciliario().getNombre() : null)
                .build();
    }
}
