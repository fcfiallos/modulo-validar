package com.tesis.identity.infrastructure.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Último recurso: cualquier RuntimeException que NO sea una DomainException
 * (bugs, fallos de infraestructura, NPEs, etc.). No se expone el mensaje
 * interno de la excepción al cliente; el detalle completo va solo al log.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        LOG.error("Error inesperado no controlado", exception);

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        return Response.status(status)
                .entity(ApiError.of("Ha ocurrido un error inesperado. Intente nuevamente más tarde.", status))
                .build();
    }
}
