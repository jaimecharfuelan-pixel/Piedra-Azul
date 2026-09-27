package com.piedraazul.identidad.infraestructura.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Matriz de roles acordada: roles en Identidad; propiedad (✔*) en controllers vía {@link SesionActual}.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String AGENDADOR = "AGENDADOR";
    private static final String MEDICO = "MEDICO";
    private static final String PACIENTE = "PACIENTE";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/registro-paciente",
                                "/actuator/health",
                                "/h2-console/**"
                        ).permitAll()

                        // Identidad — gestión de usuarios
                        .requestMatchers("/api/usuarios/**").hasRole(ADMIN)

                        // Especialidades
                        .requestMatchers(HttpMethod.POST, "/api/personas/especialidades").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/personas/especialidades/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/personas/especialidades/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)

                        // Médicos
                        .requestMatchers(HttpMethod.POST, "/api/personas/medicos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/personas/medicos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/personas/medicos/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)

                        // Pacientes (médico también registra walk-ins)
                        .requestMatchers(HttpMethod.POST, "/api/personas/pacientes")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/personas/pacientes")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/personas/pacientes/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)

                        // Disponibilidad
                        .requestMatchers(HttpMethod.PUT, "/api/disponibilidad/configuracion").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/disponibilidad/periodos")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/disponibilidad/periodos")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/disponibilidad/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)

                        // Citas — atender solo médico
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/atencion").hasRole(MEDICO)
                        .requestMatchers(HttpMethod.POST, "/api/citas")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/cancelacion")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/reagendamiento")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/citas/historial/paciente/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/citas/historial/medico/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/citas/paciente/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/citas")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/citas/rango")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO)
                        .requestMatchers(HttpMethod.GET, "/api/citas/**")
                        .hasAnyRole(ADMIN, AGENDADOR, MEDICO, PACIENTE)

                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
