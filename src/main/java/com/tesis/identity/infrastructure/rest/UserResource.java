package com.tesis.identity.infrastructure.rest;


import com.tesis.identity.application.AuthService;
import com.tesis.identity.infrastructure.persistence.UserEntity;
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
@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthService authService;

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
            @RestForm FileUpload firmaP12,       // Aquí llega el archivo
            @RestForm String p12Password
    ) throws IOException {

        // LOG DE CONTROL: Para ver qué llega
        String msg= "Recibiendo solicitud de registro para cédula: { "+ cedula + " }";
        log.info(msg);

        // VALIDACIÓN PREVENTIVA (Evita el NullPointerException)
        if (firmaP12 == null || firmaP12.filePath() == null) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("error", "Debe adjuntar el archivo de firma electrónica (.p12)")
                            .build())
                    .build();
        }

        msg = "Archivo recibido: { "+ firmaP12.fileName()+ "} ({ "+ Files.size(firmaP12.filePath())+" } bytes)";
        log.info(msg);

        // 1. Convertir archivo físico a Base64
        byte[] fileBytes = Files.readAllBytes(firmaP12.filePath());
        String p12Base64 = Base64.getEncoder().encodeToString(fileBytes);

        UserEntity user = UserEntity.builder()
                .cedula(cedula)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .nombreArtistico(nombreArtistico)
                .aceptaTerminosPlataforma(aceptaTerminos)
                .build();

        authService.registerUser(user, password, p12Base64, p12Password);

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    @Blocking
    public Response login(JsonObject credentials) {
        String email = credentials.getString("correo");
        String pass = credentials.getString("password");

        UserEntity user = authService.login(email, pass);
        return Response.ok(user).build();
    }

    @POST
    @Path("/firmar-obra-test")
    @Blocking
    public Response signTest(JsonObject input) {
        // Datos que vienen desde el "frontend" o Postman
        String cedula = input.getString("cedula");
        String p12Pass = input.getString("p12Password");
        String hashObra = input.getString("hashObra");

        JsonObject result = authService.processWorkSignature(cedula, p12Pass, hashObra);

        return Response.ok(result).build();
    }

}