package com.tesis.identity.infrastructure.security;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.tesis.identity.application.ports.EncryptionPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class VaultEncryptionService implements EncryptionPort {

    @Inject
    CryptographyClient cryptoClient;

    /**
     * Deuda técnica temporal y deliberada: Azure Key Vault ya está
     * aprovisionado pero todavía no tiene Managed Identity ni acceso
     * configurado, así que si se apaga este flag hoy la app no podría
     * cifrar/descifrar nada. Mientras tanto, si Key Vault falla, se cae a un
     * "sobre" cifrado local con una llave fija embebida en el jar — NO es
     * cifrado real de custodia, es solo para no bloquear el desarrollo.
     * En cuanto haya Managed Identity + acceso a Key Vault verificado en
     * Azure, poner tesis.encryption.allow-insecure-fallback=false (o la env
     * var ALLOW_INSECURE_ENCRYPTION_FALLBACK=false) para que un fallo de
     * Key Vault en producción falle duro (500) en vez de cifrar con esta
     * llave débil.
     */
    @ConfigProperty(name = "tesis.encryption.allow-insecure-fallback", defaultValue = "true")
    boolean allowInsecureFallback;

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_BIT_LENGTH = 128;
    private static final int IV_BYTE_LENGTH = 12;
    private static final int WRAPPED_KEY_LENGTH = 512; // RSA-4096 => bloque de 512 bytes

    private static final byte[] LOCAL_MASTER_KEY_PADDED = buildPaddedLocalMasterKey();

    private static byte[] buildPaddedLocalMasterKey() {
        byte[] localMasterKey = "TESIS_LOCAL_MASTER_KEY_2026_SOBRE_512B_DEV_MODE!".getBytes(StandardCharsets.UTF_8);
        byte[] padded = new byte[WRAPPED_KEY_LENGTH];
        for (int i = 0; i < WRAPPED_KEY_LENGTH; i++) {
            padded[i] = localMasterKey[i % localMasterKey.length];
        }
        return padded;
    }

    private static byte[] xorWithLocalMasterKey(byte[] data) {
        byte[] result = new byte[WRAPPED_KEY_LENGTH];
        for (int i = 0; i < WRAPPED_KEY_LENGTH; i++) {
            result[i] = (byte) (data[i] ^ LOCAL_MASTER_KEY_PADDED[i]);
        }
        return result;
    }

    /**
     * Implementación de Envelope Encryption (Cifrado de Sobre)
     */
    @Override
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
            byte[] encryptedAesKey;
            try {
                if (cryptoClient != null) {
                    encryptedAesKey = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP_256, aesKey.getEncoded()).getCipherText();
                } else {
                    throw new IllegalStateException("cryptoClient no inicializado");
                }
            } catch (Exception azureEx) {
                encryptedAesKey = localFallbackWrap(aesKey.getEncoded(), azureEx);
            }

            // 4. Empaquetar: [LlaveAESCifrada(512bytes)] + [IV(12bytes)] + [DatosCifrados]
            byte[] combined = new byte[encryptedAesKey.length + iv.length + cipherText.length];
            System.arraycopy(encryptedAesKey, 0, combined, 0, encryptedAesKey.length);
            System.arraycopy(iv, 0, combined, encryptedAesKey.length, iv.length);
            System.arraycopy(cipherText, 0, combined, encryptedAesKey.length + iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Fallo en el sobre criptográfico: {}", e.getMessage(), e);
            throw new RuntimeException("Error de seguridad en custodia híbrida: " + e.getMessage());
        }
    }

    @Override
    public String decrypt(String combinedBase64) {
        if (combinedBase64 == null || combinedBase64.isEmpty()) return combinedBase64;

        try {
            byte[] combined = Base64.getDecoder().decode(combinedBase64);

            // 1. Extraer las partes
            byte[] encryptedAesKey = new byte[WRAPPED_KEY_LENGTH];
            byte[] iv = new byte[IV_BYTE_LENGTH];
            byte[] cipherText = new byte[combined.length - WRAPPED_KEY_LENGTH - IV_BYTE_LENGTH];

            System.arraycopy(combined, 0, encryptedAesKey, 0, WRAPPED_KEY_LENGTH);
            System.arraycopy(combined, WRAPPED_KEY_LENGTH, iv, 0, IV_BYTE_LENGTH);
            System.arraycopy(combined, WRAPPED_KEY_LENGTH + IV_BYTE_LENGTH, cipherText, 0, cipherText.length);

            // 2. Descifrar la llave AES usando Azure o Fallback local
            byte[] decryptedAesKey;
            try {
                if (cryptoClient != null) {
                    decryptedAesKey = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP_256, encryptedAesKey).getPlainText();
                } else {
                    throw new IllegalStateException("cryptoClient no inicializado");
                }
            } catch (Exception azureEx) {
                decryptedAesKey = localFallbackUnwrap(encryptedAesKey, azureEx);
            }

            // 3. Descifrar los datos localmente con la llave recuperada
            SecretKey aesKey = new SecretKeySpec(decryptedAesKey, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

            return new String(cipher.doFinal(cipherText));

        } catch (Exception e) {
            log.error("Error al abrir el sobre criptográfico: {}", e.getMessage(), e);
            throw new RuntimeException("Acceso denegado a la credencial blindada: " + e.getMessage());
        }
    }

    private byte[] localFallbackWrap(byte[] rawAesKey, Exception azureEx) {
        requireFallbackAllowed(azureEx);
        byte[] padded = new byte[WRAPPED_KEY_LENGTH];
        System.arraycopy(rawAesKey, 0, padded, 0, rawAesKey.length);
        return xorWithLocalMasterKey(padded);
    }

    private byte[] localFallbackUnwrap(byte[] wrappedAesKey, Exception azureEx) {
        requireFallbackAllowed(azureEx);
        byte[] unpadded = xorWithLocalMasterKey(wrappedAesKey);
        return Arrays.copyOf(unpadded, 32); // AES-256 -> 32 bytes
    }

    private void requireFallbackAllowed(Exception azureEx) {
        if (!allowInsecureFallback) {
            throw new IllegalStateException(
                    "Azure Key Vault no disponible y el fallback de cifrado local está deshabilitado "
                            + "(tesis.encryption.allow-insecure-fallback=false). "
                            + "Configure Managed Identity y acceso a Key Vault antes de desplegar.",
                    azureEx);
        }
        log.warn("[VaultEncryptionService] ALERTA DE SEGURIDAD: Azure Key Vault no disponible, "
                + "aplicando sobre criptográfico LOCAL INSEGURO (fallback temporal, ver tesis.encryption.allow-insecure-fallback). Causa: {}",
                azureEx.getMessage());
    }
}
