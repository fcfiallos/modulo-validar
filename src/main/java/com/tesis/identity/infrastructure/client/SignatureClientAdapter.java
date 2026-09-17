package com.tesis.identity.infrastructure.client;

import com.tesis.identity.application.ports.SignaturePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Adaptador de infraestructura que implementa el puerto SignaturePort
 * delegando la llamada al cliente declarativo de MicroProfile Rest Client.
 */
@ApplicationScoped
public class SignatureClientAdapter implements SignaturePort {

    @Inject
    @RestClient
    SignatureClient signatureClient;

    @Override
    public String validateSignature(JsonObject body) {
        return signatureClient.validateSignature(body);
    }

    @Override
    public JsonObject signWork(JsonObject body) {
        return signatureClient.signWork(body);
    }
}
