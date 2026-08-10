package com.tesis.identity.infrastructure.client;

import com.azure.identity.AzureCliCredentialBuilder; // CAMBIO AQUÍ
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AzureKeyProducer {

    @ConfigProperty(name = "quarkus.azure.keyvault.url", defaultValue = "https://tesis-forensic-vault.vault.azure.net/")
    String vaultUrl;

    @ConfigProperty(name = "tesis.master-key.name", defaultValue = "master-custody-key")
    String keyName;

    @Produces
    @ApplicationScoped
    public CryptographyClient produceCryptographyClient() {
        try {
            var credential = new AzureCliCredentialBuilder().build();

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
            System.err.println("[AzureKeyProducer] Advertencia: No se pudo conectar a Azure Key Vault localmente (" + e.getMessage() + "). Se usará el sobre criptográfico de desarrollo local.");
            return null;
        }
    }
}