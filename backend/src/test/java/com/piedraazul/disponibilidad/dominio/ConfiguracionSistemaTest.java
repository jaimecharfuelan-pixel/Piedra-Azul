package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracionSistemaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 2);

    @Test
    @DisplayName("la configuración por defecto trae una ventana usable")
    void valorPorDefecto() {
        ConfiguracionSistema config = ConfiguracionSistema.porDefecto();

        assertEquals(ConfiguracionSistema.VENTANA_POR_DEFECTO_SEMANAS, config.getVentanaSemanas());
        assertEquals(ConfiguracionSistema.ID_UNICO, config.getId());
    }

    @Test
    @DisplayName("la ventana se puede cambiar dentro del rango permitido")
    void actualizarVentana() {
        ConfiguracionSistema config = ConfiguracionSistema.porDefecto();

        config.actualizarVentana(8);

        assertEquals(8, config.getVentanaSemanas());
        assertEquals(HOY.plusWeeks(8), config.ultimaFechaAgendable(HOY));
    }

    @Test
    @DisplayName("rechaza ventanas fuera del rango permitido")
    void rechazaVentanaInvalida() {
        ConfiguracionSistema config = ConfiguracionSistema.porDefecto();

        assertEquals("VENTANA_AGENDAMIENTO_INVALIDA",
                assertThrows(ReglaDeNegocioException.class, () -> config.actualizarVentana(0)).getCodigo());
        assertEquals("VENTANA_AGENDAMIENTO_INVALIDA",
                assertThrows(ReglaDeNegocioException.class, () -> config.actualizarVentana(53)).getCodigo());
    }

    @Test
    @DisplayName("sólo permite agendar entre hoy y el final de la ventana")
    void permiteAgendarSoloDentroDeLaVentana() {
        ConfiguracionSistema config = ConfiguracionSistema.porDefecto();
        config.actualizarVentana(2);

        assertTrue(config.permiteAgendarEn(HOY, HOY));
        assertTrue(config.permiteAgendarEn(HOY.plusWeeks(2), HOY));
        assertFalse(config.permiteAgendarEn(HOY.minusDays(1), HOY));
        assertFalse(config.permiteAgendarEn(HOY.plusWeeks(2).plusDays(1), HOY));
    }
}
