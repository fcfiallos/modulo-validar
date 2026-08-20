package com.tesis.identity.infrastructure.persistence.mapper;

import com.tesis.identity.domain.models.User;
import com.tesis.identity.infrastructure.persistence.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserEntityMapper {

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getCedula(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getCorreo(),
                entity.getNombreArtistico(),
                entity.getPasswordHash(),
                entity.getFirmaP12(),
                entity.isAceptaTerminosPlataforma(),
                entity.getFechaRegistro(),
                entity.isActivo()
        );
    }

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.id())
                .cedula(user.cedula())
                .nombres(user.nombres())
                .apellidos(user.apellidos())
                .correo(user.correo())
                .nombreArtistico(user.nombreArtistico())
                .passwordHash(user.passwordHash())
                .firmaP12(user.firmaP12())
                .aceptaTerminosPlataforma(user.aceptaTerminosPlataforma())
                .fechaRegistro(user.fechaRegistro())
                .activo(user.activo())
                .build();
    }
}
