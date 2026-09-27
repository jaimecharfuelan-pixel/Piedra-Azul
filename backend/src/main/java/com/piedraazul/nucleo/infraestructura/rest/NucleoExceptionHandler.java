package com.piedraazul.nucleo.infraestructura.rest;

import com.piedraazul.nucleo.dominio.excepciones.DomainException;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mapea excepciones del núcleo común a respuestas HTTP (RFC 7807 ProblemDetail).
 * Los códigos de {@link ReglaDeNegocioException} confirman el status (409 vs 400).
 */
@RestControllerAdvice
public class NucleoExceptionHandler {

    /**
     * Códigos que representan un choque con el estado actual del sistema, no una
     * petición mal formada: se responden como 409 CONFLICT.
     */
    private static final Set<String> CONFLICTO = Set.of(
            "SLOT_NO_DISPONIBLE",
            "CITA_NO_MODIFICABLE",
            "USERNAME_REPETIDO",
            "USERNAME_YA_REGISTRADO",
            "MEDICO_INACTIVO",
            "MEDICO_NO_DISPONIBLE",
            "ESPECIALIDAD_INACTIVA",
            "PERIODO_SOLAPADO",
            "FUERA_DE_VENTANA_AGENDAMIENTO"
    );

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex) {
        return problem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccesoDenegado(org.springframework.security.access.AccessDeniedException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage() == null ? "No tienes permiso para esta operación" : ex.getMessage()
        );
        detail.setTitle("ACCESO_DENEGADO");
        detail.setProperty("codigo", "ACCESO_DENEGADO");
        detail.setProperty("timestamp", Instant.now().toString());
        detail.setProperty("extra", Map.of());
        return detail;
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ProblemDetail handleRegla(ReglaDeNegocioException ex) {
        HttpStatus status = CONFLICTO.contains(ex.getCodigo())
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
        return problem(status, ex);
    }

    /** Errores de validación Bean Validation: detalle por campo en {@code extra}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Hay campos inválidos en la solicitud"
        );
        detail.setTitle("DATOS_INVALIDOS");
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        detail.setProperty("timestamp", Instant.now().toString());
        detail.setProperty("extra", errores);
        return detail;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleFormato(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "No se pudo interpretar la solicitud: revisa el formato de fechas (yyyy-MM-dd) y horas (HH:mm)"
        );
        detail.setTitle("FORMATO_INVALIDO");
        detail.setProperty("codigo", "FORMATO_INVALIDO");
        detail.setProperty("timestamp", Instant.now().toString());
        detail.setProperty("extra", Map.of());
        return detail;
    }

    private ProblemDetail problem(HttpStatus status, DomainException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setTitle(ex.getCodigo());
        detail.setProperty("codigo", ex.getCodigo());
        detail.setProperty("timestamp", Instant.now().toString());
        detail.setProperty("extra", Map.of());
        return detail;
    }
}
