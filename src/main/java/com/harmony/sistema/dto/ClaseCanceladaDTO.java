package com.harmony.sistema.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaseCanceladaDTO {
    private Long id;
    private LocalDate fecha;
    private String motivo;
    private String accion;
}
