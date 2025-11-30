package com.harmony.sistema.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Noticia {
    private Long id;
    private String titulo;
    private String contenido;
    private String imagenUrl;

}
