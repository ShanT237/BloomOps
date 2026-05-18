import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  Cliente, Empleado, InsumoFloral, PedidoResponse,
  PedidoRequest, ValidarInventarioRequest,
  AsignarProduccionRequest, EntregaRequest, EstadoPedido
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private readonly base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ── Clientes ──
  getClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.base}/clientes`).pipe(
      tap(data => console.log('[API] Clientes cargados:', data.length)),
      catchError(this.handleError)
    );
  }

  // ── Empleados ──
  getEmpleadosDisponibles(rol: string): Observable<Empleado[]> {
    return this.http
      .get<Empleado[]>(`${this.base}/empleados/disponibles/${rol}`)
      .pipe(
        tap(data => console.log(`[API] Empleados ${rol}:`, data.length)),
        catchError(this.handleError)
      );
  }

  // ── Inventario ──
  getInventario(): Observable<InsumoFloral[]> {
    return this.http.get<InsumoFloral[]>(`${this.base}/inventario`).pipe(
      tap(data => console.log('[API] Insumos cargados:', data.length)),
      catchError(this.handleError)
    );
  }

  // ── Pedidos ──
  getPedidos(): Observable<PedidoResponse[]> {
    return this.http
      .get<PedidoResponse[]>(`${this.base}/pedidos`)
      .pipe(catchError(this.handleError));
  }

  getPedidosPorEstado(estado: EstadoPedido): Observable<PedidoResponse[]> {
    return this.http
      .get<PedidoResponse[]>(`${this.base}/pedidos/estado/${estado}`)
      .pipe(
        tap(data => console.log(`[API] Pedidos ${estado}:`, data.length)),
        catchError(this.handleError)
      );
  }

  registrarPedido(dto: PedidoRequest): Observable<PedidoResponse> {
    return this.http
      .post<PedidoResponse>(`${this.base}/pedidos`, dto)
      .pipe(catchError(this.handleError));
  }

  validarInventario(dto: ValidarInventarioRequest): Observable<PedidoResponse> {
    // Convertir claves numéricas a string para JSON seguro
    const payload = {
      pedidoId: dto.pedidoId,
      insumosRequeridos: Object.fromEntries(
        Object.entries(dto.insumosRequeridos).map(([k, v]) => [String(k), v])
      )
    };
    return this.http
      .post<PedidoResponse>(`${this.base}/pedidos/validar-inventario`, payload)
      .pipe(catchError(this.handleError));
  }

  programarProduccion(dto: AsignarProduccionRequest): Observable<PedidoResponse> {
    return this.http
      .post<PedidoResponse>(`${this.base}/pedidos/programar-produccion`, dto)
      .pipe(catchError(this.handleError));
  }

  aprobarDespacho(pedidoId: number, domiciliarioId: number): Observable<PedidoResponse> {
    return this.http
      .post<PedidoResponse>(
        `${this.base}/pedidos/${pedidoId}/aprobar-despacho`,
        { domiciliarioId }
      )
      .pipe(catchError(this.handleError));
  }

  confirmarEntrega(dto: EntregaRequest): Observable<PedidoResponse> {
    return this.http
      .post<PedidoResponse>(`${this.base}/pedidos/confirmar-entrega`, dto)
      .pipe(catchError(this.handleError));
  }

  // ── Entregas (Nuevo CRUD) ──
  getEntregas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/entregas`).pipe(
      tap(data => console.log('[API] Entregas cargadas:', data.length)),
      catchError(this.handleError)
    );
  }

  getEntregaPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/entregas/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getEntregaPorPedidoId(pedidoId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/entregas/pedido/${pedidoId}`).pipe(
      catchError(this.handleError)
    );
  }

  getEntregasPendientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/entregas/estado/pendientes`).pipe(
      catchError(this.handleError)
    );
  }

  getEntregasEntregadas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/entregas/estado/entregadas`).pipe(
      catchError(this.handleError)
    );
  }

  getEntregasNoEntregadas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/entregas/estado/no-entregadas`).pipe(
      catchError(this.handleError)
    );
  }

  crearEntrega(dto: any): Observable<any> {
    return this.http.post<any>(`${this.base}/entregas`, dto).pipe(
      tap(() => console.log('[API] Entrega creada')),
      catchError(this.handleError)
    );
  }

  editarEntrega(id: number, dto: any): Observable<any> {
    return this.http.put<any>(`${this.base}/entregas/${id}`, dto).pipe(
      tap(() => console.log('[API] Entrega actualizada')),
      catchError(this.handleError)
    );
  }

  confirmarEntregaExitosa(id: number, dto: any): Observable<any> {
    return this.http.post<any>(`${this.base}/entregas/${id}/confirmar`, dto).pipe(
      tap(() => console.log('[API] Entrega confirmada')),
      catchError(this.handleError)
    );
  }

  rechazarEntrega(id: number, motivo: string): Observable<any> {
    return this.http.post<any>(`${this.base}/entregas/${id}/rechazar`, { motivo }).pipe(
      tap(() => console.log('[API] Entrega rechazada')),
      catchError(this.handleError)
    );
  }

  eliminarEntrega(id: number): Observable<any> {
    return this.http.delete<any>(`${this.base}/entregas/${id}`).pipe(
      tap(() => console.log('[API] Entrega eliminada')),
      catchError(this.handleError)
    );
  }

  // ── Manejo de errores centralizado ──
  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let msg: string;

    if (error.status === 0) {
      // Error de red: backend caído, CORS bloqueando, sin conexión
      msg = 'No se pudo conectar con el servidor. Verifica que el backend esté corriendo en el puerto 8080.';
      console.error('[API] Error de red / CORS:', error);
    } else if (error.status === 422) {
      // Regla de negocio violada
      msg = error.error?.mensaje ?? 'Error de validación de negocio';
    } else if (error.status === 404) {
      msg = error.error?.mensaje ?? 'Recurso no encontrado';
    } else if (error.status === 409) {
      msg = error.error?.mensaje ?? 'Conflicto: el recurso ya existe';
    } else if (error.status === 400) {
      msg = error.error?.mensaje ?? 'Datos inválidos en la solicitud';
    } else {
      msg = error.error?.mensaje ?? error.message ?? 'Error interno del servidor';
    }

    return throwError(() => new Error(msg));
  };
}