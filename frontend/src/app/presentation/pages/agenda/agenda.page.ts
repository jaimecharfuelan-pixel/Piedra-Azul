import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ConsultarSlotsUseCase,
} from '../../../application/use-cases/consultar-disponibilidad.use-case';
import {
  CancelarCitaUseCase,
  MarcarCitaAtendidaUseCase,
  ReagendarCitaUseCase,
} from '../../../application/use-cases/gestionar-cita.use-case';
import { ListarCitasPorMedicoUseCase } from '../../../application/use-cases/listar-citas-por-medico.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { fechaLegible, hoyIso } from '../../../application/shared/fecha.util';
import { Cita, ListadoCitas } from '../../../domain/models/cita.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { EstadoCita } from '../../../domain/models/estado-cita.enum';
import { Medico } from '../../../domain/models/medico.model';
import {
  CampoOrdenable,
  OrdenCitas,
  alternarOrden,
} from '../../../domain/models/orden-citas.enum';
import { SlotDisponible, claveSlot } from '../../../domain/models/slot-disponible.model';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiEmptyStateComponent } from '../../components/molecules/ui-empty-state.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';
import { UiStatCardComponent } from '../../components/molecules/ui-stat-card.component';
import { PzCitasTableComponent } from '../../components/organisms/pz-citas-table.component';
import { PzConfirmDialogComponent } from '../../components/organisms/pz-confirm-dialog.component';
import { PzSlotPickerComponent } from '../../components/organisms/pz-slot-picker.component';

type AccionPendiente = 'cancelar' | 'atender' | 'reagendar';

/**
 * RF1 — El agendador busca las citas de un médico en una fecha y ve el listado
 * con su cantidad. Desde la misma tabla puede cancelar, reagendar o cerrar la cita.
 */
