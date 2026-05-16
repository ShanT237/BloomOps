package com.floreria.repository;

import com.floreria.model.EstadoPedido;
import com.floreria.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByFechaEspecial(LocalDate fecha);

    // Pedidos que necesitan atención hoy
    @Query("SELECT p FROM Pedido p WHERE p.fechaEspecial = :hoy AND p.estado != 'ENTREGADO' AND p.estado != 'CANCELADO'")
    List<Pedido> findPedidosActivosHoy(LocalDate hoy);

    // Contar pedidos por estado
    long countByEstado(EstadoPedido estado);

    // Último número de pedido para generar el siguiente
    @Query("SELECT MAX(p.numeroPedido) FROM Pedido p WHERE p.numeroPedido LIKE CONCAT('PED-', :anio, '%')")
    Optional<String> findUltimoNumeroPedidoDelAnio(String anio);
}
