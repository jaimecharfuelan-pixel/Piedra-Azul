import { Observable } from 'rxjs';
import { CrearEspecialidadCommand, Especialidad } from '../models/especialidad.model';

export abstract class EspecialidadRepositoryPort {
  abstract listarActivas(): Observable<Especialidad[]>;
  abstract crear(comando: CrearEspecialidadCommand): Observable<Especialidad>;
}
