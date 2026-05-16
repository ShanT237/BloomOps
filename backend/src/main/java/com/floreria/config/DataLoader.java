package com.floreria.config;

import com.floreria.model.*;
import com.floreria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    @Bean
    CommandLineRunner cargarDatosIniciales(
            ClienteRepository clienteRepo,
            EmpleadoRepository empleadoRepo,
            InsumoFlorRepository insumoRepo) {

        return args -> {
            // Solo carga si la BD está vacía
            if (empleadoRepo.count() > 0) return;

            log.info("Cargando datos iniciales de prueba...");

            // Empleados
            empleadoRepo.save(Empleado.builder().nombre("Laura Gómez").telefono("3101234567").rol(RolEmpleado.RECEPCIONISTA).disponible(true).build());
            empleadoRepo.save(Empleado.builder().nombre("Carlos Flórez").telefono("3209876543").rol(RolEmpleado.FLORISTA).disponible(true).build());
            empleadoRepo.save(Empleado.builder().nombre("Ana Martínez").telefono("3155555555").rol(RolEmpleado.FLORISTA).disponible(true).build());
            empleadoRepo.save(Empleado.builder().nombre("Pedro Ríos").telefono("3001112233").rol(RolEmpleado.DOMICILIARIO).disponible(true).build());
            empleadoRepo.save(Empleado.builder().nombre("Daiana Martínez").telefono("3124445566").rol(RolEmpleado.COORDINADOR).disponible(true).build());
            empleadoRepo.save(Empleado.builder().nombre("Luis Vargas").telefono("3177778899").rol(RolEmpleado.AUXILIAR_PRODUCCION).disponible(true).build());

            // Insumos florales
            insumoRepo.save(InsumoFloral.builder().nombre("Rosas Rojas").stockDisponible(100).unidadMedida("unidad").stockMinimo(20).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Rosas Blancas").stockDisponible(80).unidadMedida("unidad").stockMinimo(20).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Girasoles").stockDisponible(60).unidadMedida("unidad").stockMinimo(10).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Tulipanes").stockDisponible(45).unidadMedida("unidad").stockMinimo(10).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Cinta Dorada").stockDisponible(30).unidadMedida("metros").stockMinimo(5).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Tarjetas de Mensaje").stockDisponible(50).unidadMedida("unidad").stockMinimo(10).build());
            insumoRepo.save(InsumoFloral.builder().nombre("Papel Celofán").stockDisponible(25).unidadMedida("metros").stockMinimo(5).build());

            // Cliente de prueba
            clienteRepo.save(Cliente.builder()
                    .nombre("Santiago Rodríguez")
                    .cedula("1234567890")
                    .telefono("3166667777")
                    .email("santiago@email.com")
                    .build());

            log.info("✅ Datos iniciales cargados exitosamente");
        };
    }
}
