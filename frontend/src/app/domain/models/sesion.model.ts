import { RolUsuario } from './rol-usuario.enum';

/** Sesión JWT que el frontend persiste tras el login. */
export interface SesionUsuario {
  readonly accessToken: string;
  readonly tokenType: string;
  readonly expiresIn: number;
  /** Epoch ms en que el token deja de valer. */
  readonly expiresAt: number;
  readonly rol: RolUsuario;
  readonly personaId: number | null;
  readonly usuarioId: number;
  readonly username: string;
}

export interface LoginCommand {
  readonly username: string;
  readonly password: string;
}

export interface RegistroPacienteAuthCommand {
  readonly username: string;
  readonly password: string;
  readonly nombreCompleto: string;
  readonly telefono: string;
}

/** Respuesta cruda de POST /api/auth/login. */
export interface TokenResponseDto {
  readonly accessToken: string;
  readonly tokenType: string;
  readonly expiresIn: number;
  readonly rol: RolUsuario;
  readonly personaId: number | null;
  readonly usuarioId: number;
}

export interface UsuarioResponseDto {
  readonly id: number;
  readonly username: string;
  readonly rol: RolUsuario;
  readonly personaId: number | null;
  readonly activo: boolean;
}
