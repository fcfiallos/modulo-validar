package com.tesis.identity.infrastructure.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.JsonObject;

@RegisterRestClient(configKey = "signature-api")
@Path("/api")
public interface SignatureClient {

    @POST
    @Path("/validar_firma_externa")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    String validateSignature(JsonObject body);

    // --- PARA EL MÓDULO B ---
    @POST
    @Path("/firmar_obra")
    @Consumes(MediaType.APPLICATION_JSON)
    JsonObject signWork(JsonObject body);
}