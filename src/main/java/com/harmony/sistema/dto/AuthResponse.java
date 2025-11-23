package com.harmony.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para autenticación.
 * Contiene el token JWT, email, rol y nombre completo del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Token JWT para autenticación.
     */
    private String token;

    /**
     * Email del usuario autenticado.
     */
    private String email;

    /**
     * Rol del usuario (ROLE_ADMIN, ROLE_CLIENTE, ROLE_PROFESOR).
     */
    private String role;

    /**
     * Nombre completo del usuario (opcional).
     * Se obtiene de la tabla Cliente o Profesor según corresponda.
     */
    private String nombreCompleto;

    /**
     * ID del usuario (Cliente o Profesor).
     */
    private Long id;
}