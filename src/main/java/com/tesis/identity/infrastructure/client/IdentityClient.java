package com.tesis.identity.infrastructure.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.JsonObject;

@RegisterRestClient(configKey = "identity-api")
@Path("/api")
public interface IdentityClient {

    @POST
    @Path("/validar-persona")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN) // Tu API devuelve "1" o "0"
    String validate(JsonObject body);
}