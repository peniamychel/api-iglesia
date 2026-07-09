package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "activo")
public class Activo implements java.io.Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "estado_conservacion", length = 50)
    private String estadoConservacion; // BUENO, REGULAR, MALO

    @Column(name = "valor_estimado")
    private Double valorEstimado;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_adquisicion")
    private Date fechaAdquisicion;

    @Column(name = "codigo", length = 100)
    private String codigo;

    @Column(name = "uri_foto", length = 255)
    private String uriFoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iglesia_id", nullable = false)
    private Iglesia iglesia;
}
