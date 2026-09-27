import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CalendarOptions,
  EventClickInfo,
  FullCalendarModule,
} from '@fullcalendar/angular';
import themePlugin from '@fullcalendar/angular/themes/classic';
import dayGridPlugin from '@fullcalendar/angular/daygrid';
import timeGridPlugin from '@fullcalendar/angular/timegrid';
import interactionPlugin from '@fullcalendar/angular/interaction';
import { ObtenerConfiguracionUseCase } from '../../../application/use-cases/configurar-disponibilidad.use-case';
import {
  ConsultarSlotsPorRangoUseCase,
  ConsultarSlotsUseCase,
} from '../../../application/use-cases/consultar-disponibilidad.use-case';
import {
  AgendarCitaUseCase,
  CancelarCitaUseCase,
  ListarCitasPacienteUseCase,
} from '../../../application/use-cases/gestionar-cita.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { ListarPacientesUseCase } from '../../../application/use-cases/listar-pacientes.use-case';
import { RegistrarPacienteUseCase } from '../../../application/use-cases/registrar-paciente.use-case';
import {
  fechaLegible,
  hoyIso,
  inicioDeSemanaIso,
  sumarDiasIso,
} from '../../../application/shared/fecha.util';
import { Cita } from '../../../domain/models/cita.model';
import { ConfiguracionSistema } from '../../../domain/models/configuracion-sistema.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { EstadoCita } from '../../../domain/models/estado-cita.enum';
import { Medico } from '../../../domain/models/medico.model';
import { Paciente } from '../../../domain/models/paciente.model';
import { SlotDisponible, claveSlot } from '../../../domain/models/slot-disponible.model';
import { slotsToFullCalendarEvents } from '../../../infrastructure/calendar/fullcalendar.mapper';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';
import { RolUsuario } from '../../../domain/models/rol-usuario.enum';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiBadgeComponent } from '../../components/atoms/ui-badge.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiEmptyStateComponent } from '../../components/molecules/ui-empty-state.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';
import { PzConfirmDialogComponent } from '../../components/organisms/pz-confirm-dialog.component';
import { PzSlotPickerComponent } from '../../components/organisms/pz-slot-picker.component';

