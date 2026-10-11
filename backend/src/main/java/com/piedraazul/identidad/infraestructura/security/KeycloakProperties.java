package com.piedraazul.identidad.infraestructura.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String issuerUri,
        String jwkSetUri,
        String realm,
        String clientId,
        String clientSecret,
        String adminUsername,
        String adminPassword
) {
    public String tokenUri() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String adminUsersUri() {
        return serverUrl + "/admin/realms/" + realm + "/users";
    }

    public String adminRoleUri(String roleName) {
        return serverUrl + "/admin/realms/" + realm + "/roles/" + roleName;
    }

    public String adminUserRoleMappingUri(String userId) {
        return serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
    }

    public String adminUserUri(String userId) {
        return serverUrl + "/admin/realms/" + realm + "/users/" + userId;
    }
}
