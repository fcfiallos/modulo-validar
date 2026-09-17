package com.tesis.identity.infrastructure.rest;

import com.tesis.identity.application.AuthService;
import com.tesis.identity.application.dto.LoginRequest;
import com.tesis.identity.application.dto.LoginResponse;
import com.tesis.identity.application.dto.RegisterUserRequest;
import com.tesis.identity.application.dto.UserResponse;
import com.tesis.identity.application.dto.WorkSignatureRequest;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.java.Log;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Log
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthService authService;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Path("/registro")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Response register(
            @RestForm String cedula,
            @RestForm String nombres,
            @RestForm String apellidos,
            @RestForm String correo,
            @RestForm String nombreArtistico,
            @RestForm String password,
            @RestForm boolean aceptaTerminos,
            @RestForm FileUpload firmaP12, // Aquí llega el archivo
            @RestForm String p12Password) throws IOException {

        log.info("Recibiendo solicitud de registro para cédula: { " + cedula + " }");

        // VALIDACIÓN PREVENTIVA (Evita el NullPointerException)
        if (firmaP12 == null || firmaP12.filePath() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("error", "Debe adjuntar el archivo de firma electrónica (.p12)")
                            .build())
                    .build();
        }

        log.info("Archivo recibido: { " + firmaP12.fileName() + " } (" + Files.size(firmaP12.filePath()) + " bytes)");

        // 1. Convertir archivo físico a Base64
        byte[] fileBytes = Files.readAllBytes(firmaP12.filePath());
        String p12Base64 = Base64.getEncoder().encodeToString(fileBytes);

        RegisterUserRequest request = new RegisterUserRequest(
                cedula, nombres, apellidos, correo, nombreArtistico,
                password, aceptaTerminos, p12Base64, p12Password);

        UserResponse created = authService.registerUser(request);

        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/login")
    @Blocking
    public Response login(LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    /**
     * Requiere un JWT válido (emitido por /login). No es "test" en el sentido
     * de estar abierto al público: entrega la firma digital de la obra, así
     * que solo un usuario autenticado puede invocarlo.
     */
    @POST
    @Path("/firmar-obra-test")
    @Authenticated
    @Blocking
    public Response signTest(WorkSignatureRequest request) {
        JsonObject result = authService.processWorkSignature(request, securityIdentity.getPrincipal().getName());
        return Response.ok(result).build();
    }
}
