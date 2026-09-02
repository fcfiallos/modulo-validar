package com.tesis.identity.application.ports;

/**
 * Puerto de índice ciego (blind index): deriva de un valor sensible un
 * identificador determinístico apto para búsqueda/unicidad en base de datos,
 * sin exponer el valor original. A diferencia de EncryptionPort (cifrado de
 * sobre, no determinístico), este SIEMPRE produce la misma salida para la
 * misma entrada, ya que se usa para igualdad exacta (WHERE ... = ?), no para
 * confidencialidad de visualización.
 */
public interface BlindIndexPort {

    String hash(String plainText);
}
