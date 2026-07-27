package com.mcmm.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(max = 50)
    private String username;

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String apellidos;

    private String uriFoto;

    private Boolean estado;

    // Marca explicita de Administrador Global. El privilegio NO debe inferirse de
    // la ausencia de miembro vinculado (eso era una fragilidad de seguridad).
    @Column(name = "es_admin")
    private Boolean esAdmin;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "miembro_id", nullable = true)
    private Miembro miembro;

    @NotBlank
    @Size(min = 3, max = 100)
    private String password;

    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = true;
        }
        if (esAdmin == null) {
            esAdmin = false;
        }
    }
}
