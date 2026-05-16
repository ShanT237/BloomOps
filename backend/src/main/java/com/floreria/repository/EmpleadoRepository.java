package com.floreria.repository;

import com.floreria.model.Empleado;
import com.floreria.model.RolEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByRolAndDisponibleTrue(RolEmpleado rol);
    List<Empleado> findByRol(RolEmpleado rol);
}
