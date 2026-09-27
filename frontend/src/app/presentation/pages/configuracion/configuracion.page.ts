import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  ActualizarVentanaAgendamientoUseCase,
  ConfigurarPeriodoDisponibilidadUseCase,
  ListarPeriodosDisponibilidadUseCase,
  ObtenerConfiguracionUseCase,
} from '../../../application/use-cases/configurar-disponibilidad.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { hoyIso, sumarDiasIso } from '../../../application/shared/fecha.util';
import {
  debeFijarMedicoPropio,
  puedeEditarVentana,
} from '../../../domain/auth/permisos';
import { ConfiguracionSistema, VENTANA_MAXIMA_SEMANAS, VENTANA_MINIMA_SEMANAS } from '../../../domain/models/configuracion-sistema.model';
import { DIAS_SEMANA, DiaSemana, abreviaturaDia, etiquetaDia } from '../../../domain/models/dia-semana.enum';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { Medico } from '../../../domain/models/medico.model';
import {
  DESCANSO_MAXIMO_MINUTOS,
  DURACION_CITA_MINIMA_MINUTOS,
  PeriodoDisponibilidad,
} from '../../../domain/models/periodo-disponibilidad.model';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiBadgeComponent } from '../../components/atoms/ui-badge.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiEmptyStateComponent } from '../../components/molecules/ui-empty-state.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

/** Valida que la franja permita al menos una cita completa. */
function franjaCoherente(grupo: AbstractControl): ValidationErrors | null {
  const horaInicio = grupo.get('horaInicio')?.value as string | null;
  const horaFin = grupo.get('horaFin')?.value as string | null;
  const duracion = Number(grupo.get('duracionCitaMinutos')?.value ?? 0);
  if (!horaInicio || !horaFin) {
    return null;
  }
  if (horaFin <= horaInicio) {
    return { franjaInvertida: true };
  }
  const minutos = aMinutos(horaFin) - aMinutos(horaInicio);
  if (duracion > 0 && minutos < duracion) {
    return { franjaCorta: { minutos, duracion } };
  }
  return null;
}

function aMinutos(hora: string): number {
  const [h, m] = hora.split(':').map(Number);
  return h * 60 + m;
}

function normalizarHora(hora: string): string {
  return hora && hora.length >= 5 ? hora.slice(0, 5) : hora;
}

