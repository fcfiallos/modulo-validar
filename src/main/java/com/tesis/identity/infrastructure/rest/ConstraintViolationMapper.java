package com.tesis.identity.infrastructure.rest;

import jakarta.json.Json;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String mensaje = "Error de integridad: El correo o la cédula ya se encuentran registrados.";
        return Response.status(Response.Status.CONFLICT) // Error 409
                .entity(Json.createObjectBuilder().add("error", mensaje).build())
                .build();
    }
}