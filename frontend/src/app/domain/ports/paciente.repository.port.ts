import { Observable } from 'rxjs';
import { Paciente, RegistrarPacienteCommand } from '../models/paciente.model';

export abstract class PacienteRepositoryPort {
  abstract listarTodos(): Observable<Paciente[]>;
  abstract buscarPorId(id: number): Observable<Paciente>;
  /** Par 1/2 sin JWT: consulta por cédula. */
  abstract buscarPorCedula(cedula: string): Observable<Paciente>;
  abstract registrar(comando: RegistrarPacienteCommand): Observable<Paciente>;
}
