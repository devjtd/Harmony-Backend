package com.harmony.sistema.dto;

import lombok.Data;

@Data
public class HorarioProfesorDTO {
    private Long id;
    private String diasDeClase;
    private String horaInicio;
    private String horaFin;
    private TallerSimpleDTO taller;

    // DTO Anidado
    @Data
    public static class TallerSimpleDTO {
        private String nombre;
        public TallerSimpleDTO(String nombre) { this.nombre = nombre; }
    }
}