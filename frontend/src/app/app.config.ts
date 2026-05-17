import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { jsonInterceptor } from './core/json.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      // SIN withFetch() → Angular usa XHR → Zone.js lo parchea correctamente
      // → change detection corre después de cada respuesta → UI actualiza
      withInterceptors([jsonInterceptor])
    ),
    provideAnimations()
  ]
};