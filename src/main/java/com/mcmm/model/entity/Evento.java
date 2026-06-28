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
@Table(name = "evento")
public class Evento {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = TipoEvento.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "tipo_evento_id")
    private TipoEvento tipoEvento;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Iglesia.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "iglesia_id", nullable = true)
    private Iglesia iglesia;

    @Column(name = "nombre", length = 254)
    private String nombre;

    @Column(name = "motivo", length = 254)
    private String motivo;

    @Column(name = "uri_foto", length = 254)
    private String uriFoto;

    @Column(name = "ubicacion", length = 254)
    private String ubicacion;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    private Boolean estado;

    @Column(name = "alcance", length = 50)
    private String alcance; // LOCAL or GENERAL

    @Column(name = "mostrar_en_calendario")
    private Boolean mostrarEnCalendario;

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