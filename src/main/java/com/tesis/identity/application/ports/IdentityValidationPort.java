package com.tesis.identity.application.ports;

import jakarta.json.JsonObject;

/**
 * Puerto de salida para la validación de identidad ante entidades externas (Registro Civil).
 * Aísla a AuthService de la implementación concreta de cliente REST.
 */
public interface IdentityValidationPort {

    String validate(JsonObject body);
}
