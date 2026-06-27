package com.tesis.identity.infrastructure.rest;

import com.tesis.identity.application.service.AuthService;
import com.tesis.identity.infrastructure.persistence.UserEntity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
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
}