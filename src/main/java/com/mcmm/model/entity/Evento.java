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

    @Column(name = "ubicacion", length = 254)
    private String ubicacion;

    @Column(name = "localidad", length = 254)
    private String localidad;

    @Column(name = "provincia", length = 254)
    private String provincia;

    @Column(name = "departamento", length = 254)
    private String departamento;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    private Boolean estado;

    @Column(name = "alcance", length = 50)
    private String alcance; // LOCAL or GENERAL

    @Column(name = "mostrar_en_calendario")
    private Boolean mostrarEnCalendario;

    @Column(name = "habilitar_inscripciones")
    private Boolean habilitarInscripciones = false;

    @Column(name = "iglesias_invitadas", length = 500)
    private String iglesiasInvitadas;

    @Column(name = "archivado")
    private Boolean archivado;

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
        if (archivado == null) {
            archivado = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}