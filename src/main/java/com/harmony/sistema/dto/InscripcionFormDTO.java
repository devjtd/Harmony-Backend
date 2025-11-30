package com.harmony.sistema.dto;

import java.util.List;

public class InscripcionFormDTO {

    private String nombre;
    private String email;
    private String telefono;

    private List<Long> talleresSeleccionados;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Long> getTalleresSeleccionados() {
        return talleresSeleccionados;
    }

    public void setTalleresSeleccionados(List<Long> talleresSeleccionados) {
        this.talleresSeleccionados = talleresSeleccionados;
    }
}