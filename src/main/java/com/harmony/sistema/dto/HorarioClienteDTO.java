package com.harmony.sistema.dto;

import lombok.Data;
import java.util.List;

@Data
public class HorarioClienteDTO {
    private Long id;
    private String diasDeClase;
    private String horaInicio;
    private String horaFin;
    private TallerSimpleDTO taller;
    private ProfesorSimpleDTO profesor;
    private Boolean finalizado;
    private String fechaFin;
    private List<ClaseCanceladaDTO> cancelaciones;

    // DTOs Anidados
    @Data
    public static class TallerSimpleDTO {
        private String nombre;

        public TallerSimpleDTO(String nombre) {
            this.nombre = nombre;
        }
    }

    @Data
    public static class ProfesorSimpleDTO {
        private String nombreCompleto;

        public ProfesorSimpleDTO(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }
    }
}