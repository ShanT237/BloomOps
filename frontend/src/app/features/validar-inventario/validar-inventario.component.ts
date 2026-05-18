import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { InsumoFloral, PedidoResponse } from '../../core/models';

interface InsumoFila extends InsumoFloral {
  requerido: number;
  estado: 'ok' | 'bajo' | 'sin-definir';
}

@Component({
  selector: 'app-validar-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './validar-inventario.component.html',
  styleUrl: './validar-inventario.component.scss'
})
export class ValidarInventarioComponent implements OnInit {
  pedidos: PedidoResponse[] = [];
  insumos: InsumoFila[] = [];
  pedidoSeleccionado = '';
  numeroPedidoActual = '';
  loading = false;
  errorMsg = '';
  exitoso = false;
  cargandoPedidos = true;
  cargandoInsumos = true;
  cargaPedidosError = '';
  cargaInsumosError = '';

  get insumosConAlerta(): InsumoFila[] {
    return this.insumos.filter(i => i.estado === 'bajo');
  }

  get hayInsumosDefinidos(): boolean {
    return this.insumos.some(i => i.requerido > 0);
  }

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.cargarPedidosRegistrados();

    this.api.getInventario().subscribe({
      next: inv => {
        this.insumos = inv.map(i => ({
          ...i,
          requerido: 0,
          estado: 'sin-definir' as const
        }));
        this.cargandoInsumos = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.cargandoInsumos = false;
        this.cargaInsumosError = err.message || 'No se pudo cargar el inventario';
        this.cdr.detectChanges();
      }
    });
  }

  onPedidoChange() {
    this.exitoso = false;
    this.errorMsg = '';
    const p = this.pedidos.find(p => p.id === +this.pedidoSeleccionado);
    this.numeroPedidoActual = p?.numeroPedido || '';
    this.insumos.forEach(i => { i.requerido = 0; i.estado = 'sin-definir'; });
  }

  cargarPedidosRegistrados() {
    this.cargandoPedidos = true;
    this.api.getPedidosPorEstado('REGISTRADO').subscribe({
      next: p => {
        this.pedidos = p;
        this.cargandoPedidos = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.cargandoPedidos = false;
        this.cargaPedidosError = err.message || 'No se pudieron cargar los pedidos';
        this.cdr.detectChanges();
      }
    });
  }

  calcularEstado(insumo: InsumoFila) {
    if (insumo.requerido <= 0) {
      insumo.estado = 'sin-definir';
    } else {
      insumo.estado = insumo.stockDisponible >= insumo.requerido ? 'ok' : 'bajo';
    }
  }

  alertarCoordinador() {
    alert('⚠️ Alerta enviada al coordinador: inventario insuficiente para el pedido ' + this.numeroPedidoActual);
  }

  confirmarInventario() {
    if (!this.pedidoSeleccionado) {
      this.errorMsg = 'Selecciona un pedido antes de confirmar inventario';
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    const insumosRequeridos: { [key: number]: number } = {};
    this.insumos
      .filter(i => i.requerido > 0)
      .forEach(i => { insumosRequeridos[i.id] = i.requerido; });

    if (Object.keys(insumosRequeridos).length === 0) {
      this.errorMsg = 'Debes ingresar al menos un insumo requerido';
      this.loading = false;
      return;
    }

    this.api.validarInventario({
      pedidoId: +this.pedidoSeleccionado,
      insumosRequeridos
    }).subscribe({
      next: () => {
        this.loading = false;
        this.exitoso = true;
        this.pedidoSeleccionado = '';
        this.numeroPedidoActual = '';
        this.cargarPedidosRegistrados();
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message || 'Error al validar inventario';
      }
    });
  }
}