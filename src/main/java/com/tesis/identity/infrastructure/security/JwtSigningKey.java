package com.tesis.identity.infrastructure.security;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Distingue el {@link com.azure.security.keyvault.keys.cryptography.CryptographyClient}
 * de firma JWT (llave Token-JWT-sistema-forense) del de cifrado de custodia
 * (llave master-custody-key) — ambos son beans del mismo tipo en Azure Key Vault.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface JwtSigningKey {
}
