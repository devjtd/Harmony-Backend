package com.harmony.sistema.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;

@Service
public class InscripcionService {

        private static final String ROLE_CLIENTE = "ROLE_CLIENTE";
        @Autowired
        private ClienteService clienteService;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private RoleRepository roleRepository;
        @Autowired
        private ClienteRepository clienteRepository;
        @Autowired
        private InscripcionRepository inscripcionRepository;
        @Autowired
        private HorarioRepository horarioRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private UserService userService;
        @Autowired
        private EmailService emailService;

        @Transactional
        public Cliente guardarOObtenerClienteTemporal(DatosPersonalesFormDTO datos) {

                System.out.println(
                                " [INSCRIPCION SERVICE] Procesando datos personales para email: " + datos.getEmail());

                Optional<User> userOpt = userRepository.findByEmail(datos.getEmail());
                if (userOpt.isPresent()) {
                        User userExistente = userOpt.get();

                        if (userExistente.isEnabled()) {
                                System.out.println(
                                                " [INSCRIPCION SERVICE BLOQUEO] El correo ya tiene una cuenta activa (enabled=true).");
                                throw new RuntimeException(
                                                "Ya tienes una cuenta activa con este correo. Por favor, inicia sesión para continuar.");
                        }

                        Cliente clienteExistente = clienteService.encontrarClientePorEmail(datos.getEmail());
                        System.out.println(" [INSCRIPCION SERVICE] Cliente ya existe con User INACTIVO (ID: "
                                        + clienteExistente.getId()
                                        + "). Devolviendo existente para completar proceso.");
                        return clienteExistente;
                }

                Optional<Cliente> clienteTemporalOpt = clienteService.encontrarClientePorCorreo(datos.getEmail());
                if (clienteTemporalOpt.isPresent()) {
                        Cliente clienteTemporal = clienteTemporalOpt.get();
                        clienteTemporal.setNombreCompleto(datos.getNombre());
                        clienteTemporal.setTelefono(datos.getTelefono());
                        clienteRepository.save(clienteTemporal);
                        System.out.println(" [INSCRIPCION SERVICE] Cliente temporal ya existe (ID: "
                                        + clienteTemporal.getId()
                                        + "). Devolviendo existente y actualizado.");
                        return clienteTemporal;
                }

                ClienteRegistroDTO registroDTO = new ClienteRegistroDTO(
                                datos.getNombre(),
                                datos.getEmail(),
                                datos.getTelefono());

                Cliente clienteRecienCreado = clienteService.crearClienteTemporal(registroDTO);

                System.out.println(" [INSCRIPCION SERVICE] Cliente recién registrado como TEMPORAL (ID: "
                                + clienteRecienCreado.getId() + ").");
                return clienteRecienCreado;
        }

