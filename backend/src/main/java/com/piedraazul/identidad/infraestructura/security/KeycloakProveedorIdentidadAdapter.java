package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.identidad.aplicacion.puertos.salida.ProveedorIdentidadPort;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Autenticación y provisión de usuarios contra Keycloak (RNF1).
 */
@Component
@ConditionalOnProperty(name = "app.security.provider", havingValue = "keycloak")
public class KeycloakProveedorIdentidadAdapter implements ProveedorIdentidadPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakProveedorIdentidadAdapter.class);

    private final KeycloakProperties properties;
    private final RestClient restClient;

    public KeycloakProveedorIdentidadAdapter(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TokenProveedor autenticar(String username, String passwordPlano) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
            form.add("client_secret", properties.clientSecret());
        }
        form.add("username", username);
        form.add("password", passwordPlano);

        try {
            Map<String, Object> body = restClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("access_token") == null) {
                throw credencialesInvalidas();
            }
            String token = String.valueOf(body.get("access_token"));
            String type = body.get("token_type") == null ? "Bearer" : String.valueOf(body.get("token_type"));
            long expires = body.get("expires_in") instanceof Number n ? n.longValue() : 300L;
            return new TokenProveedor(token, type, expires);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest ex) {
            throw credencialesInvalidas();
        }
    }

    @Override
    public void asegurarUsuario(String username, String passwordPlano, RolUsuario rol) {
        String normalizado = username.trim().toLowerCase(Locale.ROOT);
        String adminToken = tokenAdmin();
        String existente = buscarUserId(adminToken, normalizado);
        if (existente != null) {
            asignarRol(adminToken, existente, rol);
            return;
        }

        Map<String, Object> representation = Map.of(
                "username", normalizado,
                "enabled", true,
                "email", normalizado + "@piedraazul.local",
                "emailVerified", true,
                "firstName", normalizado,
                "lastName", rol.name(),
                "requiredActions", List.of(),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", passwordPlano,
                        "temporary", false
                ))
        );

        try {
            ResponseEntity<Void> created = restClient.post()
                    .uri(properties.adminUsersUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(representation)
                    .retrieve()
                    .toBodilessEntity();
            String location = created.getHeaders().getFirst(HttpHeaders.LOCATION);
            String userId = location == null ? buscarUserId(adminToken, normalizado) : location.substring(location.lastIndexOf('/') + 1);
            if (userId != null) {
                asignarRol(adminToken, userId, rol);
            }
        } catch (HttpClientErrorException.Conflict ex) {
            String userId = buscarUserId(adminToken, normalizado);
            if (userId != null) {
                asignarRol(adminToken, userId, rol);
            }
        } catch (RuntimeException ex) {
            log.warn("No se pudo sincronizar usuario {} en Keycloak: {}", normalizado, ex.getMessage());
            throw ReglaDeNegocioException.de(
                    "KEYCLOAK_USUARIO",
                    "No se pudo registrar el usuario en Keycloak: " + ex.getMessage()
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sincronizarEstado(String username, boolean activo) {
        String adminToken = tokenAdmin();
        String userId = buscarUserId(adminToken, username.trim().toLowerCase(Locale.ROOT));
        if (userId == null) {
            return;
        }
        Map<String, Object> actual = restClient.get()
                .uri(properties.adminUserUri(userId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);
        if (actual == null) {
            return;
        }
        actual.put("enabled", activo);
        restClient.put()
                .uri(properties.adminUserUri(userId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(actual)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private String tokenAdmin() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", properties.adminUsername());
        form.add("password", properties.adminPassword());

        Map<String, Object> body = restClient.post()
                .uri(properties.serverUrl() + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
        if (body == null || body.get("access_token") == null) {
            throw ReglaDeNegocioException.de("KEYCLOAK_ADMIN", "No se pudo autenticar el admin de Keycloak");
        }
        return String.valueOf(body.get("access_token"));
    }

    @SuppressWarnings("unchecked")
    private String buscarUserId(String adminToken, String username) {
        List<Map<String, Object>> users = restClient.get()
                .uri(properties.adminUsersUri() + "?username={u}&exact=true", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(List.class);
        if (users == null || users.isEmpty()) {
            return null;
        }
        Object id = users.get(0).get("id");
        return id == null ? null : String.valueOf(id);
    }

    @SuppressWarnings("unchecked")
    private void asignarRol(String adminToken, String userId, RolUsuario rol) {
        Map<String, Object> role = restClient.get()
                .uri(properties.adminRoleUri(rol.name()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);
        if (role == null || role.get("id") == null) {
            log.warn("Rol {} no existe en Keycloak", rol.name());
            return;
        }
        restClient.post()
                .uri(properties.adminUserRoleMappingUri(userId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(Map.of(
                        "id", role.get("id"),
                        "name", role.get("name")
                )))
                .retrieve()
                .toBodilessEntity();
    }

    private ReglaDeNegocioException credencialesInvalidas() {
        return ReglaDeNegocioException.de(
                "CREDENCIALES_INVALIDAS",
                "Usuario o contraseña incorrectos"
        );
    }
}
