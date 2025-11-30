package com.harmony.sistema.dto;

/**
 * DTO para la respuesta después de una inscripción exitosa.
 * Contiene las credenciales temporales del nuevo usuario/cliente.
 */
public class InscripcionResponseDTO {

    private String correo;
    private String contrasenaTemporal;

    // --- Constructores, Getters y Setters ---

    public InscripcionResponseDTO() {
    }

    public InscripcionResponseDTO(String correo, String contrasenaTemporal) {
        this.correo = correo;
        this.contrasenaTemporal = contrasenaTemporal;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaTemporal() {
        return contrasenaTemporal;
    }

    public void setContrasenaTemporal(String contrasenaTemporal) {
        this.contrasenaTemporal = contrasenaTemporal;
    }
}