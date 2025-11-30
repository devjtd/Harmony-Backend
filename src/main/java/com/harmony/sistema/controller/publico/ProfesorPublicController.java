package com.harmony.sistema.controller.publico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.service.ProfesorService;

@RestController
@RequestMapping("/api/profesores")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfesorPublicController {

    @Autowired
    private ProfesorService profesorService;

    /**
     * Endpoint para listar todos los profesores.
     * GET /api/profesores
     */
    @GetMapping
    public List<Profesor> listarProfesores() {
        System.out.println("[INFO] [CONTROLLER] Solicitud GET a /api/profesores. Devolviendo lista de profesores.");
        return profesorService.listarProfesores();
    }
}