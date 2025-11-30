package com.harmony.sistema.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosPersonalesFormDTO {
    private String nombre;
    private String email;
    private String telefono;
}