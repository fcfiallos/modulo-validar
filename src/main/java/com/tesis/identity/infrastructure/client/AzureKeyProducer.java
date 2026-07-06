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

    @ConfigProperty(name = "quarkus.azure.keyvault.url")
    String vaultUrl;

    @ConfigProperty(name = "tesis.master-key.name")
    String keyName;

    @Produces
    @ApplicationScoped
    public CryptographyClient produceCryptographyClient() {
        // FORZAMOS el uso de Azure CLI para evitar el timeout de Managed Identity
        var credential = new AzureCliCredentialBuilder().build();

        KeyClient keyClient = new KeyClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .buildClient();

        // Recuperamos el ID de la llave maestra
        String keyId = keyClient.getKey(keyName).getId();

        return new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(credential)
                .buildClient();
    }
}