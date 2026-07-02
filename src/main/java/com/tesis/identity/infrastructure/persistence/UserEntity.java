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

    @Column(name = "nombre_artistico")
    private String nombreArtistico;

    @Column(name = "password_hash")
    private String passwordHash;

    @Lob
    @Column(name = "firma_p12", columnDefinition = "TEXT")
    private String firmaP12; // El archivo .p12 en Base64

    @Column(name = "acepta_terminos_plataforma")
    private boolean aceptaTerminosPlataforma;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    private boolean activo;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
}