package com.tesis.identity.infrastructure.rest;

import com.tesis.identity.application.AuthService;
import com.tesis.identity.infrastructure.persistence.UserEntity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/registro")
    @Blocking
    public Response register(UserEntity user) {
        authService.registerUser(user, user.getPasswordHash());
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