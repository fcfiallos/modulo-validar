package com.tesis.identity.domain.exceptions;

/**
 * Violación de una regla de negocio que no encaja en un tipo más específico:
 * validación externa de identidad/firma fallida, cuenta desactivada, etc.
 * No está en el listado original de excepciones pedidas, pero evita volver
 * a caer en RuntimeException genéricas para estos casos (ver resumen de Fase 3).
 */
public final class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
