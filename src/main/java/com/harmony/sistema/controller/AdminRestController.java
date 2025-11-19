package com.harmony.sistema.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.dto.ProfesorEdicionDTO;
import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.ClienteService;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.ProfesorService;
import com.harmony.sistema.service.TallerService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminRestController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private TallerService tallerService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private HorarioService horarioService;

    // ==================== CLIENTES ====================

    // ==================== CLIENTES ====================

    @GetMapping("/clientes")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Map<String, Object>>> listarClientes() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/clientes - Listando clientes con inscripciones");
        try {
            List<Cliente> clientes = clienteService.listarClientes();

            // ✅ CORREGIDO: Incluimos las inscripciones con toda la información necesaria
            List<Map<String, Object>> clientesConInscripciones = clientes.stream().map(cliente -> {
                Map<String, Object> clienteMap = new HashMap<>();
                clienteMap.put("id", cliente.getId());
                clienteMap.put("nombreCompleto", cliente.getNombreCompleto());
                clienteMap.put("correo", cliente.getCorreo());
                clienteMap.put("telefono", cliente.getTelefono());

                // Obtener correo del User asociado
                String correoUser = cliente.getUser() != null ? cliente.getUser().getEmail() : cliente.getCorreo();
                Map<String, String> userMap = new HashMap<>();
                userMap.put("email", correoUser);
                clienteMap.put("user", userMap);

                // ✅ NUEVO: Incluir inscripciones con información detallada
                List<Map<String, Object>> inscripcionesList = cliente.getInscripciones().stream()
                        .map(inscripcion -> {
                            Map<String, Object> inscripcionMap = new HashMap<>();

                            // Información del horario
                            Map<String, Object> horarioMap = new HashMap<>();
                            Horario horario = inscripcion.getHorario();

                            horarioMap.put("id", horario.getId());
                            horarioMap.put("diasDeClase", horario.getDiasDeClase());
                            horarioMap.put("horaInicio", horario.getHoraInicio().toString());
                            horarioMap.put("horaFin", horario.getHoraFin().toString());

                            // Información del taller dentro del horario
                            Map<String, Object> tallerMap = new HashMap<>();
                            Taller taller = horario.getTaller();
                            tallerMap.put("id", taller.getId());
                            tallerMap.put("nombre", taller.getNombre());
                            horarioMap.put("taller", tallerMap);

                            inscripcionMap.put("horario", horarioMap);
                            return inscripcionMap;
                        })
                        .collect(Collectors.toList());

                clienteMap.put("inscripciones", inscripcionesList);

                System.out.println("👤 [API ADMIN] Cliente ID " + cliente.getId() +
                        " - Correo: " + correoUser +
                        " - Inscripciones: " + inscripcionesList.size());

                return clienteMap;
            }).collect(Collectors.toList());

            System.out.println("✅ [API ADMIN SUCCESS] " + clientesConInscripciones.size()
                    + " clientes obtenidos con inscripciones");
            return ResponseEntity.ok(clientesConInscripciones);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar clientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Obtiene talleres con horarios disponibles (no iniciados y con vacantes)
     */
    @GetMapping("/clientes/talleres-disponibles")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> obtenerTalleresDisponibles() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/clientes/talleres-disponibles");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            LocalDate hoy = LocalDate.now();
            System.out.println("📅 [API ADMIN] Fecha actual: " + hoy);

            List<Taller> talleresFiltrados = talleresActivos.stream().map(taller -> {
                List<Horario> horariosDisponibles = taller.getHorarios().stream()
                        .filter(horario -> {
                            boolean tieneVacantes = horario.getVacantesDisponibles() > 0;
                            boolean noHaIniciado = horario.getFechaInicio() != null
                                    && !horario.getFechaInicio().isBefore(hoy);

                            System.out.println("🔍 [API ADMIN] Horario ID " + horario.getId() + " - Vacantes: "
                                    + horario.getVacantesDisponibles() + ", Fecha inicio: " + horario.getFechaInicio()
                                    + ", No ha iniciado: " + noHaIniciado);

                            return tieneVacantes && noHaIniciado;
                        })
                        .collect(Collectors.toList());
                taller.setHorarios(horariosDisponibles);
                return taller;
            }).filter(taller -> !taller.getHorarios().isEmpty())
                    .collect(Collectors.toList());

            System.out.println("✅ [API ADMIN SUCCESS] " + talleresFiltrados.size() + " talleres disponibles");
            return ResponseEntity.ok(talleresFiltrados);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al obtener talleres disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Registra un nuevo cliente con inscripciones a talleres
     */
    @PostMapping("/clientes")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarCliente(
            @RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println(
                "🔵 [API ADMIN] POST /api/admin/clientes - Registrando cliente: " + clienteDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Email: " + clienteDto.getCorreo());
        System.out.println("📞 [API ADMIN] Teléfono: " + clienteDto.getTelefono());
        System.out.println("🎓 [API ADMIN] Talleres seleccionados: " + clienteDto.getTalleresSeleccionados());

        try {
            // Construir InscripcionFormDTO
            InscripcionFormDTO inscripcionDto = new InscripcionFormDTO();
            inscripcionDto.setNombre(clienteDto.getNombreCompleto());
            inscripcionDto.setEmail(clienteDto.getCorreo());
            inscripcionDto.setTelefono(clienteDto.getTelefono());

            // ✅ SIMPLIFICADO: Ya no necesitamos construir el Map, viene directo del DTO
            Map<Long, Long> horariosMap = clienteDto.getTalleresSeleccionados();

            if (horariosMap != null && !horariosMap.isEmpty()) {
                horariosMap.forEach((tallerId, horarioId) -> {
                    System.out.println("🔗 [API ADMIN] Taller ID " + tallerId + " -> Horario ID " + horarioId);
                });
            }

            System.out.println("🚀 [API ADMIN] Llamando a inscripcionService.procesarInscripcionCompleta");
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(
                    inscripcionDto,
                    horariosMap != null ? horariosMap : new HashMap<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente registrado exitosamente");
            response.put("email", credenciales.getCorreo());
            response.put("temporalPassword", credenciales.getContrasenaTemporal());

            System.out.println("✅ [API ADMIN SUCCESS] Cliente registrado: " + clienteDto.getNombreCompleto());
            System.out.println("🔑 [API ADMIN] Contraseña temporal: " + credenciales.getContrasenaTemporal());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al registrar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * PUT: Edita un cliente existente
     */
    @PutMapping("/clientes/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarCliente(
            @PathVariable Long id,
            @RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println("🔵 [API ADMIN] PUT /api/admin/clientes/" + id + " - Editando cliente");
        System.out.println("📝 [API ADMIN] Datos nuevos - Nombre: " + clienteDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Email: " + clienteDto.getCorreo());
        System.out.println("📞 [API ADMIN] Teléfono: " + clienteDto.getTelefono());

        try {
            // Obtener el correo original del cliente
            Cliente clienteOriginal = clienteService.listarClientes().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            String correoOriginal = clienteOriginal.getUser() != null ? clienteOriginal.getUser().getEmail()
                    : clienteOriginal.getCorreo();

            System.out.println("📧 [API ADMIN] Correo original: " + correoOriginal);

            Cliente clienteActualizado = clienteService.actualizarCliente(
                    id,
                    clienteDto.getNombreCompleto(),
                    clienteDto.getCorreo(),
                    clienteDto.getTelefono(),
                    correoOriginal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente actualizado exitosamente");
            response.put("cliente", clienteActualizado);

            System.out.println("✅ [API ADMIN SUCCESS] Cliente actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al editar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * DELETE: Elimina un cliente
     */
    @DeleteMapping("/clientes/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/clientes/" + id + " - Eliminando cliente");

        try {
            clienteService.eliminarCliente(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Cliente eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== PROFESORES ====================

    @GetMapping("/profesores")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Map<String, Object>>> listarProfesores() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/profesores - Listando profesores");
        try {
            List<Profesor> profesores = profesorService.listarProfesores();

            // Mapear profesores incluyendo el correo del User
            List<Map<String, Object>> profesoresConCorreo = profesores.stream().map(profesor -> {
                Map<String, Object> profesorMap = new HashMap<>();
                profesorMap.put("id", profesor.getId());
                profesorMap.put("nombreCompleto", profesor.getNombreCompleto());
                profesorMap.put("telefono", profesor.getTelefono());
                profesorMap.put("fotoUrl", profesor.getFotoUrl());
                profesorMap.put("informacion", profesor.getInformacion());

                // Obtener correo del User asociado
                String correo = profesor.getUser() != null ? profesor.getUser().getEmail() : "";
                profesorMap.put("correo", correo);

                System.out.println("👨‍🏫 [API ADMIN] Profesor ID " + profesor.getId() + " - Correo: " + correo);

                return profesorMap;
            }).collect(Collectors.toList());

            System.out.println("✅ [API ADMIN SUCCESS] " + profesoresConCorreo.size() + " profesores obtenidos");
            return ResponseEntity.ok(profesoresConCorreo);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar profesores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/profesores")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarProfesor(
            @RequestBody ProfesorRegistroDTO profesorDto) {

        System.out.println("🔵 [API ADMIN] POST /api/admin/profesores - Registrando profesor");
        System.out.println("👤 [API ADMIN] Nombre: " + profesorDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Correo: " + profesorDto.getCorreo());
        System.out.println("📞 [API ADMIN] Teléfono: " + profesorDto.getTelefono());

        try {
            Profesor nuevoProfesor = profesorService.registrarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor registrado exitosamente");
            response.put("profesor", nuevoProfesor);

            System.out.println("✅ [API ADMIN SUCCESS] Profesor registrado con ID: " + nuevoProfesor.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al registrar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/profesores/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarProfesor(
            @PathVariable Long id,
            @RequestBody ProfesorEdicionDTO profesorDto) {

        System.out.println("🔵 [API ADMIN] PUT /api/admin/profesores/" + id + " - Editando profesor");
        System.out.println("📝 [API ADMIN] Datos nuevos - Nombre: " + profesorDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Email: " + profesorDto.getCorreo());

        try {
            profesorDto.setId(id);
            Profesor profesorActualizado = profesorService.editarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor actualizado exitosamente");
            response.put("profesor", profesorActualizado);

            System.out.println("✅ [API ADMIN SUCCESS] Profesor actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al editar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/profesores/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarProfesor(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/profesores/" + id + " - Eliminando profesor");

        try {
            profesorService.eliminarProfesor(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Profesor eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== TALLERES ====================

    @GetMapping("/talleres")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> listarTalleres() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/talleres - Listando talleres");
        try {
            List<Taller> talleres = tallerService.listarTalleres();
            System.out.println("✅ [API ADMIN SUCCESS] " + talleres.size() + " talleres obtenidos");
            return ResponseEntity.ok(talleres);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar talleres: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/talleres/activos")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> listarTalleresActivos() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/talleres/activos - Listando talleres activos");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            System.out.println("✅ [API ADMIN SUCCESS] " + talleresActivos.size() + " talleres activos obtenidos");
            return ResponseEntity.ok(talleresActivos);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar talleres activos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== TALLERES CON LOGS DETALLADOS ====================

    @PostMapping("/talleres")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarTaller(@RequestBody Taller taller) {
        System.out.println("========================================");
        System.out.println("🔵 [API ADMIN] POST /api/admin/talleres");
        System.out.println("========================================");
        System.out.println("📝 [API ADMIN] Datos recibidos:");
        System.out.println("   - Nombre: " + taller.getNombre());
        System.out.println("   - Descripción: " + (taller.getDescripcion() != null
                ? taller.getDescripcion().substring(0, Math.min(50, taller.getDescripcion().length())) + "..."
                : "null"));
        System.out.println("   - Duración Semanas: " + taller.getDuracionSemanas());
        System.out.println("   - Clases por Semana: " + taller.getClasesPorSemana());
        System.out.println("   - Precio: " + taller.getPrecio());
        System.out.println("   - Imagen Taller: " + taller.getImagenTaller());
        System.out.println("   - Imagen Inicio: " + taller.getImagenInicio());
        System.out.println("   - Temas: " + (taller.getTemas() != null
                ? taller.getTemas().substring(0, Math.min(30, taller.getTemas().length())) + "..."
                : "null"));

        try {
            System.out.println("🚀 [API ADMIN] Llamando a tallerService.crearTallerSolo()");
            Taller nuevoTaller = tallerService.crearTallerSolo(taller);
            System.out.println("✅ [API ADMIN SUCCESS] Taller creado con ID: " + nuevoTaller.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller creado exitosamente");
            response.put("taller", nuevoTaller);

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Excepción capturada:");
            System.out.println("   - Tipo: " + e.getClass().getName());
            System.out.println("   - Mensaje: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar taller: " + e.getMessage());

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/talleres/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarTaller(
            @PathVariable Long id,
            @RequestBody Taller tallerActualizado) {

        System.out.println("========================================");
        System.out.println("🔵 [API ADMIN] PUT /api/admin/talleres/" + id);
        System.out.println("========================================");
        System.out.println("📝 [API ADMIN] Datos recibidos para actualización:");
        System.out.println("   - Taller ID: " + id);
        System.out.println("   - Nuevo Nombre: " + tallerActualizado.getNombre());
        System.out.println("   - Nueva Descripción: " + (tallerActualizado.getDescripcion() != null ? tallerActualizado.getDescripcion().substring(0, Math.min(50, tallerActualizado.getDescripcion().length())) + "..." : "null"));
        System.out.println("   - Nuevo Precio: " + tallerActualizado.getPrecio());
        System.out.println("   - Nueva Imagen Taller: " + tallerActualizado.getImagenTaller());
        System.out.println("   - Nueva Imagen Inicio: " + tallerActualizado.getImagenInicio());
        System.out.println("   - Activo: " + tallerActualizado.isActivo());

        try {
            System.out.println("🚀 [API ADMIN] Llamando a tallerService.editarTaller()");
            tallerActualizado.setId(id);
            Taller taller = tallerService.editarTaller(tallerActualizado);
            System.out.println("✅ [API ADMIN SUCCESS] Taller actualizado correctamente");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller actualizado exitosamente");
            response.put("taller", taller);

            System.out.println("========================================\n");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Excepción capturada:");
            System.out.println("   - Tipo: " + e.getClass().getName());
            System.out.println("   - Mensaje: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar taller: " + e.getMessage());

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/talleres/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarTaller(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/talleres/" + id + " - Eliminando taller");

        try {
            tallerService.eliminarTaller(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Taller eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar taller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar taller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== HORARIOS ====================

    @GetMapping("/horarios/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Horario> obtenerHorario(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] GET /api/admin/horarios/" + id + " - Obteniendo horario");

        try {
            Horario horario = horarioService.getHorarioById(id);
            System.out.println("✅ [API ADMIN SUCCESS] Horario obtenido: " + id);
            return ResponseEntity.ok(horario);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Horario no encontrado: " + id);
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/horarios")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarHorario(
            @RequestParam("tallerId") Long tallerId,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam(value = "diasDeClase", required = false) String[] diasDeClaseArray,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam("horaFin") LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println("🔵 [API ADMIN] POST /api/admin/horarios - Registrando horario");
        System.out.println("🎓 [API ADMIN] Taller ID: " + tallerId);
        System.out.println("👨‍🏫 [API ADMIN] Profesor ID: " + profesorId);
        System.out.println("📅 [API ADMIN] Fecha inicio: " + fechaInicio);

        try {
            String diasDeClase = (diasDeClaseArray != null && diasDeClaseArray.length > 0)
                    ? String.join(", ", diasDeClaseArray)
                    : "";

            if (diasDeClase.isEmpty()) {
                System.out.println("⚠️ [API ADMIN] No se seleccionaron días de clase");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Debe seleccionar al menos un día de clase");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Horario nuevoHorario = horarioService.crearHorario(tallerId, profesorId, diasDeClase,
                    horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario creado exitosamente");
            response.put("horario", nuevoHorario);

            System.out.println("✅ [API ADMIN SUCCESS] Horario registrado con ID: " + nuevoHorario.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al registrar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/horarios/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarHorario(
            @PathVariable Long id,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam("diasDeClase") String diasDeClase,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam(value = "horaFin", required = false) LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println("🔵 [API ADMIN] PUT /api/admin/horarios/" + id + " - Editando horario");

        try {
            if (horaFin == null) {
                Horario horarioExistente = horarioService.getHorarioById(id);
                horaFin = horarioExistente.getHoraFin();
            }

            Horario horarioActualizado = horarioService.editarHorario(id, profesorId, diasDeClase,
                    horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario actualizado exitosamente");
            response.put("horario", horarioActualizado);

            System.out.println("✅ [API ADMIN SUCCESS] Horario actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al editar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/horarios/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarHorario(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/horarios/" + id + " - Eliminando horario");

        try {
            horarioService.eliminarHorario(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Horario eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}