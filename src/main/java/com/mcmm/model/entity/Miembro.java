package com.mcmm.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "miembro")
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = MiembroIglesia.class, cascade = CascadeType.PERSIST, mappedBy = "miembro")
    private List<MiembroIglesia> miembroIglesias;

    @OneToOne (fetch = FetchType.LAZY,targetEntity = Persona.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Cargo.class, cascade = CascadeType.PERSIST, mappedBy = "miembro")
    private List<Cargo> cargos;

    @Column(name = "fecha_convercion")
    private Date fechaConvercion;

    @Column(name = "lugar_convercion")
    private String lugarConvercion;

    private String interventores;

    private String detalles;

    private Boolean estado;

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
