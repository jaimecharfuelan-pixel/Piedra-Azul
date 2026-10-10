import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConsultarPorCedulaUseCase } from '../../../application/use-cases/consultar-citas-por-cedula.use-case';
import { fechaLegible } from '../../../application/shared/fecha.util';
import { Cita } from '../../../domain/models/cita.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { HistorialCitasPorCedula } from '../../../domain/models/historial-citas-cedula.model';
import { Paciente } from '../../../domain/models/paciente.model';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiBadgeComponent } from '../../components/atoms/ui-badge.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiEmptyStateComponent } from '../../components/molecules/ui-empty-state.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';
import { UiStatCardComponent } from '../../components/molecules/ui-stat-card.component';

/**
 * Consulta el par de endpoints sin JWT: paciente por cédula + historial de citas.
 */
@Component({
  selector: 'app-citas-por-cedula-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    UiAlertComponent,
    UiBadgeComponent,
    UiButtonComponent,
    UiEmptyStateComponent,
    UiFieldComponent,
    UiSpinnerComponent,
    UiStatCardComponent,
  ],
  templateUrl: './citas-por-cedula.page.html',
  styleUrl: './citas-por-cedula.page.scss',
})
export class CitasPorCedulaPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly consultar = inject(ConsultarPorCedulaUseCase);

  readonly cargando = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly paciente = signal<Paciente | null>(null);
  readonly historial = signal<HistorialCitasPorCedula | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    cedula: ['', [Validators.required, Validators.pattern(/^\d{6,15}$/)]],
  });

  consultarPorCedula(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const cedula = this.formulario.getRawValue().cedula.trim().replace(/[.\s-]/g, '');
    this.cargando.set(true);
    this.error.set(null);
    this.paciente.set(null);
    this.historial.set(null);

    this.consultar.execute(cedula).subscribe({
      next: ({ paciente, historial }) => {
        this.paciente.set(paciente);
        this.historial.set(historial);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.cargando.set(false);
      },
    });
  }

  fechaTexto(fecha: string): string {
    return fechaLegible(fecha);
  }

  tonoEstado(estado: Cita['estado']): 'info' | 'exito' | 'error' | 'neutro' {
    switch (estado) {
      case 'PROGRAMADA':
        return 'info';
      case 'ATENDIDA':
        return 'exito';
      case 'CANCELADA':
        return 'error';
      default:
        return 'neutro';
    }
  }
}
