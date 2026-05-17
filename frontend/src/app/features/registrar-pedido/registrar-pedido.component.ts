import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Cliente, PedidoResponse } from '../../core/models';

@Component({
  selector: 'app-registrar-pedido',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registrar-pedido.component.html',
  styleUrl: './registrar-pedido.component.scss'
})
export class RegistrarPedidoComponent implements OnInit {
  form!: FormGroup;
  clientes: Cliente[] = [];
  loading = false;
  cargandoClientes = true;
  errorMsg = '';
  clientesError = '';
  pedidoCreado: PedidoResponse | null = null;
  minDate = '';

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const d = new Date();
    d.setDate(d.getDate() + 2);
    this.minDate = d.toISOString().split('T')[0];

    this.form = this.fb.group({
      clienteId:            ['', Validators.required],
      nombreCliente:        [''],
      cedula:               [''],
      telefono:             [''],
      email:                [''],
      tipoArreglo:          ['', Validators.required],
      colores:              [''],
      mensajeTarjeta:       [''],
      nombreDestinatario:   ['', Validators.required],
      telefonoDestinatario: ['', Validators.required],
      direccionEntrega:     ['', Validators.required],
      fechaEspecial:        ['', Validators.required],
      franjaHoraria:        ['', Validators.required],
    });

    this.api.getClientes().subscribe({
      next: c => {
        this.clientes = c;
        this.cargandoClientes = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.cargandoClientes = false;
        this.clientesError = err.message || 'No se pudieron cargar los clientes';
        this.cdr.detectChanges();
      }
    });
  }

  onClienteChange(event: Event) {
    const id = +(event.target as HTMLSelectElement).value;
    const cliente = this.clientes.find(c => c.id === id);
    if (cliente) {
      this.form.patchValue({
        nombreCliente: cliente.nombre,
        cedula:        cliente.cedula,
        telefono:      cliente.telefono,
        email:         cliente.email || ''
      });
    }
  }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    this.pedidoCreado = null;

    const v = this.form.value;
    this.api.registrarPedido({
      clienteId:            +v.clienteId,
      nombreDestinatario:   v.nombreDestinatario,
      direccionEntrega:     v.direccionEntrega,
      telefonoDestinatario: v.telefonoDestinatario,
      tipoArreglo:          v.tipoArreglo,
      colores:              v.colores,
      mensajeTarjeta:       v.mensajeTarjeta,
      fechaEspecial:        v.fechaEspecial,
      franjaHoraria:        v.franjaHoraria
    }).subscribe({
      next: pedido => {
        this.loading = false;
        this.pedidoCreado = pedido;
        this.form.reset();
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.message;
      }
    });
  }
}