package com.piedraazul.disponibilidad.aplicacion.puertos.entrada;

import com.piedraazul.disponibilidad.infraestructura.dto.SlotDisponibleDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * RF2: entrega las franjas libres de un médico para que el paciente elija.
 */
public interface ConsultarDisponibilidadUseCase {

    List<SlotDisponibleDTO> ejecutar(Long medicoId, LocalDate fecha);

    /**
     * Variante por rango de fechas: alimenta la vista semanal/mensual de FullCalendar
     * con una sola llamada. Las fechas fuera de la ventana de agendamiento se omiten
     * en lugar de fallar, porque el calendario siempre pide semanas completas.
     */
    List<SlotDisponibleDTO> ejecutarPorRango(Long medicoId, LocalDate desde, LocalDate hasta);
}
