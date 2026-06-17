package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "privilegios")
@EqualsAndHashCode(exclude = "privilegios")
@Entity
@Builder
@Table(name = "rol_cargo")
public class RolCargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    private String nombre;

    @Column(name = "nombre_rol")
    private String nombreRol;

    private Boolean estado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_cargo_privilegio",
        joinColumns = @JoinColumn(name = "rol_cargo_id"),
        inverseJoinColumns = @JoinColumn(name = "privilegio_id"))
    @Builder.Default
    private Set<Privilegio> privilegios = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) {
            estado = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
