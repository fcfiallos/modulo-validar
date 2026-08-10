package com.tesis.identity.infrastructure.rest;

import com.tesis.identity.application.CredencialesIncorrectasException;
import jakarta.json.Json;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ErrorNegocioMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(ErrorNegocioMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        String mensaje = (exception.getMessage() != null && !exception.getMessage().isBlank())
                ? exception.getMessage()
                : "Ha ocurrido un error inesperado.";

        LOG.warn("Error de negocio controlado: " + mensaje);

        int status = (exception instanceof CredencialesIncorrectasException)
                ? Response.Status.UNAUTHORIZED.getStatusCode()
                : Response.Status.BAD_REQUEST.getStatusCode();

        return Response.status(status)
                .entity(Json.createObjectBuilder().add("error", mensaje).build())
                .build();
    }
}