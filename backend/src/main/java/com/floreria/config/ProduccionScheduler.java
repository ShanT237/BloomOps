package com.floreria.config;

import com.floreria.model.*;
import com.floreria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProduccionScheduler {

    private final PedidoRepository pedidoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EntregaRepository entregaRepository;

    // Ejecuta cada minuto (configurable si se desea)
    @Scheduled(fixedDelayString = "${scheduler.produccion.delay:60000}")
    @Transactional
    public void procesarProduccionesFinalizadas() {
        LocalDateTime now = LocalDateTime.now();
        List<Pedido> enProduccion = pedidoRepository.findByEstado(EstadoPedido.EN_PRODUCCION);

        for (Pedido p : enProduccion) {
            if (p.getFechaFinProduccionEstimada() == null) continue;
            if (p.getFechaFinProduccionEstimada().isAfter(now)) continue;

            log.info("Producción finalizada automáticamente para pedido {}", p.getNumeroPedido());

            // Intentar asignar domiciliario disponible
            List<Empleado> domiciliarios = empleadoRepository.findByRolAndDisponibleTrue(RolEmpleado.DOMICILIARIO);
            Empleado domiciliario = null;
            if (!domiciliarios.isEmpty()) {
                int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(domiciliarios.size());
                domiciliario = domiciliarios.get(idx);
                domiciliario.setDisponible(false);
                empleadoRepository.save(domiciliario);
                p.setDomiciliario(domiciliario);
            }

            // Crear entrega pendiente con ETA aleatoria (entre 20 y 120 min)
            int minutosEta = java.util.concurrent.ThreadLocalRandom.current().nextInt(20, 121);
            LocalDateTime eta = now.plusMinutes(minutosEta);

            com.floreria.model.Entrega entrega = com.floreria.model.Entrega.builder()
                    .pedido(p)
                    .entregaExitosa(false)
                    .etaEntrega(eta)
                    .build();

            entregaRepository.save(entrega);

            p.setEntrega(entrega);
            p.setEstado(EstadoPedido.DESPACHADO);
            pedidoRepository.save(p);

            log.info("Pedido {} movido a DESPACHADO y entrega creada (ETA {}). Domiciliario asignado: {}",
                    p.getNumeroPedido(), eta, domiciliarNombre(domiciliario));
        }
    }

    private String domiciliarNombre(Empleado d) {
        return d == null ? "(ninguno)" : d.getNombre();
    }
}
