package com.tesis.identity.infrastructure.rest;


import com.tesis.identity.application.AuthService;
import com.tesis.identity.infrastructure.persistence.UserEntity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/registro")
    @Consumes(MediaType.MULTIPART_FORM_DATA) // <--- Cambiamos a Multipart
    @Blocking
    public Response register(
            @RestForm String cedula,
            @RestForm String nombres,
            @RestForm String apellidos,
            @RestForm String correo,
            @RestForm String nombreArtistico,
            @RestForm String password,
            @RestForm boolean aceptaTerminos,
            @RestForm FileUpload firmaP12,
            @RestForm String p12Password
    ) throws IOException {

        // 1. archivo fisico a Base64 para mandarlo a validar y guardarlo
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
}