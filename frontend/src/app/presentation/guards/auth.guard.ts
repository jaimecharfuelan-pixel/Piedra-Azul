import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthSessionStore } from '../../infrastructure/auth/auth-session.store';

/** Exige sesión JWT válida. */
export const authGuard: CanActivateFn = () => {
  const sesion = inject(AuthSessionStore);
  const router = inject(Router);
  if (sesion.autenticado()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};

/** Si ya hay sesión, manda al panel del rol (para login/registro/landing CTA). */
export const guestGuard: CanActivateFn = () => {
  const sesion = inject(AuthSessionStore);
  const router = inject(Router);
  if (!sesion.autenticado()) {
    return true;
  }
  const rol = sesion.rol();
  if (!rol) {
    return true;
  }
  return router.createUrlTree([
    rol === 'ADMINISTRADOR'
      ? '/panel/admin'
      : rol === 'AGENDADOR'
        ? '/panel/agendador'
        : rol === 'MEDICO'
          ? '/panel/medico'
          : '/panel/paciente',
  ]);
};
