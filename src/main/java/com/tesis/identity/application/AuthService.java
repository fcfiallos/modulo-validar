package com.tesis.identity.application;

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

//    --- REGRISTRO ---
    @Transactional
    public void registerUser(UserEntity user, String rawPassword) {
        // 1. Validar aceptación de términos
        if (!user.isAceptaTerminosPlataforma()) {
            throw new RuntimeException("Debe aceptar los terminos y condiciones para registrarse.");
        }

        // 2. Validar identidad
        JsonObject jsonBody = Json.createObjectBuilder()
                .add("cedula", user.getCedula())
                .add("name", user.getNombres())
                .add("surname", user.getApellidos())
                .build();

        if (!"1".equals(identityClient.validate(jsonBody))) {
            throw new RuntimeException("La identidad no pudo ser verificada con el Registro Civil.");
        }

        // 3. Hashing de contraseña
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)));
        user.persist();
    }

    // --- LOGIN ---
    public UserEntity login(String correo, String password) {
        // Buscar por correo
        Optional<UserEntity> userOpt = UserEntity.find("correo", correo).firstResultOptional();

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        UserEntity user = userOpt.get();

        // Verificar estado
        if (!user.isActivo()) {
            throw new RuntimeException("Esta cuenta ha sido desactivada.");
        }

        // Verificar password
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Contrasena incorrecta.");
        }

        return user;
    }
}