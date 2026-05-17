import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

/**
 * Interceptor funcional — solo actúa en peticiones con body (POST, PUT, PATCH).
 *
 * IMPORTANTE: NO agregar Content-Type a peticiones GET.
 * Un GET sin Content-Type es una "simple request" CORS → sin preflight.
 * Un GET con Content-Type: application/json → preflight OPTIONS obligatorio.
 * Si el preflight falla, todos los GETs fallan → ningún dato carga.
 */
export const jsonInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const methodsWithBody = ['POST', 'PUT', 'PATCH'];
  const isApiCall = req.url.includes('/api/');
  const needsContentType = methodsWithBody.includes(req.method);

  if (isApiCall && needsContentType) {
    const jsonReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json',
        'Accept':       'application/json'
      }
    });
    return next(jsonReq);
  }

  return next(req);
};