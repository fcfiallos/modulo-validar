package com.tesis.identity.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String cedula,
        String cedulaHash,
        String nombres,
        String apellidos,
        String correo,
        String nombreArtistico,
        String passwordHash,
        String firmaP12,
        boolean aceptaTerminosPlataforma,
        String rol,
        LocalDateTime fechaRegistro,
        boolean activo
) {}
