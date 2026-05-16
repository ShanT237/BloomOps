import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'registrar-pedido', pathMatch: 'full' },
  {
    path: 'registrar-pedido',
    loadComponent: () => import('./features/registrar-pedido/registrar-pedido.component')
      .then(m => m.RegistrarPedidoComponent)
  },
  {
    path: 'validar-inventario',
    loadComponent: () => import('./features/validar-inventario/validar-inventario.component')
      .then(m => m.ValidarInventarioComponent)
  },
  {
    path: 'programar-produccion',
    loadComponent: () => import('./features/programar-produccion/programar-produccion.component')
      .then(m => m.ProgramarProduccionComponent)
  },
  {
    path: 'confirmar-entrega',
    loadComponent: () => import('./features/confirmar-entrega/confirmar-entrega.component')
      .then(m => m.ConfirmarEntregaComponent)
  },
  { path: '**', redirectTo: 'registrar-pedido' }
];