package com.harmony.sistema.controller;

import com.harmony.sistema.dto.CambioClaveRequest;
import com.harmony.sistema.dto.HorarioClienteDTO; // Importado el nuevo DTO
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cliente")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteRestController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene los horarios del cliente autenticado
     */
    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioClienteDTO>> getHorarios(Authentication authentication) {
        String email = authentication.getName();
        
        // CORREGIDO: findByUserEmail ahora existe en ClienteRepository
        Cliente cliente = clienteRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<HorarioClienteDTO> horarios = cliente.getInscripciones().stream()
            .map(inscripcion -> {
                Horario h = inscripcion.getHorario();
                HorarioClienteDTO dto = new HorarioClienteDTO();
                dto.setId(h.getId());
                dto.setDiasDeClase(h.getDiasDeClase());
                dto.setHoraInicio(h.getHoraInicio().toString());
                dto.setHoraFin(h.getHoraFin().toString());
                
                // Usamos los DTOs anidados
                dto.setTaller(new HorarioClienteDTO.TallerSimpleDTO(h.getTaller().getNombre()));
                dto.setProfesor(new HorarioClienteDTO.ProfesorSimpleDTO(h.getProfesor().getNombreCompleto()));
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(horarios);
    }

    /**
     * Cambio de contraseña del cliente
     */
    @PostMapping("/cambiar-clave")
    public ResponseEntity<String> cambiarClave(
            @RequestBody CambioClaveRequest request, // Usando el DTO externo
            Authentication authentication) {
        
        String email = authentication.getName();
        
        if (!request.getNuevaContrasena().equals(request.getConfirmarContrasena())) {
            return ResponseEntity.badRequest().body("Las contraseñas no coinciden");
        }

        if (request.getNuevaContrasena().length() < 6) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(request.getNuevaContrasena()));
        userRepository.save(user);

        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }
}