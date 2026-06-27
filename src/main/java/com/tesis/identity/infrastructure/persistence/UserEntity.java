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
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String cedula;
    private String nombres;
    private String apellidos;
    @Column(unique = true, nullable = false)
    private String correo;
    private String nombreArtistico;
    private String passwordHash;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
}