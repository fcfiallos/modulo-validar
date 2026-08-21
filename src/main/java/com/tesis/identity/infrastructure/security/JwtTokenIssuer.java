package com.tesis.identity.infrastructure.security;

import com.tesis.identity.application.ports.TokenPort;
import com.tesis.identity.domain.models.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JwtTokenIssuer implements TokenPort {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Override
    public String issueToken(User user) {
        return Jwt.issuer(issuer)
                .subject(user.id().toString())
                .upn(user.correo())
                .claim("cedula", user.cedula())
                .groups(user.rol() != null ? user.rol() : "USER")
                .sign();
    }
}
