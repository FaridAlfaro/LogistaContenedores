package com.transporte.ms_solicitudes.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public void crearUsuario(String email, String password, String rol) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 1. Validar si existe
        List<UserRepresentation> existentes = usersResource.searchByUsername(email, true);
        if (!existentes.isEmpty()) {
            // Usuario ya existe, retornamos
            return;
        }

        // 2. Crear Usuario
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);
        // AÑADIDO: Indicar explícitamente que no hay acciones requeridas
        user.setRequiredActions(Collections.emptyList());

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);

                // 3. Password
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setTemporary(false);
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setValue(password);
                usersResource.get(userId).resetPassword(cred);

                // 4. Asignar Rol
                try {
                    RoleRepresentation roleRep = realmResource.roles().get(rol).toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRep));

                    // 5. GARANTÍA DE ESTADO: Forzar la actualización
                    // Esto asegura que RequiredActions se persistan como vacío
                    usersResource.get(userId).update(user);

                } catch (jakarta.ws.rs.NotFoundException e) {
                    throw new RuntimeException(
                            "El rol '" + rol + "' no existe en Keycloak. Verifique mayúsculas/minúsculas.");
                } catch (jakarta.ws.rs.ForbiddenException e) {
                    throw new RuntimeException(
                            "ERROR PERMISOS KEYCLOAK (403): El Service Account no tiene permiso para ver/asignar roles. "
                                    +
                                    "Asigne 'view-realm', 'view-users' y 'manage-users' del cliente 'realm-management' al Service Account.");
                }

            } else if (response.getStatus() == 409) {
                // Conflicto (ya existe), lo ignoramos o lanzamos excepción
            } else {
                throw new RuntimeException("Error creando usuario en Keycloak. Status: " + response.getStatus());
            }
        }
    }
}