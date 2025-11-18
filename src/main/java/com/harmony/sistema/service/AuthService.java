package com.harmony.sistema.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.dto.AuthResponse;
import com.harmony.sistema.dto.LoginRequest;
import com.harmony.sistema.dto.RegisterRequest;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesorRepository profesorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario con el rol ROLE_CLIENTE por defecto.
     * Utilizado para registros públicos desde la web.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println(" [AUTH SERVICE] ========================================");
        System.out.println(" [AUTH SERVICE] Iniciando registro para el email: " + request.getEmail());
        
        // 1. Verificar si el usuario ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            System.err.println(" [AUTH SERVICE ERROR] El email ya está registrado: " + request.getEmail());
            throw new RuntimeException("El email ya está registrado en el sistema");
        }
        
        // 2. Buscar el rol por defecto
        Optional<Role> userRole = roleRepository.findByName("ROLE_CLIENTE"); 
        
        if (userRole.isEmpty()) {
            System.err.println(" [AUTH SERVICE ERROR] El rol 'ROLE_CLIENTE' no existe en la BD");
            throw new RuntimeException("Error de configuración: Rol ROLE_CLIENTE no encontrado");
        }
        System.out.println(" [AUTH SERVICE] Rol 'ROLE_CLIENTE' encontrado");

        // 3. Construir y guardar el objeto User
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Collections.singleton(userRole.get())) 
                .build();
        
        System.out.println(" [AUTH SERVICE] Usuario construido, guardando en BD...");
        userRepository.save(user);
        System.out.println(" [AUTH SERVICE] Usuario guardado exitosamente");

        // 4. Generar el token JWT
        var jwtToken = jwtService.generateToken(user);
        System.out.println(" [AUTH SERVICE] Token JWT generado");
        System.out.println(" [AUTH SERVICE] ========================================");
        
        // 5. Retornar respuesta con token y rol
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role("ROLE_CLIENTE")
                .build();
    }

    /**
     * Autentica a un usuario y retorna el JWT con su información de rol y nombre.
     * Busca en las tablas Cliente y Profesor para obtener datos adicionales.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        System.out.println(" [AUTH SERVICE] ========================================");
        System.out.println(" [AUTH SERVICE] Iniciando login para: " + request.getEmail());
        
        try {
            // 1. Autenticar credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), 
                            request.getPassword()
                    )
            );
            System.out.println(" [AUTH SERVICE] Credenciales autenticadas correctamente");
            
        } catch (BadCredentialsException e) {
            System.err.println(" [AUTH SERVICE ERROR] Credenciales inválidas para: " + request.getEmail());
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        // 2. Recuperar la entidad User
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.err.println(" [AUTH SERVICE ERROR] Usuario no encontrado: " + request.getEmail());
                    return new RuntimeException("Usuario no encontrado");
                });
        
        System.out.println(" [AUTH SERVICE] Usuario recuperado de BD");
        
        // 3. Determinar el rol del usuario
        String roleName = user.getRoles().isEmpty() 
                ? "ROLE_CLIENTE" 
                : user.getRoles().iterator().next().getName();
        
        System.out.println(" [AUTH SERVICE] Rol detectado: " + roleName);
        
        // 4. Obtener el nombre completo según el rol
        String nombreCompleto = obtenerNombreCompleto(user, roleName);
        System.out.println(" [AUTH SERVICE] Nombre completo: " + nombreCompleto);
        
        // 5. Generar el token JWT
        var jwtToken = jwtService.generateToken(user);
        
        System.out.println(" [AUTH SERVICE] ========================================");
        System.out.println(" | JWT GENERADO PARA: " + request.getEmail());
        System.out.println(" | ROL: " + roleName);
        System.out.println(" | NOMBRE: " + nombreCompleto);
        System.out.println(" | TOKEN (primeros 50 chars): " + jwtToken.substring(0, Math.min(50, jwtToken.length())) + "...");
        System.out.println(" [AUTH SERVICE] ========================================");
        
        // 6. Retornar respuesta completa
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(roleName)
                .nombreCompleto(nombreCompleto)
                .build();
    }

    /**
     * Método auxiliar para obtener el nombre completo del usuario.
     * Busca en Cliente o Profesor según el rol.
     */
    private String obtenerNombreCompleto(User user, String roleName) {
        try {
            // Si es CLIENTE, buscar en la tabla Cliente
            if ("ROLE_CLIENTE".equals(roleName)) {
                Optional<Cliente> clienteOpt = clienteRepository.findByUser(user);
                if (clienteOpt.isPresent()) {
                    System.out.println(" [AUTH SERVICE] Nombre encontrado en tabla Cliente");
                    return clienteOpt.get().getNombreCompleto();
                }
            }
            
            // Si es PROFESOR, buscar en la tabla Profesor
            if ("ROLE_PROFESOR".equals(roleName)) {
                Optional<Profesor> profesorOpt = profesorRepository.findByUser(user);
                if (profesorOpt.isPresent()) {
                    System.out.println(" [AUTH SERVICE] Nombre encontrado en tabla Profesor");
                    return profesorOpt.get().getNombreCompleto();
                }
            }
            
            // Si es ADMIN o no se encuentra, retornar el email
            System.out.println(" [AUTH SERVICE] Nombre no encontrado, usando email");
            return user.getEmail();
            
        } catch (Exception e) {
            System.err.println(" [AUTH SERVICE ERROR] Error obteniendo nombre completo: " + e.getMessage());
            return user.getEmail();
        }
    }
}