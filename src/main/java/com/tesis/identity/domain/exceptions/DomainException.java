package com.tesis.identity.domain.exceptions;

/**
 * Raíz de las excepciones de reglas de negocio del dominio.
 * Sellada a propósito: la capa REST puede resolver el status HTTP
 * de cada caso con un switch exhaustivo, sin caer en un default ciego.
 */
public sealed abstract class DomainException extends RuntimeException
        permits InvalidCredentialsException, UserAlreadyExistsException,
                TermsNotAcceptedException, UserNotFoundException,
                BusinessRuleViolationException {

    protected DomainException(String message) {
        super(message);
    }
}
