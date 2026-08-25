package com.tesis.identity.infrastructure.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Red de seguridad ante condiciones de carrera: si dos registros concurrentes
 * pasan la verificación proactiva de AuthService.registerUser y ambos intentan
 * persistir, la restricción única de la base de datos es la que realmente
 * evita el duplicado. Este mapper traduce esa falla de Hibernate a un 409.
 */
@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        int status = Response.Status.CONFLICT.getStatusCode();
        String mensaje = "Error de integridad: El correo o la cédula ya se encuentran registrados.";
        return Response.status(status)
                .entity(ApiError.of(mensaje, status))
                .build();
    }
}
