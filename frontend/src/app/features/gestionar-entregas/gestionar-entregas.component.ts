import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';

interface Entrega {
  id: number;
  pedidoId: number;
  numeroPedido: string;
  nombreCliente: string;
  nombreDestinatario: string;
  direccionEntrega: string;
  tipoArreglo: string;
  fechaHoraEntrega: string;
  nombreReceptor: string;
  firmaReceptor: string;
  observaciones: string;
  entregaExitosa: boolean;
  motivoNoEntrega: string;
  estado: string;
  tieneSignatura: boolean;
}

@Component({
  selector: 'app-gestionar-entregas',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './gestionar-entregas.component.html',
  styleUrls: ['./gestionar-entregas.component.scss']
})
export class GestionarEntregasComponent implements OnInit {

  entregas: Entrega[] = [];
  filtroEstado: string = 'todos';
  mostrarModal: boolean = false;
  modoEdicion: boolean = false;
  entregaSeleccionada: Entrega | null = null;
  formulario: FormGroup;
  cargando: boolean = false;
  mensaje: string = '';
  tipoMensaje: 'exito' | 'error' = 'exito';

  constructor(
    private apiService: ApiService,
    private fb: FormBuilder
  ) {
    this.formulario = this.fb.group({
      nombreReceptor: ['', Validators.required],
      firmaReceptor: ['', Validators.required],
      observaciones: ['']
    });
  }

  ngOnInit(): void {
    this.cargarEntregas();
  }

  cargarEntregas(): void {
    this.cargando = true;
    this.apiService.getEntregas().subscribe({
      next: (data: any) => {
        this.entregas = data;
        this.cargando = false;
      },
      error: (err: any) => {
        this.mostrarMensaje('Error al cargar entregas', 'error');
        this.cargando = false;
      }
    });
  }

  filtrarEntregas(): Entrega[] {
    if (this.filtroEstado === 'todos') {
      return this.entregas;
    }
    return this.entregas.filter(e => e.estado.toLowerCase() === this.filtroEstado.toLowerCase());
  }

  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.entregaSeleccionada = null;
    this.formulario.reset();
    this.mostrarModal = true;
  }

  abrirModalEditar(entrega: Entrega): void {
    if (entrega.entregaExitosa) {
      this.mostrarMensaje('No se puede editar una entrega ya confirmada', 'error');
      return;
    }
    this.modoEdicion = true;
    this.entregaSeleccionada = entrega;
    this.formulario.patchValue({
      nombreReceptor: entrega.nombreReceptor,
      firmaReceptor: entrega.firmaReceptor,
      observaciones: entrega.observaciones
    });
    this.mostrarModal = true;
  }

  confirmarEntrega(entrega: Entrega): void {
    if (!entrega.nombreReceptor || !entrega.firmaReceptor) {
      this.mostrarMensaje('Falta capturar nombre y firma del receptor', 'error');
      return;
    }

    this.cargando = true;
    this.apiService.confirmarEntregaExitosa(entrega.id, {
      nombreReceptor: entrega.nombreReceptor,
      firmaReceptor: entrega.firmaReceptor,
      observaciones: entrega.observaciones
    }).subscribe({
      next: (data: any) => {
        this.mostrarMensaje('Entrega confirmada exitosamente ✓', 'exito');
        this.cargarEntregas();
        this.cargando = false;
      },
      error: (err: any) => {
        this.mostrarMensaje('Error al confirmar entrega', 'error');
        this.cargando = false;
      }
    });
  }

  rechazarEntrega(entrega: Entrega): void {
    const motivo = prompt('Ingrese el motivo de no entrega:');
    if (!motivo) return;

    this.cargando = true;
    this.apiService.rechazarEntrega(entrega.id, motivo).subscribe({
      next: (data: any) => {
        this.mostrarMensaje('Entrega marcada como no entregada', 'exito');
        this.cargarEntregas();
        this.cargando = false;
      },
      error: (err: any) => {
        this.mostrarMensaje('Error al rechazar entrega', 'error');
        this.cargando = false;
      }
    });
  }

  guardarEntrega(): void {
    if (!this.formulario.valid) {
      this.mostrarMensaje('Complete todos los campos requeridos', 'error');
      return;
    }

    const datos = this.formulario.value;
    this.cargando = true;

    if (this.modoEdicion && this.entregaSeleccionada) {
      this.apiService.editarEntrega(this.entregaSeleccionada.id, datos).subscribe({
        next: (data: any) => {
          this.mostrarMensaje('Entrega actualizada exitosamente', 'exito');
          this.cargarEntregas();
          this.cerrarModal();
          this.cargando = false;
        },
        error: (err: any) => {
          this.mostrarMensaje('Error al actualizar entrega', 'error');
          this.cargando = false;
        }
      });
    }
  }

  eliminarEntrega(entrega: Entrega): void {
    if (entrega.entregaExitosa) {
      this.mostrarMensaje('No se puede eliminar una entrega confirmada', 'error');
      return;
    }

    if (!confirm('¿Está seguro de que desea cancelar esta entrega?')) {
      return;
    }

    this.cargando = true;
    this.apiService.eliminarEntrega(entrega.id).subscribe({
      next: (data: any) => {
        this.mostrarMensaje('Entrega cancelada exitosamente', 'exito');
        this.cargarEntregas();
        this.cargando = false;
      },
      error: (err: any) => {
        this.mostrarMensaje('Error al cancelar entrega', 'error');
        this.cargando = false;
      }
    });
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.formulario.reset();
  }

  mostrarMensaje(texto: string, tipo: 'exito' | 'error'): void {
    this.mensaje = texto;
    this.tipoMensaje = tipo;
    setTimeout(() => {
      this.mensaje = '';
    }, 3000);
  }

  getClaseEstado(estado: string): string {
    switch (estado.toLowerCase()) {
      case 'entregada':
        return 'badge-success';
      case 'pendiente':
        return 'badge-warning';
      case 'en ruta':
        return 'badge-info';
      case 'no entregada':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  getIconoEstado(estado: string): string {
    switch (estado.toLowerCase()) {
      case 'entregada':
        return '✓';
      case 'pendiente':
        return '⏱';
      case 'en ruta':
        return '🚚';
      case 'no entregada':
        return '✗';
      default:
        return '•';
    }
  }
}
