package com.tesis.identity.application;

import com.tesis.identity.infrastructure.client.SignatureClient;
import com.tesis.identity.infrastructure.persistence.UserEntity;
import com.tesis.identity.infrastructure.client.IdentityClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.extern.java.Log;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

@Log
@ApplicationScoped
public class AuthService {

    @Inject
    @RestClient
    IdentityClient identityClient;
    @Inject @RestClient
    SignatureClient signatureClient;

//    --- REGRISTRO ---
    @Transactional
    public void registerUser(UserEntity user, String rawPassword, String p12Base64, String p12Password) {

        // 1. Validar términos
        if (!user.isAceptaTerminosPlataforma())
            throw new RuntimeException("Debe aceptar los términos.");

        // 2. VALIDACIÓN LEGAL (API Registro Civil)
        JsonObject idBody = Json.createObjectBuilder()
                .add("cedula", user.getCedula())
                .add("name", user.getNombres())
                .add("surname", user.getApellidos())
                .build();
        if (!"1".equals(identityClient.validate(idBody)))
            throw new RuntimeException("Identidad no confirmada por el Registro Civil.");

        // 3. VALIDACIÓN FORENSE DE LA FIRMA (API Validadora)
        JsonObject sigBody = Json.createObjectBuilder()
                .add("p12Base64", p12Base64)
                .add("password", p12Password)
                .add("cedula", user.getCedula())
                .build();
        if (!"1".equals(signatureClient.validateSignature(sigBody)))
            throw new RuntimeException("La firma subida no pertenece a su número de cédula.");

        // 4. GUARDADO Y CUSTODIA (Estilo SRI)
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)));
        user.setFirmaP12(p12Base64);
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

    @Transactional
    public JsonObject processWorkSignature(String cedula, String p12Password, String hashObra) {
        // 1. Recuperar al usuario de la BD usando la cédula
        UserEntity user = UserEntity.find("cedula", cedula).firstResult();

        if (user == null) {
            throw new RuntimeException("Usuario no encontrado en el sistema.");
        }

        // 2. Extraer el p12Base64 (que ahora es TEXT real, no un número)
        String p12Stored = user.getFirmaP12();

        // 3. Construir el JSON para la Azure Function
        JsonObject jsonToAzure = Json.createObjectBuilder()
                .add("p12Base64", p12Stored)
                .add("password", p12Password)
                .add("hashObra", hashObra)
                .build();

        // 4. Llamar a la Azure Function y retornar la Firma Digital
        String msg = "Enviando material criptográfico a Azure para firma de obra de: { "+ cedula + " }";
        log.info(msg);
        return signatureClient.signWork(jsonToAzure);
    }
}