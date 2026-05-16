package com.floreria.repository;

import com.floreria.model.InsumoFloral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InsumoFlorRepository extends JpaRepository<InsumoFloral, Long> {

    // Insumos bajo el stock mínimo (alertas)
    @Query("SELECT i FROM InsumoFloral i WHERE i.stockDisponible <= i.stockMinimo")
    List<InsumoFloral> findInsumosConAlerta();
}
