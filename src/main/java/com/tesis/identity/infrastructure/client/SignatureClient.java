package com.tesis.identity.infrastructure.client;

import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "signature-api")
@Path("/api")
@ClientHeaderParam(name = "x-functions-key", value = "{getApiKey}")
public interface SignatureClient {

    default String getApiKey() {
        return ConfigProvider.getConfig().getOptionalValue("azure.function.key", String.class).orElse("");
    }

    @POST
    @Path("/validar_firma_externa")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Timeout(10000)
    @Retry(maxRetries = 2, delay = 500)
    String validateSignature(JsonObject body);

    @POST
    @Path("/firmar_obra")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(15000)
    @Retry(maxRetries = 2, delay = 500)
    JsonObject signWork(JsonObject body);
}