import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthSessionStore } from '../auth/auth-session.store';

/**
 * Adjunta Bearer token a todas las peticiones excepto login/registro público.
 */
export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const url = req.url;
  const publica =
    url.includes('/api/auth/login') || url.includes('/api/auth/registro-paciente');

  if (publica) {
    return next(req);
  }

  const token = inject(AuthSessionStore).accessToken();
  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    })
  );
};
