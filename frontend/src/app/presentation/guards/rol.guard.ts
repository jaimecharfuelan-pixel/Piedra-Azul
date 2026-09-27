import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { RolUsuario } from '../../domain/models/rol-usuario.enum';
import { AuthSessionStore } from '../../infrastructure/auth/auth-session.store';
import { AccionUi, rolPuede, rutaPanelPorRol } from '../../domain/auth/permisos';

/** Exige que el rol de la sesión esté en `data.roles`. */
export const rolGuard: CanActivateFn = (route) => {
  const sesion = inject(AuthSessionStore);
  const router = inject(Router);
  const roles = (route.data['roles'] as RolUsuario[] | undefined) ?? [];
  const rol = sesion.rol();

  if (rol && (roles.length === 0 || roles.includes(rol))) {
    return true;
  }

  if (rol) {
    return router.createUrlTree([rutaPanelPorRol(rol)]);
  }
  return router.createUrlTree(['/login']);
};

/** Exige permiso de acción UI (`data.accion`). */
export const accionGuard: CanActivateFn = (route) => {
  const sesion = inject(AuthSessionStore);
  const router = inject(Router);
  const accion = route.data['accion'] as AccionUi | undefined;
  const rol = sesion.rol();

  if (accion && rolPuede(rol, accion)) {
    return true;
  }
  if (rol) {
    return router.createUrlTree([rutaPanelPorRol(rol)]);
  }
  return router.createUrlTree(['/login']);
};
