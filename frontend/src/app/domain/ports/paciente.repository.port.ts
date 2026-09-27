import { Observable } from 'rxjs';
import { Paciente, RegistrarPacienteCommand } from '../models/paciente.model';

export abstract class PacienteRepositoryPort {
  abstract listarTodos(): Observable<Paciente[]>;
  abstract buscarPorId(id: number): Observable<Paciente>;
  abstract registrar(comando: RegistrarPacienteCommand): Observable<Paciente>;
}
