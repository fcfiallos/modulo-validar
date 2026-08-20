package com.tesis.identity.application;

import com.tesis.identity.application.dto.LoginRequest;
import com.tesis.identity.application.dto.RegisterUserRequest;
import com.tesis.identity.application.dto.UserResponse;
import com.tesis.identity.application.dto.WorkSignatureRequest;
import com.tesis.identity.application.mapper.UserMapper;
import com.tesis.identity.application.ports.EncryptionPort;
import com.tesis.identity.application.ports.UserRepositoryPort;
import com.tesis.identity.domain.models.User;
import com.tesis.identity.infrastructure.client.IdentityClient;
import com.tesis.identity.infrastructure.client.SignatureClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.extern.java.Log;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.mindrot.jbcrypt.BCrypt;

@Log
@ApplicationScoped
public class AuthService {

    @Inject
    @RestClient
    IdentityClient identityClient;
    @Inject
    @RestClient
    SignatureClient signatureClient;

    @Inject
    EncryptionPort encryptionService;

    @Inject
    UserRepositoryPort userRepository;

    @Inject
    UserMapper userMapper;

    // --- REGISTRO ---
    public UserResponse registerUser(RegisterUserRequest request) {

        // 1. Validar términos
        if (!request.aceptaTerminos())
            throw new RuntimeException("Debe aceptar los términos.");

        // 2. VALIDACIÓN LEGAL (API Registro Civil)
        JsonObject idBody = Json.createObjectBuilder()
                .add("cedula", request.cedula())
                .add("name", request.nombres())
                .add("surname", request.apellidos())
                .build();
        if (!"1".equals(identityClient.validate(idBody)))
            throw new RuntimeException("Identidad no confirmada por el Registro Civil.");

        // 3. VALIDACIÓN FORENSE DE LA FIRMA (API Validadora)
        JsonObject sigBody = Json.createObjectBuilder()
                .add("p12Base64", request.p12Base64())
                .add("password", request.p12Password())
                .add("cedula", request.cedula())
                .build();
        if (!"1".equals(signatureClient.validateSignature(sigBody)))
            throw new RuntimeException("La firma subida no pertenece a su número de cédula.");

        log.info("Cifrando datos sensibles en HSM Azure antes de persistir...");

        // Enmascaramos nombres y firma p12 (cifrado de sobre), y hasheamos el password
        String encryptedNombres = encryptionService.encrypt(request.nombres());
        String encryptedApellidos = encryptionService.encrypt(request.apellidos());
        String encryptedFirmaP12 = encryptionService.encrypt(request.p12Base64());
        String passwordHash = BCrypt.hashpw(request.password(), BCrypt.gensalt(12));

        User newUser = new User(
                null,
                request.cedula(),
                encryptedNombres,
                encryptedApellidos,
                request.correo(),
                request.nombreArtistico(),
                passwordHash,
                encryptedFirmaP12,
                request.aceptaTerminos(),
                null,
                true
        );

        User savedUser = userRepository.save(newUser);
        return userMapper.toResponse(savedUser);
    }

    // --- LOGIN ---
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.correo())
                .orElseThrow(() -> new CredencialesIncorrectasException(
                        "Credenciales incorrectas. Verifique su correo y contraseña."));

        if (!user.activo()) {
            throw new RuntimeException("Esta cuenta ha sido desactivada.");
        }

        if (!BCrypt.checkpw(request.password(), user.passwordHash())) {
            throw new CredencialesIncorrectasException(
                    "Credenciales incorrectas. Verifique su correo y contraseña.");
        }

        // Los nombres/apellidos se guardan cifrados: los descifrarnos para la respuesta
        User userLegible = new User(
                user.id(),
                user.cedula(),
                encryptionService.decrypt(user.nombres()),
                encryptionService.decrypt(user.apellidos()),
                user.correo(),
                user.nombreArtistico(),
                user.passwordHash(),
                user.firmaP12(),
                user.aceptaTerminosPlataforma(),
                user.fechaRegistro(),
                user.activo()
        );

        return userMapper.toResponse(userLegible);
    }

    // --- FIRMA DE OBRA ---
    public JsonObject processWorkSignature(WorkSignatureRequest request) {
        User user = userRepository.findByCedula(request.cedula())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema."));

        log.info("Solicitando llave maestra a Azure para liberar credencial de custodia...");
        String decryptedP12 = encryptionService.decrypt(user.firmaP12());

        JsonObject jsonToAzure = Json.createObjectBuilder()
                .add("p12Base64", decryptedP12)
                .add("password", request.p12Password())
                .add("hashObra", request.hashObra())
                .build();

        return signatureClient.signWork(jsonToAzure);
    }
}
