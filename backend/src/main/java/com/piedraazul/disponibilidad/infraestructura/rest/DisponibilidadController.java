package com.piedraazul.disponibilidad.infraestructura.rest;

import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarPeriodoDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarVentanaAgendamientoUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarPeriodosDisponibilidadUseCase;
import com.piedraazul.disponibilidad.infraestructura.dto.ActualizarVentanaCommand;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfiguracionSistemaResponseDTO;
import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;
import com.piedraazul.disponibilidad.infraestructura.dto.SlotDisponibleDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API del módulo Disponibilidad (RF2 lectura de franjas, RF3 configuración).
 *
 * <p>La autorización por rol (Admin/Agendador/Médico para configurar, cualquiera
 * autenticado para consultar franjas) se resuelve en la capa de seguridad; los
 * casos de uso no conocen roles.</p>
 */
@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final ConfigurarVentanaAgendamientoUseCase configurarVentanaUseCase;
    private final ConfigurarPeriodoDisponibilidadUseCase configurarPeriodoUseCase;
    private final ConsultarPeriodosDisponibilidadUseCase consultarPeriodosUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;

    public DisponibilidadController(
            ConfigurarVentanaAgendamientoUseCase configurarVentanaUseCase,
            ConfigurarPeriodoDisponibilidadUseCase configurarPeriodoUseCase,
            ConsultarPeriodosDisponibilidadUseCase consultarPeriodosUseCase,
            ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase
    ) {
        this.configurarVentanaUseCase = configurarVentanaUseCase;
        this.configurarPeriodoUseCase = configurarPeriodoUseCase;
        this.consultarPeriodosUseCase = consultarPeriodosUseCase;
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
    }

    @GetMapping("/configuracion")
    public ConfiguracionSistemaResponseDTO obtenerConfiguracion() {
        return configurarVentanaUseCase.consultar();
    }

    @PutMapping("/configuracion")
    public ConfiguracionSistemaResponseDTO actualizarVentana(@Valid @RequestBody ActualizarVentanaCommand comando) {
        return configurarVentanaUseCase.ejecutar(comando.semanas());
    }

    @PostMapping("/periodos")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodoDisponibilidadResponseDTO configurarPeriodo(@Valid @RequestBody ConfigurarPeriodoCommand comando) {
        return configurarPeriodoUseCase.ejecutar(comando);
    }

    @GetMapping("/periodos")
    public List<PeriodoDisponibilidadResponseDTO> listarPeriodos(@RequestParam Long medicoId) {
        return consultarPeriodosUseCase.porMedico(medicoId);
    }

    @GetMapping("/slots")
    public List<SlotDisponibleDTO> slotsPorFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return consultarDisponibilidadUseCase.ejecutar(medicoId, fecha);
    }

    @GetMapping("/slots/rango")
    public List<SlotDisponibleDTO> slotsPorRango(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return consultarDisponibilidadUseCase.ejecutarPorRango(medicoId, desde, hasta);
    }
}
