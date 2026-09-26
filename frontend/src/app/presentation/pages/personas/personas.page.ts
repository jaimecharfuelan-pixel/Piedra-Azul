import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CrearEspecialidadUseCase,
  ListarEspecialidadesUseCase,
} from '../../../application/use-cases/gestionar-especialidad.use-case';
import {
  CrearMedicoUseCase,
  ListarMedicosUseCase,
} from '../../../application/use-cases/gestionar-medico.use-case';
import { ListarPacientesUseCase } from '../../../application/use-cases/listar-pacientes.use-case';
import { Especialidad } from '../../../domain/models/especialidad.model';
import { Medico } from '../../../domain/models/medico.model';
import { Paciente } from '../../../domain/models/paciente.model';

@Component({
  selector: 'app-personas-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './personas.page.html',
  styleUrl: './personas.page.scss',
})
export class PersonasPageComponent implements OnInit {
  private readonly listarEspecialidades = inject(ListarEspecialidadesUseCase);
  private readonly crearEspecialidad = inject(CrearEspecialidadUseCase);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly crearMedico = inject(CrearMedicoUseCase);
  private readonly listarPacientes = inject(ListarPacientesUseCase);

  readonly especialidades = signal<Especialidad[]>([]);
  readonly medicos = signal<Medico[]>([]);
  readonly pacientes = signal<Paciente[]>([]);
  readonly error = signal<string | null>(null);

  nuevaEspecialidad = '';
  nuevoMedicoNombre = '';
  nuevoMedicoEspecialidadId: number | null = null;

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
}
