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
@Table(name = "plantilla_certificado")
public class PlantillaCertificado {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 254)
    private String nombre;

    @Column(name = "configuracion_json", length = 65535)
    private String configuracionJson;

    @Column(name = "uri_logo", length = 255)
    private String uriLogo;

    @Column(name = "uri_marca_agua")
    private String uriMarcaAgua;

    @Column(name = "uri_firma")
    private String uriFirma;

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
