package com.tesis.identity.application.mapper;

import com.tesis.identity.application.dto.UserResponse;
import com.tesis.identity.domain.models.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.id(),
                user.cedula(),
                user.nombres(),
                user.apellidos(),
                user.correo(),
                user.nombreArtistico(),
                user.aceptaTerminosPlataforma(),
                user.fechaRegistro(),
                user.activo()
        );
    }
}
