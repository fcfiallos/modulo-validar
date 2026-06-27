package com.tesis.identity.application.service;

import com.tesis.identity.infrastructure.persistence.UserEntity;
import com.tesis.identity.infrastructure.client.IdentityClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    @RestClient
    IdentityClient identityClient;

    @Transactional
    public void registerUser(UserEntity user, String rawPassword) {
        // 1. Validar identidad con Python (Azure)
        JsonObject jsonBody = Json.createObjectBuilder()
                .add("cedula", user.getCedula())
                .add("name", user.getNombres())
                .add("surname", user.getApellidos())
                .build();

        String result = identityClient.validate(jsonBody);

        if (!"1".equals(result)) {
            throw new RuntimeException("La identidad no pudo ser verificada.");
        }

        // 2. Seguridad: Hashing de contraseña (Requerimiento forense)
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)));
        user.persist();
    }

    public Optional<UserEntity> login(String correo, String password) {
        Optional<UserEntity> user = UserEntity.find("correo", correo).firstResultOptional();
        if (user.isPresent() && BCrypt.checkpw(password, user.get().getPasswordHash())) {
            return user;
        }
        return Optional.empty();
    }
}