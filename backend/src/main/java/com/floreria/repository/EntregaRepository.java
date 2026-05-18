package com.floreria.repository;

import com.floreria.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    Optional<Entrega> findByPedidoId(Long pedidoId);

    @Query("SELECT e FROM Entrega e WHERE e.entregaExitosa = true ORDER BY e.fechaHoraEntrega DESC")
    List<Entrega> findAllEntregadas();

    @Query("SELECT e FROM Entrega e WHERE e.entregaExitosa = false AND e.fechaHoraEntrega IS NULL")
    List<Entrega> findAllPendientes();

    @Query("SELECT e FROM Entrega e WHERE e.entregaExitosa = false AND e.fechaHoraEntrega IS NOT NULL")
    List<Entrega> findAllFallidas();

    @Query("SELECT e FROM Entrega e WHERE e.fechaHoraEntrega BETWEEN ?1 AND ?2 ORDER BY e.fechaHoraEntrega DESC")
    List<Entrega> findByFechaHoraEntregaBetween(LocalDateTime inicio, LocalDateTime fin);
}
