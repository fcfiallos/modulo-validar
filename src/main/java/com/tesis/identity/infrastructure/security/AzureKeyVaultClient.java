package com.tesis.identity.infrastructure.security;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AzureKeyVaultClient {

    private static final Logger LOG = Logger.getLogger(AzureKeyVaultClient.class);

    @ConfigProperty(name = "tesis.azure.keyvault.url", defaultValue = "https://tesis-forensic-vault.vault.azure.net/")
    String vaultUrl;

    @ConfigProperty(name = "tesis.master-key.name", defaultValue = "master-custody-key")
    String keyName;

    @ConfigProperty(name = "tesis.jwt.key-name", defaultValue = "Token-JWT-sistema-forense-1")
    String jwtKeyName;

    /**
     * DefaultAzureCredential encadena varias formas de autenticarse y usa la
     * primera que funcione: variables de entorno, Managed Identity (App
     * Service/Container Apps), Azure CLI, VS Code, etc. Esto permite que el
     * mismo código funcione en local (con `az login`) y en Azure (con
     * Managed Identity) sin cambiar nada aquí.
     */
    @Produces
    @ApplicationScoped
    public CryptographyClient produceCryptographyClient() {
        try {
            var credential = new DefaultAzureCredentialBuilder().build();

            KeyClient keyClient = new KeyClientBuilder()
                    .vaultUrl(vaultUrl)
                    .credential(credential)
                    .buildClient();

            String keyId = keyClient.getKey(keyName).getId();

            return new CryptographyClientBuilder()
                    .keyIdentifier(keyId)
                    .credential(credential)
                    .buildClient();
        } catch (Exception e) {
            LOG.warn("[AzureKeyVaultClient] No se pudo conectar a Azure Key Vault (" + e.getMessage()
                    + "). Se usará el sobre criptográfico local, ver VaultEncryptionService.", e);
            return null;
        }
    }

    /**
     * Cliente de firma para JWT (Token-JWT-sistema-forense). A diferencia del
     * cifrado de custodia, aquí no hay fallback local: si Key Vault no
     * responde, emitir tokens debe fallar duro en vez de firmar con algo
     * débil (JwtTokenIssuer no captura esta excepción).
     */
    @Produces
    @JwtSigningKey
    @ApplicationScoped
    public CryptographyClient produceJwtCryptographyClient() {
        var credential = new DefaultAzureCredentialBuilder().build();

        KeyClient keyClient = new KeyClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .buildClient();

        String keyId = keyClient.getKey(jwtKeyName).getId();

        return new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(credential)
                .buildClient();
    }
}
