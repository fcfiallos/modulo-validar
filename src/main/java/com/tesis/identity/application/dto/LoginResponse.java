package com.tesis.identity.application.dto;

/**
 * Respuesta del login: datos públicos del usuario + el JWT que debe
 * mandarse como "Authorization: Bearer {token}" en las siguientes
 * peticiones (a este módulo y a Módulo B).
 */
public record LoginResponse(UserResponse user, String token) {}
