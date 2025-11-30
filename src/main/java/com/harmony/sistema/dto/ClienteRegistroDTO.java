package com.harmony.sistema.dto;

import java.util.Map;

public class ClienteRegistroDTO {
    private String nombreCompleto;
    private String correo;
    private String telefono;

    // Key = tallerId, Value = horarioId
    private Map<Long, Long> talleresSeleccionados;

    // Constructores
    public ClienteRegistroDTO() {
    }

    public ClienteRegistroDTO(String nombreCompleto, String correo, String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.telefono = telefono;
    }

    // Getters y Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Map<Long, Long> getTalleresSeleccionados() {
        return talleresSeleccionados;
    }

    public void setTalleresSeleccionados(Map<Long, Long> talleresSeleccionados) {
        this.talleresSeleccionados = talleresSeleccionados;
    }
}