package com.piedraazul.disponibilidad.aplicacion.puertos.entrada;

import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;

/**
 * RF3: define los días, la franja horaria, la duración de la cita y el descanso
 * entre citas de un médico/terapista a partir de una fecha.
 *
 * <p>Lo pueden invocar Admin, Agendador o el propio Médico; el rol se valida en
 * el Controller/JWT, el caso de uso no sabe de roles.</p>
 */
public interface ConfigurarPeriodoDisponibilidadUseCase {

    PeriodoDisponibilidadResponseDTO ejecutar(ConfigurarPeriodoCommand comando);
}