/** Configuración de ventana de agendamiento y horarios por médico. */
@Component({
  selector: 'app-configuracion-page',
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
  ],
  templateUrl: './configuracion.page.html',
  styleUrl: './configuracion.page.scss',
})
export class ConfiguracionPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly sesion = inject(AuthSessionStore);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly obtenerConfiguracion = inject(ObtenerConfiguracionUseCase);
  private readonly actualizarVentana = inject(ActualizarVentanaAgendamientoUseCase);
  private readonly configurarPeriodo = inject(ConfigurarPeriodoDisponibilidadUseCase);
  private readonly listarPeriodos = inject(ListarPeriodosDisponibilidadUseCase);

  readonly medicos = signal<readonly Medico[]>([]);
  readonly configuracion = signal<ConfiguracionSistema | null>(null);
  readonly periodos = signal<readonly PeriodoDisponibilidad[]>([]);

  readonly guardandoVentana = signal(false);
  readonly guardandoPeriodo = signal(false);
  readonly cargandoPeriodos = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly aviso = signal<string | null>(null);

  readonly puedeVentana = computed(() => puedeEditarVentana(this.sesion.rol()));
  readonly medicoFijado = computed(() => debeFijarMedicoPropio(this.sesion.rol()));
  readonly medicoSesionNombre = computed(() => {
    const id = this.sesion.personaId();
    return this.medicos().find((m) => m.id === id)?.nombreCompleto ?? 'Tu horario';
  });

  readonly dias = DIAS_SEMANA;
  readonly duracionMinima = DURACION_CITA_MINIMA_MINUTOS;
  readonly descansoMaximo = DESCANSO_MAXIMO_MINUTOS;

  readonly formVentana = this.fb.nonNullable.group({
    semanas: [
      4,
      [
        Validators.required,
        Validators.min(VENTANA_MINIMA_SEMANAS),
        Validators.max(VENTANA_MAXIMA_SEMANAS),
      ],
    ],
  });

  readonly formPeriodo = this.fb.nonNullable.group(
    {
      medicoId: [null as number | null, Validators.required],
      fechaInicio: [hoyIso(), Validators.required],
      horaInicio: ['08:00', Validators.required],
      horaFin: ['12:00', Validators.required],
      duracionCitaMinutos: [
        30,
        [Validators.required, Validators.min(DURACION_CITA_MINIMA_MINUTOS), Validators.max(480)],
      ],
      descansoEntreCitasMinutos: [
        0,
        [Validators.required, Validators.min(0), Validators.max(DESCANSO_MAXIMO_MINUTOS)],
      ],
    },
    { validators: franjaCoherente }
  );

  /** Días de atención seleccionados (fuera del FormGroup). */
  readonly diasElegidos = signal<ReadonlySet<DiaSemana>>(
    new Set([
      DiaSemana.LUNES,
      DiaSemana.MARTES,
      DiaSemana.MIERCOLES,
      DiaSemana.JUEVES,
      DiaSemana.VIERNES,
    ])
  );

  readonly periodoVigente = computed(() => this.periodos().find((periodo) => periodo.vigente));

  /** El horario abierto (sin fecha de fin) es el que se cierra al guardar uno nuevo. */
  readonly periodoAbierto = computed(() => this.periodos().find((periodo) => periodo.fechaFin === null));

  /** Snapshot del formulario de periodo para computed reactivos. */
  readonly valoresPeriodo = signal(this.formPeriodo.getRawValue());
  readonly periodoFormularioValido = signal(this.formPeriodo.valid);

  /** Citas por día según franja, duración y descanso del formulario. */
  readonly citasPorDia = computed(() => {
    const { horaInicio, horaFin, duracionCitaMinutos, descansoEntreCitasMinutos } =
      this.valoresPeriodo();
    const paso = duracionCitaMinutos + descansoEntreCitasMinutos;
    if (!horaInicio || !horaFin || paso <= 0) {
      return 0;
    }
    const disponible = aMinutos(horaFin) - aMinutos(horaInicio);
    if (disponible < duracionCitaMinutos) {
      return 0;
    }
    return Math.floor((disponible - duracionCitaMinutos) / paso) + 1;
  });

  readonly errorFranja = computed(() => {
    this.valoresPeriodo();
    const errores = this.formPeriodo.errors;
    if (!errores) {
      return null;
    }
    if (errores['franjaInvertida']) {
      return 'La hora de fin debe ser posterior a la de inicio.';
    }
    if (errores['franjaCorta']) {
      const { minutos, duracion } = errores['franjaCorta'] as { minutos: number; duracion: number };
      return `La franja dura ${minutos} minutos y no alcanza para una cita de ${duracion}.`;
    }
    return null;
  });

  readonly formularioPeriodoListo = computed(
    () => this.periodoFormularioValido() && this.diasElegidos().size > 0
  );

  ayudaFechaInicio(): string {
    const abierto = this.periodoAbierto();
    if (!abierto) {
      return 'Desde este día el médico podrá recibir citas. Un médico nuevo no tiene horario hasta que lo guardes.';
    }
    return `Ya hay un horario desde el ${abierto.fechaInicio}. Puedes usar esa misma fecha para actualizarlo, o una posterior para reemplazarlo (el anterior se cierra el día previo).`;
  }

  avisoHorarioExistente(): string | null {
    const abierto = this.periodoAbierto();
    if (!abierto) {
      return null;
    }
    const fecha = this.valoresPeriodo().fechaInicio;
    if (!fecha) {
      return `Este médico ya atiende desde el ${abierto.fechaInicio}.`;
    }
    if (fecha < abierto.fechaInicio) {
      return `La fecha debe ser el ${abierto.fechaInicio} (para actualizar) o posterior (para reemplazar).`;
    }
    if (fecha === abierto.fechaInicio) {
      return `Se actualizará el horario que ya rige desde el ${abierto.fechaInicio}.`;
    }
    return `El horario actual (desde ${abierto.fechaInicio}) se cerrará el ${sumarDiasIso(fecha, -1)} y este pasará a ser el vigente.`;
  }

  ngOnInit(): void {
    this.formPeriodo.statusChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.sincronizarFormularioPeriodo());
    this.formPeriodo.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.sincronizarFormularioPeriodo());
    this.sincronizarFormularioPeriodo();

    this.obtenerConfiguracion.execute().subscribe({
      next: (config) => {
        this.configuracion.set(config);
        this.formVentana.patchValue({ semanas: config.ventanaSemanas });
      },
      error: (err) => this.error.set(aErrorDominio(err)),
    });

    this.listarMedicos.execute().subscribe({
      next: (medicos) => {
        this.medicos.set(medicos);
        if (this.medicoFijado()) {
          const propio = this.sesion.personaId();
          if (propio != null) {
            this.formPeriodo.patchValue({ medicoId: propio });
            this.cargarPeriodos();
          }
          return;
        }
        const primero = medicos[0];
        if (primero) {
          this.formPeriodo.patchValue({ medicoId: primero.id });
          this.cargarPeriodos();
        }
      },
      error: (err) => this.error.set(aErrorDominio(err)),
    });
  }

  private sincronizarFormularioPeriodo(): void {
    this.valoresPeriodo.set(this.formPeriodo.getRawValue());
    this.periodoFormularioValido.set(this.formPeriodo.valid);
  }

  etiqueta(dia: DiaSemana): string {
    return etiquetaDia(dia);
  }

  abreviatura(dia: DiaSemana): string {
    return abreviaturaDia(dia);
  }

  estaElegido(dia: DiaSemana): boolean {
    return this.diasElegidos().has(dia);
  }

  alternarDia(dia: DiaSemana): void {
    const copia = new Set(this.diasElegidos());
    if (copia.has(dia)) {
      copia.delete(dia);
    } else {
      copia.add(dia);
    }
    this.diasElegidos.set(copia);
  }

  elegirDiasHabiles(): void {
    this.diasElegidos.set(
      new Set([
        DiaSemana.LUNES,
        DiaSemana.MARTES,
        DiaSemana.MIERCOLES,
        DiaSemana.JUEVES,
        DiaSemana.VIERNES,
      ])
    );
  }

  guardarVentana(): void {
    if (!this.puedeVentana() || this.formVentana.invalid) {
      this.formVentana.markAllAsTouched();
      return;
    }
    this.guardandoVentana.set(true);
    this.error.set(null);
    this.actualizarVentana.execute(this.formVentana.getRawValue().semanas).subscribe({
      next: (config) => {
        this.configuracion.set(config);
        this.aviso.set(
          `Ventana actualizada: se podrá agendar hasta el ${config.agendamientoHasta}.`
        );
        this.guardandoVentana.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.guardandoVentana.set(false);
      },
    });
  }

  onMedicoCambiado(): void {
    const crudo = this.formPeriodo.controls.medicoId.value;
    if (crudo !== null && crudo !== undefined) {
      this.formPeriodo.patchValue({ medicoId: Number(crudo) }, { emitEvent: true });
    }
    this.sincronizarFormularioPeriodo();
    this.cargarPeriodos();
  }

  cargarPeriodos(): void {
    const medicoId = this.formPeriodo.getRawValue().medicoId;
    if (medicoId === null) {
      this.periodos.set([]);
      return;
    }
    this.cargandoPeriodos.set(true);
    this.listarPeriodos.execute(Number(medicoId)).subscribe({
      next: (periodos) => {
        this.periodos.set(periodos);
        this.proponerFechaInicio(periodos);
        this.sincronizarFormularioPeriodo();
        this.cargandoPeriodos.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.cargandoPeriodos.set(false);
      },
    });
  }

  /**
   * Si el médico ya tiene un horario abierto, no se permite una fecha anterior
   * a ese inicio. La misma fecha actualiza; una posterior reemplaza.
   */
  private proponerFechaInicio(periodos: readonly PeriodoDisponibilidad[]): void {
    const abierto = periodos.find((periodo) => periodo.fechaFin === null);
    if (!abierto) {
      return;
    }
    const actual = this.formPeriodo.getRawValue().fechaInicio;
    if (!actual || actual < abierto.fechaInicio) {
      this.formPeriodo.patchValue({ fechaInicio: abierto.fechaInicio });
    }
  }

  guardarPeriodo(): void {
    this.formPeriodo.markAllAsTouched();
    this.sincronizarFormularioPeriodo();
    if (!this.formularioPeriodoListo()) {
      this.error.set({
        codigo: 'DATOS_INCOMPLETOS',
        mensaje:
          this.diasElegidos().size === 0
            ? 'Selecciona al menos un día de atención.'
            : 'Completa médico, fecha de inicio y la franja horaria para guardar el horario.',
      });
      return;
    }
    const valores = this.formPeriodo.getRawValue();
    if (valores.medicoId === null) {
      return;
    }

    const abierto = this.periodoAbierto();
    if (abierto && valores.fechaInicio < abierto.fechaInicio) {
      this.error.set({
        codigo: 'PERIODO_SOLAPADO',
        mensaje: `Este médico ya tiene un horario desde el ${abierto.fechaInicio}. Elige esa misma fecha para actualizarlo, o una posterior para reemplazarlo.`,
      });
      return;
    }

    this.guardandoPeriodo.set(true);
    this.error.set(null);
    this.configurarPeriodo
      .execute({
        medicoId: Number(valores.medicoId),
        fechaInicio: valores.fechaInicio,
        fechaFin: null,
        diasAtencion: [...this.diasElegidos()],
        horaInicio: normalizarHora(valores.horaInicio),
        horaFin: normalizarHora(valores.horaFin),
        duracionCitaMinutos: Number(valores.duracionCitaMinutos),
        descansoEntreCitasMinutos: Number(valores.descansoEntreCitasMinutos),
      })
      .subscribe({
        next: (periodo) => {
          this.aviso.set(
            `Horario de ${periodo.medicoNombre} vigente desde el ${periodo.fechaInicio}: ${periodo.horaInicio} a ${periodo.horaFin}, citas de ${periodo.duracionCitaMinutos} min.`
          );
          this.guardandoPeriodo.set(false);
          this.cargarPeriodos();
        },
        error: (err) => {
          this.error.set(aErrorDominio(err));
          this.guardandoPeriodo.set(false);
        },
      });
  }

  diasEnTexto(periodo: PeriodoDisponibilidad): string {
    return periodo.diasAtencion.map((dia) => abreviaturaDia(dia)).join(', ');
  }

  errorDe(nombre: keyof typeof this.formPeriodo.controls, mensaje: string): string | null {
    const control = this.formPeriodo.controls[nombre];
    return control.touched && control.invalid ? mensaje : null;
  }
}
