package com.floreria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

public enum RolEmpleado {
    RECEPCIONISTA, FLORISTA, DOMICILIARIO, COORDINADOR, AUXILIAR_PRODUCCION
}
