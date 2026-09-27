package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Recorre la franja horaria del periodo en pasos de
 * {@code duracionCitaMinutos + descansoEntreCitasMinutos} y descarta los pasos
 * que se solapan con los rangos ya ocupados.
 *
 * <p>El recorrido se hace en minutos desde medianoche (no con
 * {@code LocalTime.plusMinutes}) para que una franja que termina cerca de las
 * 23:59 no genere slots dando la vuelta al día.</p>
 */
public class CalculadorSlotsPorIntervaloFijo implements CalculadorSlotsStrategy {

    @Override
    public List<SlotDisponible> calcular(PeriodoDisponibilidad periodo, LocalDate fecha, List<TimeRange> rangosOcupados) {
        Objects.requireNonNull(periodo, "periodo es obligatorio");
        Objects.requireNonNull(fecha, "fecha es obligatoria");
        List<TimeRange> ocupados = rangosOcupados == null ? List.of() : rangosOcupados;

        if (!periodo.incluyeFecha(fecha) || !periodo.atiendeEnDia(DiaSemana.desde(fecha))) {
            return List.of();
        }

        int inicioFranja = minutoDelDia(periodo.getFranjaHoraria().getHoraInicio());
        int finFranja = minutoDelDia(periodo.getFranjaHoraria().getHoraFin());
        int duracion = periodo.getDuracionCitaMinutos();
        int paso = periodo.getPasoMinutos();

        List<SlotDisponible> slots = new ArrayList<>();
        for (int inicio = inicioFranja; inicio + duracion <= finFranja; inicio += paso) {
            TimeRange candidato = new TimeRange(aHora(inicio), aHora(inicio + duracion));
            boolean ocupado = ocupados.stream().anyMatch(candidato::solapaCon);
            if (!ocupado) {
                slots.add(new SlotDisponible(fecha, candidato));
            }
        }
        return List.copyOf(slots);
    }

    private static int minutoDelDia(LocalTime hora) {
        return hora.toSecondOfDay() / 60;
    }

    private static LocalTime aHora(int minutoDelDia) {
        return LocalTime.ofSecondOfDay(minutoDelDia * 60L);
    }
}
