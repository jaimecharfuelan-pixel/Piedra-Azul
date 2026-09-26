package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;

/**
 * Puerto público consumido por Identidad al crear Usuario MEDICO o PACIENTE.
 */
public interface RegistrarPersonaPort {
    Long crearPersonaParaUsuario(RolUsuario rol, DatosPersonaDTO datos);
}
