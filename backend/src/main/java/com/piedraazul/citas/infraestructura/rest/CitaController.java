package com.piedraazul.citas.infraestructura.rest;

import com.piedraazul.citas.aplicacion.puertos.entrada.ConsultarCitasPorCedulaUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.ExportarCitasCsvUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.aplicacion.puertos.entrada.ListarCitasPorMedicoUseCase;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.HistorialCitasPorCedulaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ListadoCitasResponseDTO;
import com.piedraazul.citas.infraestructura.dto.MarcarAtendidaCommand;
import com.piedraazul.citas.infraestructura.dto.ReagendarCitaCommand;
import com.piedraazul.identidad.infraestructura.security.SesionActual;
import com.piedraazul.nucleo.dominio.RolUsuario;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * API del módulo Citas. Roles en SecurityConfig; propiedad (✔*) vía {@link SesionActual}.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final GestionarCitaUseCase gestionarCitaUseCase;
    private final ListarCitasPorMedicoUseCase listarCitasPorMedicoUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;
    private final ExportarCitasCsvUseCase exportarCitasCsvUseCase;
    private final ConsultarCitasPorCedulaUseCase consultarCitasPorCedulaUseCase;
    private final SesionActual sesion;

    public CitaController(
            GestionarCitaUseCase gestionarCitaUseCase,
            ListarCitasPorMedicoUseCase listarCitasPorMedicoUseCase,
            ConsultarHistorialUseCase consultarHistorialUseCase,
            ExportarCitasCsvUseCase exportarCitasCsvUseCase,
            ConsultarCitasPorCedulaUseCase consultarCitasPorCedulaUseCase,
            SesionActual sesion
    ) {
        this.gestionarCitaUseCase = gestionarCitaUseCase;
        this.listarCitasPorMedicoUseCase = listarCitasPorMedicoUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
        this.exportarCitasCsvUseCase = exportarCitasCsvUseCase;
        this.consultarCitasPorCedulaUseCase = consultarCitasPorCedulaUseCase;
        this.sesion = sesion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponseDTO agendar(@Valid @RequestBody AgendarCitaCommand comando) {
        sesion.siEsPacienteExigirPersona(comando.pacienteId());
        return gestionarCitaUseCase.agendar(comando);
    }

    @PutMapping("/{citaId}/cancelacion")
    public CitaResponseDTO cancelar(@PathVariable Long citaId) {
        asegurarPacienteDueñoSiAplica(citaId);
        return gestionarCitaUseCase.cancelar(citaId);
    }

    @PutMapping("/{citaId}/reagendamiento")
    public CitaResponseDTO reagendar(
            @PathVariable Long citaId,
            @Valid @RequestBody ReagendarCitaCommand comando
    ) {
        asegurarPacienteDueñoSiAplica(citaId);
        return gestionarCitaUseCase.reagendar(citaId, comando);
    }

    @PutMapping("/{citaId}/atencion")
    public ConsultaResponseDTO marcarAtendida(
            @PathVariable Long citaId,
            @Valid @RequestBody MarcarAtendidaCommand comando
    ) {
        CitaResponseDTO cita = gestionarCitaUseCase.buscarPorId(citaId);
        sesion.exigirPersonaId(cita.medicoId());
        return gestionarCitaUseCase.marcarAtendida(citaId, comando.observaciones());
    }

    @GetMapping("/exportacion")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        sesion.siEsMedicoExigirPersona(medicoId);
        String csv = exportarCitasCsvUseCase.ejecutar(medicoId, fecha);
        byte[] cuerpo = csv.getBytes(StandardCharsets.UTF_8);
        String nombre = "agenda-" + medicoId + "-" + fecha + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(cuerpo);
    }

    /** RF5: historial de citas por cédula (agendador y médico; ver SecurityConfig). */
    @GetMapping("/paciente/cedula/{cedula}")
    public HistorialCitasPorCedulaResponseDTO listarPorCedula(@PathVariable String cedula) {
        return consultarCitasPorCedulaUseCase.ejecutar(cedula);
    }

    @GetMapping("/{citaId}")
    public CitaResponseDTO buscarPorId(@PathVariable Long citaId) {
        CitaResponseDTO cita = gestionarCitaUseCase.buscarPorId(citaId);
        if (sesion.tieneRol(RolUsuario.PACIENTE)) {
            sesion.exigirPersonaId(cita.pacienteId());
        }
        return cita;
    }

    @GetMapping
    public ListadoCitasResponseDTO listarPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String orden
    ) {
        sesion.siEsMedicoExigirPersona(medicoId);
        return listarCitasPorMedicoUseCase.ejecutar(medicoId, fecha, OrdenCitas.desde(orden));
    }

    @GetMapping("/rango")
    public List<CitaResponseDTO> listarPorRango(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        sesion.siEsMedicoExigirPersona(medicoId);
        return listarCitasPorMedicoUseCase.ejecutarPorRango(medicoId, desde, hasta);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<CitaResponseDTO> listarPorPaciente(@PathVariable Long pacienteId) {
        sesion.siEsPacienteExigirPersona(pacienteId);
        return gestionarCitaUseCase.listarPorPaciente(pacienteId);
    }

    @GetMapping("/historial/paciente/{pacienteId}")
    public List<ConsultaResponseDTO> historialPorPaciente(@PathVariable Long pacienteId) {
        sesion.siEsPacienteExigirPersona(pacienteId);
        return consultarHistorialUseCase.porPaciente(pacienteId);
    }

    @GetMapping("/historial/medico/{medicoId}")
    public List<ConsultaResponseDTO> historialPorMedico(@PathVariable Long medicoId) {
        sesion.siEsMedicoExigirPersona(medicoId);
        return consultarHistorialUseCase.porMedico(medicoId);
    }

    private void asegurarPacienteDueñoSiAplica(Long citaId) {
        if (!sesion.tieneRol(RolUsuario.PACIENTE)) {
            return;
        }
        CitaResponseDTO cita = gestionarCitaUseCase.buscarPorId(citaId);
        if (!Objects.equals(sesion.personaId(), cita.pacienteId())) {
            throw new AccessDeniedException("No puedes modificar la cita de otro paciente");
        }
    }
}
