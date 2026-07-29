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

    // La fecha de la participación es la de su registro: se usa createdAt y se
    // eliminó la columna "fecha", que guardaba el mismo dato duplicado.

    private Boolean estado;

    @Column(name = "entregado")
    private Boolean entregado;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    /** Libro y folio del registro físico donde se asienta el certificado entregado. */
    @Column(name = "numero_libro", length = 50)
    private String numeroLibro;

    @Column(name = "numero_folio", length = 50)
    private String numeroFolio;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Usuario.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "entregado_por")
    private Usuario entregadoPor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Código corto e impreso, pensado para tipearlo a mano. Su espacio es
     * pequeño (32^4), así que la verificación por este medio va limitada en
     * intentos y muestra datos reducidos.
     */
    @Column(name = "codigo_unico", unique = true)
    private String codigoUnico;

    /**
     * Token del QR: 96 bits aleatorios en Base64-URL (16 caracteres). Es
     * imposible de adivinar, así que quien escanea el certificado accede a la
     * verificación completa.
     */
    @Column(name = "token_verificacion", unique = true, length = 36)
    private String tokenVerificacion;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) {
            estado = true; // Establecer estado en true si no se ha asignado
        }
        if (tokenVerificacion == null || tokenVerificacion.isEmpty()) {
            // 12 bytes aleatorios en Base64-URL: 16 caracteres, 96 bits
            byte[] bytes = new byte[12];
            new java.security.SecureRandom().nextBytes(bytes);
            tokenVerificacion = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
        // Red de seguridad: el código se genera en ParticipacionEventoImpl, que sí
        // comprueba que no exista ya. Aquí no hay acceso al repositorio, así que
        // esta rama solo cubre inserciones que no pasen por el servicio.
        if (codigoUnico == null || codigoUnico.isEmpty()) {
            // Mismo alfabeto sin caracteres ambiguos (sin 0/O ni 1/I) que usa el servicio
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            java.security.SecureRandom random = new java.security.SecureRandom();
            StringBuilder sb = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            codigoUnico = sb.toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}