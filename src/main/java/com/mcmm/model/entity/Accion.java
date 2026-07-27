package com.mcmm.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"servicio", "roles"})
@EqualsAndHashCode(exclude = {"servicio", "roles"})
@Entity
@Builder
@Table(name = "accion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"servicio_id", "codigo"})
})
public class Accion implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id", nullable = false)
    @JsonIgnore
    private Servicio servicio;

    @Column(name = "codigo", length = 100, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo;

    @ManyToMany(mappedBy = "acciones")
    @JsonIgnore
    @Builder.Default
    private Set<RolCargo> roles = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Devuelve el código de autoridad en formato SERVICIO:CODIGO (ej: MIEMBROS:VER, OBREROS:DESIGNAR)
     */
    public String getAuthorityCode() {
        if (servicio != null && servicio.getCodigo() != null) {
            return servicio.getCodigo() + ":" + codigo;
        }
        return codigo;
    }
}
