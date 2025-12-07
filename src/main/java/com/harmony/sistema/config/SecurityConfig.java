package com.harmony.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.harmony.sistema.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] PUBLIC_ROUTES = {
            "/", "/acerca", "/profesores", "/inscripcion", "/talleres",
            "/blog", "/pago", "/contacto/**", "/confirmacion", "/css/**", "/js/**", "/images/**",
    };

    // Configura la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[INFO] [CONFIG] Inicializando Bean: SecurityFilterChain (Configuración de Seguridad HTTP)");

        http
                // Deshabilita CSRF y configura autorización
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/horario", "/cambiar-clave").hasAnyRole("CLIENTE", "PROFESOR")
                        .anyRequest().authenticated())

                // Configura proveedor y filtro JWT
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura gestión de sesiones como STATELESS para JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configura manejo de excepciones para retornar 401 en lugar de redirigir
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("[SECURITY] Acceso no autorizado a: " + request.getRequestURI());
                            response.sendError(401, "No autorizado");
                        }));

        return http.build();
    }

}
