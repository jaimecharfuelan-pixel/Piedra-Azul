import { Observable } from 'rxjs';
import {
  ActualizarMedicoCommand,
  CrearMedicoCommand,
  Medico,
} from '../models/medico.model';

export abstract class MedicoRepositoryPort {
  abstract listarActivos(): Observable<Medico[]>;
  abstract buscarPorId(id: number): Observable<Medico>;
  abstract crear(comando: CrearMedicoCommand): Observable<Medico>;
  abstract actualizar(id: number, comando: ActualizarMedicoCommand): Observable<Medico>;
}
