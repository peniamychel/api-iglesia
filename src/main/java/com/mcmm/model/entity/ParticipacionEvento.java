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
@Table(name = "participacion_evento")
public class ParticipacionEvento {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Certificado.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "certificado_id")
    private Certificado certificado; // nullable as per ER diagram

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Miembro.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Evento.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Column(name = "fecha")
    private Date fecha;

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
            estado = true; // Establecer estado en true si no se ha asignado
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}