import { Component, OnInit } from '@angular/core';
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

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.cargarPedidos();
    this.api.getEmpleadosDisponibles('FLORISTA').subscribe({
      next: e => {
        this.floristas = e;
        this.cargandoFloristas = false;
      },
      error: () => {
        this.cargandoFloristas = false;
      }
    });
    this.api.getEmpleadosDisponibles('DOMICILIARIO').subscribe({
      next: e => {
        this.domiciliarios = e;
        this.cargandoDomiciliarios = false;
      },
      error: () => {
        this.cargandoDomiciliarios = false;
      }
    });
  }

  cargarPedidos() {
    this.cargandoPedidos = true;
    this.api.getPedidosPorEstado('INVENTARIO_VALIDADO').subscribe({
      next: p => {
        this.pedidos = p;
        this.cargandoPedidos = false;
      },
      error: () => {
        this.cargandoPedidos = false;
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
      next: () => {
        this.loading = false;
        this.exitoso = true;
        this.pedidos = this.pedidos.filter(p => p.id !== this.pedidoSeleccionadoId);
        this.pedidoSeleccionado = null;
        this.pedidoSeleccionadoId = null;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message;
      }
    });
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