@Component({
  selector: 'app-agenda-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    UiAlertComponent,
    UiButtonComponent,
    UiEmptyStateComponent,
    UiFieldComponent,
    UiSpinnerComponent,
    UiStatCardComponent,
    PzCitasTableComponent,
    PzConfirmDialogComponent,
    PzSlotPickerComponent,
  ],
  templateUrl: './agenda.page.html',
  styleUrl: './agenda.page.scss',
})
export class AgendaPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly listarCitas = inject(ListarCitasPorMedicoUseCase);
  private readonly cancelarCita = inject(CancelarCitaUseCase);
  private readonly atenderCita = inject(MarcarCitaAtendidaUseCase);
  private readonly reagendarCita = inject(ReagendarCitaUseCase);
  private readonly consultarSlots = inject(ConsultarSlotsUseCase);

  readonly medicos = signal<readonly Medico[]>([]);
  readonly listado = signal<ListadoCitas | null>(null);
  readonly cargando = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly orden = signal<OrdenCitas>(OrdenCitas.HORA_ASC);

  /** Refinamiento en cliente sobre el resultado ya traído del servidor. */
  readonly textoPaciente = signal('');
  readonly estadoFiltro = signal<EstadoCita | 'TODOS'>('TODOS');

  readonly citaSeleccionada = signal<Cita | null>(null);
  readonly accion = signal<AccionPendiente | null>(null);
  readonly procesando = signal(false);
  readonly observaciones = signal('');
  readonly slotsReagendar = signal<readonly SlotDisponible[]>([]);
  readonly slotReagendarElegido = signal<SlotDisponible | null>(null);
  readonly fechaReagendar = signal(hoyIso());
  readonly cargandoSlots = signal(false);

  readonly estados: readonly (EstadoCita | 'TODOS')[] = [
    'TODOS',
    EstadoCita.PROGRAMADA,
    EstadoCita.ATENDIDA,
    EstadoCita.CANCELADA,
  ];

  readonly filtros = this.fb.nonNullable.group({
    medicoId: [null as number | null, Validators.required],
    fecha: [hoyIso(), Validators.required],
  });

  /** Filas visibles después de aplicar la búsqueda por paciente y el estado. */
  readonly citasVisibles = computed<readonly Cita[]>(() => {
    const todas = this.listado()?.citas ?? [];
    const texto = this.textoPaciente().trim().toLowerCase();
    const estado = this.estadoFiltro();
    return todas.filter((cita) => {
      const coincideTexto = texto === '' || cita.pacienteNombre.toLowerCase().includes(texto);
      const coincideEstado = estado === 'TODOS' || cita.estado === estado;
      return coincideTexto && coincideEstado;
    });
  });

  readonly hayRefinamiento = computed(
    () => this.textoPaciente().trim() !== '' || this.estadoFiltro() !== 'TODOS'
  );

  ngOnInit(): void {
    this.listarMedicos.execute().subscribe({
      next: (medicos) => {
        this.medicos.set(medicos);
        const primero = medicos[0];
        if (primero) {
          this.filtros.patchValue({ medicoId: primero.id });
          this.buscar();
        }
      },
      error: (err) => this.error.set(aErrorDominio(err)),
    });
  }

  buscar(): void {
    if (this.filtros.invalid) {
      this.filtros.markAllAsTouched();
      return;
    }
    const { medicoId, fecha } = this.filtros.getRawValue();
    if (medicoId === null) {
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    this.listarCitas.execute(medicoId, fecha, this.orden()).subscribe({
      next: (listado) => {
        this.listado.set(listado);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.listado.set(null);
        this.cargando.set(false);
      },
    });
  }

  cambiarOrden(campo: CampoOrdenable): void {
    this.orden.set(alternarOrden(this.orden(), campo));
    this.buscar();
  }

  limpiarRefinamiento(): void {
    this.textoPaciente.set('');
    this.estadoFiltro.set('TODOS');
  }

  fechaEnTexto(): string {
    return fechaLegible(this.filtros.getRawValue().fecha);
  }

  // --- Acciones sobre una cita -------------------------------------------

  pedirCancelar(cita: Cita): void {
    this.abrirAccion(cita, 'cancelar');
  }

  pedirAtender(cita: Cita): void {
    this.observaciones.set('');
    this.abrirAccion(cita, 'atender');
  }

  pedirReagendar(cita: Cita): void {
    this.slotReagendarElegido.set(null);
    this.fechaReagendar.set(cita.fecha);
    this.abrirAccion(cita, 'reagendar');
    this.cargarSlotsReagendar(cita.medicoId, cita.fecha);
  }

  cerrarDialogo(): void {
    this.accion.set(null);
    this.citaSeleccionada.set(null);
    this.procesando.set(false);
  }

  cambiarFechaReagendar(fecha: string): void {
    this.fechaReagendar.set(fecha);
    this.slotReagendarElegido.set(null);
    const cita = this.citaSeleccionada();
    if (cita) {
      this.cargarSlotsReagendar(cita.medicoId, fecha);
    }
  }

  elegirSlotReagendar(slot: SlotDisponible): void {
    this.slotReagendarElegido.set(slot);
  }

  claveSlotElegido(): string | null {
    const slot = this.slotReagendarElegido();
    return slot ? claveSlot(slot) : null;
  }

  confirmar(): void {
    const cita = this.citaSeleccionada();
    const accion = this.accion();
    if (!cita || !accion) {
      return;
    }

    this.procesando.set(true);
    switch (accion) {
      case 'reagendar':
        this.ejecutarReagendar(cita);
        return;
      case 'cancelar':
        this.cancelarCita.execute(cita.id).subscribe({
          next: () =>
            this.terminar(`Se canceló la cita de ${cita.pacienteNombre} (${cita.horaInicio}).`),
          error: (err: unknown) => this.fallar(err),
        });
        return;
      case 'atender':
        this.atenderCita.execute(cita.id, this.observaciones()).subscribe({
          next: () =>
            this.terminar(
              `Se registró la consulta de ${cita.pacienteNombre} (${cita.horaInicio}).`
            ),
          error: (err: unknown) => this.fallar(err),
        });
        return;
    }
  }

  tituloDialogo(): string {
    switch (this.accion()) {
      case 'cancelar':
        return 'Cancelar la cita';
      case 'atender':
        return 'Marcar la cita como atendida';
      case 'reagendar':
        return 'Reagendar la cita';
      default:
        return '';
    }
  }

  mensajeDialogo(): string {
    const cita = this.citaSeleccionada();
    if (!cita) {
      return '';
    }
    switch (this.accion()) {
      case 'cancelar':
        return `La franja de ${cita.horaInicio} a ${cita.horaFin} volverá a quedar disponible para otro paciente.`;
      case 'atender':
        return `Se creará la entrada del historial de ${cita.pacienteNombre}. La cita quedará cerrada y ya no se podrá modificar.`;
      case 'reagendar':
        return `Elige una nueva franja libre para ${cita.pacienteNombre}. La cita actual es ${cita.fecha} de ${cita.horaInicio} a ${cita.horaFin}.`;
      default:
        return '';
    }
  }

  private abrirAccion(cita: Cita, accion: AccionPendiente): void {
    this.aviso.set(null);
    this.error.set(null);
    this.citaSeleccionada.set(cita);
    this.accion.set(accion);
  }

  private cargarSlotsReagendar(medicoId: number, fecha: string): void {
    this.cargandoSlots.set(true);
    this.slotsReagendar.set([]);
    this.consultarSlots.execute(medicoId, fecha).subscribe({
      next: (slots) => {
        this.slotsReagendar.set(slots);
        this.cargandoSlots.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.cargandoSlots.set(false);
      },
    });
  }

  private ejecutarReagendar(cita: Cita): void {
    const slot = this.slotReagendarElegido();
    if (!slot) {
      this.procesando.set(false);
      return;
    }
    this.reagendarCita
      .execute(cita.id, {
        fecha: slot.fecha,
        horaInicio: slot.horaInicio,
        horaFin: slot.horaFin,
      })
      .subscribe({
        next: () =>
          this.terminar(
            `La cita de ${cita.pacienteNombre} quedó para el ${slot.fecha} a las ${slot.horaInicio}.`
          ),
        error: (err: unknown) => this.fallar(err),
      });
  }

  private terminar(mensaje: string): void {
    this.aviso.set(mensaje);
    this.cerrarDialogo();
    this.buscar();
  }

  private fallar(err: unknown): void {
    this.error.set(aErrorDominio(err));
    this.cerrarDialogo();
  }
}
