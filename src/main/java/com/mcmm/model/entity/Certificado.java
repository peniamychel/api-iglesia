package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "certificado")
public class Certificado {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Evento.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = PlantillaCertificado.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "plantilla_certificado_id")
    private PlantillaCertificado plantillaCertificado;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = TipoCertificado.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "tipo_certificado_id")
    private TipoCertificado tipoCertificado;

    @Column(name = "motivo_certificado", length = 254)
    private String motivoCertificado;

    @Column(name = "codigo_certificado", length = 254)
    private String codigoCertificado;

    @Column(name = "uri_foto")
    private String uriFoto;

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