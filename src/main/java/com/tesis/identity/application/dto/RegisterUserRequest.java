package com.tesis.identity.application.dto;

/**
 * Datos de entrada para el caso de uso de registro.
 * El REST resource arma este DTO a partir del formulario multipart
 * (el archivo .p12 ya viene convertido a Base64).
 */
public record RegisterUserRequest(
        String cedula,
        String nombres,
        String apellidos,
        String correo,
        String nombreArtistico,
        String password,
        boolean aceptaTerminos,
        String p12Base64,
        String p12Password
) {}
