package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarVentanaAgendamientoUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfiguracionSistemaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * RF3: ventana (en semanas) durante la cual se habilita el agendamiento.
 */
@Service
@Transactional
public class ConfigurarVentanaAgendamientoService implements ConfigurarVentanaAgendamientoUseCase {

    private final DisponibilidadRepository disponibilidadRepository;
    private final Clock clock;

    public ConfigurarVentanaAgendamientoService(DisponibilidadRepository disponibilidadRepository, Clock clock) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.clock = clock;
    }

    @Override
    public ConfiguracionSistemaResponseDTO ejecutar(int semanas) {
        ConfiguracionSistema config = disponibilidadRepository.obtenerConfiguracionSistema();
        config.actualizarVentana(semanas);
        ConfiguracionSistema guardada = disponibilidadRepository.guardarConfiguracionSistema(config);
        return DisponibilidadMapper.toDto(guardada, LocalDate.now(clock));
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionSistemaResponseDTO consultar() {
        return DisponibilidadMapper.toDto(
                disponibilidadRepository.obtenerConfiguracionSistema(),
                LocalDate.now(clock)
        );
    }
}
