import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CrearEspecialidadUseCase,
  ListarEspecialidadesUseCase,
} from '../../../application/use-cases/gestionar-especialidad.use-case';
import {
  CrearMedicoUseCase,
  ListarMedicosUseCase,
} from '../../../application/use-cases/gestionar-medico.use-case';
import { ListarPacientesUseCase } from '../../../application/use-cases/listar-pacientes.use-case';
import { RegistrarPacienteUseCase } from '../../../application/use-cases/registrar-paciente.use-case';
import { Especialidad } from '../../../domain/models/especialidad.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { Medico } from '../../../domain/models/medico.model';
import { Paciente } from '../../../domain/models/paciente.model';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

@Component({
  selector: 'app-personas-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    UiAlertComponent,
    UiButtonComponent,
    UiFieldComponent,
  ],
  templateUrl: './personas.page.html',
  styleUrl: './personas.page.scss',
})
export class PersonasPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly listarEspecialidades = inject(ListarEspecialidadesUseCase);
  private readonly crearEspecialidad = inject(CrearEspecialidadUseCase);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly crearMedico = inject(CrearMedicoUseCase);
  private readonly listarPacientes = inject(ListarPacientesUseCase);
  private readonly registrarPaciente = inject(RegistrarPacienteUseCase);

  readonly especialidades = signal<Especialidad[]>([]);
  readonly medicos = signal<Medico[]>([]);
  readonly pacientes = signal<Paciente[]>([]);
  readonly error = signal<string | null>(null);
  readonly errorDominio = signal<ErrorDominio | null>(null);
  readonly registrando = signal(false);

  nuevaEspecialidad = '';
  nuevoMedicoNombre = '';
  nuevoMedicoEspecialidadId: number | null = null;

  readonly formPaciente = this.fb.nonNullable.group({
    nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(180)]],
    telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
  });

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.error.set(null);
    this.listarEspecialidades.execute().subscribe({
      next: (data) => this.especialidades.set(data),
      error: (err) => this.error.set(err?.error?.detail ?? 'Error al cargar especialidades'),
    });
    this.listarMedicos.execute().subscribe({
      next: (data) => this.medicos.set(data),
      error: (err) => this.error.set(err?.error?.detail ?? 'Error al cargar médicos'),
    });
    this.listarPacientes.execute().subscribe({
      next: (data) => this.pacientes.set(data),
      error: () => {
        /* pacientes pueden estar vacíos sin Identidad */
      },
    });
  }

  onCrearEspecialidad(): void {
    if (!this.nuevaEspecialidad.trim()) {
      return;
    }
    this.crearEspecialidad.execute({ nombre: this.nuevaEspecialidad.trim() }).subscribe({
      next: () => {
        this.nuevaEspecialidad = '';
        this.recargar();
      },
      error: (err) => this.error.set(err?.error?.detail ?? 'No se pudo crear la especialidad'),
    });
  }

  onCrearMedico(): void {
    if (!this.nuevoMedicoNombre.trim() || this.nuevoMedicoEspecialidadId == null) {
      return;
    }
    this.crearMedico
      .execute({
        nombreCompleto: this.nuevoMedicoNombre.trim(),
        especialidadId: this.nuevoMedicoEspecialidadId,
      })
      .subscribe({
        next: () => {
          this.nuevoMedicoNombre = '';
          this.nuevoMedicoEspecialidadId = null;
          this.recargar();
        },
        error: (err) => this.error.set(err?.error?.detail ?? 'No se pudo crear el médico'),
      });
  }

  onRegistrarPaciente(): void {
    if (this.formPaciente.invalid) {
      this.formPaciente.markAllAsTouched();
      return;
    }
    const { nombreCompleto, telefono } = this.formPaciente.getRawValue();
    this.registrando.set(true);
    this.error.set(null);
    this.errorDominio.set(null);
    this.registrarPaciente.execute({ nombreCompleto: nombreCompleto.trim(), telefono }).subscribe({
      next: () => {
        this.formPaciente.reset({ nombreCompleto: '', telefono: '' });
        this.registrando.set(false);
        this.recargar();
      },
      error: (err) => {
        this.errorDominio.set(aErrorDominio(err));
        this.registrando.set(false);
      },
    });
  }

  errorPaciente(nombre: 'nombreCompleto' | 'telefono', mensaje: string): string | null {
    const control = this.formPaciente.controls[nombre];
    return control.touched && control.invalid ? mensaje : null;
  }
}
