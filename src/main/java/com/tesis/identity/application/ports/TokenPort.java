package com.tesis.identity.application.ports;

import com.tesis.identity.domain.models.User;

/**
 * Puerto de emisión de tokens de sesión. Implementado en
 * infrastructure.security.JwtTokenIssuer usando SmallRye JWT.
 */
public interface TokenPort {

    String issueToken(User user);
}
