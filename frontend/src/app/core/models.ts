export type EstadoPedido =
  | 'REGISTRADO'
  | 'INVENTARIO_VALIDADO'
  | 'EN_PRODUCCION'
  | 'REVISION_CALIDAD'
  | 'DESPACHADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export type RolEmpleado =
  | 'RECEPCIONISTA'
  | 'FLORISTA'
  | 'DOMICILIARIO'
  | 'COORDINADOR'
  | 'AUXILIAR_PRODUCCION';

export interface Cliente {
  id: number;
  nombre: string;
  cedula: string;
  telefono: string;
  email?: string;
}

export interface Empleado {
  id: number;
  nombre: string;
  telefono: string;
  rol: RolEmpleado;
  disponible: boolean;
}

export interface InsumoFloral {
  id: number;
  nombre: string;
  stockDisponible: number;
  unidadMedida: string;
  stockMinimo: number;
}

export interface PedidoResponse {
  id: number;
  numeroPedido: string;
  nombreCliente: string;
  nombreDestinatario: string;
  direccionEntrega: string;
  telefonoDestinatario: string;
  tipoArreglo: string;
  colores: string;
  mensajeTarjeta: string;
  fechaEspecial: string;
  franjaHoraria: string;
  estado: EstadoPedido;
  fechaRegistro: string;
  fechaActualizacion: string;
  duracionProduccionMinutos?: number;
  fechaFinProduccionEstimada?: string;
  floristaNombre?: string;
  domiciliarioNombre?: string;
}

export interface PedidoRequest {
  clienteId: number;
  nombreDestinatario: string;
  direccionEntrega: string;
  telefonoDestinatario: string;
  tipoArreglo: string;
  colores: string;
  mensajeTarjeta: string;
  fechaEspecial: string;
  franjaHoraria: string;
}

export interface ValidarInventarioRequest {
  pedidoId: number;
  insumosRequeridos: { [insumoId: number]: number };
}

export interface AsignarProduccionRequest {
  pedidoId: number;
  floristaId: number;
}

export interface EntregaRequest {
  pedidoId: number;
  nombreReceptor: string;
  firmaReceptor: string;
  observaciones?: string;
}