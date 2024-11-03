package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "miembros_iglesia")
public class MiembroIglesia implements java.io.Serializable{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, targetEntity = Miembro.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_miembros" )
    private Miembro idMiembro;

    @ManyToOne (fetch = FetchType.LAZY,targetEntity = Iglesia.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_iglesia")
    private Iglesia idIglesia;

    @Column(name = "fecha")
    private Date fecha;

    private String motivoTraspaso;

    @Column(name = "fecha_traspaso")
    private Date fechaTraspaso;

    @Column(name = "uri_carta_traspaso")
    private String uriCartaTraspaso;

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
