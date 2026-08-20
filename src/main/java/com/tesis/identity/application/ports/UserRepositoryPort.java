package com.tesis.identity.application.ports;

import com.tesis.identity.domain.models.User;

import java.util.Optional;

/**
 * Puerto de acceso a datos de usuarios. La aplicación depende de esta
 * abstracción, no de la implementación JPA/Panache concreta
 * (implementada en infrastructure.persistence.UserRepository).
 */
public interface UserRepositoryPort {

    Optional<User> findByEmail(String correo);

    Optional<User> findByCedula(String cedula);

    User save(User user);
}
