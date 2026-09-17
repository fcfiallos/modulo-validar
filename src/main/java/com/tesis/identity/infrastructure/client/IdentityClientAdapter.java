package com.tesis.identity.infrastructure.client;

import com.tesis.identity.application.ports.IdentityValidationPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Adaptador de infraestructura que implementa el puerto IdentityValidationPort
 * delegando la llamada al cliente declarativo de MicroProfile Rest Client.
 */
@ApplicationScoped
public class IdentityClientAdapter implements IdentityValidationPort {

    @Inject
    @RestClient
    IdentityClient identityClient;

    @Override
    public String validate(JsonObject body) {
        return identityClient.validate(body);
    }
}
