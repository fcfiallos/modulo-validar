package com.tesis.identity.infrastructure.security;

import com.tesis.identity.application.ports.BlindIndexPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/**
 * Índice ciego para la cédula: HMAC-SHA256 con una llave secreta fija por
 * despliegue. Es determinístico a propósito (misma cédula => mismo hash
 * siempre), lo que permite mantener la unicidad y la búsqueda por igualdad en
 * BD sin guardar la cédula en texto plano. La cédula real se guarda cifrada
 * con EncryptionPort (no determinístico) solo para mostrarla; este hash NUNCA
 * se usa para reconstruir la cédula, solo para comparar.
 * <p>
 * La llave vive fuera del repo (variable de entorno), igual que
 * DB_PASSWORD/CERT_PASSWORD. No pasa por Key Vault a propósito: usarla
 * requeriría una llamada remota por cada login/registro/consulta por cédula,
 * reintroduciendo la misma latencia de DefaultAzureCredential que ya
 * resolvimos para JWT/cifrado (ver AzureKeyVaultClient).
 */
@ApplicationScoped
public class BlindIndexService implements BlindIndexPort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @ConfigProperty(name = "tesis.cedula.hmac-secret")
    String hmacSecret;

    @Override
    public String hash(String plainText) {
        if (plainText == null) return null;

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] result = mac.doFinal(plainText.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el índice ciego: " + e.getMessage(), e);
        }
    }
}