/** Reserva de cita: paciente autenticado o selección de paciente por staff. */
@Component({
  selector: 'app-agendar-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FullCalendarModule,
    UiAlertComponent,
    UiBadgeComponent,
    UiButtonComponent,
    UiEmptyStateComponent,
    UiFieldComponent,
    UiSpinnerComponent,
    PzConfirmDialogComponent,
    PzSlotPickerComponent,
  ],
  templateUrl: './agendar.page.html',
  styleUrl: './agendar.page.scss',
})
export class AgendarPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sesion = inject(AuthSessionStore);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly listarPacientes = inject(ListarPacientesUseCase);
  private readonly registrarPaciente = inject(RegistrarPacienteUseCase);
  private readonly obtenerConfiguracion = inject(ObtenerConfiguracionUseCase);
  private readonly consultarSlots = inject(ConsultarSlotsUseCase);
  private readonly consultarSlotsRango = inject(ConsultarSlotsPorRangoUseCase);
  private readonly agendarCita = inject(AgendarCitaUseCase);
  private readonly cancelarCita = inject(CancelarCitaUseCase);
  private readonly listarMisCitas = inject(ListarCitasPacienteUseCase);

  readonly esPaciente = computed(() => this.sesion.rol() === RolUsuario.PACIENTE);
  readonly puedeRegistrarWalkIn = computed(
    () =>
      this.sesion.tieneRol(
        RolUsuario.ADMINISTRADOR,
        RolUsuario.AGENDADOR,
        RolUsuario.MEDICO
      )
  );

  readonly medicos = signal<readonly Medico[]>([]);
  readonly pacientes = signal<readonly Paciente[]>([]);
  readonly configuracion = signal<ConfiguracionSistema | null>(null);
  readonly slots = signal<readonly SlotDisponible[]>([]);
  readonly slotElegido = signal<SlotDisponible | null>(null);
  readonly misCitas = signal<readonly Cita[]>([]);

  readonly cargandoSlots = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly confirmando = signal(false);
  readonly procesando = signal(false);
  readonly registrando = signal(false);
  readonly mostrandoRegistro = signal(false);
  readonly citaACancelar = signal<Cita | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    pacienteId: [null as number | null, Validators.required],
    medicoId: [null as number | null, Validators.required],
    fecha: [hoyIso(), Validators.required],
  });

  /** Registro walk-in de paciente desde esta pantalla. */
  readonly formRegistro = this.fb.nonNullable.group({
    nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(180)]],
    telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
  });

  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [themePlugin, dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: { left: 'prev,next', center: 'title', right: 'timeGridWeek,dayGridMonth' },
    locale: 'es',
    firstDay: 1,
    allDaySlot: false,
    slotMinTime: '06:00:00',
    slotMaxTime: '21:00:00',
    height: 520,
    editable: false,
    selectable: false,
    nowIndicator: true,
    events: [],
    eventClick: (info: EventClickInfo) => this.elegirDesdeCalendario(info),
  };

  readonly claveElegida = computed(() => {
    const slot = this.slotElegido();
    return slot ? claveSlot(slot) : null;
  });

  /** Indica si el formulario y la franja elegida permiten confirmar. */
  puedeConfirmar(): boolean {
    return this.formulario.valid && this.slotElegido() !== null;
  }

  readonly proximasCitas = computed<readonly Cita[]>(() =>
    this.misCitas().filter((cita) => cita.estado === EstadoCita.PROGRAMADA)
  );

  ngOnInit(): void {
    this.obtenerConfiguracion.execute().subscribe({
      next: (config) => this.configuracion.set(config),
      error: (err) => this.error.set(aErrorDominio(err)),
    });

    if (this.esPaciente()) {
      const personaId = this.sesion.personaId();
      if (personaId != null) {
        this.formulario.patchValue({ pacienteId: personaId });
        this.cargarMisCitas();
      }
    } else {
      this.listarPacientes.execute().subscribe({
        next: (pacientes) => {
          this.pacientes.set(pacientes);
          const primero = pacientes[0];
          if (primero) {
            this.formulario.patchValue({ pacienteId: primero.id });
            this.cargarMisCitas();
          }
        },
        error: (err) => this.error.set(aErrorDominio(err)),
      });
    }

    this.listarMedicos.execute().subscribe({
      next: (medicos) => {
        this.medicos.set(medicos);
        const primero = medicos[0];
        if (primero) {
          this.formulario.patchValue({ medicoId: primero.id });
          this.recargarDisponibilidad();
        }
      },
      error: (err) => this.error.set(aErrorDominio(err)),
    });
  }

  medicoSeleccionado(): Medico | undefined {
    const id = this.formulario.getRawValue().medicoId;
    return this.medicos().find((medico) => medico.id === id);
  }

  pacienteSeleccionado(): Paciente | undefined {
    const id = this.formulario.getRawValue().pacienteId;
    return this.pacientes().find((paciente) => paciente.id === id);
  }

  fechaEnTexto(): string {
    return fechaLegible(this.formulario.getRawValue().fecha);
  }

  /** Cambiar médico, fecha o paciente invalida la franja ya elegida. */
  recargarDisponibilidad(): void {
    this.slotElegido.set(null);
    this.aviso.set(null);
    const { medicoId, fecha } = this.formulario.getRawValue();
    if (medicoId === null) {
      return;
    }

    this.cargandoSlots.set(true);
    this.error.set(null);
    this.consultarSlots.execute(medicoId, fecha).subscribe({
      next: (slots) => {
        this.slots.set(slots);
        this.cargandoSlots.set(false);
      },
      error: (err) => {
        this.slots.set([]);
        this.error.set(aErrorDominio(err));
        this.cargandoSlots.set(false);
      },
    });

    this.cargarSemanaEnCalendario(medicoId, fecha);
  }

  cambiarPaciente(): void {
    this.cargarMisCitas();
  }

  abrirRegistro(): void {
    this.mostrandoRegistro.set(true);
    this.formRegistro.reset({ nombreCompleto: '', telefono: '' });
  }

  cerrarRegistro(): void {
    this.mostrandoRegistro.set(false);
    this.registrando.set(false);
  }

  confirmarRegistro(): void {
    if (this.formRegistro.invalid) {
      this.formRegistro.markAllAsTouched();
      return;
    }
    const { nombreCompleto, telefono } = this.formRegistro.getRawValue();
    this.registrando.set(true);
    this.error.set(null);
    this.registrarPaciente.execute({ nombreCompleto: nombreCompleto.trim(), telefono }).subscribe({
      next: (paciente) => {
        this.pacientes.set([...this.pacientes(), paciente]);
        this.formulario.patchValue({ pacienteId: paciente.id });
        this.aviso.set(`Registro listo, ${paciente.nombreCompleto}. Ya puedes elegir médico y franja.`);
        this.cerrarRegistro();
        this.cargarMisCitas();
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.registrando.set(false);
      },
    });
  }

  errorRegistro(nombre: 'nombreCompleto' | 'telefono', mensaje: string): string | null {
    const control = this.formRegistro.controls[nombre];
    return control.touched && control.invalid ? mensaje : null;
  }

  elegirSlot(slot: SlotDisponible): void {
    this.slotElegido.set(slot);
    this.pintarCalendario();
  }

  abrirConfirmacion(): void {
    if (!this.puedeConfirmar()) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.confirmando.set(true);
  }

  confirmarAgendamiento(): void {
    const slot = this.slotElegido();
    const { pacienteId, medicoId } = this.formulario.getRawValue();
    if (!slot || pacienteId === null || medicoId === null) {
      return;
    }

    this.procesando.set(true);
    this.agendarCita
      .execute({
        pacienteId,
        medicoId,
        fecha: slot.fecha,
        horaInicio: slot.horaInicio,
        horaFin: slot.horaFin,
      })
      .subscribe({
        next: (cita) => {
          this.aviso.set(
            `Cita confirmada con ${cita.medicoNombre} el ${cita.fecha} de ${cita.horaInicio} a ${cita.horaFin}.`
          );
          this.confirmando.set(false);
          this.procesando.set(false);
          this.recargarDisponibilidad();
          this.cargarMisCitas();
        },
        error: (err) => {
          this.error.set(aErrorDominio(err));
          this.confirmando.set(false);
          this.procesando.set(false);
          this.recargarDisponibilidad();
        },
      });
  }

  cerrarConfirmacion(): void {
    this.confirmando.set(false);
    this.procesando.set(false);
  }

  pedirCancelacion(cita: Cita): void {
    this.aviso.set(null);
    this.citaACancelar.set(cita);
  }

  cerrarCancelacion(): void {
    this.citaACancelar.set(null);
    this.procesando.set(false);
  }

  confirmarCancelacion(): void {
    const cita = this.citaACancelar();
    if (!cita) {
      return;
    }
    this.procesando.set(true);
    this.cancelarCita.execute(cita.id).subscribe({
      next: () => {
        this.aviso.set(`Se canceló tu cita del ${cita.fecha} a las ${cita.horaInicio}.`);
        this.cerrarCancelacion();
        this.cargarMisCitas();
        this.recargarDisponibilidad();
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.cerrarCancelacion();
      },
    });
  }

  private cargarMisCitas(): void {
    const pacienteId = this.formulario.getRawValue().pacienteId;
    if (pacienteId === null) {
      return;
    }
    this.listarMisCitas.execute(pacienteId).subscribe({
      next: (citas) => this.misCitas.set(citas),
      error: (err) => this.error.set(aErrorDominio(err)),
    });
  }

  private cargarSemanaEnCalendario(medicoId: number, fecha: string): void {
    const desde = inicioDeSemanaIso(fecha);
    const hasta = sumarDiasIso(desde, 6);
    this.consultarSlotsRango.execute(medicoId, desde, hasta).subscribe({
      next: (slots) => {
        this.calendarOptions = {
          ...this.calendarOptions,
          initialDate: fecha,
          events: slotsToFullCalendarEvents(slots, this.claveElegida()),
        };
      },
      // El calendario es un apoyo visual: si falla, la lista de franjas sigue funcionando.
      error: () => {
        this.calendarOptions = { ...this.calendarOptions, initialDate: fecha, events: [] };
      },
    });
  }

  private pintarCalendario(): void {
    const eventos = this.calendarOptions.events;
    if (!Array.isArray(eventos)) {
      return;
    }
    const clave = this.claveElegida();
    this.calendarOptions = {
      ...this.calendarOptions,
      events: eventos.map((evento) => {
        const esElegido = evento['extendedProps']?.['clave'] === clave;
        return {
          ...evento,
          title: esElegido ? '✓ Seleccionada' : 'Disponible',
          backgroundColor: esElegido ? '#14487f' : '#e3eefb',
          borderColor: esElegido ? '#0a2b52' : '#7fb2e8',
          textColor: esElegido ? '#ffffff' : '#0a2b52',
        };
      }),
    };
  }

  private elegirDesdeCalendario(info: EventClickInfo): void {
    const props = info.event.extendedProps as {
      tipo?: string;
      fecha?: string;
      horaInicio?: string;
      horaFin?: string;
    };
    if (props.tipo !== 'slot' || !props.fecha || !props.horaInicio || !props.horaFin) {
      return;
    }
    // Si la franja es de otro día, se mueve el filtro y se recargan las franjas de ese día.
    if (props.fecha !== this.formulario.getRawValue().fecha) {
      this.formulario.patchValue({ fecha: props.fecha });
      const medicoId = this.formulario.getRawValue().medicoId;
      if (medicoId !== null) {
        this.cargandoSlots.set(true);
        this.consultarSlots.execute(medicoId, props.fecha).subscribe({
          next: (slots) => {
            this.slots.set(slots);
            this.cargandoSlots.set(false);
            this.elegirSlot({
              fecha: props.fecha!,
              horaInicio: props.horaInicio!,
              horaFin: props.horaFin!,
            });
          },
          error: (err) => {
            this.error.set(aErrorDominio(err));
            this.cargandoSlots.set(false);
          },
        });
      }
      return;
    }
    this.elegirSlot({
      fecha: props.fecha,
      horaInicio: props.horaInicio,
      horaFin: props.horaFin,
    });
  }
}
