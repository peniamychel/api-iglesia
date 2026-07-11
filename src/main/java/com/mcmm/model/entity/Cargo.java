package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Entity
@Table(name = "cargo")
public class Cargo {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RolCargo.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rol_cargo_id")
    private RolCargo rolCargo;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Iglesia.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "iglesia_id")
    private Iglesia iglesia;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Miembro.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    private Boolean estado;

    @Column(name = "uri_acta_asignacion")
    private String uriActaAsignacion;

    @Column(name = "uri_acta_deslindacion")
    private String uriActaDeslindacion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) {
            estado = true; // Establecer estado en true si no se ha asignado
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
