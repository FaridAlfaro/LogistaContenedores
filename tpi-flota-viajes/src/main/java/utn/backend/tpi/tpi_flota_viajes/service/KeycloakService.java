package utn.backend.tpi.tpi_flota_viajes.service;

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
import utn.backend.tpi.tpi_flota_viajes.exception.BadRequestException;
import utn.backend.tpi.tpi_flota_viajes.exception.ConflictException;

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

    public void crearUsuario(String username, String password, String rol) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 1. Verificar existencia previa
        List<UserRepresentation> existentes = usersResource.searchByUsername(username, true);
        if (!existentes.isEmpty()) {
            throw new ConflictException("El usuario ya existe en Keycloak: " + username);
        }

        // 2. Preparar usuario
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(username); // Usamos username como email
        user.setFirstName("Transportista");
        user.setLastName(username);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());

        // 3. Intentar crear
        try (Response response = usersResource.create(user)) {

            if (response.getStatus() == 201) {
                String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);

                // Asignar Password
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setTemporary(false);
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setValue(password);
                usersResource.get(userId).resetPassword(cred);

                // Asignar Rol
                try {
                    System.out.println("Intentando asignar rol: " + rol + " al usuario: " + userId);
                    RoleRepresentation roleRepresentation = realmResource.roles().get(rol).toRepresentation();
                    System.out.println("Rol encontrado: " + roleRepresentation.getName());

                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
                    System.out.println("Rol asignado exitosamente.");
                } catch (jakarta.ws.rs.ForbiddenException e) {
                    System.err.println("ERROR 403: No tiene permisos para asignar roles.");
                    throw new RuntimeException(
                            "ERROR 403: El backend no tiene permisos para asignar roles. Verifique 'Service Account Roles' en Keycloak.",
                            e);
                } catch (jakarta.ws.rs.NotFoundException e) {
                    System.err.println("ERROR 404: El rol '" + rol + "' no existe en Keycloak.");
                    throw new RuntimeException("ERROR 404: El rol '" + rol + "' no existe en el realm configurado.", e);
                } catch (Exception e) {
                    System.err.println("ERROR al asignar rol: " + e.getMessage());
                    throw e;
                }

                // 4. FORZAR UPDATE para asegurar que emailVerified y requiredActions se
                // persistan
                UserRepresentation userUpdate = usersResource.get(userId).toRepresentation();
                userUpdate.setEmailVerified(true);
                userUpdate.setEnabled(true);
                userUpdate.setRequiredActions(Collections.emptyList());
                usersResource.get(userId).update(userUpdate);

            } else if (response.getStatus() == 409) {
                throw new ConflictException("El usuario ya existe en Keycloak (Conflicto 409).");
            } else {
                // Capturar otros errores (400 Bad Request, etc.)
                throw new BadRequestException("Error al crear usuario en Keycloak. Status: " + response.getStatus()
                        + " - " + response.getStatusInfo());
            }
        }
    }
}