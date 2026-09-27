import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CalendarOptions, FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import themePlugin from '@fullcalendar/angular/themes/classic';
import dayGridPlugin from '@fullcalendar/angular/daygrid';
import timeGridPlugin from '@fullcalendar/angular/timegrid';
import interactionPlugin from '@fullcalendar/angular/interaction';
import { ListarCitasCalendarioUseCase } from '../../../application/use-cases/listar-citas-calendario.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { hoyIso, inicioDeSemanaIso, sumarDiasIso } from '../../../application/shared/fecha.util';
import { debeFijarMedicoPropio } from '../../../domain/auth/permisos';
import { Cita } from '../../../domain/models/cita.model';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { Medico } from '../../../domain/models/medico.model';
import {
  aplicarColorEventoCalendario,
  citasToFullCalendarEvents,
} from '../../../infrastructure/calendar/fullcalendar.mapper';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiSpinnerComponent } from '../../components/atoms/ui-spinner.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

/**
 * Vista de calendario de las citas de un médico, con el color según el estado.
 */
@Component({
  selector: 'app-calendar-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FullCalendarModule,
    UiAlertComponent,
    UiFieldComponent,
    UiSpinnerComponent,
  ],
  templateUrl: './calendar.page.html',
  styleUrl: './calendar.page.scss',
})
export class CalendarPageComponent implements OnInit {
  @ViewChild(FullCalendarComponent) calendario?: FullCalendarComponent;
  private readonly sesion = inject(AuthSessionStore);
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly listarCitas = inject(ListarCitasCalendarioUseCase);

  readonly medicos = signal<readonly Medico[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly totalCitas = signal(0);

  readonly medicoFijado = computed(() => debeFijarMedicoPropio(this.sesion.rol()));
  readonly medicoSesionNombre = computed(() => {
    const id = this.sesion.personaId();
    return this.medicos().find((m) => m.id === id)?.nombreCompleto ?? 'Tu calendario';
  });

  medicoId: number | null = null;
  semanaDe = hoyIso();

  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [themePlugin, dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,timeGridDay' },
    locale: 'es',
    timeZone: 'local',
    firstDay: 1,
    allDaySlot: false,
    slotMinTime: '06:00:00',
    slotMaxTime: '21:00:00',
    scrollTime: '07:00:00',
    height: 'auto',
    editable: false,
    selectable: false,
    nowIndicator: true,
    displayEventEnd: true,
    forceEventDuration: true,
    events: [],
    eventDidMount: (info) => {
      const estado = String(info.event.extendedProps['estado'] ?? '');
      aplicarColorEventoCalendario(info.el, estado);
    },
  };

  ngOnInit(): void {
    this.listarMedicos.execute().subscribe({
      next: (medicos) => {
        this.medicos.set(medicos);
        if (this.medicoFijado()) {
          const propio = this.sesion.personaId();
          if (propio != null) {
            this.medicoId = propio;
            this.cargar();
          }
          return;
        }
        const primero = medicos[0];
        if (primero) {
          this.medicoId = primero.id;
          this.cargar();
        }
      },
      error: (err) => this.error.set(aErrorDominio(err)),
    });
  }

  cargar(): void {
    if (this.medicoId === null) {
      return;
    }
    const desde = inicioDeSemanaIso(this.semanaDe);
    const hasta = sumarDiasIso(desde, 34);

    this.cargando.set(true);
    this.error.set(null);
    this.listarCitas.execute(this.medicoId, desde, hasta).subscribe({
      next: (citas) => {
        this.totalCitas.set(citas.length);
        const eventos = citasToFullCalendarEvents(citas);
        const ancla = this.fechaAnclaVista(citas);
        const scroll = this.horaScroll(citas, ancla);
        const api = this.calendario?.getApi();
        if (api) {
          api.setOption('scrollTime', scroll);
          api.gotoDate(ancla);
          api.removeAllEvents();
          for (const evento of eventos) {
            api.addEvent(evento);
          }
        } else {
          this.calendarOptions = {
            ...this.calendarOptions,
            initialDate: ancla,
            scrollTime: scroll,
            events: eventos,
          };
        }
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(aErrorDominio(err));
        this.cargando.set(false);
      },
    });
  }

  /**
   * Si la fecha filtro cae en domingo (fin de semana ISO), la vista semanal
   * no incluye el lunes siguiente donde suelen estar las citas. Anclamos a la
   * primera cita de la semana elegida o, si no hay, a la más cercana.
   */
  private fechaAnclaVista(citas: readonly Cita[]): string {
    if (citas.length === 0) {
      return this.semanaDe;
    }
    const ordenadas = [...citas].sort((a, b) =>
      `${a.fecha}T${a.horaInicio}`.localeCompare(`${b.fecha}T${b.horaInicio}`)
    );
    const inicioSemana = inicioDeSemanaIso(this.semanaDe);
    const finSemana = sumarDiasIso(inicioSemana, 6);
    const enSemana = ordenadas.find((c) => c.fecha >= inicioSemana && c.fecha <= finSemana);
    if (enSemana) {
      return enSemana.fecha;
    }
    const desdeFiltro = ordenadas.find((c) => c.fecha >= this.semanaDe);
    return desdeFiltro?.fecha ?? ordenadas[0].fecha;
  }

  private horaScroll(citas: readonly Cita[], ancla: string): string {
    const delDia = citas
      .filter((c) => c.fecha === ancla)
      .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
    const hora = delDia[0]?.horaInicio ?? citas[0]?.horaInicio;
    if (!hora) {
      return '07:00:00';
    }
    const normalizada = hora.length === 5 ? `${hora}:00` : hora.slice(0, 8);
    return normalizada;
  }
}
