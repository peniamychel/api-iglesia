package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Entity
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

    // La plantilla ya no lleva imágenes (logo, marca de agua ni firma): el
    // certificado se imprime sobre papel preimpreso y el sistema solo coloca los
    // datos encima. Las columnas uri_logo, uri_marca_agua y uri_firma quedan sin
    // uso hasta que se eliminen de la base.

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
