package com.piedraazul.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre por HTTP el flujo completo de los tres requisitos funcionales:
 * el administrador configura (RF3), el paciente ve franjas y agenda (RF2) y el
 * agendador lista las citas del día con su cantidad (RF1).
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
class FlujoAgendamientoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private Long medicoId;
    private Long pacienteId;
    private LocalDate fecha;

    @BeforeEach
    void prepararCatalogo() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        Long especialidadId = idDe(mvc.perform(post("/api/personas/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Especialidad " + sufijo + "\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        medicoId = idDe(mvc.perform(post("/api/personas/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Dra. Prueba " + sufijo + "\",\"especialidadId\":"
                                + especialidadId + "}"))
                .andExpect(status().isCreated())
                .andReturn());

        pacienteId = idDe(mvc.perform(post("/api/personas/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Paciente " + sufijo + "\",\"telefono\":\"3001112233\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCompleto").value("Paciente " + sufijo))
                .andReturn());

        // Tres días adelante: dentro de la ventana y sin el filtro de "franjas ya pasadas".
        fecha = LocalDate.now().plusDays(3);
    }

    @Test
    @DisplayName("RF3 configura, RF2 agenda sobre franjas reales y RF1 lista con cantidad y orden")
    void flujoCompleto() throws Exception {
        configurarVentana(4);
        configurarHorarioTodosLosDias(LocalDate.now(), "08:00", "12:00", 30, 0);

        assertEquals(8, cantidadDeSlots(), "08:00 a 12:00 en pasos de 30 min son 8 franjas");

        Long primeraCita = idDe(agendar("08:00", "08:30").andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.medicoId").value(medicoId))
                .andReturn());

        assertEquals(7, cantidadDeSlots(), "la franja agendada deja de ofrecerse");

        agendar("08:00", "08:30")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SLOT_NO_DISPONIBLE"));

        agendar("08:15", "08:45")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SLOT_NO_DISPONIBLE"));

        Long segundaCita = idDe(agendar("09:00", "09:30").andExpect(status().isCreated()).andReturn());

        mvc.perform(get("/api/citas")
                        .param("medicoId", String.valueOf(medicoId))
                        .param("fecha", fecha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.cantidadProgramadas").value(2))
                .andExpect(jsonPath("$.cantidadCanceladas").value(0))
                .andExpect(jsonPath("$.orden").value("HORA_ASC"))
                .andExpect(jsonPath("$.citas[0].horaInicio").value("08:00"))
                .andExpect(jsonPath("$.citas[0].pacienteNombre").isString());

        mvc.perform(get("/api/citas")
                        .param("medicoId", String.valueOf(medicoId))
                        .param("fecha", fecha.toString())
                        .param("orden", "HORA_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orden").value("HORA_DESC"))
                .andExpect(jsonPath("$.citas[0].horaInicio").value("09:00"));

        mvc.perform(put("/api/citas/" + primeraCita + "/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        assertEquals(7, cantidadDeSlots(), "cancelar libera la franja de las 08:00 pero sigue ocupada la de las 09:00");

        mvc.perform(put("/api/citas/" + segundaCita + "/atencion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observaciones\":\"Paciente estable\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citaId").value(segundaCita))
                .andExpect(jsonPath("$.observaciones").value("Paciente estable"));

        mvc.perform(get("/api/citas/historial/paciente/" + pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].citaId").value(segundaCita));

        mvc.perform(put("/api/citas/" + segundaCita + "/cancelacion"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CITA_NO_MODIFICABLE"));
    }

    @Test
    @DisplayName("el descanso entre citas reduce la cantidad de franjas ofrecidas")
    void descansoEntreCitasSeparaLasFranjas() throws Exception {
        configurarVentana(4);
        configurarHorarioTodosLosDias(LocalDate.now(), "14:00", "18:00", 45, 15);

        assertEquals(4, cantidadDeSlots(), "45 + 15 minutos de paso caben 4 veces en 4 horas");

        agendar("14:00", "14:45").andExpect(status().isCreated());
        // 14:45 cae en el descanso, no es un inicio válido de la rejilla
        agendar("14:45", "15:30")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SLOT_NO_DISPONIBLE"));
    }

    @Test
    @DisplayName("registrar un horario nuevo cierra automáticamente el anterior")
    void elHorarioNuevoCierraElAnterior() throws Exception {
        LocalDate inicioViejo = LocalDate.now();
        LocalDate inicioNuevo = inicioViejo.plusDays(10);

        configurarVentana(6);
        configurarHorarioTodosLosDias(inicioViejo, "08:00", "12:00", 30, 0);
        configurarHorarioTodosLosDias(inicioNuevo, "15:00", "18:00", 60, 0);

        MvcResult resultado = mvc.perform(get("/api/disponibilidad/periodos")
                        .param("medicoId", String.valueOf(medicoId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode periodos = leer(resultado);
        assertEquals(2, periodos.size());
        // Vienen del más reciente al más antiguo
        assertTrue(periodos.get(0).get("fechaFin").isNull(), "el periodo nuevo queda abierto");
        assertEquals(inicioNuevo.toString(), periodos.get(0).get("fechaInicio").asText());
        assertEquals(inicioViejo.plusDays(9).toString(), periodos.get(1).get("fechaFin").asText());
    }

    @Test
    @DisplayName("no se puede agendar más allá de la ventana configurada")
    void rechazaFechaFueraDeLaVentana() throws Exception {
        configurarVentana(1);
        configurarHorarioTodosLosDias(LocalDate.now(), "08:00", "12:00", 30, 0);

        fecha = LocalDate.now().plusWeeks(5);

        agendar("08:00", "08:30")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FUERA_DE_VENTANA_AGENDAMIENTO"));

        mvc.perform(get("/api/disponibilidad/slots")
                        .param("medicoId", String.valueOf(medicoId))
                        .param("fecha", fecha.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FUERA_DE_VENTANA_AGENDAMIENTO"));
    }

    @Test
    @DisplayName("la validación de formulario responde con el detalle por campo")
    void validaElCuerpoDeLaPeticion() throws Exception {
        mvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicoId\":null,\"pacienteId\":null,\"fecha\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.extra.medicoId").exists());
    }

    @Test
    @DisplayName("RF2 exige registro de paciente con nombre y teléfono válidos")
    void rechazaRegistroDePacienteIncompleto() throws Exception {
        mvc.perform(post("/api/personas/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"\",\"telefono\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.extra.nombreCompleto").exists())
                .andExpect(jsonPath("$.extra.telefono").exists());
    }

    private void configurarVentana(int semanas) throws Exception {
        mvc.perform(put("/api/disponibilidad/configuracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semanas\":" + semanas + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventanaSemanas").value(semanas));
    }

    private void configurarHorarioTodosLosDias(
            LocalDate desde,
            String horaInicio,
            String horaFin,
            int duracion,
            int descanso
    ) throws Exception {
        String cuerpo = """
                {
                  "medicoId": %d,
                  "fechaInicio": "%s",
                  "diasAtencion": ["LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO","DOMINGO"],
                  "horaInicio": "%s",
                  "horaFin": "%s",
                  "duracionCitaMinutos": %d,
                  "descansoEntreCitasMinutos": %d
                }
                """.formatted(medicoId, desde, horaInicio, horaFin, duracion, descanso);

        mvc.perform(post("/api/disponibilidad/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duracionCitaMinutos").value(duracion))
                .andExpect(jsonPath("$.diasAtencion.length()").value(7));
    }

    private org.springframework.test.web.servlet.ResultActions agendar(String horaInicio, String horaFin)
            throws Exception {
        String cuerpo = """
                {
                  "pacienteId": %d,
                  "medicoId": %d,
                  "fecha": "%s",
                  "horaInicio": "%s",
                  "horaFin": "%s"
                }
                """.formatted(pacienteId, medicoId, fecha, horaInicio, horaFin);

        return mvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private int cantidadDeSlots() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/disponibilidad/slots")
                        .param("medicoId", String.valueOf(medicoId))
                        .param("fecha", fecha.toString()))
                .andExpect(status().isOk())
                .andReturn();
        return leer(resultado).size();
    }

    private JsonNode leer(MvcResult resultado) throws Exception {
        return json.readTree(resultado.getResponse().getContentAsString());
    }

    private Long idDe(MvcResult resultado) throws Exception {
        return leer(resultado).get("id").asLong();
    }
}
