package com.floreria.repository;

import com.floreria.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    Optional<Entrega> findByPedidoId(Long pedidoId);
}
