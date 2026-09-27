import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  HistorialPorMedicoUseCase,
  HistorialPorPacienteUseCase,
} from '../../../application/use-cases/consultar-historial.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { ListarPacientesUseCase } from '../../../application/use-cases/listar-pacientes.use-case';
import {
  debeFijarMedicoPropio,
  historialSoloPropio,
  puedeHistorialPorMedico,
  puedeListarPacientes,
} from '../../../domain/auth/permisos';
import { Consulta } from '../../../domain/models/consulta.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { Medico } from '../../../domain/models/medico.model';
import { Paciente } from '../../../domain/models/paciente.model';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiEmptyStateComponent } from '../../components/molecules/ui-empty-state.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

type Vista = 'paciente' | 'medico';

/**
 * Historial de consultas: lo que queda registrado cuando una cita se atiende.
 */
@Component({
  selector: 'app-historial-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    UiAlertComponent,
    UiButtonComponent,
    UiEmptyStateComponent,
    UiFieldComponent,
    UiSpinnerComponent,
  ],
  templateUrl: './historial.page.html',
  styleUrl: './historial.page.scss',
})
export class HistorialPageComponent implements OnInit {
  private readonly sesion = inject(AuthSessionStore);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly listarPacientes = inject(ListarPacientesUseCase);
  private readonly historialPaciente = inject(HistorialPorPacienteUseCase);
  private readonly historialMedico = inject(HistorialPorMedicoUseCase);

  readonly medicos = signal<readonly Medico[]>([]);
  readonly pacientes = signal<readonly Paciente[]>([]);
  readonly consultas = signal<readonly Consulta[]>([]);
  readonly vista = signal<Vista>('paciente');
  readonly cargando = signal(false);
  readonly error = signal<ErrorDominio | null>(null);

  readonly soloPropio = computed(() => historialSoloPropio(this.sesion.rol()));
  readonly puedeListar = computed(() => puedeListarPacientes(this.sesion.rol()));
  readonly puedePorMedico = computed(() => puedeHistorialPorMedico(this.sesion.rol()));
  readonly medicoFijado = computed(() => debeFijarMedicoPropio(this.sesion.rol()));
  readonly medicoSesionNombre = computed(() => {
    const id = this.sesion.personaId();
    return this.medicos().find((m) => m.id === id)?.nombreCompleto ?? 'Tu historial';
  });

  pacienteId: number | null = null;
  medicoId: number | null = null;

  ngOnInit(): void {
    if (this.soloPropio()) {
      this.vista.set('paciente');
      this.pacienteId = this.sesion.personaId();
      this.consultar();
      return;
    }

    if (this.puedeListar()) {
      this.listarPacientes.execute().subscribe({
        next: (pacientes) => {
          this.pacientes.set(pacientes);
          const primero = pacientes[0];
          if (primero && this.vista() === 'paciente') {
            this.pacienteId = primero.id;
            this.consultar();
          }
        },
        error: (err) => this.error.set(aErrorDominio(err)),
      });
    }

    if (this.puedePorMedico()) {
      this.listarMedicos.execute().subscribe({
        next: (medicos) => {
          this.medicos.set(medicos);
          if (this.medicoFijado()) {
            this.medicoId = this.sesion.personaId();
          } else {
            this.medicoId = medicos[0]?.id ?? null;
          }
        },
        error: (err) => this.error.set(aErrorDominio(err)),
      });
    }
  }

  cambiarVista(vista: Vista): void {
    if (this.soloPropio()) {
      return;
    }
    if (vista === 'medico' && !this.puedePorMedico()) {
      return;
    }
    this.vista.set(vista);
    this.consultas.set([]);
    if (vista === 'medico' && this.medicoFijado()) {
      this.medicoId = this.sesion.personaId();
    }
    this.consultar();
  }

  consultar(): void {
    const esPorPaciente = this.vista() === 'paciente';
    const id = esPorPaciente ? this.pacienteId : this.medicoId;
    if (id === null) {
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    const peticion = esPorPaciente
      ? this.historialPaciente.execute(id)
      : this.historialMedico.execute(id);

    peticion.subscribe({
      next: (consultas) => {
        this.consultas.set(consultas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.consultas.set([]);
        this.cargando.set(false);
      },
    });
  }
}
