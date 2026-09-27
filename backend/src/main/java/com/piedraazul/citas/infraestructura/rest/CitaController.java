package com.piedraazul.citas.infraestructura.rest;

import com.piedraazul.citas.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.ListarCitasPorMedicoUseCase;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ListadoCitasResponseDTO;
import com.piedraazul.citas.infraestructura.dto.MarcarAtendidaCommand;
import com.piedraazul.citas.infraestructura.dto.ReagendarCitaCommand;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * API del módulo Citas (RF1 listado del agendador, RF2 agendamiento del paciente
 * y ciclo de vida completo de la cita).
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final GestionarCitaUseCase gestionarCitaUseCase;
    private final ListarCitasPorMedicoUseCase listarCitasPorMedicoUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;

    public CitaController(
            GestionarCitaUseCase gestionarCitaUseCase,
            ListarCitasPorMedicoUseCase listarCitasPorMedicoUseCase,
            ConsultarHistorialUseCase consultarHistorialUseCase
    ) {
        this.gestionarCitaUseCase = gestionarCitaUseCase;
        this.listarCitasPorMedicoUseCase = listarCitasPorMedicoUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponseDTO agendar(@Valid @RequestBody AgendarCitaCommand comando) {
        return gestionarCitaUseCase.agendar(comando);
    }

    @PutMapping("/{citaId}/cancelacion")
    public CitaResponseDTO cancelar(@PathVariable Long citaId) {
        return gestionarCitaUseCase.cancelar(citaId);
    }

    @PutMapping("/{citaId}/reagendamiento")
    public CitaResponseDTO reagendar(
            @PathVariable Long citaId,
            @Valid @RequestBody ReagendarCitaCommand comando
    ) {
        return gestionarCitaUseCase.reagendar(citaId, comando);
    }

    @PutMapping("/{citaId}/atencion")
    public ConsultaResponseDTO marcarAtendida(
            @PathVariable Long citaId,
            @Valid @RequestBody MarcarAtendidaCommand comando
    ) {
        return gestionarCitaUseCase.marcarAtendida(citaId, comando.observaciones());
    }

    @GetMapping("/{citaId}")
    public CitaResponseDTO buscarPorId(@PathVariable Long citaId) {
        return gestionarCitaUseCase.buscarPorId(citaId);
    }

    /** RF1: listado con cantidad y orden configurable. */
    @GetMapping
    public ListadoCitasResponseDTO listarPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String orden
    ) {
        return listarCitasPorMedicoUseCase.ejecutar(medicoId, fecha, OrdenCitas.desde(orden));
    }

    @GetMapping("/rango")
    public List<CitaResponseDTO> listarPorRango(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return listarCitasPorMedicoUseCase.ejecutarPorRango(medicoId, desde, hasta);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<CitaResponseDTO> listarPorPaciente(@PathVariable Long pacienteId) {
        return gestionarCitaUseCase.listarPorPaciente(pacienteId);
    }

    @GetMapping("/historial/paciente/{pacienteId}")
    public List<ConsultaResponseDTO> historialPorPaciente(@PathVariable Long pacienteId) {
        return consultarHistorialUseCase.porPaciente(pacienteId);
    }

    @GetMapping("/historial/medico/{medicoId}")
    public List<ConsultaResponseDTO> historialPorMedico(@PathVariable Long medicoId) {
        return consultarHistorialUseCase.porMedico(medicoId);
    }
}
