import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { PedidoResponse } from '../../core/models';

@Component({
  selector: 'app-confirmar-entrega',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './confirmar-entrega.component.html',
  styleUrl: './confirmar-entrega.component.scss'
})
export class ConfirmarEntregaComponent implements OnInit, AfterViewInit {
  @ViewChild('firmaCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  pedidos: PedidoResponse[] = [];
  pedidoSeleccionadoId = '';
  pedidoActual: PedidoResponse | null = null;

  horaEntrega = '';
  nombreReceptor = '';
  observaciones = '';

  firmaTiene = false;
  dibujando = false;
  loading = false;
  cargandoPedidos = true;
  errorMsg = '';
  cargaPedidosError = '';
  exitoso = false;

  private ctx!: CanvasRenderingContext2D;

  get dentroFranja(): boolean {
    if (!this.pedidoActual || !this.horaEntrega) return false;
    const [inicio, fin] = this.pedidoActual.franjaHoraria.split('-');
    return this.horaEntrega >= inicio && this.horaEntrega <= fin;
  }

  constructor(private api: ApiService) {}

  ngOnInit() {
    const now = new Date();
    this.horaEntrega = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;

    this.api.getPedidosPorEstado('DESPACHADO').subscribe({
      next: p => {
        this.pedidos = p;
        this.cargandoPedidos = false;
      },
      error: err => {
        this.cargandoPedidos = false;
        this.cargaPedidosError = err.message || 'No se pudieron cargar los pedidos despachados';
      }
    });
  }

  ngAfterViewInit() {
    this.inicializarCanvas();
  }

  inicializarCanvas() {
    if (this.canvasRef) {
      const canvas = this.canvasRef.nativeElement;
      this.ctx = canvas.getContext('2d')!;
      this.ctx.strokeStyle = '#2C3E2D';
      this.ctx.lineWidth = 2;
      this.ctx.lineCap = 'round';
      this.ctx.lineJoin = 'round';
    }
  }

  onPedidoChange() {
    this.pedidoActual = this.pedidos.find(p => p.id === +this.pedidoSeleccionadoId) || null;
    this.nombreReceptor = '';
    this.observaciones = '';
    this.errorMsg = '';
    this.exitoso = false;
    this.limpiarFirma();
    setTimeout(() => this.inicializarCanvas(), 50);
  }

  iniciarDibujo(e: MouseEvent) {
    this.dibujando = true;
    const pos = this.getPosicion(e);
    this.ctx.beginPath();
    this.ctx.moveTo(pos.x, pos.y);
  }

  dibujar(e: MouseEvent) {
    if (!this.dibujando) return;
    const pos = this.getPosicion(e);
    this.ctx.lineTo(pos.x, pos.y);
    this.ctx.stroke();
    this.firmaTiene = true;
  }

  terminarDibujo() {
    this.dibujando = false;
  }

  limpiarFirma() {
    this.firmaTiene = false;
    if (this.ctx && this.canvasRef) {
      const canvas = this.canvasRef.nativeElement;
      this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
  }

  private getPosicion(e: MouseEvent): { x: number; y: number } {
    const canvas = this.canvasRef.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY
    };
  }

  cerrarPedido() {
    if (!this.pedidoActual || !this.firmaTiene || !this.nombreReceptor) return;
    this.loading = true;
    this.errorMsg = '';

    const firmaBase64 = this.canvasRef.nativeElement.toDataURL('image/png');

    this.api.confirmarEntrega({
      pedidoId:      this.pedidoActual.id,
      nombreReceptor: this.nombreReceptor,
      firmaReceptor:  firmaBase64,
      observaciones:  this.observaciones
    }).subscribe({
      next: () => {
        this.loading = false;
        this.exitoso = true;
        this.pedidos = this.pedidos.filter(p => p.id !== +this.pedidoSeleccionadoId);
        this.pedidoSeleccionadoId = '';
        this.pedidoActual = null;
        this.limpiarFirma();
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message;
      }
    });
  }
}