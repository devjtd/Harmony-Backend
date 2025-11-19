/*
package com.harmony.sistema.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;
    private String correo; // <--- AÑADIDO: El cliente almacena el correo
    private String telefono;

    // Relación con la entidad User para autenticación/login
    // IMPORTANTE: Se elimina 'nullable = false' para permitir clientes sin User
    // (temporales)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;

    // Relación OneToMany: Un cliente puede tener muchas inscripciones diferentes.
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "cliente-inscripciones")
    private List<Inscripcion> inscripciones;
}



package com.harmony.sistema.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity; // ¡Importación nueva!
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "horario")
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diasDeClase;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private int vacantesDisponibles;

    @ManyToOne
    @JoinColumn(name = "taller_id", nullable = false)
    @JsonBackReference(value = "taller-horarios")
    private Taller taller;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    @JsonBackReference(value = "profesor-horarios")
    private Profesor profesor;

    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "horario-inscripciones")
    private List<Inscripcion> inscripciones;
}


package com.harmony.sistema.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInscripcion;
    private boolean pagado;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonBackReference(value = "cliente-inscripciones")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "horario_id")
    @JsonBackReference(value = "horario-inscripciones")
    private Horario horario;
}

package com.harmony.sistema.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Profesor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;

    private String telefono;

    private String fotoUrl;

    @Column(columnDefinition = "TEXT")
    private String informacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "profesor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "profesor-horarios")
    private List<Horario> horariosImpartidos;
}


package com.harmony.sistema.model;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Taller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(nullable = false, length = 255)
    private String descripcion;

    private Integer duracionSemanas;

    private Integer clasesPorSemana;

    private String imagenTaller;

    private String imagenInicio;

    private boolean activo;

    private BigDecimal precio;

    @Column(columnDefinition = "TEXT")
    private String temas;

    @OneToMany(mappedBy = "taller", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "taller-horarios")
    private List<Horario> horarios;
}





*/