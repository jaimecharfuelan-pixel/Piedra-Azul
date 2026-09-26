package com.piedraazul.nucleo.infraestructura.rest;

import com.piedraazul.nucleo.dominio.excepciones.DomainException;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Mapea excepciones del núcleo común a respuestas HTTP.
 * Los códigos de {@link ReglaDeNegocioException} confirman el status (409 vs 400).
 */
@RestControllerAdvice
public class NucleoExceptionHandler {

    private static final Set<String> CONFLICTO = Set.of(
            "SLOT_NO_DISPONIBLE",
            "CITA_NO_MODIFICABLE",
            "USERNAME_REPETIDO",
            "MEDICO_INACTIVO"
    );

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex) {
        return problem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ProblemDetail handleRegla(ReglaDeNegocioException ex) {
        HttpStatus status = CONFLICTO.contains(ex.getCodigo())
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
        return problem(status, ex);
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
