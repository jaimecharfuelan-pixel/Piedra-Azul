import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CalendarOptions, FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import themePlugin from '@fullcalendar/angular/themes/classic';
import dayGridPlugin from '@fullcalendar/angular/daygrid';
import timeGridPlugin from '@fullcalendar/angular/timegrid';
import interactionPlugin from '@fullcalendar/angular/interaction';
import { ListarCitasCalendarioUseCase } from '../../../application/use-cases/listar-citas-calendario.use-case';
import { ListarMedicosUseCase } from '../../../application/use-cases/gestionar-medico.use-case';
import { hoyIso, inicioDeSemanaIso, sumarDiasIso } from '../../../application/shared/fecha.util';
import { ErrorDominio } from '../../../domain/models/error-dominio.model';
import { Medico } from '../../../domain/models/medico.model';
import {
  aplicarColorEventoCalendario,
  citasToFullCalendarEvents,
} from '../../../infrastructure/calendar/fullcalendar.mapper';
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
  private readonly listarMedicos = inject(ListarMedicosUseCase);
  private readonly listarCitas = inject(ListarCitasCalendarioUseCase);

  readonly medicos = signal<readonly Medico[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<ErrorDominio | null>(null);
  readonly totalCitas = signal(0);

  medicoId: number | null = null;
  semanaDe = hoyIso();

  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [themePlugin, dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,timeGridDay' },
    locale: 'es',
    firstDay: 1,
    allDaySlot: false,
    slotMinTime: '06:00:00',
    slotMaxTime: '21:00:00',
    height: 'auto',
    editable: false,
    selectable: false,
    nowIndicator: true,
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
        const api = this.calendario?.getApi();
        if (api) {
          api.gotoDate(this.semanaDe);
          api.removeAllEvents();
          for (const evento of eventos) {
            api.addEvent(evento);
          }
        } else {
          this.calendarOptions = {
            ...this.calendarOptions,
            initialDate: this.semanaDe,
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
}
