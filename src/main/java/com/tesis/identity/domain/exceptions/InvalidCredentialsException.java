package com.tesis.identity.domain.exceptions;

/**
 * Correo no registrado o contraseña incorrecta. Se usa el mismo tipo/mensaje
 * genérico para ambos casos a propósito, para no revelar si un correo existe.
 */
public final class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
