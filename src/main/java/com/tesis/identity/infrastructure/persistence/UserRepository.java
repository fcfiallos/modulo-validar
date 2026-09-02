package com.tesis.identity.infrastructure.persistence;

import com.tesis.identity.application.ports.UserRepositoryPort;
import com.tesis.identity.domain.models.User;
import com.tesis.identity.infrastructure.persistence.mapper.UserEntityMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements UserRepositoryPort, PanacheRepository<UserEntity> {

    @Inject
    UserEntityMapper mapper;

    @Override
    public Optional<User> findByEmail(String correo) {
        return find("correo", correo).firstResultOptional().map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByCedulaHash(String cedulaHash) {
        return find("cedulaHash", cedulaHash).firstResultOptional().map(mapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        persist(entity);
        return mapper.toDomain(entity);
    }
}
