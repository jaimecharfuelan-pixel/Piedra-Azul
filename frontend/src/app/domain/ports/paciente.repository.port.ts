import { Observable } from 'rxjs';
import { Paciente } from '../models/paciente.model';

export abstract class PacienteRepositoryPort {
  abstract listarTodos(): Observable<Paciente[]>;
  abstract buscarPorId(id: number): Observable<Paciente>;
}
