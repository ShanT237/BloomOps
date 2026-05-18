import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Empleado } from '../../core/models';

@Component({
  selector: 'app-gestionar-empleados',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './gestionar-empleados.component.html',
  styleUrl: './gestionar-empleados.component.scss'
})
export class GestionarEmpleadosComponent implements OnInit {
  form!: FormGroup;
  empleados: Empleado[] = [];
  loading = false;
  errorMsg = '';
  roles = ['RECEPCIONISTA', 'FLORISTA', 'DOMICILIARIO', 'COORDINADOR', 'AUXILIAR_PRODUCCION'];
  editingId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      telefono: ['', Validators.required],
      rol: ['FLORISTA', Validators.required]
    });

    this.loadEmpleados();
  }

  loadEmpleados() {
    this.api.getEmpleados().subscribe({
      next: e => {
        this.empleados = e;
        this.cdr.detectChanges();
      },
      error: err => {
        this.errorMsg = err.message || 'No se pudieron cargar los empleados';
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    const v = this.form.value;
    const payload = { nombre: v.nombre, telefono: v.telefono, rol: v.rol, disponible: true };
    if (this.editingId) {
      this.api.editarEmpleado(this.editingId, payload).subscribe({
        next: emp => {
          const idx = this.empleados.findIndex(e => e.id === emp.id);
          if (idx >= 0) this.empleados[idx] = emp;
          this.form.reset({ rol: 'FLORISTA' });
          this.editingId = null;
          this.loading = false;
        },
        error: err => {
          this.errorMsg = err.message || 'Error al actualizar empleado';
          this.loading = false;
        }
      });
    } else {
      this.api.crearEmpleado(payload).subscribe({
        next: emp => {
          this.empleados.push(emp);
          this.form.reset({ rol: 'FLORISTA' });
          this.loading = false;
        },
        error: err => {
          this.errorMsg = err.message || 'Error al crear empleado';
          this.loading = false;
        }
      });
    }
  }

  onEdit(emp: Empleado) {
    this.editingId = emp.id;
    this.form.patchValue({ nombre: emp.nombre, telefono: emp.telefono, rol: emp.rol });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onCancelEdit() {
    this.editingId = null;
    this.form.reset({ rol: 'FLORISTA' });
  }

  onDelete(emp: Empleado) {
    if (!confirm(`Eliminar a ${emp.nombre}?`)) return;
    this.api.eliminarEmpleado(emp.id).subscribe({
      next: () => {
        this.empleados = this.empleados.filter(e => e.id !== emp.id);
      },
      error: err => {
        this.errorMsg = err.message || 'Error al eliminar empleado';
      }
    });
  }
}
