package com.harmony.sistema.service;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        // ==========================================
        // NUEVOS MÉTODOS PARA GESTIÓN DE INSCRIPCIONES
        // ==========================================

        @Transactional
        public void inscribirClienteExistente(Long clienteId, Long horarioId) {
                System.out.println(" [INSCRIPCION SERVICE] Inscribiendo cliente existente ID: " + clienteId
                                + " en horario ID: " + horarioId);

                Cliente cliente = clienteRepository.findById(clienteId)
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));

                Horario horario = horarioRepository.findById(horarioId)
                                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));

                // Validar si ya está inscrito en este horario
                Optional<Inscripcion> inscripcionExistente = inscripcionRepository
                                .findByClienteIdAndHorarioId(clienteId, horarioId);
                if (inscripcionExistente.isPresent()) {
                        throw new RuntimeException("El cliente ya está inscrito en este horario.");
                }

                // Validar vacantes
                if (horario.getVacantesDisponibles() <= 0) {
                        throw new RuntimeException("No hay vacantes disponibles en el horario seleccionado.");
                }

                Inscripcion inscripcion = new Inscripcion();
                inscripcion.setCliente(cliente);
                inscripcion.setHorario(horario);
                inscripcion.setFechaInscripcion(LocalDate.now());
                inscripcion.setPagado(true); // Asumimos pagado si lo agrega el admin

                inscripcionRepository.save(inscripcion);

                // Decrementar vacantes
                horario.setVacantesDisponibles(horario.getVacantesDisponibles() - 1);
                horarioRepository.save(horario);

                System.out.println(" [INSCRIPCION SERVICE] Inscripción exitosa.");
        }

        @Transactional
        public void eliminarInscripcion(Long clienteId, Long horarioId) {
                System.out.println(" [INSCRIPCION SERVICE] Eliminando inscripción - Cliente ID: " + clienteId
                                + ", Horario ID: " + horarioId);

                Inscripcion inscripcion = inscripcionRepository.findByClienteIdAndHorarioId(clienteId, horarioId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Inscripción no encontrada para el cliente y horario especificados."));

                Horario horario = inscripcion.getHorario();

                inscripcionRepository.delete(inscripcion);

                // Incrementar vacantes
                horario.setVacantesDisponibles(horario.getVacantesDisponibles() + 1);
                horarioRepository.save(horario);

                System.out.println(" [INSCRIPCION SERVICE] Inscripción eliminada y vacante liberada.");
        }

        public void solicitarBaja(Long clienteId, Long horarioId, String motivo) {
                System.out.println(" [INSCRIPCION SERVICE] Solicitud de baja - Cliente ID: " + clienteId
                                + ", Horario ID: " + horarioId);

                Cliente cliente = clienteRepository.findById(clienteId)
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

                Horario horario = horarioRepository.findById(horarioId)
                                .orElseThrow(() -> new RuntimeException("Horario no encontrado."));

                // Validar que la inscripción exista
                inscripcionRepository.findByClienteIdAndHorarioId(clienteId, horarioId)
                                .orElseThrow(() -> new RuntimeException(
                                                "No se encontró una inscripción activa para este horario."));

                String asunto = "Solicitud de Baja - " + cliente.getNombreCompleto();
                String cuerpo = String.format("""
                                Solicitud de Baja de Taller

                                Cliente: %s
                                Correo: %s
                                Taller: %s
                                Horario: %s

                                Motivo de la solicitud:
                                %s
                                """,
                                cliente.getNombreCompleto(),
                                cliente.getCorreo(),
                                horario.getTaller().getNombre(),
                                horario.getDiasDeClase() + " " + horario.getHoraInicio() + "-" + horario.getHoraFin(),
                                motivo);

                // Enviar correo al admin (usando el email configurado en properties, aquí
                // hardcodeado o inyectado si fuera necesario,
                // pero usaremos el del cliente como remitente lógico en el cuerpo)
                // Nota: Para simplificar, enviamos al admin definido en ContactoController o
                // similar.
                // Como EmailService es genérico, necesitamos el email del admin.
                // Por ahora usaremos un log fuerte y si es posible inyectar el valor.

                String adminEmail = "admin@harmony.com"; // Placeholder, idealmente desde properties

                emailService.enviarCorreo(adminEmail, asunto, cuerpo);
                System.out.println(" [INSCRIPCION SERVICE] Notificación de baja enviada al admin.");
        }

        public List<Inscripcion> obtenerInscripcionesPorCliente(Long clienteId) {
                return inscripcionRepository.findByClienteId(clienteId);
        }
}