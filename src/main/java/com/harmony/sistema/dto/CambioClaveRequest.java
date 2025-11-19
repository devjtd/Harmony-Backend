package com.harmony.sistema.dto;

import lombok.Data;

@Data
public class CambioClaveRequest {
    private String nuevaContrasena;
    private String confirmarContrasena;
}