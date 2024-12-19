package com.mcmm.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "iglesia")
public class Iglesia implements java.io.Serializable{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = MiembroIglesia.class, cascade = CascadeType.PERSIST, mappedBy = "iglesia")
    private List<MiembroIglesia> miembroIglesias;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Cargo.class, cascade = CascadeType.PERSIST, mappedBy = "iglesia")
    private List<Cargo> cargos;

    private String nombre;

    private String direccion;

    private Integer telefono;

    @Column(name = "fecha_fundacion")
    private Date fechaFundacion;

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
            estado = true;  // Establecer estado en true si no se ha asignado
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
