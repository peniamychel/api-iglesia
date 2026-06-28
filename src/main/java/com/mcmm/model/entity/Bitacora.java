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
@Table(name = "bitacora")
public class Bitacora implements java.io.Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "username")
    private String username;

    @Column(name = "accion", nullable = false)
    private String accion; // CREAR, MODIFICAR, ELIMINAR, EMITIR_CERTIFICADO, etc.

    @Column(name = "modulo", nullable = false)
    private String modulo; // MIEMBRO, CERTIFICADO, etc.

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ip_address")
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
