package com.tesis.identity.application;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class VaultEncryptionService {

    @Inject
    CryptographyClient cryptoClient;

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_BIT_LENGTH = 128;
    private static final int IV_BYTE_LENGTH = 12;

    /**
     * Implementación de Envelope Encryption (Cifrado de Sobre)
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;

        try {
            // 1. Generar una llave AES-256 local (Data Encryption Key - DEK)
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();

            // 2. Cifrar los datos con AES-GCM
            byte[] iv = new byte[IV_BYTE_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            // 3. Cifrar la LLAVE AES con la LLAVE RSA de Azure (Key Encryption Key - KEK)
            byte[] encryptedAesKey = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP_256,
                    aesKey.getEncoded()).getCipherText();

            // 4. Empaquetar: [LlaveAESCifrada(512bytes)] + [IV(12bytes)] + [DatosCifrados]
            byte[] combined = new byte[encryptedAesKey.length + iv.length + cipherText.length];
            System.arraycopy(encryptedAesKey, 0, combined, 0, encryptedAesKey.length);
            System.arraycopy(iv, 0, combined, encryptedAesKey.length, iv.length);
            System.arraycopy(cipherText, 0, combined, encryptedAesKey.length + iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Fallo en el sobre criptográfico: {}", e.getMessage());
            throw new RuntimeException("Error de seguridad en custodia híbrida.");
        }
    }

    public String decrypt(String combinedBase64) {
        if (combinedBase64 == null || combinedBase64.isEmpty()) return combinedBase64;

        try {
            byte[] combined = Base64.getDecoder().decode(combinedBase64);

            // 1. Extraer las partes (RSA 4096 genera un bloque de 512 bytes)
            int keyLength = 512;
            byte[] encryptedAesKey = new byte[keyLength];
            byte[] iv = new byte[IV_BYTE_LENGTH];
            byte[] cipherText = new byte[combined.length - keyLength - IV_BYTE_LENGTH];

            System.arraycopy(combined, 0, encryptedAesKey, 0, keyLength);
            System.arraycopy(combined, keyLength, iv, 0, IV_BYTE_LENGTH);
            System.arraycopy(combined, keyLength + IV_BYTE_LENGTH, cipherText, 0, cipherText.length);

            // 2. Descifrar la llave AES usando Azure
            byte[] decryptedAesKey = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP_256,
                    encryptedAesKey).getPlainText();

            // 3. Descifrar los datos localmente con la llave recuperada
            SecretKey aesKey = new SecretKeySpec(decryptedAesKey, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

            return new String(cipher.doFinal(cipherText));

        } catch (Exception e) {
            log.error("Error al abrir el sobre criptográfico: {}", e.getMessage());
            throw new RuntimeException("Acceso denegado a la credencial blindada.");
        }
    }
}