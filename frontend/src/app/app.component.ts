import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="sidebar-logo">
          <div class="logo-icon">🌸</div>
          <div class="logo-name">BloomOps</div>
          <div class="logo-sub">Gestión de Envíos Florales</div>
        </div>
        <nav class="sidebar-nav">
          <div class="nav-label">Flujo del Pedido</div>
          <a routerLink="/registrar-pedido" routerLinkActive="active">📋 Registrar Pedido</a>
          <a routerLink="/validar-inventario" routerLinkActive="active">📦 Validar Inventario</a>
          <a routerLink="/programar-produccion" routerLinkActive="active">🌿 Programar Producción</a>
          <a routerLink="/confirmar-entrega" routerLinkActive="active">✅ Confirmar Entrega</a>
        </nav>
        <div class="sidebar-footer">BloomOps v1.0</div>
      </aside>
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `
})
export class AppComponent {}