package com.harmony.sistema.dto;

import java.util.List;

import com.harmony.sistema.model.Horario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TallerResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenTaller;
    private Integer duracionSemanas;
    private Integer clasesPorSemana;
    private Double precio;
    private String temas;

    // Lista de horarios abiertos/disponibles
    private List<Horario> horariosAbiertos;

    // Indicador si el taller alguna vez tuvo horarios definidos
    private boolean tieneHorariosDefinidos;
}