import { Injectable, signal, computed } from '@angular/core';
import { RolUsuario } from '../../domain/models/rol-usuario.enum';
import { SesionUsuario, TokenResponseDto } from '../../domain/models/sesion.model';

const CLAVE_SESION = 'pz.sesion';

/**
 * Almacén de sesión JWT en memoria + localStorage.
 */
@Injectable({ providedIn: 'root' })
export class AuthSessionStore {
  private readonly sesionSignal = signal<SesionUsuario | null>(this.leerPersistida());

  readonly sesion = this.sesionSignal.asReadonly();
  readonly autenticado = computed(() => {
    const s = this.sesionSignal();
    return !!s && s.expiresAt > Date.now();
  });
  readonly rol = computed(() => this.sesionSignal()?.rol ?? null);
  readonly personaId = computed(() => this.sesionSignal()?.personaId ?? null);
  readonly username = computed(() => this.sesionSignal()?.username ?? null);

  accessToken(): string | null {
    const s = this.sesionSignal();
    if (!s || s.expiresAt <= Date.now()) {
      return null;
    }
    return s.accessToken;
  }

  guardarTrasLogin(dto: TokenResponseDto, username: string): SesionUsuario {
    const sesion: SesionUsuario = {
      accessToken: dto.accessToken,
      tokenType: dto.tokenType || 'Bearer',
      expiresIn: dto.expiresIn,
      expiresAt: Date.now() + dto.expiresIn * 1000,
      rol: dto.rol,
      personaId: dto.personaId,
      usuarioId: dto.usuarioId,
      username: username.trim().toLowerCase(),
    };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this.sesionSignal.set(sesion);
    return sesion;
  }

  cerrarSesion(): void {
    localStorage.removeItem(CLAVE_SESION);
    this.sesionSignal.set(null);
  }

  tieneRol(...roles: RolUsuario[]): boolean {
    const actual = this.sesionSignal()?.rol;
    return !!actual && roles.includes(actual);
  }

  private leerPersistida(): SesionUsuario | null {
    try {
      const crudo = localStorage.getItem(CLAVE_SESION);
      if (!crudo) {
        return null;
      }
      const sesion = JSON.parse(crudo) as SesionUsuario;
      if (!sesion?.accessToken || !sesion.expiresAt || sesion.expiresAt <= Date.now()) {
        localStorage.removeItem(CLAVE_SESION);
        return null;
      }
      return sesion;
    } catch {
      localStorage.removeItem(CLAVE_SESION);
      return null;
    }
  }
}
