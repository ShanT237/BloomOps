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
    tap(data => console.log('Clientes:', data)),
    catchError(this.handleError)
  );
}
  // ── Empleados ──
  getEmpleadosDisponibles(rol: string): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(`${this.base}/empleados/disponibles/${rol}`)
      .pipe(catchError(this.handleError));
  }

  // ── Inventario ──
  getInventario(): Observable<InsumoFloral[]> {
    return this.http.get<InsumoFloral[]>(`${this.base}/inventario`)
      .pipe(catchError(this.handleError));
  }

  // ── Pedidos ──
  getPedidos(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.base}/pedidos`)
      .pipe(catchError(this.handleError));
  }

  getPedidosPorEstado(estado: EstadoPedido): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.base}/pedidos/estado/${estado}`)
      .pipe(catchError(this.handleError));
  }

  registrarPedido(dto: PedidoRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${this.base}/pedidos`, dto)
      .pipe(catchError(this.handleError));
  }

  validarInventario(dto: ValidarInventarioRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${this.base}/pedidos/validar-inventario`, dto)
      .pipe(catchError(this.handleError));
  }

  programarProduccion(dto: AsignarProduccionRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${this.base}/pedidos/programar-produccion`, dto)
      .pipe(catchError(this.handleError));
  }

  aprobarDespacho(pedidoId: number, domiciliarioId: number): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(
      `${this.base}/pedidos/${pedidoId}/aprobar-despacho`,
      { domiciliarioId }
    ).pipe(catchError(this.handleError));
  }

  confirmarEntrega(dto: EntregaRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${this.base}/pedidos/confirmar-entrega`, dto)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const msg = error.error?.mensaje || error.message || 'Error de conexión con el servidor';
    return throwError(() => new Error(msg));
  }
}