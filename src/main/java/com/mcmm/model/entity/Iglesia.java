package com.mcmm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Entity
@Table(name = "iglesia", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Iglesia implements java.io.Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = MiembroIglesia.class, cascade = CascadeType.PERSIST, mappedBy = "iglesia")
    private List<MiembroIglesia> miembroIglesias;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Cargo.class, cascade = CascadeType.PERSIST, mappedBy = "iglesia")
    private List<Cargo> cargos;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    private String direccion;

    private Long telefono;

    @Column(name = "fecha_fundacion", columnDefinition = "DATE") // columnDefinition especifica el tipo de dato en la
                                                                 // base de datos
    private Date fechaFundacion;

    private Boolean estado;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "uri_foto")
    private String uriFoto;

    private Double latitud;
    private Double longitud;

    private Integer orden;

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