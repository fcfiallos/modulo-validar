package com.tesis.identity.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue // Quarkus/Hibernate manejará el UUID automáticamente
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(unique = true, nullable = false, length = 150)
    private String correo;

    @Column(name = "nombre_artistico", length = 100)
    private String nombreArtistico;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "acepta_terminos_plataforma")
    private boolean aceptaTerminosPlataforma;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    private boolean activo;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
}