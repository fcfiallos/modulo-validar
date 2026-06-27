package com.tesis.identity.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String cedula,
        String nombres,
        String apellidos,
        String correo,
        String nombreArtistico,
        String passwordHash,
        LocalDateTime fechaRegistro,
        boolean activo
) {}