package com.tesis.identity.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta pública del usuario. Deliberadamente NO incluye
 * passwordHash ni firmaP12: son datos internos/sensibles que
 * nunca deben salir por la API.
 */
public record UserResponse(
        UUID id,
        String cedula,
        String nombres,
        String apellidos,
        String correo,
        String nombreArtistico,
        boolean aceptaTerminosPlataforma,
        String rol,
        LocalDateTime fechaRegistro,
        boolean activo
) {}
