package com.tesis.identity.infrastructure.security;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.tesis.identity.application.ports.TokenPort;
import com.tesis.identity.domain.models.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Firma el JWT de forma remota contra Azure Key Vault (Token-JWT-sistema-forense):
 * la llave privada nunca sale del vault. smallrye-jwt no soporta firmar con un
 * CryptographyClient, así que el token (header.payload.signature) se arma a mano.
 */
@ApplicationScoped
public class JwtTokenIssuer implements TokenPort {

    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    private static final String HEADER_JSON = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";

    @Inject
    @JwtSigningKey
    CryptographyClient cryptoClient;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
    long lifespanSeconds;

    @Override
    public String issueToken(User user) {
        String signingInput = BASE64URL.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8))
                + "." + BASE64URL.encodeToString(buildPayload(user).getBytes(StandardCharsets.UTF_8));

        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 no disponible: " + e.getMessage(), e);
        }

        SignResult signResult = cryptoClient.sign(SignatureAlgorithm.RS256, digest);
        return signingInput + "." + BASE64URL.encodeToString(signResult.getSignature());
    }

    private String buildPayload(User user) {
        long iat = Instant.now().getEpochSecond();
        long exp = iat + lifespanSeconds;
        return Json.createObjectBuilder()
                .add("iss", issuer)
                .add("sub", user.id().toString())
                .add("upn", user.correo())
                .add("cedula", user.cedula())
                .add("groups", Json.createArrayBuilder().add(user.rol() != null ? user.rol() : "USER"))
                .add("iat", iat)
                .add("exp", exp)
                .add("jti", UUID.randomUUID().toString())
                .build()
                .toString();
    }
}
