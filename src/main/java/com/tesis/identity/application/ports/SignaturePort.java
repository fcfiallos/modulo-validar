package com.tesis.identity.application.ports;

import jakarta.json.JsonObject;

/**
 * Puerto de salida para validación y ejecución de firma digital ante el servicio externo.
 * Aísla a AuthService de la implementación concreta de cliente REST.
 */
public interface SignaturePort {

    String validateSignature(JsonObject body);

    JsonObject signWork(JsonObject body);
}
