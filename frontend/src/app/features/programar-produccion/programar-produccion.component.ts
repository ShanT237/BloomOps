import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { PedidoResponse, Empleado } from '../../core/models';

@Component({
  selector: 'app-programar-produccion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './programar-produccion.component.html',
  styleUrl: './programar-produccion.component.scss'
})
export class ProgramarProduccionComponent implements OnInit {
  pedidos: PedidoResponse[] = [];
  pedidosEnProduccion: PedidoResponse[] = [];
  floristas: Empleado[] = [];
  domiciliarios: Empleado[] = [];

  pedidoSeleccionado: PedidoResponse | null = null;
  pedidoSeleccionadoId: number | null = null;
  floristaId = '';
  domiciliarioId = '';

  loading = false;
  cargandoPedidos = true;
  cargandoFloristas = true;
  cargandoDomiciliarios = true;
  errorMsg = '';
  exitoso = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.cargarPedidos();
    this.cargarPedidosEnProduccion();
    this.api.getEmpleadosDisponibles('FLORISTA').subscribe({
      next: e => {
        this.floristas = e;
        this.cargandoFloristas = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoFloristas = false;
        this.cdr.detectChanges();
      }
    });
    this.api.getEmpleadosDisponibles('DOMICILIARIO').subscribe({
      next: e => {
        this.domiciliarios = e;
        this.cargandoDomiciliarios = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoDomiciliarios = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarPedidos() {
    this.cargandoPedidos = true;
    this.api.getPedidosPorEstado('INVENTARIO_VALIDADO').subscribe({
      next: p => {
        this.pedidos = p;
        this.cargandoPedidos = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoPedidos = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarPedidosEnProduccion() {
    this.api.getPedidosPorEstado('EN_PRODUCCION').subscribe({
      next: p => {
        this.pedidosEnProduccion = p;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }

  seleccionarPedido(p: PedidoResponse) {
    this.pedidoSeleccionado = p;
    this.pedidoSeleccionadoId = p.id;
    this.floristaId = '';
    this.domiciliarioId = '';
    this.errorMsg = '';
    this.exitoso = false;
  }

  confirmarAsignacion() {
    if (!this.pedidoSeleccionado || !this.floristaId) return;
    this.loading = true;
    this.errorMsg = '';

    this.api.programarProduccion({
      pedidoId: this.pedidoSeleccionado.id,
      floristaId: +this.floristaId
    }).subscribe({
      next: pedido => {
        this.loading = false;
        this.exitoso = true;
        this.pedidos = this.pedidos.filter(p => p.id !== this.pedidoSeleccionadoId);
        this.pedidosEnProduccion = [...this.pedidosEnProduccion, pedido];
        this.pedidoSeleccionado = null;
        this.pedidoSeleccionadoId = null;
        this.floristaId = '';
        this.cargarPedidos();
        this.cargarPedidosEnProduccion();
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message;
      }
    });
  }

  aprobarCalidad(p: PedidoResponse) {
    if (!this.domiciliarioId) { this.errorMsg = 'Seleccione un domiciliario'; return; }
    this.loading = true;
    this.api.aprobarDespacho(p.id, +this.domiciliarioId).subscribe({
      next: () => {
        this.loading = false;
        // quitar de la lista de en producción
        this.pedidosEnProduccion = this.pedidosEnProduccion.filter(x => x.id !== p.id);
        this.exitoso = true;
        this.cdr.detectChanges();
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message;
      }
    });
  }

  // Estimación sencilla de tiempo de producción (puede ajustarse)
  estTiempoProduccion(): string {
    return 'Aprox. 2 horas';
  }

  getBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'INVENTARIO_VALIDADO': 'badge-pendiente',
      'EN_PRODUCCION':       'badge-asignado',
      'DESPACHADO':          'badge-entregado'
    };
    return map[estado] || 'badge-pendiente';
  }

  getEstadoLabel(estado: string): string {
    const map: Record<string, string> = {
      'INVENTARIO_VALIDADO': 'Pendiente',
      'EN_PRODUCCION':       'Asignado',
      'DESPACHADO':          'Despachado'
    };
    return map[estado] || estado;
  }
}