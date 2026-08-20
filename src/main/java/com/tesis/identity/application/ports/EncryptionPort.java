package com.tesis.identity.application.ports;

/**
 * Puerto de cifrado/descifrado de datos sensibles (envelope encryption).
 * Implementado en infrastructure.security.VaultEncryptionService usando Azure Key Vault.
 */
public interface EncryptionPort {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}