        @Transactional
        public CredencialesDTO procesarInscripcionCompleta(InscripcionFormDTO dto,
                        Map<Long, Long> horariosSeleccionados) {
                System.out.println(" [INSCRIPCION SERVICE] Iniciando proceso de inscripción completa/admin para: "
                                + dto.getEmail());

                Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
                if (userOpt.isPresent() && userOpt.get().isEnabled()) {
                        System.out.println(
                                        " [INSCRIPCION SERVICE BLOQUEO] El correo ya tiene una cuenta activa (enabled=true).");
                        throw new RuntimeException(
                                        "El correo ya tiene una cuenta activa. Por favor, revisa el listado de clientes.");
                }

                Cliente clienteExistente = clienteService.encontrarClientePorCorreo(dto.getEmail())
                                .orElseGet(() -> {
                                        ClienteRegistroDTO registroDTO = new ClienteRegistroDTO(
                                                        dto.getNombre(),
                                                        dto.getEmail(),
                                                        dto.getTelefono());

                                        Cliente clienteRecienCreado = clienteService.crearClienteTemporal(registroDTO);
                                        System.out.println(
                                                        " [INSCRIPCION SERVICE] Cliente no encontrado. Registrado como nuevo TEMPORAL (ID: "
                                                                        + clienteRecienCreado.getId()
                                                                        + ") para continuar.");
                                        return clienteRecienCreado;
                                });

                clienteExistente.setNombreCompleto(dto.getNombre());
                clienteExistente.setTelefono(dto.getTelefono());
                clienteRepository.save(clienteExistente);
                System.out.println(" [INSCRIPCION SERVICE] Cliente/Cliente Temporal (ID: " + clienteExistente.getId()
                                + ") asegurado.");

                User newUser = userOpt.orElseGet(() -> User.builder().build());

                String rawPassword = userService.generadorRandomPassword();
                String encodedPassword = passwordEncoder.encode(rawPassword);
                System.out.println(" [INSCRIPCION SERVICE] Contraseña Encriptada (Encoded) generada.");

                Role roleCliente = roleRepository.findByName(ROLE_CLIENTE)
                                .orElseThrow(() -> new RuntimeException("Error: El rol CLIENTE no fue encontrado."));
                System.out.println(" [INSCRIPCION SERVICE] Rol 'ROLE_CLIENTE' obtenido.");

                newUser.setEmail(dto.getEmail());
                newUser.setPassword(encodedPassword);
                newUser.setEnabled(true);
                newUser.setRoles(Set.of(roleCliente));

                userRepository.save(newUser);
                System.out.println(" [INSCRIPCION SERVICE] User persistido/actualizado (ID: " + newUser.getId()
                                + ") con email: " + newUser.getEmail());

                clienteExistente.setUser(newUser);
                clienteRepository.save(clienteExistente);
                System.out.println(
                                " [INSCRIPCION SERVICE] Cliente existente (ID: " + clienteExistente.getId()
                                                + ") asociado al nuevo User.");

                // ✅ MODIFICADO: Ahora decrementamos vacantes
                LocalDate fechaActual = LocalDate.now();
                System.out.println(" [INSCRIPCION SERVICE] Procesando " + horariosSeleccionados.size()
                                + " inscripciones.");

                horariosSeleccionados.forEach((tallerId, horarioId) -> {
                        Horario horario = horarioRepository.findById(horarioId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Horario con ID " + horarioId + " no encontrado."));

                        // ✅ NUEVO: Validar que haya vacantes disponibles
                        if (horario.getVacantesDisponibles() <= 0) {
                                throw new RuntimeException(
                                                "No hay vacantes disponibles en el horario seleccionado (ID: "
                                                                + horarioId + ")");
                        }

                        Inscripcion inscripcion = new Inscripcion();
                        inscripcion.setCliente(clienteExistente);
                        inscripcion.setHorario(horario);
                        inscripcion.setFechaInscripcion(fechaActual);
                        inscripcion.setPagado(true);

                        inscripcionRepository.save(inscripcion);

                        // ✅ NUEVO: Decrementar vacantes disponibles
                        horario.setVacantesDisponibles(horario.getVacantesDisponibles() - 1);
                        horarioRepository.save(horario);

                        System.out.println(" [INSCRIPCION SERVICE] Inscripción creada para Horario ID: " + horarioId);
                        System.out.println(" [INSCRIPCION SERVICE] Vacantes restantes en horario " + horarioId + ": "
                                        + horario.getVacantesDisponibles());
                });

                try {
                        String asunto = "¡Bienvenido a Harmony! Tus Credenciales de Acceso";
                        String cuerpo = "Hola " + dto.getNombre() + ",\n\n" +
                                        "¡Tu inscripción ha sido confirmada y tu cuenta ha sido creada con éxito! \n\n"
                                        +
                                        "Tus credenciales de acceso son:\n" +
                                        "Usuario (Correo Electrónico): " + dto.getEmail() + "\n" +
                                        "Contraseña Temporal: " + rawPassword + "\n\n" +
                                        "Por tu seguridad, te recomendamos encarecidamente cambiar tu contraseña inmediatamente después de iniciar sesión.\n\n"
                                        +
                                        "¡Te esperamos en clase!\n" +
                                        "Saludos cordiales,\n" +
                                        "El equipo de Harmony";

                        emailService.enviarCorreo(dto.getEmail(), asunto, cuerpo);
                        System.out.println(" [INSCRIPCION SERVICE] Correo de bienvenida enviado a: " + dto.getEmail());
                } catch (Exception e) {
                        System.out.println(" [INSCRIPCION SERVICE ERROR] Error al enviar el correo de bienvenida a "
                                        + dto.getEmail() + ": "
                                        + e.getMessage());
                }

                System.out.println(
                                " [INSCRIPCION SERVICE SUCCESS] Proceso de inscripción completa finalizado.");

                return new CredencialesDTO(dto.getEmail(), rawPassword);
        }
}