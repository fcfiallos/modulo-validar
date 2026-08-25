package com.tesis.identity.infrastructure.rest;

import com.tesis.identity.domain.exceptions.BusinessRuleViolationException;
import com.tesis.identity.domain.exceptions.DomainException;
import com.tesis.identity.domain.exceptions.InvalidCredentialsException;
import com.tesis.identity.domain.exceptions.TermsNotAcceptedException;
import com.tesis.identity.domain.exceptions.UserAlreadyExistsException;
import com.tesis.identity.domain.exceptions.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Traduce las excepciones de dominio (reglas de negocio conocidas) al status
 * HTTP correcto. JAX-RS despacha aquí en vez de a GlobalExceptionMapper
 * porque DomainException es más específico que RuntimeException.
 */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    private static final Logger LOG = Logger.getLogger(DomainExceptionMapper.class);

    @Override
    public Response toResponse(DomainException exception) {
        int status = switch (exception) {
            case InvalidCredentialsException e -> 401;
            case UserNotFoundException e -> 404;
            case UserAlreadyExistsException e -> 409;
            case TermsNotAcceptedException e -> 422;
            case BusinessRuleViolationException e -> 422;
        };

        LOG.warn("Error de negocio controlado [" + exception.getClass().getSimpleName() + "]: " + exception.getMessage());

        return Response.status(status)
                .entity(ApiError.of(exception.getMessage(), status))
                .build();
    }
}
