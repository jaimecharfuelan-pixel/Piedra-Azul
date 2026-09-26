import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FullCalendarModule,
  CalendarOptions,
  EventInput,
} from '@fullcalendar/angular';
import themePlugin from '@fullcalendar/angular/themes/classic';
import dayGridPlugin from '@fullcalendar/angular/daygrid';
import timeGridPlugin from '@fullcalendar/angular/timegrid';
import interactionPlugin from '@fullcalendar/angular/interaction';
import { ListarCitasCalendarioUseCase } from '../../../application/use-cases/listar-citas-calendario.use-case';
import { citasToFullCalendarEvents } from '../../../infrastructure/calendar/fullcalendar.mapper';

@Component({
  selector: 'app-calendar-page',
  standalone: true,
  imports: [CommonModule, FullCalendarModule],
  templateUrl: './calendar.page.html',
  styleUrl: './calendar.page.scss',
})
export class CalendarPageComponent implements OnInit {
  private readonly listarCitas = inject(ListarCitasCalendarioUseCase);

  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [themePlugin, dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    height: 'auto',
    editable: false,
    selectable: true,
    events: [] as EventInput[],
  };

  ngOnInit(): void {
    const hoy = new Date();
    const desde = hoy.toISOString().slice(0, 10);
    const hastaDate = new Date(hoy);
    hastaDate.setDate(hoy.getDate() + 7);
    const hasta = hastaDate.toISOString().slice(0, 10);

    this.listarCitas.execute('medico-demo', desde, hasta).subscribe((citas) => {
      const mapped = citasToFullCalendarEvents(citas);
      this.calendarOptions = { ...this.calendarOptions, events: mapped };
    });
  }
}
